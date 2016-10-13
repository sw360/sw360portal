/*
 *
 * This code is taken from the Ektorp project which is hosted at
 *     https://github.com/helun/Ektorp
 * and is licensed under Apache 2.0
 * The changes were especially introduced via the commit
 *     https://github.com/helun/Ektorp/commit/1b534a99c689661e81aa497ba4f05e143eca8e04
 * and are related to following issue in the issue tracker
 *     https://github.com/helun/Ektorp/issues/201
 *
 */
package org.eclipse.sw360.datahandler.couchdb.CouchDbConnectorWithSecurity;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.sw360.datahandler.couchdb.DatabaseInstance;
import org.eclipse.sw360.datahandler.couchdb.MapperFactory;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.impl.StdCouchDbConnector;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CouchDbConnectorWithSecurity extends StdCouchDbConnector {

    private String adminRole = "_admin";

    public CouchDbConnectorWithSecurity(String dbName, DatabaseInstance instance, MapperFactory mapperFactory) {
        super(dbName,instance,mapperFactory);
    }

    private String securityPath() {
        return String.format("%s_security", dbURI);
    }

    public Security getSecurity() {
        return restTemplate.get(securityPath(),
                new StdResponseHandler<Security>() {
                    @Override
                    public Security success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(),
                                Security.class);
                    }
                }
        );
    }

    public Status updateSecurity(Security security) {
        try {
            return restTemplate.put(securityPath(),
                    objectMapper.writeValueAsString(security),
                    new StdResponseHandler<Status>() {
                        @Override
                        public Status success(HttpResponse hr) throws Exception {
                            return objectMapper.readValue(hr.getContent(),
                                    Status.class);
                        }
                    }
            );
        } catch(JsonProcessingException e) {
            throw new IllegalStateException("Failed to update security: " + e.getMessage());
        }
    }

    public Optional<Status> restrictAccessToAdmins() {
        boolean hasChanged = false;

        Function<SecurityGroup,SecurityGroup> addAdminRole = securityGroup -> {
            List<String> newGroupRoles = securityGroup.getRoles();
            newGroupRoles.add(adminRole);
            return new SecurityGroup(securityGroup.getNames(), newGroupRoles);
        };

        Security security = Optional.ofNullable(getSecurity())
                .orElse(new Security());

        SecurityGroup adminGroup = Optional.ofNullable(security.getAdmins())
                .orElse(new SecurityGroup());
        SecurityGroup memberGroup = Optional.ofNullable(security.getMembers())
                .orElse(new SecurityGroup());

        if(!adminGroup.getRoles().contains(adminRole)){
            adminGroup = addAdminRole.apply(adminGroup);
            hasChanged = true;
        }

        if(!memberGroup.getRoles().contains(adminRole)){
            memberGroup = addAdminRole.apply(memberGroup);
            hasChanged = true;
        }

        if(hasChanged){
            return Optional.of(updateSecurity(new Security(adminGroup,memberGroup)));
        }
        return Optional.empty();
    }
}
