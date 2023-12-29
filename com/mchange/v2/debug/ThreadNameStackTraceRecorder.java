package com.mchange.v2.debug;

import com.mchange.lang.ThrowableUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ThreadNameStackTraceRecorder {
   static final String NL = System.getProperty("line.separator", "\r\n");
   Set set = new HashSet();
   String dumpHeader;
   String stackTraceHeader;

   public ThreadNameStackTraceRecorder(String var1) {
      this(var1, "Debug Stack Trace.");
   }

   public ThreadNameStackTraceRecorder(String var1, String var2) {
      this.dumpHeader = var1;
      this.stackTraceHeader = var2;
   }

   public synchronized Object record() {
      ThreadNameStackTraceRecorder.Record var1 = new ThreadNameStackTraceRecorder.Record(this.stackTraceHeader);
      this.set.add(var1);
      return var1;
   }

   public synchronized void remove(Object var1) {
      this.set.remove(var1);
   }

   public synchronized int size() {
      return this.set.size();
   }

   public synchronized String getDump() {
      return this.getDump(null);
   }

   public synchronized String getDump(String var1) {
      SimpleDateFormat var2 = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss.SSSS");
      StringBuffer var3 = new StringBuffer(2047);
      var3.append(NL);
      var3.append("----------------------------------------------------");
      var3.append(NL);
      var3.append(this.dumpHeader);
      var3.append(NL);
      if (var1 != null) {
         var3.append(var1);
         var3.append(NL);
      }

      boolean var4 = true;
      Iterator var5 = this.set.iterator();

      while(var5.hasNext()) {
         if (var4) {
            var4 = false;
         } else {
            var3.append("---");
            var3.append(NL);
         }

         ThreadNameStackTraceRecorder.Record var6 = (ThreadNameStackTraceRecorder.Record)var5.next();
         var3.append(var2.format(new Date(var6.time)));
         var3.append(" --> Thread Name: ");
         var3.append(var6.threadName);
         var3.append(NL);
         var3.append("Stack Trace: ");
         var3.append(ThrowableUtils.extractStackTrace(var6.stackTrace));
      }

      var3.append("----------------------------------------------------");
      var3.append(NL);
      return var3.toString();
   }

   private static final class Record implements Comparable {
      long time = System.currentTimeMillis();
      String threadName = Thread.currentThread().getName();
      Throwable stackTrace;

      Record(String var1) {
         this.stackTrace = new Exception(var1);
      }

      @Override
      public int compareTo(Object var1) {
         ThreadNameStackTraceRecorder.Record var2 = (ThreadNameStackTraceRecorder.Record)var1;
         if (this.time > var2.time) {
            return 1;
         } else if (this.time < var2.time) {
            return -1;
         } else {
            int var3 = System.identityHashCode(this);
            int var4 = System.identityHashCode(var2);
            if (var3 > var4) {
               return 1;
            } else {
               return var3 < var4 ? -1 : 0;
            }
         }
      }
   }
}
