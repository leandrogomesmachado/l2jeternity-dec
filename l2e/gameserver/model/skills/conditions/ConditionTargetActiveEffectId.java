package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetActiveEffectId extends Condition {
   private final int _effectId;
   private final int _effectLvl;

   public ConditionTargetActiveEffectId(int effectId) {
      this._effectId = effectId;
      this._effectLvl = -1;
   }

   public ConditionTargetActiveEffectId(int effectId, int effectLevel) {
      this._effectId = effectId;
      this._effectLvl = effectLevel;
   }

   @Override
   public boolean testImpl(Env env) {
      Effect e = env.getTarget().getFirstEffect(this._effectId);
      return e != null && (this._effectLvl == -1 || this._effectLvl <= e.getSkill().getLevel());
   }
}
