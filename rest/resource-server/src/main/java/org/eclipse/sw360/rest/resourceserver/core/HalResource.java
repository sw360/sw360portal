/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HalResource<T> extends Resource<T> {

    private Map<String, Object> embeddedMap;

    public HalResource(T content, Link... links) {
        super(content, links);
    }

    @SuppressWarnings("unchecked")
    public void addEmbeddedResource(String relation, Object embeddedResource) {
        if (embeddedMap == null) {
            embeddedMap = new HashMap<>();
        }

        Object embeddedResources = embeddedMap.get(relation);
        boolean isPluralRelation = relation.endsWith("s");



        // if a relation is plural, the content will always be rendered as an array
        if(isPluralRelation) {
            if (embeddedResources == null) {
                embeddedResources = new ArrayList<>();
            }
            ((List<Object>) embeddedResources).add(embeddedResource);

        // if a relation is singular, it would be a single object if there is only one object available
        // Otherwise it would be rendered as array
        } else {
            if (embeddedResources == null) {
                embeddedResources = embeddedResource;
            } else {
                if (embeddedResources instanceof List) {
                    ((List<Object>) embeddedResources).add(embeddedResource);
                } else {
                    List<Object> embeddedResourcesList = new ArrayList<>();
                    embeddedResourcesList.add(embeddedResources);
                    embeddedResourcesList.add(embeddedResource);
                    embeddedResources = embeddedResourcesList;
                }
            }
        }
        embeddedMap.put(relation, embeddedResources);
    }

    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedRecources() {
        return embeddedMap;
    }
}
