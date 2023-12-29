package l2e.commons.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PriorityThreadFactory implements ThreadFactory {
   private static final Logger _log = Logger.getLogger(PriorityThreadFactory.class.getName());
   private final int _prio;
   private final String _name;
   private final AtomicInteger _threadNumber = new AtomicInteger(1);
   private final ThreadGroup _group;

   public PriorityThreadFactory(String name, int prio) {
      this._prio = prio;
      this._name = name;
      this._group = new ThreadGroup(this._name);
   }

   @Override
   public Thread newThread(Runnable r) {
      Thread t = new Thread(this._group, r) {
         @Override
         public void run() {
            try {
               super.run();
            } catch (Exception var2) {
               PriorityThreadFactory._log.log(Level.WARNING, "Exception: " + var2, (Throwable)var2);
            }
         }
      };
      t.setName(this._name + "-" + this._threadNumber.getAndIncrement());
      t.setPriority(this._prio);
      return t;
   }

   public ThreadGroup getGroup() {
      return this._group;
   }
}
