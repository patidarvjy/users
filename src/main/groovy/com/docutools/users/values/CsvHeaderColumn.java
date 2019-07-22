package com.docutools.users.values;

public enum CsvHeaderColumn {
    FIRST_NAME("registerForm_firstName"),
    LAST_NAME("registerForm_lastName"),
    COMPANY_NAME("projects_company"),
    EMAIL("registerForm_email"),
    PHONE("userSettings_phone"),
    FAX("personas_fax"),
    JOB_TITLE("personas_importFieldAttributeJobTitle"),
    DEPARTMENT("personas_department"),
    STREET("personas_importFieldAttributeStreet"),
    ZIP("personas_importFieldAttributeZip"),
    CITY("projectSettings_city"),
    COUNTRY("projectSettings_country");

    private String value;

    CsvHeaderColumn(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}