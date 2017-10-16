/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.authserver;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClientCredentialsGrantTest extends IntegrationTestBase {

    private ResponseEntity<String> responseEntity;

    @Before
    public void before() {
        responseEntity = getTokenWithParameters("client_id=trusted-sw360-client&grant_type=client_credentials");
    }

    @Test
    public void should_connect_to_authorization_server_with_client_credentials() {
        assertThat(HttpStatus.OK, is(responseEntity.getStatusCode()));
    }

    @Test
    public void should_get_expected_response_headers() throws IOException {
        checkResponseBody(responseEntity);
    }

    @Test
    public void should_get_expected_jwt_attributes() throws IOException {
        checkJwtClaims(responseEntity, "ROLE_TRUSTED_SW360_CLIENT");
    }
}