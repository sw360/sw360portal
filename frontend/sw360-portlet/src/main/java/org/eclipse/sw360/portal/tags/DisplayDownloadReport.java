/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
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
import static org.eclipse.sw360.datahandler.common.CommonUtils.afterFunction;
import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.datahandler.thrift.ThriftUtils.extractField;
import static org.eclipse.sw360.datahandler.thrift.attachments.Attachment._Fields.ATTACHMENT_CONTENT_ID;
import static org.eclipse.sw360.datahandler.thrift.attachments.Attachment._Fields.ATTACHMENT_TYPE;
import static org.eclipse.sw360.datahandler.thrift.attachments.Attachment._Fields.FILENAME;

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
