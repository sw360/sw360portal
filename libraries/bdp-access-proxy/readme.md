# Summary
This is an access layer to access the Blackduck Protex API through a concise object model.

# ! Be warned
this is currently work in progress and a lot of changes are likely to occur.

# Howto build

```
mvn clean install
```

If you do not have access to a bdp instance you can deactivate the system tests that connect directly to a bdp server
by calling

```
mvn clean install -PskipSystemTests
```

# Structural Overview
The project consists of five packages: an api package, an impl package a replay package (contains the mock code), 
a capture package and a test package.
The api package contains all the interfaces that can be used to access bdp.
The impl package contains an implementation that can access an actual bdp instance.
The replay package contains a mock implementation of the bdp access.
the capture package contains code to dump the data from BD Bdp
The test package contains system tests to test the functionality on a system wide level.

# Howto use the API
The whole api is accessible from a `BdpApiAccess` object. Call the `retrieveUser()` method to get a `User` object from 
which you can traverse the object tree.

If you use the mock `com.bosch.osmi.bdp.access.mock.BdpApiAccessMockImpl` implementation you can use an example 
file that already contains one entry. However, if you want another source you can use the overloaded constructor and 
pass a file path to another json file. E.g.
```
com.bosch.osmi.bdp.access.mock.BdpApiAccessMockImpl.BdpApiAccessMockImpl(<homedir>/.bdp-access-files/mockdata.json)
```

# How to add ...

## a new Property to an existing Entity

1. In module bdp-access-api at bdp-access-proxy/bdp-access-api/src/main/java/com/bosch/osmi/bdp/access/api/model
 search for the appropriate Entity (e.g. User if you want to add a property to User)

2. Open the interface and add a getter-Method with the appropriate return type and save

3. Open bdp-access-impl and path `bdp-access-proxy/bdp-access-impl/src/main/java/com/bosch/osmi/bdp/access/impl/model`

3. Open the implementation of the entity you want to add a property (e.g. UserImpl if you want to add a property to User)

4. Add a getter-method with the same signature as previously added to the interface with an appropriate implementation

    - You can use BdpApiAccessImpl access to retrieve the required api-implementation from the
    BdpSDK. E.g. if you need an access to the ProjectApi you can call access.getProjectApi() to retrieve
    the ProjectApi.
    
    - In case you need an Bdp API that is not yet implemented in `BdpApiAccessImpl` just add it the same way 
    the other apis retrieval methods are implemented

5. If you implement a property with a return type previously not used than this has to be added to 
com.bosch.osmi.bdp.access.mock.generator.MockDataGenerator.executeMethodsOfType too.
    
    
## a new Entity

1. Create a new interface in `bdp-access-proxy/bdp-access-api/src/main/java/com/bosch/osmi/bdp/access/api/model`

2. Derive this interface from `com.bosch.osmi.bdp.access.api.model.BdpEntity`

3. Create a new Impl class that implements that interface in `bdp-access-proxy/bdp-access-impl/src/main/java/com/bosch/osmi/bdp/access/impl/model`

4. Proceed for the properties as described in the previous section
