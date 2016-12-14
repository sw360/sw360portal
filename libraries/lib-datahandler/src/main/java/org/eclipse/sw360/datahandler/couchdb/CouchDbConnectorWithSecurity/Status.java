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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Standard status response
 *
 * @author henrik lundgren
 *
 */
public class Status implements Serializable {

	private static final long serialVersionUID = 6617269292660336903L;

	@JsonProperty("ok")
	boolean ok;

	private Map<String, Object> unknownFields;

	public boolean isOk() {
		return ok;
	}

	private Map<String, Object> unknown() {
		if (unknownFields == null) {
			unknownFields = new HashMap<String, Object>();
		}
		return unknownFields;
	}

	@JsonAnySetter
	public void setUnknown(String key, Object value) {
		unknown().put(key, value);
	}

	public Object getField(String key) {
		return unknown().get(key);
	}
}
