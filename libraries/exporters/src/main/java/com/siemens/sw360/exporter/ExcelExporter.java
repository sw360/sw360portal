/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.exporter;


import com.siemens.sw360.datahandler.thrift.SW360Exception;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created on 06/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ExcelExporter<T> {

    private final ExporterHelper<T> helper;

    public ExcelExporter(ExporterHelper<T> helper) {
        this.helper = helper;
    }

    public InputStream makeExcelExport(List<T> documents) throws IOException, SW360Exception {
        final Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Data");

        /** Adding styles to cells */
        CellStyle cellStyle = createCellStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);

        /** Create header row */
        Row headerRow = sheet.createRow(0);
        List<String> headerNames = helper.getHeaders();
        fillRow(headerRow, headerNames, headerStyle);

        /** Create data rows */
        fillValues(sheet, documents, cellStyle);

        /** Resize the columns */
        for (int iColumns = 0; iColumns < helper.getColumns(); iColumns++) {
            sheet.autoSizeColumn(iColumns);
        }

        /** Copy the streams */
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Convert all documents to
     */
    private void fillValues(Sheet sheet, List<T> documents, CellStyle style) throws SW360Exception {
        int numberoOfDocuments = documents.size();
        int nextExcelSheetRow = 1;
        for (int currentDocNumber = 0; currentDocNumber < numberoOfDocuments; currentDocNumber++) {
            T document = documents.get(currentDocNumber);
            SubTable table = helper.makeRows(document);
            for(int currentTableRow = 0; currentTableRow < table.getnRows(); currentTableRow ++){
                List<String> rowValues = table.getRow(currentTableRow);
                Row row = sheet.createRow(nextExcelSheetRow);
                nextExcelSheetRow++;
                fillRow(row, rowValues, style);
            }
        }
    }

    /**
     * Write the values into the row, setting the cells to the given style
     */
    private void fillRow(Row row, List<String> values, CellStyle style) {
        if(values.size() < helper.getColumns()){
            throw new IllegalArgumentException("List of row values is too short.");
        }
        for (int column = 0; column < helper.getColumns(); column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(values.get(column));
            cell.setCellStyle(style);
        }
    }

    /**
     * Create style for data cells
     */
    private static CellStyle createCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        return cellStyle;
    }

    /**
     * Create header style, same has cell style but with bold font
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerCellStyle = createCellStyle(workbook);
        Font font = workbook.createFont();
        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerCellStyle.setFont(font);
        return headerCellStyle;
    }
}
