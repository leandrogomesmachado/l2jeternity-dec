package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerSex extends Condition {
   private final int _sex;

   public ConditionPlayerSex(int sex) {
      this._sex = sex;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         return (env.getPlayer().getAppearance().getSex() ? 1 : 0) == this._sex;
      }
   }
}
