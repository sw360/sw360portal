/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> exceptionHandler(Exception e) {
		return new ResponseEntity<>(new ErrorMessage(e, HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Data
	@RequiredArgsConstructor
	private static class ErrorMessage {

		public ErrorMessage(Exception e, HttpStatus httpStatus) {
			this.httpStatus = httpStatus.value();
			this.httpError = httpStatus.getReasonPhrase();
			this.message = e.getMessage();
		}

        @JsonSerialize(using = JsonInstantSerializer.class)
		private Instant timestamp = Instant.now();
		final private int httpStatus;
		final private String httpError;
		final private String message;
	}
}