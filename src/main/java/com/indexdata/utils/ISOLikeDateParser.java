/*
 * Copyright (c) 1995-2013, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jakub
 */
public class ISOLikeDateParser {
  private final static Pattern datePattern =
    Pattern.compile(
    "^(\\d{4})(?:[-/.]?(\\d{2}))?(?:[-/.]?(\\d{2}))?(?:[T @](\\d{2}))?(?:[:.]?(\\d{2}))?(?:[:.]?(\\d{2}))?(?:[,]?(\\d{3}))?(Z|[+-]\\d{2}(?::?\\d{2})?)?$");
  //1 - year, 2 - month, 3 - day, 4 - hour, 5 - min, 6 - secs, 7 - milisecs, 8 - timezone
  
  public static Date parse(String dateStr) throws ParseException {
    return parse(dateStr, null);
  }
  
  public static Date parse(String dateStr, TimeZone timezone) throws ParseException {
    Matcher m = datePattern.matcher(dateStr);
    if (m.matches()) {
      //start with time zone
      TimeZone tz;
      if (timezone != null) {
        tz = timezone;
      } else {
        String tzs = m.group(8);
        if (tzs == null || tzs.equals("Z")) {
          tz = TimeZone.getTimeZone("GMT");
        } else {
          tz = TimeZone.getTimeZone("GMT" + tzs);
        }
      }
      Calendar c = Calendar.getInstance(tz);
      //year
      String ys = m.group(1);
      if (ys == null) {
        throw new ParseException(
          "ISO date string must at least contain a year component",
          m.start(1));
      }
      int yi = Integer.parseInt(ys);
      if (yi < 0 || yi > 9999) {
        throw new ParseException("ISO year must be in the range from 0000 to 9999, given "
          + ys,
          m.start(1));
      }
      c.set(Calendar.YEAR, yi);
      //month
      int moi = 1;
      String mos = m.group(2);
      if (mos != null) {
        moi = Integer.parseInt(mos);
        if (moi < 1 || moi > 12) {
          throw new ParseException("ISO month must be in the range from 01 to 12, given "
            + mos,
            m.start(2));
        }
      }
      c.set(Calendar.MONTH, moi-1); //zero indexed!
      //day-of-month
      int di = 1;
      String ds = m.group(3);
      if (ds != null) {
        di = Integer.parseInt(ds);
        if (di < 1 || di > 31) {
          throw new ParseException("ISO day-of-month must be in the range from 01 to 31, given "
            + ds,
            m.start(3));
        }
      }
      c.set(Calendar.DAY_OF_MONTH, di);
      //hour
      int hi = 0;
      String hs = m.group(4);
      if (hs != null) {
        hi = Integer.parseInt(hs);
        if (hi < 0 || hi > 23) {
          throw new ParseException("ISO hour must be in the range from 00 to 23, given "
            + hs,
            m.start(4));
        }
      }
      c.set(Calendar.HOUR_OF_DAY, hi);
      //mins
      int mi = 0;
      String ms = m.group(5);
      if (ms != null) {
        mi = Integer.parseInt(ms);
        if (mi < 0 || mi > 59) {
          throw new ParseException("ISO minutes must be in the range from 00 to 59, given "
            + ms,
            m.start(5));
        }
      }
      c.set(Calendar.MINUTE, mi);
      //secs
      int si = 0;
      String ss = m.group(6);
      if (ss != null) {
        si = Integer.parseInt(ss);
        if (si < 0 || si > 59) {
          throw new ParseException("ISO seconds must be in  the range from 00 to 59, given "
            + ss,
            m.start(6));
        }
      }
      c.set(Calendar.SECOND, si);
      //msecs
      int msi = 0;
      String mss = m.group(7);
      if (mss != null) {
        msi = Integer.parseInt(mss);
        if (msi < 0 || msi > 999) {
          throw new ParseException("ISO milliseconds must be in  the range from 000 to 999, given "
            + mss,
            m.start(6));
        }
      }
      c.set(Calendar.MILLISECOND, msi);
      //timezone handled as first item
      return c.getTime();
    }
    throw new ParseException("Unparsable ISO date format", 0);
  }
  
}
