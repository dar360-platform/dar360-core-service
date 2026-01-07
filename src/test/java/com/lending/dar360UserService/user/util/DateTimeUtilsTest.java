package com.lending.dar360UserService.user.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeUtilsTest {

    @InjectMocks
    private DateTimeUtils dateTimeUtils;

    @Test
    public void formatOffsetDateTime() {
        assertEquals(StringUtils.EMPTY, DateTimeUtils.formatOffsetDateTime(null, "YYYYMMDD"));
        String output = DateTimeUtils.formatOffsetDateTime(OffsetDateTime.of(2020,1,1, 1,1,1,1, ZoneOffset.UTC), "YYYYMMDD");
        assertEquals("20200101", output);
    }

    @Test
    public void offsetDateTimeFromString() {
        Assert.assertNotNull(DateTimeUtils.offsetDateTimeFromString("2011-12-03T10:15:30+01:00"));
        Assert.assertNull(DateTimeUtils.offsetDateTimeFromString("2011-12-03"));
    }

    @Test
    public void convertToOffsetDateTime() {
        Assert.assertNotNull(DateTimeUtils.convertToOffsetDateTime("20111203", ""));
        Assert.assertNotNull(DateTimeUtils.convertToOffsetDateTime("20111203abc", ""));
        Assert.assertNotNull(DateTimeUtils.convertToOffsetDateTime("20111203", "toDate"));
    }
}