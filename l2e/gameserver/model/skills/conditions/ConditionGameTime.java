package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.model.stats.Env;

public class ConditionGameTime extends Condition {
   private final ConditionGameTime.CheckGameTime _check;
   private final boolean _required;

   public ConditionGameTime(ConditionGameTime.CheckGameTime check, boolean required) {
      this._check = check;
      this._required = required;
   }

   @Override
   public boolean testImpl(Env env) {
      switch(this._check) {
         case NIGHT:
            return GameTimeController.getInstance().isNight() == this._required;
         default:
            return !this._required;
      }
   }

   public static enum CheckGameTime {
      NIGHT;
   }
}
