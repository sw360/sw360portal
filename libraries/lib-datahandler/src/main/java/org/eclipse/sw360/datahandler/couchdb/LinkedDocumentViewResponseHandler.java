package org.eclipse.sw360.datahandler.couchdb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.sw360.datahandler.couchdb.annotation.LinkedDocument;
import org.eclipse.sw360.datahandler.couchdb.annotation.LinkedDocuments;
import org.ektorp.DbAccessException;
import org.ektorp.ViewResultException;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdResponseHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This response handler is able to parse views that were created to load linked
 * documents. Such views contains as key always the main document id. However
 * the document itself can be of the main type or of type of a linked document.
 *
 * This handler will parse all rows and remember each document and its id.
 * Afterwards all main documents are traversed and ids are resolved to the
 * Remembered documents.
 *
 * The resulting json is then parsed by the object mapper which will generate
 * the correct types for each node.
 *
 * Fields that contain ids to linked documents must be annotated with
 * {@link LinkedDocuments} or {@link LinkedDocument}.
 */
public class LinkedDocumentViewResponseHandler<T> extends StdResponseHandler<List<T>> {
    private static final String ROWS_FIELD_NAME = "rows";

    private final Class<T> mainType;
    private final String mainTypeName;
    private final ObjectMapper objectMapper;

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    private static class Row {
        @SuppressWarnings("unused")
        private String id;
        private JsonNode key;
        @SuppressWarnings("unused")
        private JsonNode value;
        private JsonNode doc;
        private String error;
    }

    public LinkedDocumentViewResponseHandler(Class<T> mainType, String mainTypeName, ObjectMapper objectMapper) {
        this.mainType = mainType;
        this.mainTypeName = mainTypeName;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<T> success(HttpResponse response) throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(response.getContent());

        try {
            return parseResult(parser);
        } finally {
            parser.close();
        }
    }

    private List<T> parseResult(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new DbAccessException("Expected data to start with an Object");
        }

        while (parser.nextValue() != JsonToken.END_OBJECT) {
            String currentName = parser.getCurrentName();
            if (ROWS_FIELD_NAME.equals(currentName)) {
                return parseRows(parser);
            }
        }

        throw new DbAccessException("No rows found!");
    }

    private List<T> parseRows(JsonParser parser) throws IOException {
        Set<Row> mainRows = Sets.newHashSet();
        Map<String, JsonNode> objects = Maps.newHashMap();

        if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new DbAccessException("Expected rows to start with an Array");
        }

        // parse rows and remember objects and there id. Also remember main objects
        while (parser.nextToken() == JsonToken.START_OBJECT) {
            Row row = parser.readValueAs(Row.class);
            if (row.doc == null) {
                throw new ViewResultException(row.key, "Document not set in row.");
            }
            if (row.error != null) {
                throw new ViewResultException(row.key, row.error);
            }

            String objectId = row.doc.path("_id").asText();
            if (objectId == null) {
                throw new ViewResultException(row.doc, "Document does not specify an id (_id)");
            }
            objects.put(objectId, row.doc);

            String type = row.doc.path("type").asText();
            if (mainTypeName.equals(type)) {
                mainRows.add(row);
            }
        }

        // now resolve linked document fields in the main objects
        List<T> results = Lists.newArrayList();
        for (Row row : mainRows) {
            try {
                replaceIdsWithObjects(row.doc, objects);
                results.add(objectMapper.readValue(row.doc.traverse(parser.getCodec()), mainType));
            } catch (Exception exception) {
                throw new ViewResultException(row.doc, exception.getMessage());
            }
        }
        return results;
    }

    private void replaceIdsWithObjects(JsonNode document, Map<String, JsonNode> objects) {
        // we only have to respect annotated fields
        for (AnnotatedField field : objectMapper.getDeserializationConfig().introspect(SimpleType.construct(mainType)).getClassInfo().fields()) {
            if (field.hasAnnotation(LinkedDocuments.class)) {
                /*
                 *  support of arrays in the form of
                 *      - [ { _id: "<id>", ... } ]
                 *      - [ "<id>", ... ]
                 */

                JsonNode nodeToFill = document.path(field.getName());
                if (nodeToFill.isMissingNode()) {
                    throw new ViewResultException(document, "Field not found: " + field.getName());
                }

                if (!nodeToFill.isArray()) {
                    throw new ViewResultException(nodeToFill, "Field is not an array!");
                }

                ArrayNode nodeWithIds;
                String targetField = field.getAnnotation(LinkedDocuments.class).targetField();
                if (targetField != null) {
                    // we want to write the resolved objects into another field
                    // the original field will remain untouched
                    nodeWithIds = (ArrayNode) nodeToFill;
                    nodeToFill = JsonNodeFactory.instance.arrayNode();
                    ((ObjectNode) document).put(targetField, nodeToFill);
                } else {
                    nodeWithIds = (ArrayNode) nodeToFill.deepCopy();
                    ((ArrayNode) nodeToFill).removeAll();
                }

                for (JsonNode node : nodeWithIds) {
                    ((ArrayNode) nodeToFill).add(mapToObject(node, objects));
                }
            } else if (field.hasAnnotation(LinkedDocument.class)) {
                /*
                 *  support of fields in the form of
                 *      - { linkedId: { _id: "<id>" } }
                 *      - { linkedId: "<id>" }
                 */
                JsonNode node = document.path(field.getName());
                if (node.isMissingNode()) {
                    throw new ViewResultException(document, "Field not found: " + field.getName());
                }

                ((ObjectNode) document).put(field.getName(), mapToObject(node, objects));
            }
        }
    }

    private JsonNode mapToObject(JsonNode node, Map<String, JsonNode> objects) {
        if (node.isTextual()) {
            return objects.get(node.asText());
        } else if (node.isObject()) {
            return objects.get(node.path("_id").asText());
        } else {
            throw new ViewResultException(node, "Neither object nor string node.");
        }
    }
}
