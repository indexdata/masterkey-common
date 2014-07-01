/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Formatter;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * A CronLine is an internal representation of the time specification
 * as used by cron. It consists of 5 fields: min, hr, mday, month, wday.
 * Each of these is a simple numerical String (but that can be changed later,
 * if we ever want to implement advanced features like 14,45 or 2/5).
 * 
 * @author heikki
 */
public class CronLine {
    public final static int MINUTE = 0;
    public final static int HOUR = 1;
    public final static int DAY_OF_MONTH = 2;
    public final static int MONTH = 3;
    public final static int DAY_OF_WEEK = 4;
    public final static int DAILY_PERIOD = 24 * 60;
    public final static int WEEKLY_PERIOD = 7 * 24 * 60;
    public final static int MONTHLY_PERIOD = 31 * 24 * 60;
    public final static int YEARLY_PERIOD = 12 * 31 * 24 * 60;
    private String[] fields;
    private final NumberPatternRange minute;
    private final NumberPatternRange hour; 
    private final NumberPatternRange day;
    private final NumberPatternRange month;
    private final NumberPatternRange weekday;
    private static Logger logger = Logger.getLogger("com.indexdata.masterkey.harvester");
    
    class NumberPatternRange  {
      private final int min;
      private final int max; 
      private int value = -1;
      private final boolean isWildcard;
      private final String label;
      
      public NumberPatternRange(int min, int max, String strValue, String label) 
        throws CronLineParseException { 
      	this.min = min;
      	this.max = max;
        this.isWildcard = strValue.equals("*");
        this.label = label;
      	if (!isWildcard) {
          try {
            this.value = Integer.parseInt(strValue);
          } catch (NumberFormatException nfe) {
            throw new CronLineParseException("Malformed "+label+" value: " +strValue, nfe);
          }
        }
      	validateRange();
      }
      
      private void validateRange() throws CronLineParseException {
	if (!isWildcard() && value < min || value > max)
	  throw new CronLineParseException(label + " value '" + value + "' out of range [" + min + ":" + max + "]");
      }

      /**
       * 
       */
      
      public void set(int value) throws CronLineParseException {
	this.value = value;
	validateRange();
      }

      public void reset() {
	  value = min;
      }
      
      /**
       * Increment the number, but reset if larger than max. 
       */
      public void inc() {
	value++; 
	if (value > max)
	  reset();
      }
      
      @Override
      public boolean equals(Object obj) {
	if (this == obj)
	  return true;
	if (obj instanceof NumberPatternRange) {
	  NumberPatternRange other  = (NumberPatternRange) obj;
	  if (this.isWildcard() || other.isWildcard())
	    return true;
	  if (this.value == other.value)
	    return true;
	}
	return false;
      }

      public boolean equals(Integer value) {
	if (this.isWildcard())
	    return true;
	return this.value == value;
      }

      public boolean isWildcard() {
	return isWildcard;
      }
    }
    /**
     * Constructs a CronLine from a string representation of following format:
     * "%d %d %d %d %d" applied to minute, hour, day-of-month, month, day-of-week
     * @param line For example: "55 23 * * 1" which means every Tuesday 23:55
     * @throws com.indexdata.utils.CronLineParseException
     */
    public CronLine(String line) throws CronLineParseException {
        
        if (line == null) {
            throw new CronLineParseException("Cron line cannot be null");
        }
        fields = line.split(" +");
        if ((fields == null) || (fields.length != 5)) {
            throw new CronLineParseException("Malformed cron line: '" + line);
        }

        minute = new NumberPatternRange(0, 59, fields[0], "MINUTE");
        hour   = new NumberPatternRange(0, 23, fields[1], "HOUR");
        day    = new NumberPatternRange(1, 31, fields[2], "DAY_OF_MONTH");
        month  = new NumberPatternRange(1, 12, fields[3], "MONTH");
        weekday = new NumberPatternRange(0, 6, fields[4], "DAY_OF_WEEK");    
    }

    /**
     * Matches this cron line against the parameter and returns true if the param
     * is more general (contains wildcards) or equal.
     * @param pattern pattern to match against
     * @return true/false
     */
    public boolean matches(CronLine pattern) {
        boolean m = true;
        for (int i = 0; i < fields.length; i++) {
            String pf = pattern.fields[i];
            String ff = fields[i];
            if (!pf.equals("*") && !pf.equals(ff)) {
                // TODO there is no way this can turn true again, so why not just return???
                // or there is something wrong with the logic. 
                m = false;
            }
        }
        return m;
    }

    /**
     * Return a cron line that corresponds to value date and time.
     * @return and instance of CronLine
     * @throws com.indexdata.utils.CronLineParseException
     */
    public static CronLine currentCronLine() {
        Calendar cal = new GregorianCalendar(); // defaults to now()
        return createCronLine(cal);
    }
    
    public static CronLine createCronLine(Calendar cal) {
        int min = cal.get(Calendar.MINUTE);
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        int mday = cal.get(Calendar.DAY_OF_MONTH);
        int mon = cal.get(Calendar.MONTH) + 1;  // JAN = 1
        int wday = cal.get(Calendar.DAY_OF_WEEK) - 1; //SUN = 0
        Formatter f = new Formatter();
        f.format("%d %d %d %d %d", min, hr, mday, mon, wday);
        try {
          return new CronLine(f.toString());
        } catch(CronLineParseException clpe) {
          //we never get here so it's safe to remap as a non-checked exception
          throw new IllegalStateException(clpe);
        } finally { 
          f.close();
        }
    }

    @Override
    public String toString() {
        String s = "";
        String sep = " ";
        for (int i = 0; i < fields.length; i++) {
            if (i == fields.length - 1) {
                sep = "";
            }
            s += fields[i] + sep;
        }
        return s;
    }

    /**
     * Checks the shortest period of the cron line.
     * @return period in minutes
     */
    public int shortestPeriod() {
        int period = 0;
        if (fields[0].equals("*")) {
            return 1;
        }
        if (fields[1].equals("*")) {
            return 60;
        }
        if (fields[4].equals("*")) {
            period = CronLine.DAILY_PERIOD;
        }
        if (fields[2].equals("*")) {
            return (period == CronLine.DAILY_PERIOD ? period : CronLine.WEEKLY_PERIOD);
        }
        if (fields[3].equals("*")) {
            return (period == CronLine.DAILY_PERIOD ? CronLine.MONTHLY_PERIOD : CronLine.WEEKLY_PERIOD);
        }
        return CronLine.YEARLY_PERIOD;
    }
    
    /**
     * Returns value of a given cron field.
     * @param field cron field number
     * @return
     */
    public String get(int field) {
        if (field < 0 || field > 4) return null;
        return fields[field];
    }
    
    /**
     * Converts cron entry to date;
     * @param cronLine
     * @return
     */
    public Date toDate() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (!"*".equals(this.get(MINUTE))) 
            cal.set(Calendar.MINUTE, Integer.parseInt(this.get(MINUTE)));
        if (!"*".equals(this.get(HOUR))) 
            cal.set(Calendar.HOUR, Integer.parseInt(this.get(HOUR)));
        if (!"*".equals(this.get(MONTH))) 
            cal.set(Calendar.MONTH, Integer.parseInt(this.get(MONTH)) - 1);
        if (!"*".equals(this.get(DAY_OF_MONTH))) 
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.get(DAY_OF_MONTH)));
        if (!"*".equals(this.get(DAY_OF_WEEK))) 
            cal.set(Calendar.DAY_OF_WEEK, Integer.parseInt(this.get(DAY_OF_WEEK)));
        return cal.getTime();
    }

    /**
     * Finds the next day that matches this CronLine, ignoring the time of day (hr,min,seconds,millis)
     * @param offsetDate The point in time from which the search for a matching date should start
     * @return
     */
    int yearsToScan = 10; 
    int timeOut = 365*yearsToScan;

    @Deprecated
    public Date nextMatchingDateOld(Date offsetDate) throws CronLineParseException {        
        
        // Need to limit scan in case we're looking for a non-valid date
        // Adjust time part of offset date to make it matchable with this cron line
        Calendar cal = new GregorianCalendar();
        cal.setTime(offsetDate);        
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MINUTE, Integer.parseInt(this.get(MINUTE)));
        cal.set(Calendar.HOUR, Integer.parseInt(this.get(HOUR)));
        
        // Must start with a time in the future.
        if (!cal.getTime().after(new Date())) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
       
        // Create the first cron line to test
        CronLine offsetCronLine = CronLine.createCronLine(cal);
        // Make cron line a day at a time until match is found
        while (!offsetCronLine.matches(this) && timeOut>0) { 
           cal.add(Calendar.DATE, 1);
           offsetCronLine = CronLine.createCronLine(cal);
           timeOut--;
	}
        if (timeOut==0) {
            logger.log(Level.ERROR, "Could not find matching day for \""+this.toString()+"\" within the next "+yearsToScan+" years.");
            throw new CronLineParseException("Could not find matching day for \""+this.toString()+"\" within the next "+yearsToScan+" years.");
        }
        return cal.getTime();        
    }
    
    public Date nextMatchingDate(Date date) throws CronLineParseException {
      Calendar next = new GregorianCalendar();
      next.setTimeZone(TimeZone.getTimeZone("UTC"));
      next.set(Calendar.SECOND, 0);
      next.set(Calendar.MILLISECOND, 0);
      next.setTime(date);
      int year = next.get(Calendar.YEAR);
      int count = 0; 
      while (true) {
        if (year + yearsToScan < next.get(Calendar.YEAR))
          throw new CronLineParseException("Could not find matching day for \""+this.toString()+"\" within the next "
            +yearsToScan + " years. (" + count + ")");
	count++; 

        if (!this.month.equals(next.get(Calendar.MONTH)+1)) {
          next.add(Calendar.MONTH, 1);
          next.set(Calendar.DAY_OF_MONTH, 1);
          next.set(Calendar.HOUR_OF_DAY, 0);
          next.set(Calendar.MINUTE, 0);
          continue;
        }
        if (!this.day.equals(next.get(Calendar.DAY_OF_MONTH))) {
          next.add(Calendar.DAY_OF_MONTH, 1);
          next.set(Calendar.HOUR_OF_DAY, 0);
          next.set(Calendar.MINUTE, 0);
          continue;
        }
        if (!this.weekday.equals(next.get(Calendar.DAY_OF_WEEK))) {
          next.add(Calendar.DAY_OF_MONTH, 1);
          next.set(Calendar.HOUR_OF_DAY, 0);
          next.set(Calendar.MINUTE, 0);
          continue;
        }
        if (!this.hour.equals(next.get(Calendar.HOUR_OF_DAY))) {
          next.add(Calendar.HOUR_OF_DAY, 1);
          next.set(Calendar.MINUTE, 0);
          continue;
        }
        if (!this.minute.equals(next.get(Calendar.MINUTE))) {
          next.add(Calendar.MINUTE,1);
          continue;
        }
        break;
      }      
      next.set(Calendar.SECOND, 0);
      next.set(Calendar.MILLISECOND, 0);

      return next.getTime();
    }
} // class CronLine
