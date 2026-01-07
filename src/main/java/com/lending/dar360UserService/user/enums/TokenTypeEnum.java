package com.lending.dar360UserService.user.enums;

public enum TokenTypeEnum {
    CREATE_NEW(0),UNLOCK(1),FORGOT(2);
    private int value;

    TokenTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TokenTypeEnum parse(int value) {

        for (TokenTypeEnum c : TokenTypeEnum.values()) {
            if (c.getValue() == value) {
                return c;
            }
        }

        return null;
    }
}
