package com.indexdata.utils;

import org.apache.log4j.Logger;

/**
 * Logs execution times with a message and an optional label
 * 
 * A logger named "service-proxy.performance" must be defined and
 * debug enabled in LOG4J.
 * 
 * @author Niels Erik Nielsen
 *
 */
public class PerformanceLogger {
  private static Logger performanceLog = Logger.getLogger("service-proxy.performance");
  private static String filler = " ";
  
  /** 
   * Get start time in milliseconds and write log line with a short label and a 
   * message
   * 
   * @param label
   * @param message
   * @return
   */
  public static long start(String label, String message) {    
    if (performanceLog.isDebugEnabled()) {
      log(String.format("%10s %-7.7s %-80.80s", filler, label, message));      
    }
    return System.currentTimeMillis();      
  }
  
  /**
   * Get start time in milliseconds and write a log line
   * 
   * @param message
   * @return
   */
  public static long start(String message) {    
    if (performanceLog.isDebugEnabled()) {      
      log(String.format("%10s %-80.80s", filler, message));
    }
    return System.currentTimeMillis();      
  }
    
  
  /**
   * Get start time in milliseconds
   * @return
   */
  public static long start () {
    return System.currentTimeMillis();
  }
  
  /**
   * Calculate execution time in milliseconds and log it with a message
   * 
   * @param message log statement
   * @param startTime start time to measure from (in milliseconds)
   */
  public static void finish (String message, long startTime) {
    long time = System.currentTimeMillis() - startTime;    
    log(String.format("%10d %7.7s %-80.80s", time, filler, message));
  }
  
  /**
   * Calculate execution time in millisecondes and log it with a short 
   * label and a message
   * 
   * @param label
   * @param message
   * @param startTime
   */
  public static void finish (String label, String message, long startTime) {
    long time = System.currentTimeMillis() - startTime;
    log(String.format("%10d %-7.7s %-80.80s", time, label, message));
  }
  
  /**
   * Write a log statement (no timing)
   * 
   * @param message
   */
  public static void log (String message) {
    performanceLog.debug(message);
  }
  
  /**
   * 
   * @return true if performance logger is debug enabled in LOG4J
   */
  public static boolean isEnabled () {
    return performanceLog.isDebugEnabled();
  }
  
}
