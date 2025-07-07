package com.lksnext.ParkingELadron.domain;

public class LanguageItem {
    private final String code;
    private final String name;
    private final int flagResId;

    public LanguageItem(String code, String name, int flagResId) {
        this.code = code;
        this.name = name;
        this.flagResId = flagResId;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getFlagResId() { return flagResId; }
}