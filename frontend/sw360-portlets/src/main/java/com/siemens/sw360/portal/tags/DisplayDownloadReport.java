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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import org.apache.thrift.TBase;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.jstl.core.LoopTagSupport;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Maps.transformValues;
import static com.siemens.sw360.datahandler.common.CommonUtils.afterFunction;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.extractField;
import static com.siemens.sw360.datahandler.thrift.attachments.Attachment._Fields.ATTACHMENT_CONTENT_ID;
import static com.siemens.sw360.datahandler.thrift.attachments.Attachment._Fields.ATTACHMENT_TYPE;
import static com.siemens.sw360.datahandler.thrift.attachments.Attachment._Fields.FILENAME;

/**
 * This displays a Download Report attachment if available
 *
 * @author daniele.fognini@tngtech.com
 */
public class DisplayDownloadReport extends LoopTagSupport {

    private FluentIterable<Attachment> attachments;
    private Iterator<Map.Entry<String, String>> attachmentIterator;

    @Override
    protected Object next() throws JspTagException {
        return attachmentIterator.next();
    }

    @Override
    protected boolean hasNext() throws JspTagException {
        return attachmentIterator.hasNext();
    }

    @Override
    protected void prepare() throws JspTagException {
        Function<Attachment, String> extractId = extractField(ATTACHMENT_CONTENT_ID, String.class);
        Function<Attachment, String> extractFileName = extractField(FILENAME, String.class);

        Map<String, Attachment> mapped = attachments.uniqueIndex(extractId);

        Map<String, String> idToName = transformValues(mapped, extractFileName);

        attachmentIterator = idToName.entrySet().iterator();
    }

    @Override
    public int doStartTag() throws JspException {
        JspWriter jspWriter = pageContext.getOut();
        try {
            jspWriter.write("</span>");
        } catch (IOException e) {
            throw new JspException(e);
        }

        if (attachments.isEmpty()) {
            try {
                jspWriter.print("no report");
            } catch (Exception e) {
                throw new JspException(e);
            }
            return SKIP_BODY;
        } else {
            return super.doStartTag();
        }
    }

    @Override
    public int doEndTag() throws JspException {
        JspWriter jspWriter = pageContext.getOut();
        try {
            jspWriter.write("</span>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return super.doEndTag();
    }

    public void setAttachments(Set<Attachment> attachments) {
        Function<Attachment, AttachmentType> extractAttachmentType = extractField(ATTACHMENT_TYPE, AttachmentType.class);

        this.attachments = FluentIterable.from(nullToEmptySet(attachments))
                .filter(afterFunction(extractAttachmentType).is(equalTo(AttachmentType.CLEARING_REPORT)));
    }

    public void setAttachmentEntryVar(String name) {
        this.setVar(name);
    }
}
