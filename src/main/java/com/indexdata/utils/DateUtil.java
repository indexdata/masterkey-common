/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author jakub
 */
public class DateUtil {
  public static final String RFC_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
  public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
  public enum DateTimeFormat {
    RFC, ISO, RFC_GMT
  }

  private static final ThreadLocal<DateFormat> rfcDateFormat =
    new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat df = new SimpleDateFormat(RFC_DATE_FORMAT);
            return df;
        }
    };
  
    private static final ThreadLocal<DateFormat> rfcDateFormatGMT =
    new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat df = new SimpleDateFormat(RFC_DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return df;
        }
    };
  
  private static final ThreadLocal<DateFormat> isoDateFormat =
    new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat df = new SimpleDateFormat(ISO_DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df;
        }
    };

  public static Date parse(String dateString) throws ParseException {
    return rfcDateFormat.get().parse(dateString);
  }
  
  public static Date parse(String dateString, DateTimeFormat dtf) throws ParseException {
    switch (dtf) {
      case RFC: return rfcDateFormat.get().parse(dateString);
      case RFC_GMT: return rfcDateFormatGMT.get().parse(dateString);
      case ISO: return ISOLikeDateParser.parse(dateString);
      default: throw new IllegalArgumentException("Unknown date format " + dtf);
    }
  }

  public static String serialize(Date date) {
    return rfcDateFormat.get().format(date);
  }
  
  public static String serialize(Date date, DateTimeFormat dtf) throws ParseException {
    switch (dtf) {
      case RFC: return rfcDateFormat.get().format(date);
      case RFC_GMT: return rfcDateFormatGMT.get().format(date);
      case ISO: return isoDateFormat.get().format(date);
      default: throw new IllegalArgumentException("Unknown date format " + dtf);
    }
  }
}
