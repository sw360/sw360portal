/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.tags;

import com.siemens.sw360.datahandler.common.CommonUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This displays a list
 *
 * @author Cedric.Bodet@tngtech.com Johannes.Najjar@tngtech.com
 */
public class DisplayCollection extends SimpleTagSupport {

    private Collection<String> value;
    private Collection<String> autoFillValue;

    public void setValue(Collection<String> value) {
        this.value = value;
    }

    public void setAutoFillValue(Collection<String> autoFillValue) {
        this.autoFillValue = autoFillValue;
    }

    public void doTag() throws JspException, IOException {
        Collection<String> fullValue;

        if (value == null)
            fullValue = autoFillValue;
        else {
            fullValue = value;
        }


        if (null != fullValue && !fullValue.isEmpty()) {
            List<String> valueList = new ArrayList<>(fullValue);
            Collections.sort(valueList, String.CASE_INSENSITIVE_ORDER);
            getJspContext().getOut().print(CommonUtils.COMMA_JOINER.join(valueList));
        }
    }
}
