///*
// * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
// *
// * This program is free software; you can redistribute it and/or modify it under
// * the terms of the GNU General Public License Version 2.0 as published by the
// * Free Software Foundation with classpath exception.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
// * more details.
// *
// * You should have received a copy of the GNU General Public License along with
// * this program (please see the COPYING file); if not, write to the Free
// * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// * 02110-1301, USA.
// */
//
//package com.siemens.sw360.importer;
//
//import com.siemens.sw360.datahandler.common.ImportCSV;
//import com.siemens.sw360.datahandler.thrift.licenses.*;
//import org.apache.commons.csv.CSVRecord;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static org.junit.Assert.*;
//
///**
// * Testing the CSV conversion to Thrift objects
// * TODO mcj erase?
// *
// * @author cedric.bodet@tngtech.com
// * @author manuel.wickmann@tngtech.com
// */
//public class ConvertRecordTest {
//
//    private static final int NUMBER_OF_RISK_CATEGORIES = 3;
//    private static final int NUMBER_OF_RISKS = 5;
//    private static final int NUMBER_OF_TODOS = 6;
//
//
//    Map<Integer, RiskCategory> riskCategoryMap;
//    Map<Integer, Risk> riskMap;
//    Map<Integer, Todo> todoMap;
//    Map<Integer, Set<Integer>> obligationTodoMapping;
//    Map<String, Obligation> obligationMap;
//    Map<Integer, LicenseType> licenseTypeMap;
//    Map<String, Set<Integer>> licenseTodoMap;
//    Map<String, Set<Integer>> licenseRiskMap;
//    Map<String, License> licenseMap;
//
//
//    @Before
//    public void setUp() throws Exception {
//        List<CSVRecord> riskCategoryRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/riskcategory.csv").openStream());
//        riskCategoryMap = ConvertRecord.convertRiskCategories(riskCategoryRecords);
//
//        List<CSVRecord> riskRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/risk.csv").openStream());
//        riskMap = ConvertRecord.convertRisks(riskRecords, riskCategoryMap);
//
//        List<CSVRecord> obligationRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/obligation.csv").openStream());
//        obligationMap = ConvertRecord.convertObligation(obligationRecords);
//
//        List<CSVRecord> obligationTodoRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/obligationtodo.csv").openStream());
//        obligationTodoMapping = ConvertRecord.convertObligationTodo(obligationTodoRecords);
//
//        List<CSVRecord> todoRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/todo.csv").openStream());
//        todoMap = ConvertRecord.convertTodos(todoRecords, obligationMap, obligationTodoMapping);
//
//        List<CSVRecord> licenseTypeRecords = ImportCSV.readAsCSVRecords(getClass().getResource("/licensetype.csv").openStream());
//        licenseTypeMap = ConvertRecord.convertLicenseTypes(licenseTypeRecords);
//
//        List<CSVRecord> licenseTodoRecord = ImportCSV.readAsCSVRecords(getClass().getResource("/licensetodo.csv").openStream());
//        licenseTodoMap = ConvertRecord.convertRelationalTable(licenseTodoRecord);
//
//        List<CSVRecord> licenseRiskRecord = ImportCSV.readAsCSVRecords(getClass().getResource("/licenserisk.csv").openStream());
//        licenseRiskMap = ConvertRecord.convertRelationalTable(licenseRiskRecord);
//
//        List<CSVRecord> licenseRecord = ImportCSV.readAsCSVRecords(getClass().getResource("/license.csv").openStream());
//        licenseMap = ConvertRecord.fillLicenses(licenseRecord, licenseTypeMap, todoMap, riskMap, licenseTodoMap, licenseRiskMap);
//    }
//
//    @Test
//    public void testRiskCategories() throws Exception {
//        assertEquals(NUMBER_OF_RISK_CATEGORIES, riskCategoryMap.size());
//    }
//
//    @Test
//    public void testRiskCategoriesEntry() throws Exception {
//        RiskCategory category = riskCategoryMap.get(6);
//        assertEquals(6, category.getId());
//        assertEquals("Indemnification, and the like", category.getText());
//    }
//
//    @Test
//    public void testRiskCategoriesEntryQuotation() throws Exception {
//        RiskCategory category = riskCategoryMap.get(5);
//        assertEquals(5, category.getId());
//        assertEquals("Risk to'right to use'", category.getText());
//    }
//
//    @Test
//    public void testRisks() throws Exception {
//        assertEquals(NUMBER_OF_RISKS, riskMap.size());
//    }
//
//    @Test
//    public void testRisksEntry() throws Exception {
//        Risk risk = riskMap.get(49);
//
//        assertEquals(49, risk.getId());
//        assertEquals(5, risk.getCategory().getId());
//        assertEquals("Risk to'right to use'", risk.getCategory().getText());
//        assertEquals("Risk 49", risk.getText());
//    }
//
//    @Test
//    public void testTodos() throws Exception {
//        assertEquals(NUMBER_OF_TODOS, todoMap.size());
//    }
//
//    @Test
//    public void testTodosEntry() throws Exception {
//        Todo todo = todoMap.get(6);
//
//        assertEquals("6", todo.getId());
//        assertEquals("Todo6", todo.getText());
//        assertFalse(todo.isDevelopement());
//        assertTrue(todo.isDistribution());
//
//        assertEquals(1, todo.getObligationsSize());
//        assertEquals("Obl3", todo.getObligations().iterator().next().getName());
//    }
//
//    @Test
//    public void testObligations() throws Exception {
//        assertEquals(3, obligationMap.size());
//    }
//
//    @Test
//    public void testObligationsEntry() throws Exception {
//        Obligation obligation = obligationMap.get("15");
//
//        assertEquals("15", obligation.getId());
//        assertEquals("Obl15", obligation.getName());
//    }
//
//    @Test
//    public void testLicenses() throws Exception {
//        assertEquals(3, licenseMap.size());
//    }
//
//    @Test
//    public void testLicensesEntryArtistic() throws Exception {
//        License license = licenseMap.get("Artistic-1.0-Perl");
//
//        assertEquals("Artistic-1.0-Perl", license.getShortname());
//        assertEquals("Artistic License special for Perl", license.getFullname());
//        assertEquals(2, license.getLicenseType().getId());
//        assertEquals("Red", license.getLicenseType().getType());
//        assertFalse(license.isSetGPLv2Compat());
//        assertFalse(license.isGPLv2Compat());
//        assertTrue(license.isSetGPLv3Compat());
//        assertFalse(license.isGPLv3Compat());
//        assertEquals(1, license.getTodosSize());
//        assertEquals("9", license.getTodos().iterator().next().getId());
//        assertEquals(1, license.getRisksSize());
//        assertEquals(61, license.getRisks().get(0).getId());
//    }
//
//    @Test
//    public void testLicensesEntryApache() throws Exception {
//        License license = licenseMap.get("Apache-2.0");
//        Todo todo5 = todoMap.get(5);
//        Risk risk23 = riskMap.get(23);
//        Risk risk2 = riskMap.get(2);
//
//        assertEquals("Apache-2.0", license.getShortname());
//        assertEquals("Apache License 2.0", license.getFullname());
//        assertEquals(5, license.getLicenseType().getId());
//        assertEquals("Green", license.getLicenseType().getType());
//        assertTrue(license.isSetGPLv2Compat());
//        assertFalse(license.isGPLv2Compat());
//        assertTrue(license.isSetGPLv3Compat());
//        assertTrue(license.isGPLv3Compat());
//        assertEquals(3, license.getTodosSize());
//        assertTrue(license.getTodos().contains(todo5));
//        assertEquals(3, license.getRisksSize());
//        assertTrue(license.getRisks().contains(risk23));
//        assertFalse(license.getRisks().contains(risk2));
//    }
//
//}