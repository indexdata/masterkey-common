/*
 * Copyright (c) 1995-2014, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.text.ParseException;
import java.util.Date;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jakub
 */
public class ISOLikeDateParserTest {
  
  public ISOLikeDateParserTest() {
  }

  @Test
  public void testParse() throws ParseException {
    //year only
    final String date1Str = "2014";
    final Date date1Exp =  new Date(1388534400000L);
    final Date date1Out = ISOLikeDateParser.parse(date1Str);
    assertEquals("Check parsing for "+date1Str, date1Exp, date1Out);
    //year and month
    final String date12Str = "2014-07";
    final Date date12Exp =  new Date(1404172800000L);
    final Date date12Out = ISOLikeDateParser.parse(date12Str);
    assertEquals("Check parsing for "+date12Str, date12Exp, date12Out);
    //full date wihouth time
    final String date13Str = "2014-07-08";
    final Date date13Exp =  new Date(1404777600000L);
    final Date date13Out = ISOLikeDateParser.parse(date13Str);
    assertEquals("Check parsing for "+date13Str, date13Exp, date13Out);
    //date with hour and mins
    final String date14Str = "2014-07-08T04:17";
    final Date date14Exp =  new Date(1404793020000L);
    final Date date14Out = ISOLikeDateParser.parse(date14Str);
    assertEquals("Check parsing for "+date14Str, date14Exp, date14Out);
    //date with hour mins and secs
    final String date15Str = "2014-07-08T04:17:01";
    final Date date15Exp =  new Date(1404793021000L);
    final Date date15Out = ISOLikeDateParser.parse(date15Str);
    assertEquals("Check parsing for "+date15Str, date15Exp, date15Out);
    //with numertic tz designator
    final String date21Str = "2014-07-08T04:17:01+00:00";
    final Date date21Exp =  new Date(1404793021000L);
    final Date date21Out = ISOLikeDateParser.parse(date21Str);
    assertEquals("Check parsing for "+date21Str, date21Exp, date21Out);
    //with strign tz designator
    final String date22Str = "2014-07-08T04:17:01Z";
    final Date date2Exp =  new Date(1404793021000L);
    final Date date2Out = ISOLikeDateParser.parse(date22Str);
    assertEquals("Check parsing for "+date22Str, date2Exp, date2Out);
    //check parsing with space as a date/time delimiter, withour tz designator
    final String date3Str = "2014-07-08 04:17:01";
    final Date date3Exp =  new Date(1404793021000L);
    final Date date3Out = ISOLikeDateParser.parse(date3Str);
    assertEquals("Check parsing for "+date3Str, date3Exp, date3Out);
    //check with higer precision, e.g used by log4j
    final String date4Str = "2014-07-08 04:17:01,549";
    final Date date4Exp =  new Date(1404793021549L);
    final Date date4Out = ISOLikeDateParser.parse(date4Str);
    assertEquals("Check parsing for "+date4Str, date4Exp, date4Out);
  }
  
  @Test
  public void testMiliPrecisionRange() throws ParseException {
    final String lowStr = "2014-07-08 04:17:01,100";
    final Date low = ISOLikeDateParser.parse(lowStr);
    final String hiStr = "2014-07-08 04:17:01,549";
    final Date hi = ISOLikeDateParser.parse(hiStr);
    assertTrue("Checking inequality: "+hiStr + " > " +lowStr, hi.after(low));
    assertTrue("Checking inequality: "+lowStr + " < " +hiStr, low.before(hi));
    
  }
  
}
