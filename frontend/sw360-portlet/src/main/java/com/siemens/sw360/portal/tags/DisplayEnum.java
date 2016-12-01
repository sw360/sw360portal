/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import org.eclipse.sw360.datahandler.common.ThriftEnumUtils;
import org.apache.thrift.TEnum;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * This displays a list
 *
 * @author Cedric.Bodet@tngtech.com Johannes.Najjar@tngtech.com
 */
public class DisplayEnum extends SimpleTagSupport {

    private TEnum value;

    public void setValue(TEnum value) {
        this.value = value;
    }

    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(ThriftEnumUtils.enumToString(value));
    }
}