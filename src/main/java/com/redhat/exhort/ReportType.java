package com.redhat.exhort;

public enum ReportType {

    HTML("html"),
    JSON("json");

    private final String value;
    ReportType(String stringValue) {
        value = stringValue;
    }
}
