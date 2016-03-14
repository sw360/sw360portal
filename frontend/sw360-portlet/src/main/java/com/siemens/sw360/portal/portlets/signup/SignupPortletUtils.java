package com.siemens.sw360.portal.portlets.signup;

import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortletUtils;

import javax.portlet.PortletRequest;
import java.util.HashSet;

/**
 * Signup portlet utils
 *
 * @author alex.borodin@evosoft.com
 */

public abstract class SignupPortletUtils {
    private SignupPortletUtils() {
        // Utility class with only static functions
    }

    static void updateUserFromRequest(PortletRequest request, User user) {
        for (User._Fields field : User._Fields.values()) {
            switch (field) {
                default:
                    setFieldValue(request, user, field);
            }
        }
    }

    private static void setFieldValue(PortletRequest request, User user , User._Fields field) {
        PortletUtils.setFieldValue(request, user, field, User.metaDataMap.get(field), "");
    }

}
