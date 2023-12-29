package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetAbnormal extends Condition {
   private final int _abnormalId;

   public ConditionTargetAbnormal(int abnormalId) {
      this._abnormalId = abnormalId;
   }

   @Override
   public boolean testImpl(Env env) {
      for(AbnormalEffect ef : env.getTarget().getAbnormalEffects()) {
         if (ef != null && ef.getId() == this._abnormalId) {
            return true;
         }
      }

      return false;
   }
}
