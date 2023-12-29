package l2e.gameserver.taskmanager.actionrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.taskmanager.actionrunner.tasks.AutomaticTask;

public class ActionRunner {
   private static final Logger _log = Logger.getLogger(ActionRunner.class.getName());
   private static ActionRunner _instance = new ActionRunner();
   private final Map<String, List<ActionWrapper>> _futures = new HashMap<>();
   private final Lock _lock = new ReentrantLock();

   public static ActionRunner getInstance() {
      return _instance;
   }

   private ActionRunner() {
   }

   public void register(AutomaticTask task) {
      this.register(task.reCalcTime(true), task);
   }

   public void register(long time, ActionWrapper wrapper) {
      if (time == 0L) {
         _log.info("Try register " + wrapper.getName() + " not defined time.");
      } else if (time <= System.currentTimeMillis()) {
         ThreadPoolManager.getInstance().execute(wrapper);
      } else {
         this.addScheduled(wrapper.getName(), wrapper, time - System.currentTimeMillis());
      }
   }

   protected void addScheduled(String name, ActionWrapper r, long diff) {
      this._lock.lock();

      try {
         String lower = name.toLowerCase();
         List<ActionWrapper> wrapperList = this._futures.get(lower);
         if (wrapperList == null) {
            this._futures.put(lower, wrapperList = new ArrayList<>());
         }

         r.schedule(diff);
         wrapperList.add(r);
      } finally {
         this._lock.unlock();
      }
   }

   protected void remove(String name, ActionWrapper f) {
      this._lock.lock();

      try {
         String lower = name.toLowerCase();
         List<ActionWrapper> wrapperList = this._futures.get(lower);
         if (wrapperList != null) {
            wrapperList.remove(f);
            if (wrapperList.isEmpty()) {
               this._futures.remove(lower);
            }

            return;
         }
      } finally {
         this._lock.unlock();
      }
   }

   public void clear(String name) {
      this._lock.lock();

      try {
         String lower = name.toLowerCase();
         List<ActionWrapper> wrapperList = this._futures.remove(lower);
         if (wrapperList != null) {
            for(ActionWrapper f : wrapperList) {
               f.cancel();
            }

            wrapperList.clear();
            return;
         }
      } finally {
         this._lock.unlock();
      }
   }

   public void info() {
      this._lock.lock();

      try {
         for(Entry<String, List<ActionWrapper>> entry : this._futures.entrySet()) {
            _log.info("Name: " + (String)entry.getKey() + "; size: " + entry.getValue().size());
         }
      } finally {
         this._lock.unlock();
      }
   }
}
