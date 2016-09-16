package com.vonhof.babelshark;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ConvertUtilsTest {

    @Test //Not much test here - quick fix for production issue
    public void testParseDate() throws Exception {

        Date date1 = ConvertUtils.parseDate("Thu Sep 28 20:29:30 JST 2000");
        assertNotNull(date1);

        Date date2 = ConvertUtils.parseDate("Fri Sep 16 11:36:00 UTC 2016");
        assertNotNull(date2);

        Date date3 = ConvertUtils.parseDate("Sep 16, 2016 1:48:41 PM");
        assertNotNull(date3);

        assertNull(ConvertUtils.parseDate(null));

        assertNull(ConvertUtils.parseDate(""));
    }
}