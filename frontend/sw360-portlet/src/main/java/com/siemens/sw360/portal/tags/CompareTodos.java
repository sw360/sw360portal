/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.licenses.Obligation;
import com.siemens.sw360.datahandler.thrift.licenses.Todo;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;
import static com.siemens.sw360.portal.tags.TagUtils.*;

/**
 * Display the todos that are added or added to/removed from department whitelist
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class CompareTodos extends NameSpaceAwareTag {
    public static final List<Todo._Fields> RELEVANT_FIELDS = FluentIterable
            .from(copyOf(Todo._Fields.values())).filter(field -> isFieldRelevant(field)).toList();

    private List<Todo> old;
    private List<Todo> update;
    private List<Todo> delete;
    private String department;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setOld(List<Todo> old) {
        this.old = nullToEmptyList(old);
    }

    public void setUpdate(List<Todo> update) {
        this.update = nullToEmptyList(update);
    }

    public void setDelete(List<Todo> delete) {
        this.delete = nullToEmptyList(delete);
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setTableClasses(String tableClasses) {
        this.tableClasses = tableClasses;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public int doStartTag() throws JspException {

        JspWriter jspWriter = pageContext.getOut();
        StringBuilder display = new StringBuilder();
        String namespace = getNamespace();

        try {
            renderTodos(display, old, update, delete);

            String renderString = display.toString();

            if (Strings.isNullOrEmpty(renderString)) {
                renderString = "<h4> No changes in TODOs </h4>";
            }

            jspWriter.print(renderString);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private void renderTodos(StringBuilder display, List<Todo> current, List<Todo> update, List<Todo> delete) {
        List<Todo> newWhitelistedTodos = update
                .stream()
                .filter(CommonUtils::isTemporaryTodo)
                .filter(todo -> todo.isSetWhitelist() && todo.getWhitelist().contains(department))
                .collect(Collectors.toList());
        Map<String, Todo> newWhitelistedTodosById = getTodosById(newWhitelistedTodos);
        Set<String> newWhitelistedTodoIds = newWhitelistedTodosById.keySet();
        renderTodoList(display, newWhitelistedTodosById, newWhitelistedTodoIds, "Add to database and to whitelist of " + department);

        List<Todo> newBlacklistedTodos = update
                .stream()
                .filter(CommonUtils::isTemporaryTodo)
                .filter(todo -> !todo.isSetWhitelist() || !todo.getWhitelist().contains(department))
                .collect(Collectors.toList());
        Map<String, Todo> newBlacklistedTodosById = getTodosById(newBlacklistedTodos);
        Set<String> newBlacklistedTodoIds = newBlacklistedTodosById.keySet();
        renderTodoList(display, newBlacklistedTodosById, newBlacklistedTodoIds, "Add to database and <em>not</em> to whitelist of " + department);

        Map<String, Todo> currentTodosById = getTodosById(current);
        Set<String> currentTodoIds = currentTodosById.keySet();
        List<Todo> whitelistedTodos = update
                .stream()
                .filter(todo -> !CommonUtils.isTemporaryTodo(todo))
                .filter(todo -> todo.isSetWhitelist() && todo.getWhitelist().contains(department))
                .filter(todo -> currentTodoIds.contains(todo.getId()))
                .filter(todo -> !(currentTodosById.get(todo.getId()).isSetWhitelist() &&
                        currentTodosById.get(todo.getId()).getWhitelist().contains(department)))
                .collect(Collectors.toList());

        Set<String> whitelistedTodoIds = getTodosById(whitelistedTodos).keySet();
        renderTodoList(display, currentTodosById, whitelistedTodoIds, "Add to whitelist of " + department);

        List<Todo> blacklistedTodos = delete
                .stream()
                .filter(todo -> todo.isSetWhitelist() && todo.getWhitelist().contains(department))
                .filter(todo -> currentTodoIds.contains(todo.getId()))
                .filter(todo -> currentTodosById.get(todo.getId()).isSetWhitelist() &&
                        currentTodosById.get(todo.getId()).getWhitelist().contains(department))
                .collect(Collectors.toList());

        Set<String> blacklistedTodoIds = getTodosById(blacklistedTodos).keySet();
        renderTodoList(display, currentTodosById, blacklistedTodoIds, "Remove from whitelist of " + department);

    }

    private void renderTodoList(StringBuilder display, Map<String, Todo> allTodos, Set<String> todoIds, String msg) {
        if (todoIds.isEmpty()) return;
        display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));

        renderTodoRowHeader(display, msg);
        for (String deletedTodoId : todoIds) {
            renderTodoRow(display, allTodos.get(deletedTodoId));
        }

        display.append("</table>");
    }

    private static void renderTodoRowHeader(StringBuilder display, String msg) {

        display.append(String.format("<thead><tr><th colspan=\"%d\"> %s</th></tr><tr>", RELEVANT_FIELDS.size(), msg));
        for (Todo._Fields field : RELEVANT_FIELDS) {
            display.append(String.format("<th>%s</th>", field.getFieldName()));
        }
        display.append("</tr></thead>");
    }

    private static void renderTodoRow(StringBuilder display, Todo todo) {
        display.append("<tr>");
        for (Todo._Fields field : RELEVANT_FIELDS) {

            FieldMetaData fieldMetaData = Todo.metaDataMap.get(field);
            Object fieldValue = todo.getFieldValue(field);
            if (field.equals(Todo._Fields.OBLIGATIONS) && fieldValue != null) {
                fieldValue =
                        ((List<Obligation>) fieldValue).stream()
                                .map(Obligation::getName)
                                .collect(Collectors.toList());
            }
            display.append(String.format("<td>%s</td>", getDisplayString(fieldMetaData, fieldValue)));

        }
        display.append("</tr>");
    }

    private static Map<String, Todo> getTodosById(List<Todo> currentTodos) {
        return Maps.uniqueIndex(currentTodos, input -> input.getId());
    }

    private static boolean isFieldRelevant(Todo._Fields field) {
        switch (field) {
            //ignored Fields
            case ID:
            case REVISION:
            case TYPE:
            case OBLIGATION_DATABASE_IDS:
            case TODO_ID:
            case DEVELOPMENT_STRING:
            case DISTRIBUTION_STRING:
            case WHITELIST:
                return false;
            default:
                return true;
        }
    }
}
