package com.mysql.cj.log;

public interface ProfilerEventHandler {
   void init(Log var1);

   void destroy();

   void consumeEvent(ProfilerEvent var1);
}
