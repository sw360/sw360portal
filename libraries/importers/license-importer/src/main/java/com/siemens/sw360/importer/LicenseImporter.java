/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.importer;

import com.siemens.sw360.commonIO.ConvertRecord;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.ImportCSV;
import com.siemens.sw360.commonIO.TypeMappings;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Main class of the license importer
 *
 * @author cedric.bodet@tngtech.com
 * @author manuel.wickmann@tngtech.com
 */
public class LicenseImporter {

    private static final Logger log = Logger.getLogger(LicenseImporter.class);

    private LicenseImporter() {
        // Utility class with only static functions
    }

    private static URL getURL(String filename) throws MalformedURLException {
        return new File(System.getProperty("user.dir") + File.separator + filename).toURI().toURL();
    }

    private static void addLicenses(LicenseService.Iface licenseClient) throws IOException {
        Properties properties = CommonUtils.loadProperties(LicenseImporter.class, "/csvfiles.properties");
        String riskCategoryFilename = properties.getProperty("riskCategoryFilename", "dbo.Riskcategory.csv");
        String riskFilename = properties.getProperty("riskFilename", "dbo.Risk.csv");
        String obligationFilename = properties.getProperty("obligationFilename", "dbo.Obligation.csv");
        String obligationTodoFilename = properties.getProperty("obligationTodoFilename", "dbo.obligationtodo.csv");
        String todoFilename = properties.getProperty("todoFilename", "dbo.Todo.csv");
        String licenseTypeFilename = properties.getProperty("licenseTypeFilename", "dbo.Licensetype.csv");
        String licenseTodoFilename = properties.getProperty("licenseTodoFilename", "dbo.licensetodo.csv");
        String licenseRiskFilename = properties.getProperty("licenseRiskFilename", "dbo.licenserisk.csv");
        String licenseFilename = properties.getProperty("licenseFilename", "dbo.License.csv");

        log.debug("Parsing risk Categories ...");
        Map<Integer, RiskCategory> riskCategoryMap = getIntegerRiskCategoryMap(licenseClient, riskCategoryFilename);

        log.debug("Parsing risks ...");
        Map<Integer, Risk> riskMap = getIntegerRiskMap(licenseClient, riskFilename, riskCategoryMap);


        log.debug("Parsing obligations ...");
        Map<Integer, Obligation> obligationMap = getIntegerObligationMap(licenseClient, obligationFilename);


        log.debug("Parsing obligation todos ...");
        Map<Integer, Set<Integer>> obligationTodoMapping;
        try (final InputStream in = getURL(obligationTodoFilename).openStream()) {
            List<CSVRecord> obligationTodoRecords = ImportCSV.readAsCSVRecords(in);
            obligationTodoMapping = ConvertRecord.convertObligationTodo(obligationTodoRecords);
        }

        log.debug("Parsing todos ...");
        Map<Integer, Todo> todoMap;
        todoMap = getTodoMap(licenseClient, todoFilename, obligationMap, obligationTodoMapping);

        log.debug("Parsing license types ...");
        Map<Integer, LicenseType> licenseTypeMap = getLicenseTypeMap(licenseClient, licenseTypeFilename);


        log.debug("Parsing license todos ...");
        Map<String, Set<Integer>> licenseTodoMap;
        try (final InputStream in = getURL(licenseTodoFilename).openStream()) {
            List<CSVRecord> licenseTodoRecord = ImportCSV.readAsCSVRecords(in);
            licenseTodoMap = ConvertRecord.convertRelationalTable(licenseTodoRecord);
        }

        log.debug("Parsing license risks ...");
        Map<String, Set<Integer>> licenseRiskMap;
        try (final InputStream in = getURL(licenseRiskFilename).openStream()) {
            List<CSVRecord> licenseRiskRecord = ImportCSV.readAsCSVRecords(in);
            licenseRiskMap = ConvertRecord.convertRelationalTable(licenseRiskRecord);
        }

        log.debug("Parsing licenses ...");
        List<CSVRecord> licenseRecord;
        try (final InputStream in = getURL(licenseFilename).openStream()) {
            licenseRecord = ImportCSV.readAsCSVRecords(in);
        }

        final List<License> licensesToAdd = ConvertRecord.fillLicenses(licenseRecord, licenseTypeMap, todoMap, riskMap, licenseTodoMap, licenseRiskMap);

        ConvertRecord.addLicenses(licenseClient, licensesToAdd, log);
    }

    @NotNull
    private static Map<Integer, LicenseType> getLicenseTypeMap(LicenseService.Iface licenseClient, String licenseTypeFilename) throws IOException {
        Map<Integer, LicenseType> licenseTypeMap = null;
        try (final InputStream in = getURL(licenseTypeFilename).openStream()) {
            licenseTypeMap = TypeMappings.getIdentifierToTypeMapAndWriteMissingToDatabase(licenseClient, in, LicenseType.class, Integer.class);
        } catch (TException e) {
            log.error("Error saving LicenseTypes", e);
        }
        return CommonUtils.nullToEmptyMap(licenseTypeMap);
    }


    @NotNull
    private static Map<Integer, Obligation> getIntegerObligationMap(LicenseService.Iface licenseClient, String obligationFilename) throws IOException {
        Map<Integer, Obligation> obligationMap = null;
        try (final InputStream in = getURL(obligationFilename).openStream()) {
            obligationMap = TypeMappings.getIdentifierToTypeMapAndWriteMissingToDatabase(licenseClient, in, Obligation.class, Integer.class);
        } catch (TException e) {
            log.error("Error saving Obligations", e);
        }
        return CommonUtils.nullToEmptyMap(obligationMap);
    }


    @NotNull
    private static Map<Integer, RiskCategory> getIntegerRiskCategoryMap(LicenseService.Iface licenseClient, String riskCategoryFilename) throws IOException {
        Map<Integer, RiskCategory> riskCategoryMap = null;
        try (final InputStream in = getURL(riskCategoryFilename).openStream()) {
            riskCategoryMap = TypeMappings.getIdentifierToTypeMapAndWriteMissingToDatabase(licenseClient, in, RiskCategory.class, Integer.class);
        } catch (TException e) {
            log.error("Error saving risk categories", e);
        }
        return CommonUtils.nullToEmptyMap(riskCategoryMap);
    }


    private static Map<Integer, Todo> getTodoMap(LicenseService.Iface licenseClient, String todoFilename,
                                                 Map<Integer, Obligation> obligationMap,
                                                 Map<Integer, Set<Integer>> obligationTodoMapping) throws IOException {

        Map<Integer, Todo> todoMap = null;
        try (final InputStream in = getURL(todoFilename).openStream()) {
            todoMap = TypeMappings.getTodoMap(licenseClient, obligationMap, obligationTodoMapping, in);
        } catch (TException e) {
            log.error("Error saving Todos", e);
        }
        return CommonUtils.nullToEmptyMap(todoMap);
    }

    @NotNull
    private static Map<Integer, Risk> getIntegerRiskMap(LicenseService.Iface licenseClient, String riskFilename, Map<Integer, RiskCategory> riskCategoryMap) throws IOException {
        Map<Integer, Risk> riskMap = null;
        try (final InputStream in = getURL(riskFilename).openStream()) {
            riskMap = TypeMappings.getIntegerRiskMap(licenseClient, riskCategoryMap, in);

        } catch (TException e) {
            log.error("Error saving risks", e);
        }
        return CommonUtils.nullToEmptyMap(riskMap);
    }

    public static void main(String[] args) throws IOException, TException {
        final ThriftClients thriftClients = new ThriftClients();
        final LicenseService.Iface licenseClient = thriftClients.makeLicenseClient();
        addLicenses(licenseClient);
    }

}
