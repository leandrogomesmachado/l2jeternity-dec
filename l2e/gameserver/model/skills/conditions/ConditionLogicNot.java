package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionLogicNot extends Condition {
   private final Condition _condition;

   public ConditionLogicNot(Condition condition) {
      this._condition = condition;
      if (this.getListener() != null) {
         this._condition.setListener(this);
      }
   }

   @Override
   void setListener(ConditionListener listener) {
      if (listener != null) {
         this._condition.setListener(this);
      } else {
         this._condition.setListener(null);
      }

      super.setListener(listener);
   }

   @Override
   public boolean testImpl(Env env) {
      return !this._condition.test(env);
   }
}
