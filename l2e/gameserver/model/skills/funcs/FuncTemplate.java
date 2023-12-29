package l2e.gameserver.model.skills.funcs;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public final class FuncTemplate {
   protected static final Logger _log = Logger.getLogger(FuncTemplate.class.getName());
   public Condition attachCond;
   public Condition applayCond;
   public final Class<?> func;
   public final Constructor<?> constructor;
   public final Stats stat;
   public final int order;
   public final Lambda lambda;

   public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda) {
      this.attachCond = pAttachCond;
      this.applayCond = pApplayCond;
      this.stat = pStat;
      this.order = pOrder;
      this.lambda = pLambda;

      try {
         this.func = Class.forName("l2e.gameserver.model.skills.funcs.Func" + pFunc);
      } catch (ClassNotFoundException var9) {
         throw new RuntimeException(var9);
      }

      try {
         this.constructor = this.func.getConstructor(Stats.class, Integer.TYPE, Object.class, Lambda.class);
      } catch (NoSuchMethodException var8) {
         throw new RuntimeException(var8);
      }
   }

   public Func getFunc(Env env, Object owner) {
      if (this.attachCond != null && !this.attachCond.test(env)) {
         return null;
      } else {
         try {
            Func f = (Func)this.constructor.newInstance(this.stat, this.order, owner, this.lambda);
            if (this.applayCond != null) {
               f.setCondition(this.applayCond);
            }

            return f;
         } catch (Exception var4) {
            _log.log(Level.WARNING, "", (Throwable)var4);
            return null;
         }
      }
   }
}
