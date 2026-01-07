package ae.dar360.user.util;


import ae.dar360.user.enums.DateTimeFormat;
import ae.dar360.user.enums.ZoneTimeEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateTimeUtils {

    public static final String DD_MM_YYYY_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";

    public static String formatOffsetDateTime(final OffsetDateTime offsetDateTime, final String pattern) {
        if (offsetDateTime == null) {
            return StringUtils.EMPTY;
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(offsetDateTime);
    }
    public static OffsetDateTime offsetDateTimeFromString(String date) {
        try {
            return OffsetDateTime.parse(date) ;
        } catch (Exception ex ) {
            return null;
        }
    }
    public static OffsetDateTime convertToOffsetDateTime(String date, String type) {
        LocalDateTime localDateTime;
        try {
            LocalDate localDate = LocalDate.parse(date,
                    DateTimeFormatter.ofPattern(DateTimeFormat.YEAR_MONTH_DAY.getValue()));
            if("toDate".equals(type)) {
                localDateTime= localDate.atTime(LocalTime.MAX);
            }else{
                localDateTime = localDate.atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            localDateTime = LocalDateTime.now();
        }
        return localDateTime.atOffset(ZoneOffset.of(ZoneTimeEnum.UTC.getValue()));
    }



}
