package com.mysql.cj.log;

public class LoggingProfilerEventHandler implements ProfilerEventHandler {
   private Log logger;

   @Override
   public void consumeEvent(ProfilerEvent evt) {
      if (evt.getEventType() == 0) {
         this.logger.logWarn(evt);
      } else {
         this.logger.logInfo(evt);
      }
   }

   @Override
   public void destroy() {
      this.logger = null;
   }

   @Override
   public void init(Log log) {
      this.logger = log;
   }
}
