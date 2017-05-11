/*
 * Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import org.eclipse.sw360.datahandler.common.CommonUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This displays a list
 *
 * @author Cedric.Bodet@tngtech.com Johannes.Najjar@tngtech.com
 * @author
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
            valueList = valueList.stream().map(s -> escapeHtml(s)).collect(Collectors.toList());
            getJspContext().getOut().print(CommonUtils.COMMA_JOINER.join(valueList));
        }
    }
}
