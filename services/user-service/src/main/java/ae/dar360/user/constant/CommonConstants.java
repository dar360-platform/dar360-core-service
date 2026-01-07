package ae.dar360.user.constant;

import java.util.regex.Pattern;

public class CommonConstants {



    public static final String PREFIX_IB = "DAR360";
    public static final String PREFIX_PHONE = "+971";
    private static final String EMAIL_PATTERN_1 = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String EMAIL_PATTERN_2 = "[._-]{2,}";
    private static final String MATCHES_ANY_CHARACTERS_PATTERN = "[a-zA-Z]+";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX_1 =
        Pattern.compile(EMAIL_PATTERN_1, Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX_2 =
            Pattern.compile(EMAIL_PATTERN_2, Pattern.CASE_INSENSITIVE);
    public static final Pattern MATCHES_ANY_CHARACTERS =
        Pattern.compile(MATCHES_ANY_CHARACTERS_PATTERN, Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_PASSWORD =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*., ?]).+$",
            Pattern.CASE_INSENSITIVE);

    public static final String UNDERLINE = "_";
    public static final String ATTACHMENT_FILENAME = "attachment; filename=";
    public static final String ASTERISK = "*";

    public static final String DEFAULT_USER = "Admin";
    public static final String NOBODY = "nobody";
}
