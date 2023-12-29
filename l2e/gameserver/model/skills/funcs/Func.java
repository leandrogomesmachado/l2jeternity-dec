package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public abstract class Func {
   public final Stats stat;
   public final int order;
   public final double value;
   public final Object funcOwner;
   public Condition cond;

   public Func(Stats stat, int order, Object funcOwner) {
      this(stat, order, funcOwner, 0.0);
   }

   public Func(Stats stat, int order, Object owner, double value) {
      this.stat = stat;
      this.order = order;
      this.funcOwner = owner;
      this.value = value;
   }

   public void setCondition(Condition cond) {
      this.cond = cond;
   }

   public abstract void calc(Env var1);
}
