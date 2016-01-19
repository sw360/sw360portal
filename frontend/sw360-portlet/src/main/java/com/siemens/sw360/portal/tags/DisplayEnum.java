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

import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
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