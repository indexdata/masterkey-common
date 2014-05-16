/*
 * Copyright (c) 1995-2014, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;

/**
 * ThreadPoolExecutor with ability to set and clean-up MDC context when threads are
 * returned to the pool.
 * @author jakub
 */
public class MDCThreadPoolExecutor extends ThreadPoolExecutor {
  
  //derrived from Executors#newCachedThreadPool
  public static ThreadPoolExecutor newCachedThreadPool() {
    return new MDCThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
  }

  public MDCThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
    long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  public MDCThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
    long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
    ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
      threadFactory);
  }

  public MDCThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
    long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
    RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public MDCThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
    long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
    ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
      threadFactory, handler);
  }
 
  
  /**
   * All executions will have MDC injected. {@code ThreadPoolExecutor}'s
   * submission methods ({@code submit()} etc.) all delegate to this.
   */
  @Override
  public void execute(Runnable command) {
    super.execute(wrap(command, MDC.getCopyOfContextMap()));
  }

  public static Runnable wrap(final Runnable runnable,
    final Map<String, String> context) {
    return new Runnable() {
      @Override
      public void run() {
        Map previous = MDC.getCopyOfContextMap();
        if (context == null) {
          MDC.clear();
        } else {
          MDC.setContextMap(context);
        }
        try {
          runnable.run();
        } finally {
          if (previous == null) {
            MDC.clear();
          } else {
            MDC.setContextMap(previous);
          }
        }
      }  
    };
  }
}
