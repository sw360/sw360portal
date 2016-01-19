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
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.all;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayEnumSelection extends SimpleTagSupport {

    private Class type;
    private TEnum selected;
    private String selectedName;
    private Boolean useStringValues = false;
    private Iterable<? extends TEnum> options;

    public void setType(Class type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public void setOptions(Iterable options) throws JspException {
        if (!all(options, instanceOf(TEnum.class))) {
            throw new JspException("given type options are not of class TEnum");
        }

        this.options = (Iterable<? extends TEnum>) options;
    }

    public void setSelected(TEnum selected) {
        this.selected = selected;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }

    public void setUseStringValues(Boolean useStringValues) {
        this.useStringValues = useStringValues;
    }

    public void doTag() throws JspException, IOException {
        if (options != null) {
            doEnumValues(options);
        } else if (type != null) {
            doEnumValues(ThriftEnumUtils.MAP_ENUMTYPE_MAP.get(type).keySet());
        } else {
            throw new JspException("you must select either a TEnum type or a collection of values");
        }
    }

    private void doEnumValues(Iterable<? extends TEnum> enums) throws IOException {
        JspWriter jspWriter = getJspContext().getOut();
        for (TEnum enumItem : enums) {
            String enumItemDescription = ThriftEnumUtils.enumToString(enumItem);

            boolean selected = enumItem.equals(this.selected) || enumItem.toString().equals(this.selectedName);
            String value = useStringValues ? enumItem.toString() : "" + enumItem.getValue();
            jspWriter.write(String.format(
                    "<option value=\"%s\" class=\"textlabel stackedLabel\" " +
                            (selected ? "selected=\"selected\" " : "") +
                            ">%s</option>",
                    value, enumItemDescription));
        }
    }
}