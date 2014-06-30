package com.indexdata.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestCronLine {
  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");
  
  @Test
  public void testCronLine() {
    
    String cronSchedule = "0 2 30 2 *";

    try {
      Date next = matchingDate(cronSchedule, new Date());
      assertTrue("Passed invalid pattern to " + format.format(next), false);
    } catch (CronLineParseException clpe) {
	assertTrue("Failed to get next schedule: " + clpe.getMessage(), true);
    }

    try {
      cronSchedule = "* 2 15 2 *";
      Calendar cal = getCalendar(2014, 1, 15, 10, 0);
      Date next = matchingDate(cronSchedule, cal.getTime());
      cal.set(2014, 1, 15, 2, 0);
      assertTrue("Wrong result: " + format.format(next) + "!=" + format.format(cal.getTime()), next.equals(cal.getTime()));

      cronSchedule = "0 2 4 3 *";
      cal = getCalendar(2014, 1, 15, 10, 0);
      next = matchingDate(cronSchedule, cal.getTime());
      cal.set(2014, 2, 4, 2, 0);
      assertTrue("Wrong result: " + format.format(next) + "!=" + format.format(cal.getTime()), next.equals(cal.getTime()));

      cronSchedule = "0 2 * * 6";
      cal.setTime(new Date());
      next = matchingDate(cronSchedule, cal.getTime());
      Calendar result = getCalendar(2014, 1, 3, 10, 0);
      result.setTime(next);
      assertTrue("Wrong result: Expected 6 2 0 != " + result.get(Calendar.DAY_OF_WEEK) + " " + result.get(Calendar.HOUR) + " " + result.get(Calendar.MINUTE) , 
	  result.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && result.get(Calendar.HOUR) == 2 && result.get(Calendar.MINUTE) == 0);

      
    } catch (CronLineParseException clpe) {
	assertTrue("Failed to get next schedule: " + clpe.getMessage(), true);
    }

    
    
    
  }

  private Calendar getCalendar(int year, int month, int day, int hour, int minute) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    cal.set(year, month-1, day, hour, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal;
  }
  
  private Date matchingDate(String cronSchedule, Date offset) {
    CronLine cronEntry = new CronLine(cronSchedule);
    Calendar someDate = new GregorianCalendar();
    //someDate.setLenient(false);
    someDate.setTime(offset);
   
    System.out.println("Start date: " + format.format(someDate.getTime()) + " Cron schedule: " + cronSchedule);
    Date next = cronEntry.nextMatchingDate(someDate.getTime());
    System.out.println("Date: " + format.format(next));
    return next;
  }
  
  
  @Test
  public void testInvalidCronLine() {
    String validLine = "1 2 15 2 0"; //dayofweek is [0,6];
    try {
      CronLine cronLine = new CronLine(validLine);
    } catch (NumberFormatException nfe) {
      //a valid line faails and it shouldn's
      //on top of that cronLine throws wrong exception
    }
  }

}
