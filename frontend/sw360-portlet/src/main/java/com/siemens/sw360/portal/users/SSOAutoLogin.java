/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.users;

import com.siemens.sw360.datahandler.common.CommonUtils;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Basic single-sign-on implementation, just parses email and external id from 
 * incoming request
 *
 * @author cedric.bodet@tngtech.com, michael.c.jaeger@siemens.com
 */
public class SSOAutoLogin implements AutoLogin {

    private static final Logger log = LoggerFactory.getLogger(SSOAutoLogin.class);
    
    public static final String PROPERTIES_FILE_PATH = "/sw360.properties";
    private Properties props;
    
    public static final String AUTH_EMAIL_KEY = "key.auth.email";
    public static String AUTH_EMAIL_VALUE = "EMAIL";
    public static final String AUTH_EXTID_KEY = "key.auth.extid";
    public static String AUTH_EXTID_VALUE = "EXTID";

    public SSOAutoLogin() {
    	super();
        Properties props = CommonUtils.loadProperties(SSOAutoLogin.class, PROPERTIES_FILE_PATH);
        AUTH_EMAIL_VALUE = props.getProperty(AUTH_EMAIL_KEY, AUTH_EMAIL_VALUE);
        AUTH_EXTID_VALUE = props.getProperty(AUTH_EXTID_KEY, AUTH_EXTID_VALUE);
        log.info("Expecting the following header values for auto login email: '" 
          + AUTH_EMAIL_VALUE + "' and external ID: '" + AUTH_EXTID_VALUE + "'");
    }

    @Override
    public String[] handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws AutoLoginException {
        log.error("System exception during SSOAutologin", e);
        return new String[]{};
    }

    @Override
    public String[] login(HttpServletRequest request, HttpServletResponse response) throws AutoLoginException {
        String emailId = request.getHeader(AUTH_EMAIL_VALUE);
        String extid = request.getHeader(AUTH_EXTID_VALUE);

        log.info("Attempting auto login for email: '" + emailId + "' and external id: '" + extid + "'");

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            log.debug(key + ":" + value);
        }

        if (emailId == null || emailId.isEmpty() || extid == null || extid.isEmpty()) {
            log.error("Empty credentials, auto login impossible.");
            return new String[]{};
        }
        long companyId = PortalUtil.getCompanyId(request);

        User user = null;
        try {
            user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailId);
        } catch (SystemException | PortalException e) {
            log.error("Exception during get user by email: '" + emailId + "' and company id: '" + companyId + "'", e);
        }

        // If user was found by liferay
        if (user != null) {
            // Create a return credentials object
            return new String[]{
                    String.valueOf(user.getUserId()),
                    user.getPassword(), // Encrypted Liferay password
                    Boolean.TRUE.toString() // True: password is encrypted
            };
        } else {
            log.error("Could not get user with email: '" + emailId + "'.");
            return new String[]{};
        }
    }
}
