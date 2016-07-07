/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.components.summary;

/**
 * Created by bodet on 10/02/15.
 *
 * @author cedric.bodet@tngtech.com
 * @author andreas.reichel@tngtech.com
 */
public enum SummaryType {
    SUMMARY, // Create a summary view of a document for use in the frontend summary datatables
    SHORT, // Create a very short copy of a document (typically with only ID and name), in particular for use in the frontend home portlets.
    EXPORT_SUMMARY, // Create a more detailed summary for the purpose of Excel export
    HOME, // Create a more detailed summary with releases array for MyComponents portlet
    DETAILED_EXPORT_SUMMARY // Create a more detailed summary for the purpose of CSV export
}
