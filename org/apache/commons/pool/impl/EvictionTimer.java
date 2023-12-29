package org.apache.commons.pool.impl;

import java.util.Timer;
import java.util.TimerTask;

class EvictionTimer {
   private static Timer _timer;
   private static int _usageCount;

   private EvictionTimer() {
   }

   static synchronized void schedule(TimerTask task, long delay, long period) {
      if (null == _timer) {
         _timer = new Timer(true);
      }

      ++_usageCount;
      _timer.schedule(task, delay, period);
   }

   static synchronized void cancel(TimerTask task) {
      task.cancel();
      --_usageCount;
      if (_usageCount == 0) {
         _timer.cancel();
         _timer = null;
      }
   }
}
