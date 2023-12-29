package org.apache.commons.dbcp;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class AbandonedObjectPool extends GenericObjectPool {
   private final AbandonedConfig config;
   private final List trace = new ArrayList();

   public AbandonedObjectPool(PoolableObjectFactory factory, AbandonedConfig config) {
      super(factory);
      this.config = config;
   }

   @Override
   public Object borrowObject() throws Exception {
      if (this.config != null && this.config.getRemoveAbandoned() && this.getNumIdle() < 2 && this.getNumActive() > this.getMaxActive() - 3) {
         this.removeAbandoned();
      }

      Object obj = super.borrowObject();
      if (obj instanceof AbandonedTrace) {
         ((AbandonedTrace)obj).setStackTrace();
      }

      if (obj != null && this.config != null && this.config.getRemoveAbandoned()) {
         synchronized(this.trace) {
            this.trace.add(obj);
         }
      }

      return obj;
   }

   @Override
   public void returnObject(Object obj) throws Exception {
      if (this.config != null && this.config.getRemoveAbandoned()) {
         synchronized(this.trace) {
            boolean foundObject = this.trace.remove(obj);
            if (!foundObject) {
               return;
            }
         }
      }

      super.returnObject(obj);
   }

   @Override
   public void invalidateObject(Object obj) throws Exception {
      if (this.config != null && this.config.getRemoveAbandoned()) {
         synchronized(this.trace) {
            boolean foundObject = this.trace.remove(obj);
            if (!foundObject) {
               return;
            }
         }
      }

      super.invalidateObject(obj);
   }

   private void removeAbandoned() {
      long now = System.currentTimeMillis();
      long timeout = now - (long)(this.config.getRemoveAbandonedTimeout() * 1000);
      ArrayList remove = new ArrayList();
      synchronized(this.trace) {
         for(AbandonedTrace pc : this.trace) {
            if (pc.getLastUsed() <= timeout && pc.getLastUsed() > 0L) {
               remove.add(pc);
            }
         }
      }

      for(AbandonedTrace pc : remove) {
         if (this.config.getLogAbandoned()) {
            pc.printStackTrace();
         }

         try {
            this.invalidateObject(pc);
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }
   }
}
