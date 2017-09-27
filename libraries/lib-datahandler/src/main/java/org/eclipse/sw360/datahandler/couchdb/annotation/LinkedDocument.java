package org.eclipse.sw360.datahandler.couchdb.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to mark fields that only contain an id but should be expanded to a full
 * object.
 */
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface LinkedDocument {

}
