/**
 * Aladdin Online Lending Application
 */
package com.lending.dar360UserService.user.util;

import com.lending.dar360UserService.user.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Matcher;

@Slf4j
public final class StringUtil {

    private static final StringUtil stringUtil = new StringUtil();

    private StringUtil() {
        // do nothing
    }

    public static StringUtil getInstance() {
        return stringUtil;
    }

  
    public static boolean containString(final String value, final List<String> values) {
        if (!CollectionUtils.isNotEmpty(values) || !StringUtils.isNoneEmpty(value)) {
            return false;
        }
        return
            values.stream().filter((String publicValue) -> StringUtils.contains(value, publicValue))
                .count() > NumberUtils.LONG_ZERO.longValue();
    }

    public static String toUpperCaseNoSpace(final String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll(StringUtils.SPACE, CommonConstants.UNDERLINE).toUpperCase();
    }

    public static String convertNumberToStringFullCharacter(Integer numberCurrent,
        final Integer max, final int stepIncrease) {
        if (null == numberCurrent || null == max) {
            return StringUtils.EMPTY;
        }
        final StringBuilder prefix = new StringBuilder();
        int maxCharacter = max.toString().length();
        numberCurrent = numberCurrent + stepIncrease;
        while (maxCharacter - numberCurrent.toString().length() > 0) {
            maxCharacter--;
            prefix.append("0");
        }
        return prefix.append(numberCurrent).toString();
    }

    public static boolean validateEmail(final String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        final Matcher matcher1 = CommonConstants.VALID_EMAIL_ADDRESS_REGEX_1.matcher(email);
        final boolean result1 = matcher1.matches();
        final Matcher matcher2 = CommonConstants.VALID_EMAIL_ADDRESS_REGEX_2.matcher(email);
        final boolean result2 = matcher2.find();
        return result1 && !result2;
    }

    public static boolean isContainLeastString(final String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        final Matcher matcher = CommonConstants.MATCHES_ANY_CHARACTERS.matcher(text);
        return matcher.find();
    }

    public static boolean validatePhone(final String phone) {
        return !StringUtils.isEmpty(phone) && phone.startsWith(CommonConstants.PREFIX_PHONE)
            && phone.length() <= 13;
    }

    public static boolean validPassword(final String password) {
        final Matcher m = CommonConstants.REGEX_PASSWORD.matcher(password);
        return m.matches();
    }

}
