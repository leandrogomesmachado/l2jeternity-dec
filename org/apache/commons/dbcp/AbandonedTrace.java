package org.apache.commons.dbcp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbandonedTrace {
   private final AbandonedConfig config;
   private volatile Exception createdBy;
   private final List traceList = new ArrayList();
   private volatile long lastUsed = 0L;

   public AbandonedTrace() {
      this.config = null;
      this.init(null);
   }

   public AbandonedTrace(AbandonedConfig config) {
      this.config = config;
      this.init(null);
   }

   public AbandonedTrace(AbandonedTrace parent) {
      this.config = parent.getConfig();
      this.init(parent);
   }

   private void init(AbandonedTrace parent) {
      if (parent != null) {
         parent.addTrace(this);
      }

      if (this.config != null) {
         if (this.config.getLogAbandoned()) {
            this.createdBy = new AbandonedTrace.AbandonedObjectException();
         }
      }
   }

   protected AbandonedConfig getConfig() {
      return this.config;
   }

   protected long getLastUsed() {
      return this.lastUsed;
   }

   protected void setLastUsed() {
      this.lastUsed = System.currentTimeMillis();
   }

   protected void setLastUsed(long time) {
      this.lastUsed = time;
   }

   protected void setStackTrace() {
      if (this.config != null) {
         if (this.config.getLogAbandoned()) {
            this.createdBy = new AbandonedTrace.AbandonedObjectException();
         }
      }
   }

   protected void addTrace(AbandonedTrace trace) {
      synchronized(this.traceList) {
         this.traceList.add(trace);
      }

      this.setLastUsed();
   }

   protected void clearTrace() {
      synchronized(this.traceList) {
         this.traceList.clear();
      }
   }

   protected List getTrace() {
      synchronized(this.traceList) {
         return new ArrayList(this.traceList);
      }
   }

   public void printStackTrace() {
      if (this.createdBy != null && this.config != null) {
         this.createdBy.printStackTrace(this.config.getLogWriter());
      }

      synchronized(this.traceList) {
         for(AbandonedTrace at : this.traceList) {
            at.printStackTrace();
         }
      }
   }

   protected void removeTrace(AbandonedTrace trace) {
      synchronized(this.traceList) {
         this.traceList.remove(trace);
      }
   }

   static class AbandonedObjectException extends Exception {
      private static final long serialVersionUID = 7398692158058772916L;
      private static final SimpleDateFormat format = new SimpleDateFormat(
         "'DBCP object created' yyyy-MM-dd HH:mm:ss 'by the following code was never closed:'"
      );
      private final long _createdTime = System.currentTimeMillis();

      public AbandonedObjectException() {
      }

      @Override
      public String getMessage() {
         synchronized(format) {
            return format.format(new Date(this._createdTime));
         }
      }
   }
}
