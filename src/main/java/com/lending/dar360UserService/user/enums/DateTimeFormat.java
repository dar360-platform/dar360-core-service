package com.lending.dar360UserService.user.enums;

public enum DateTimeFormat {

    YEAR_MONTH_DAY("yyyy-MM-dd"), DAY_MONTH_YEAR("dd/MM/yyyy"), MONTH_DAY_YEAR("MM/dd/yyyy"),
    DAY_MONTH_YEAR_TIME_LOCALE_TIME("dd/MM/yyyy hh:mm a");
    private String value;

    DateTimeFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}