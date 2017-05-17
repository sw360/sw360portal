/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags.links;

import org.apache.log4j.Logger;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.portal.tags.ContextAwareTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.eclipse.sw360.portal.tags.TagUtils.addDownloadLink;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This displays a Download link for an attachment given its contentdId
 *
 * @author daniele.fognini@tngtech.com
 */
public class DisplayDownloadAttachment extends ContextAwareTag {
    protected Collection<String> ids = Collections.emptySet();
    private String contextType;
    private String contextId;
    private String name = "";
    private Logger log = Logger.getLogger(DisplayDownloadAttachment.class);

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter jspWriter = pageContext.getOut();
            addDownloadLink(pageContext, jspWriter, name, ids, contextType, contextId);
        } catch (Exception e) {
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    public void setId(String id) {
        this.ids = Collections.singleton(id);
    }

    public void setIds(Object object) {
        if (object != null){
            if (object instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) object;
                if (collection.size() > 0) {
                    Set<String> ids = new HashSet<>();
                    Object elem = collection.stream().findAny().get();
                    if(elem instanceof Attachment){
                        collection.forEach(
                                e -> ids.add((((Attachment) e).attachmentContentId))
                        );
                    } else {
                        log.error("Unhandled element type");
                    }
                    this.ids = ids;
                }
            } else {
                log.error("Unhandled collection type");
            }
        }
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }


    public void setName(String name) {
        this.name = escapeHtml(" " + name);
    }
}
