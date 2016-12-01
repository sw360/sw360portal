package org.eclipse.sw360.portal.tags;

import com.google.common.collect.Sets;
import org.eclipse.sw360.portal.common.PortalConstants;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.protocol.TType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.portal.tags.urlutils.UrlWriterImpl.resourceUrl;
import static java.lang.String.format;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Utils for Tags
 *
 * @author Johannes.Najjar@tngtech.com
 * @author birgit.heydenreich@tngtech.com
 */
public class TagUtils {

    public static final String NOT_SET = "--not set--";
    public static final String FIELD_NAME = "Field Name";
    public static final String CURRENT_VAL = "Current Value";
    public static final String DELETED_VAL = "Former Value";
    public static final String SUGGESTED_VAL = "Suggested Value";

    public static <U extends TFieldIdEnum, T extends TBase<T, U>> void displaySimpleFieldOrSet(StringBuilder display,
                                                                                               T oldInstance,
                                                                                               T additions,
                                                                                               T deletions,
                                                                                               U field,
                                                                                               FieldMetaData fieldMetaData,
                                                                                               String prefix) {
        Object oldFieldValue = oldInstance.getFieldValue(field);
        Object deletedFieldValue = deletions.getFieldValue(field);
        Object updateFieldValue = additions.getFieldValue(field);

        if (updateFieldValue == null && deletedFieldValue == null) {
            return; //no intent to change something
        }
        if (updateFieldValue != null && updateFieldValue.equals(deletedFieldValue)) {
            return; //no intent to change something
        }
        if (updateFieldValue != null && updateFieldValue.equals(oldFieldValue)) {
            return; //no actual change
        }
        if (oldFieldValue == null && updateFieldValue == null) {
            return; //no actual change
        }
        if ((oldFieldValue != null && !oldFieldValue.equals(updateFieldValue)) || oldFieldValue == null) {
            if (fieldMetaData.valueMetaData.type == TType.SET) {
                displaySet(
                        display,
                        (Set<String>) oldFieldValue,
                        (Set<String>) updateFieldValue,
                        (Set<String>) deletedFieldValue,
                        field,
                        fieldMetaData,
                        prefix);
            } else {
                displaySimpleField(display, oldFieldValue, updateFieldValue, deletedFieldValue, field, fieldMetaData, prefix);
            }
        }
    }

    private static <U extends TFieldIdEnum, T extends TBase<T, U>> void displaySet(StringBuilder display,
                                                                                   Set<String> oldFieldValue,
                                                                                   Set<String> updateFieldValue,
                                                                                   Set<String> deletedFieldValue,
                                                                                   U field,
                                                                                   FieldMetaData fieldMetaData,
                                                                                   String prefix) {
        String oldDisplay = null;
        String deleteDisplay = "n.a. (modified list)";
        String updateDisplay = null;

        if (oldFieldValue != null) {
            oldDisplay = getDisplayString(fieldMetaData, oldFieldValue);
        }
        if (updateFieldValue != null) {
            updateDisplay = getDisplayString(fieldMetaData,
                    Sets.difference(
                            Sets.union(nullToEmptySet(oldFieldValue), nullToEmptySet(updateFieldValue)),
                            nullToEmptySet(deletedFieldValue)));
        }
        if (isNullOrEmpty(updateDisplay) && isNullOrEmpty(oldDisplay)) {
            return;
        }
        if (isNullOrEmpty(updateDisplay)) {
            updateDisplay = NOT_SET;
        }
        if (isNullOrEmpty(oldDisplay)) {
            oldDisplay = NOT_SET;
        }

        display.append(String.format("<tr><td>%s:</td>", prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td>", oldDisplay, prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td>", deleteDisplay, prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td></tr> ", updateDisplay, prefix + field.getFieldName()));
    }

    private static <U extends TFieldIdEnum, T extends TBase<T, U>> void displaySimpleField(StringBuilder display,
                                                                                           Object oldFieldValue,
                                                                                           Object updateFieldValue,
                                                                                           Object deletedFieldValue,
                                                                                           U field,
                                                                                           FieldMetaData fieldMetaData,
                                                                                           String prefix) {
        String oldDisplay = null;
        String deleteDisplay = null;
        String updateDisplay = null;

        if (oldFieldValue != null) {
            oldDisplay = getDisplayString(fieldMetaData, oldFieldValue);
        }
        if (deletedFieldValue != null) {
            deleteDisplay = getDisplayString(fieldMetaData, deletedFieldValue);
        }
        if (updateFieldValue != null) {
            updateDisplay = getDisplayString(fieldMetaData, updateFieldValue);
        }
        if (isNullOrEmpty(updateDisplay) && isNullOrEmpty(oldDisplay)) {
            return;
        }
        if (isNullOrEmpty(updateDisplay)) {
            updateDisplay = NOT_SET;
        }
        if (isNullOrEmpty(deleteDisplay)) {
            deleteDisplay = NOT_SET;
        }
        if (isNullOrEmpty(oldDisplay)) {
            oldDisplay = NOT_SET;
        }

        display.append(String.format("<tr><td>%s:</td>", prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td>", oldDisplay, prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td>", deleteDisplay, prefix + field.getFieldName()));
        display.append(String.format("<td>%s</td></tr> ", updateDisplay, prefix + field.getFieldName()));
    }

    public static String getDisplayString(FieldMetaData fieldMetaData, Object fieldValue) {
        String fieldDisplay = "";
        switch (fieldMetaData.valueMetaData.type) {
            case org.apache.thrift.protocol.TType.LIST:
                if (fieldValue != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<ul>");
                    for (Object o : ((List<Object>) fieldValue)) {
                        sb.append("<li>" + o.toString() + "</li>");
                    }
                    sb.append("</ul>");
                    fieldDisplay = sb.toString();
                }
                break;
            case org.apache.thrift.protocol.TType.SET:
                if (fieldValue != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<ul>");
                    for (Object o : ((Set<Object>) fieldValue)) {
                        sb.append("<li>" + o.toString() + "</li>");
                    }
                    sb.append("</ul>");
                    fieldDisplay = sb.toString();
                }
                break;
            default:

                fieldDisplay = fieldValue == null ? "" : fieldValue.toString();
        }
        return fieldDisplay;
    }

    public static void addDownloadLink(PageContext pageContext, JspWriter jspWriter, String name, String id)
                                                                                     throws IOException, JspException {
        name = escapeHtml(" " + name);
        jspWriter.write("<a href='");
        resourceUrl(pageContext)
                .withParam(PortalConstants.ACTION, PortalConstants.ATTACHMENT_DOWNLOAD)
                .withParam(PortalConstants.ATTACHMENT_ID, id)
                .writeUrlToJspWriter();
        jspWriter.write(format(
                "'><img src='%s/images/downloadEnable.jpg' alt='Download%s' title='Download%s'/>",
                ((HttpServletRequest) pageContext.getRequest()).getContextPath(), name, name));
        jspWriter.write("</a>");
    }

}
