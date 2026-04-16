package com.mobiledetails.app;

public class DeviceDetail {
    private final String label;
    private final String value;
    private final String category;

    public DeviceDetail(String label, String value, String category) {
        this.label = label;
        this.value = value;
        this.category = category;
    }

    public String getLabel() { return label; }
    public String getValue()  { return value; }
    public String getCategory() { return category; }
}
