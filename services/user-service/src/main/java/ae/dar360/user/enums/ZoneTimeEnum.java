package ae.dar360.user.enums;

public enum ZoneTimeEnum {
    UTC("+00:00");

    private String value;

    ZoneTimeEnum(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Check given filter type is equal current filter type enum
     *
     * @param filterType The filter type
     * @return true if filter type is equal current filter type enum, false otherwise
     */
    public boolean isEqual(String filterType) {
        return value.equals(filterType);
    }
}