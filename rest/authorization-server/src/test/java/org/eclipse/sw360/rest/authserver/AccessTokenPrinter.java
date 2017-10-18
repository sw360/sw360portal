/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.authserver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AccessTokenPrinter {
    public static Properties getPropertiesFromApplicationYml() {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new ClassPathResource("application.yml"));
        return yamlPropertiesFactoryBean.getObject();
    }

    public static void main(String[] args) throws IOException {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ERROR);

        if (args.length != 3) {
            System.out.println("usage: ./gradlew printAccessToken <auth server URL> <user> <password>");
            return;
        }

        AccessTokenPrinter accessTokenPrinter = new AccessTokenPrinter();
        accessTokenPrinter.printAccessToken(args[0], args[1], args[2]);
    }

    private void printAccessToken(String authServerURL, String userId, String userPassword) throws IOException {
        String encodedPassword = null;
        try {
            encodedPassword = URLEncoder.encode(userPassword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }

        String url = authServerURL + "/oauth/token?grant_type=password&username=" + userId + "&password=" + encodedPassword;

        ResponseEntity<String> responseEntity = new TestRestTemplate(
                "trusted-sw360-client",
                "sw360-secret")
                .postForEntity(url,
                        null,
                        String.class);

        String responseBody = responseEntity.getBody();
        JsonNode responseBodyJsonNode = new ObjectMapper().readTree(responseBody);
        assertThat(responseBodyJsonNode.has("access_token"), is(true));

        String accessToken = responseBodyJsonNode.get("access_token").asText();
        System.out.println("Authorization: Bearer " + accessToken);
    }
}
