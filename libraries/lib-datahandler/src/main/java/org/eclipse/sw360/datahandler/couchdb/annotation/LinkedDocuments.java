package org.eclipse.sw360.datahandler.couchdb.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark fields that only contain ids but should be expanded to full
 * objects.
 */
@JacksonAnnotation
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LinkedDocuments {

    /**
     * Can be used to control where to write the resolved objects to. By default the
     * annotated filed is replaced with the resolved object.
     *
     * <h4>Example</h4>
     *
     * <pre>
     *    {
     *       ids: [ ...],
     *       objects: []
     *    }
     * </pre>
     *
     * If such a json is parsed and the field "id" is annotated with
     * <code>&#64;LinkedDocuments(targetField = "objects")</code>, the "ids"-field
     * will remain untouched and the "objects"-field will be filled with the
     * resolved objects.
     */
    public String targetField();
}
