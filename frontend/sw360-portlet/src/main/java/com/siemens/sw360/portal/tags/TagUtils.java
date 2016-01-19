package com.siemens.sw360.portal.tags;

import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Utils for Tags
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class TagUtils {

    public static final String NOT_SET = "--not set--";
    public static final String FIELD_NAME = "Field Name";
    public static final String CURRENT_VAL = "Current Value";
    public static final String SUGGESTED_VAL = "Suggested Value";

    public static <U extends TFieldIdEnum, T extends TBase<T, U>> void displaySimpleField(StringBuilder display, T oldInstance, T updateInstance, U field, FieldMetaData fieldMetaData, String prefix) {
        Object oldFieldValue = oldInstance.getFieldValue(field);
        Object updateFieldValue = updateInstance.getFieldValue(field);
        if (oldFieldValue == null && updateFieldValue == null) {
            return;
        }
        if ((oldFieldValue != null && !oldFieldValue.equals(updateFieldValue)) || oldFieldValue == null) {
            String oldDisplay = null;
            String updateDisplay = null;

            if (oldFieldValue != null) {
                oldDisplay = getDisplayString(fieldMetaData, oldFieldValue);
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

            if (isNullOrEmpty(oldDisplay)) {
                oldDisplay = NOT_SET;
            }

            display.append(String.format("<tr><td>%s:</td>", prefix + field.getFieldName()));
            display.append(String.format("<td><input type=\"text\" readonly=\"\" value=\"%s\" id=\"%sOld\" class=\"oldValue\" /></td>", oldDisplay, prefix + field.getFieldName()));
            display.append(String.format("<td><input type=\"text\" readonly=\"\" value=\"%s\" id=\"%sNew\" class=\"newValue\" /></td></tr> ", updateDisplay, prefix + field.getFieldName()));

        }

    }

    // TODO check if the commented out sections can be removed
    public static String getDisplayString(FieldMetaData fieldMetaData, Object fieldValue) {
        String fieldDisplay;
        switch (fieldMetaData.valueMetaData.type) {
            case org.apache.thrift.protocol.TType.SET:
//                            instance.setFieldValue(field, CommonUtils.splitToSet(value));
//                            break;
            case org.apache.thrift.protocol.TType.ENUM:
//                            if (!"".equals(value))
//                                instance.setFieldValue(field, enumFromString(value, field));
//                            break;
            case org.apache.thrift.protocol.TType.I32:
//                            if (!"".equals(value))
//                                instance.setFieldValue(field, Integer.parseInt(value));
//                            break;
            case org.apache.thrift.protocol.TType.BOOL:
//                            if (!"".equals(value))
//                                instance.setFieldValue(field, true);
//                            break;
            default:

                fieldDisplay = fieldValue.toString();
        }
        return fieldDisplay;
    }




}
