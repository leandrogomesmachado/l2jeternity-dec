package org.apache.commons.dbcp;

import java.io.PrintWriter;

public class AbandonedConfig {
   private boolean removeAbandoned = false;
   private int removeAbandonedTimeout = 300;
   private boolean logAbandoned = false;
   private PrintWriter logWriter = new PrintWriter(System.out);

   public boolean getRemoveAbandoned() {
      return this.removeAbandoned;
   }

   public void setRemoveAbandoned(boolean removeAbandoned) {
      this.removeAbandoned = removeAbandoned;
   }

   public int getRemoveAbandonedTimeout() {
      return this.removeAbandonedTimeout;
   }

   public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
      this.removeAbandonedTimeout = removeAbandonedTimeout;
   }

   public boolean getLogAbandoned() {
      return this.logAbandoned;
   }

   public void setLogAbandoned(boolean logAbandoned) {
      this.logAbandoned = logAbandoned;
   }

   public PrintWriter getLogWriter() {
      return this.logWriter;
   }

   public void setLogWriter(PrintWriter logWriter) {
      this.logWriter = logWriter;
   }
}
