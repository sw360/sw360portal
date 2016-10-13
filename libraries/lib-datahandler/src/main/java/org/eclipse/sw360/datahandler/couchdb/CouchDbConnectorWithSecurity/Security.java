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

/**
 * @author n3integration
 */
public class Security implements Serializable {

    private static final long serialVersionUID = -255108257321474837L;

    private final SecurityGroup admins;
    private final SecurityGroup members;

    public Security() {
        admins = new SecurityGroup();
        members = new SecurityGroup();
    }

    @JsonCreator
    public Security(@JsonProperty("admins") SecurityGroup admins, @JsonProperty("members") SecurityGroup members) {
        this.admins = admins;
        this.members = members;
    }

    public SecurityGroup getAdmins() {
        return admins;
    }

    public SecurityGroup getMembers() {
        return members;
    }
}