package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public final class ConditionItemId extends Condition {
   private final int _itemId;

   public ConditionItemId(int itemId) {
      this._itemId = itemId;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getItem() != null && env.getItem().getId() == this._itemId;
   }
}
