package com.lending.dar360UserService.user.enums;

public enum TokenStatus {
    INACTIVE(0),ACTIVE(1);
    private int value;

    TokenStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean contains(int value) {

        for (TokenStatus c : TokenStatus.values()) {
            if (c.getValue() == value) {
                return true;
            }
        }

        return false;
    }
}
