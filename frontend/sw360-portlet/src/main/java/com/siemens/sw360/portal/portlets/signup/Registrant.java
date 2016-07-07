/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.portlets.signup;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Validator;
import com.siemens.sw360.datahandler.common.SW360Assert;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.users.UserPortletUtils;
import com.siemens.sw360.portal.users.UserUtils;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Registrant class for SignupPortlet
 *
 * @author alex.borodin@evosoft.com
 */
class Registrant extends User {

    private boolean isMale;
    private String password;
    private String password2;

    public Registrant(ActionRequest request) {
        super();
        isMale = false;
        password = request.getParameter(PortalConstants.PASSWORD);
        password2 = request.getParameter(PortalConstants.PASSWORD_REPEAT);
        setWantsMailNotification(true);
    }

    public com.liferay.portal.model.User addLifeRayUser(PortletRequest request) throws PortalException, SystemException {
        return UserPortletUtils.addLiferayUser(request, getGivenname(), getLastname(), getEmail(),
                getDepartment(), UserUtils.getRoleConstantFromUserGroup(getUserGroup()), isMale, getExternalid(), password, false, false);

    }

    public boolean validateUserData(ActionRequest request) {
        if (isNullOrEmpty(getGivenname())) {
            SessionMessages.add(request, "request_processed", "First name cannot be empty");
            return false;
        }
        if (isNullOrEmpty(getLastname())) {
            SessionMessages.add(request, "request_processed", "Last name cannot be empty");
            return false;
        }
        if (!Validator.isEmailAddress(getEmail())) {
            SessionMessages.add(request, "request_processed", "Email is not valid");
            return false;
        }
        if (isNullOrEmpty(getDepartment())) {
            SessionMessages.add(request, "request_processed", "Department cannot be empty");
            return false;
        }
        if (isNullOrEmpty(getExternalid())) {
            SessionMessages.add(request, "request_processed", "External ID cannot be empty");
            return false;
        }
        if (isNullOrEmpty(password)) {
            SessionMessages.add(request, "request_processed", "Password cannot be empty");
            return false;
        }
        try {
            SW360Assert.assertEquals(password, password2);
        } catch (SW360Exception e) {
            SessionMessages.add(request, "request_processed", "Passwords don't match");
            return false;
        }

        return true;
    }


}
