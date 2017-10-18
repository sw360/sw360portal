/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.authserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Component
public class Sw360AuthenticationProvider implements AuthenticationProvider {
    @Value("${sw360.test-user-id:#{null}}")
    private String testUserId;

    @Value("${sw360.test-user-password:#{null}}")
    private String testUserPassword;

    @Value("${sw360.sw360-portal-server-url}")
    private String sw360PortalServerURL;

    @Value("${sw360.sw360-liferay-company-id}")
    private String sw360LiferayCompanyId;

    @Autowired
    Environment environment;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName(); // this must be an email
        String password = authentication.getCredentials().toString();

        boolean isDev = environment.getActiveProfiles().length == 1 && environment.getActiveProfiles()[0].equals("dev");

        if (isDev && testUserId != null && testUserPassword != null) {
            // For easy testing without having a LifeRay portal running,
            // we mock an existing sw360 user
            if (name.equals(testUserId) && password.equals(testUserPassword)) {
                return createAuthenticationToken(name, password);
            }
        } else if (isValidString(sw360PortalServerURL) && isValidString(sw360LiferayCompanyId)) {
            String url = sw360PortalServerURL +
                    String.format("/api/jsonws/user/get-user-id-by-email-address?companyId=%s&emailAddress=%s",
                            sw360LiferayCompanyId, name);
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            String encodedPassword = null;
            try {
                encodedPassword = URLDecoder.decode(password, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            RestTemplate restTemplate = restTemplateBuilder.basicAuthorization(
                    name, encodedPassword).build();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, null, String.class);
            String userId = response.getBody();

            // if this is a number, everything is ok
            try {
                Integer.parseInt(userId);
            } catch (NumberFormatException e) {
                return null;
            }
            return createAuthenticationToken(name, password);
        }
        return null;
    }

    private Authentication createAuthenticationToken(String name, String password) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SW360_USER"));
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private boolean isValidString(String string) {
        return string != null && string.trim().length() != 0;
    }
}