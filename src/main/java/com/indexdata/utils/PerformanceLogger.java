package com.indexdata.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Logs execution times with a message and an optional label
 * 
 * A logger named "service-proxy.performance" must be defined and debug enabled
 * in LOG4J.
 * 
 * @author Niels Erik Nielsen
 * 
 */
public class PerformanceLogger {
	private static Logger performanceLog = Logger.getLogger("service-proxy.performance");
	private static String filler = "-";
	private static Level defaultLevel = Level.DEBUG;


	
  /**
   * Get start time in milliseconds
   * 
   * @return
   */
  public static long start() {
    return System.currentTimeMillis();
  }
		
  /**
   * Get start time in milliseconds and write a log line
   * 
   * @param message
   * @return
   */
  public static long start(String message) {
    return start("-",message);
  }
	
	/**
	 * Get start time in milliseconds and write log line with a short label and
	 * a message
	 * 
	 * @param label
	 * @param message
	 * @return
	 */
	public static long start(String label, String message) {
		return start(defaultLevel, label, message);
	}
	
  public static long start(Level level, String message) {
    return start(level,filler,message); 
  }

	public static long start(Level level, String label, String message) {
		if (performanceLog.isEnabledFor(level)) {
			log(String.format("%6s %-16.16s %-80.80s", filler, label, message));
		}
		return System.currentTimeMillis();
	}

	/**
	 * Calculate execution time in milliseconds and log it with a message
	 * 
	 * @param message
	 *            log statement
	 * @param startTime
	 *            start time to measure from (in milliseconds)
	 */
	public static void finish(String message, long startTime) {
	  finish(defaultLevel, filler, message, startTime);
	}
	
	public static void finish(Level level, String message, long startTime) {
	  finish(level, filler, message, startTime);
	}
	
	public static void finish(String label, String message, long startTime) {
	  finish (defaultLevel, label, message, startTime);
	}

	/**
	 * Calculate execution time in millisecondes and log it with a short label
	 * and a message
	 * 
	 * @param label
	 * @param message
	 * @param startTime
	 */
	public static void finish(Level level, String label, String message, long startTime) {
		long time = System.currentTimeMillis() - startTime;
		log(level,String.format("%6d %-16.16s %-80.80s", time, label, message));
	}

	public static void log (String message) {
	  log (defaultLevel, message);
	}
	
	/**
	 * Write a log statement
	 * 
	 * @param message
	 */
	public static void log(Level level, String message) {	  
		performanceLog.log(level, message);
	}

}
