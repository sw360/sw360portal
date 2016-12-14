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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author n3integration
 */
public class SecurityGroup implements Serializable {

    private static final long serialVersionUID = -346108257321474838L;

    private final List<String> names;
    private final List<String> roles;

    public SecurityGroup() {
        this.names = new ArrayList<String>();
        this.roles = new ArrayList<String>();
    }

    @JsonCreator
    public SecurityGroup(@JsonProperty("names") List<String> names, @JsonProperty("roles") List<String> roles) {
        this.names = names;
        this.roles = roles;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getRoles() {
        return roles;
    }
}