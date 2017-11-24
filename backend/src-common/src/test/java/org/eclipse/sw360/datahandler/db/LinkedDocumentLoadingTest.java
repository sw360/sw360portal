package org.eclipse.sw360.datahandler.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.datahandler.TestUtils;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.couchdb.DatabaseMixIn;
import org.eclipse.sw360.datahandler.couchdb.MapperFactory;
import org.eclipse.sw360.datahandler.couchdb.annotation.LinkedDocuments;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.eclipse.sw360.datahandler.TestUtils.assertTestString;

public class LinkedDocumentLoadingTest {

    private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;

    private ComponentRepository componentRepository;
    private ReleaseRepository releaseRepository;
    private VendorRepository vendorRepository;

    @Before
    public void setUp() throws Exception {
        assertTestString(dbName);
        // assertTestString(attachmentsDbName);

        TestUtils.createDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);

        // Prepare the database
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient(), dbName,
                new TestMapperFactory());

        vendorRepository = new VendorRepository(databaseConnector);
        releaseRepository = new ReleaseRepository(databaseConnector, vendorRepository);
        componentRepository = new ComponentRepository(databaseConnector, releaseRepository, vendorRepository);
    }

    @After
    public void tearDown() throws Exception {
        // Delete the database
        TestUtils.deleteDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);
    }

    @Test
    public void testReleaseResolvingForOneComponentWithoutRelease() {
        Component component = new Component("Component 1").setId("c1");
        componentRepository.add(component);

        // check that releases are not resolved by default
        component = componentRepository.get("c1");
        Assert.assertNotNull(component);
        Assert.assertNull(component.getReleaseIds());
        Assert.assertNull(component.getReleases());

        // check that releases are resolved with special method
        component = componentRepository.getWithReleases("c1");
        Assert.assertNotNull(component);
        Assert.assertNull(component.getReleaseIds());
        Assert.assertNull(component.getReleases());
    }

    @Test
    public void testReleaseResolvingForOneComponent() {
        Release release1 = new Release("r1", "1.0", "c1").setId("r1");
        releaseRepository.add(release1);

        Release release2 = new Release("r2", "2.0", "c1").setId("r2");
        releaseRepository.add(release2);

        Component component = new Component("Component 1").setId("c1");
        component.addToReleaseIds("r1");
        component.addToReleaseIds("r2");
        componentRepository.add(component);

        // check that releases are not resolved by default
        component = componentRepository.get("c1");
        Assert.assertNotNull(component);
        Assert.assertThat(component.getReleaseIds(), Matchers.containsInAnyOrder("r1", "r2"));
        Assert.assertNull(component.getReleases());

        // check that releases are resolved with special method
        component = componentRepository.getWithReleases("c1");
        Assert.assertNotNull(component);
        Assert.assertThat(component.getReleaseIds(), Matchers.containsInAnyOrder("r1", "r2"));
        Assert.assertThat(component.getReleases(), Matchers.containsInAnyOrder(release1, release2));
    }

    @Test
    public void testReleaseResolvingForMoreComponents() {
        Release release1 = new Release("r1", "1.0", "c1").setId("r1");
        releaseRepository.add(release1);

        Release release2 = new Release("r2", "2.0", "c1").setId("r2");
        releaseRepository.add(release2);

        Release release3 = new Release("r3", "2.0", "c3").setId("r3");
        releaseRepository.add(release3);

        Component component1 = new Component("Component 1").setId("c1");
        component1.addToReleaseIds("r1");
        component1.addToReleaseIds("r2");
        componentRepository.add(component1);

        Component component2 = new Component("Component 2").setId("c2");
        component2.addToReleaseIds("r2");
        component2.addToReleaseIds("r3");
        componentRepository.add(component2);

        // check that releases are resolved with special method
        List<Component> components = componentRepository.getWithReleases();
        for (Component component : components) {
            if (component.getId().equals("c1")) {
                Assert.assertThat(component.getReleaseIds(), Matchers.containsInAnyOrder("r1", "r2"));
                Assert.assertThat(component.getReleases(), Matchers.containsInAnyOrder(release1, release2));
            }
            if (component.getId().equals("c2")) {
                Assert.assertThat(component.getReleaseIds(), Matchers.containsInAnyOrder("r2", "r3"));
                Assert.assertThat(component.getReleases(), Matchers.containsInAnyOrder(release2, release3));
            }
        }
    }

    private static class TestMapperFactory extends MapperFactory {
        @Override
        public ObjectMapper createObjectMapper() {
            ObjectMapper objectMapper = super.createObjectMapper();
            objectMapper.addMixInAnnotations(Component.class, ComponentMixin.class);
            return objectMapper;
        }
    }

    private abstract static class ComponentMixin extends DatabaseMixIn {
        @LinkedDocuments(targetField = "releases")
        private Set<String> releaseIds;
    }
}
