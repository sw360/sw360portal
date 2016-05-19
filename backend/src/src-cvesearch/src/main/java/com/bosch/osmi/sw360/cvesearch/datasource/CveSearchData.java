package com.bosch.osmi.sw360.cvesearch.datasource;

import java.util.List;
import java.util.Map;

public class CveSearchData {

    private String id;
    private String Modified;
    private List<String> references;
    private String Published;
    private List<String> acess;
    // private String cvss-time;
    private List<String> vulnerable_configuration;
    private float cvss;
    private List<String> vulnerable_configuration_2_2;
    private Map<String,String> impact;
    private String summary;
}
