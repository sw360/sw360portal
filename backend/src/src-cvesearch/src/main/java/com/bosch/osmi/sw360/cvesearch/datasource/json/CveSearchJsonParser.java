/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.bosch.osmi.sw360.cvesearch.datasource.json;

import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchData;
import com.google.gson.*;

import java.io.BufferedReader;
import java.lang.reflect.Type;

public class CveSearchJsonParser {
    Type type;

    public CveSearchJsonParser(Type type) {
        this.type = type;
    }

    private class VulnerableConfigurationEntryDeserializer implements JsonDeserializer<CveSearchData.VulnerableConfigurationEntry> {
        @Override
        public CveSearchData.VulnerableConfigurationEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()){
                final String id = jsonElement.getAsString();
                return new CveSearchData.VulnerableConfigurationEntry(id);
            }else{
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                final String title = jsonObject.get("title").getAsString();
                final String id = jsonObject.get("id").getAsString();
                return new CveSearchData.VulnerableConfigurationEntry(title, id);
            }
        }
    }

    public Object parseJsonBuffer(BufferedReader json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CveSearchData.VulnerableConfigurationEntry.class, new VulnerableConfigurationEntryDeserializer());
        final Gson gson = gsonBuilder.create();
        return gson.fromJson(json,type);
    }
}
