package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public abstract class ConditionInventory extends Condition {
   protected final int _slot;

   public ConditionInventory(int slot) {
      this._slot = slot;
   }

   @Override
   public abstract boolean testImpl(Env var1);
}
