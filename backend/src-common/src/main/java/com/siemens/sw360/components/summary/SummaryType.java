/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.components.summary;

/**
 * Created by bodet on 10/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public enum SummaryType {
    SUMMARY, // Create a summary view of a document for use in the frontend summary datatables
    SHORT, // Create a very short copy of a document (typically with only ID and name), in particular for use in the frontend home portlets.
    EXPORT_SUMMARY, // Create a more detailed summary for the purpose of Excel export
    DETAILED_EXPORT_SUMMARY // Create a more detailed summary for the purpose of CSV export
}
