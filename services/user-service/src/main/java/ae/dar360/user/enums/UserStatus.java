package ae.dar360.user.enums;

public enum UserStatus {
    INACTIVE(0),ACTIVE(1),DELETE(2);
    private int value;

    UserStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean contains(int value) {

        for (UserStatus c : UserStatus.values()) {
            if (c.getValue() == value) {
                return true;
            }
        }

        return false;
    }
}
