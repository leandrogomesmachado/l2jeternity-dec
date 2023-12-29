package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class FocusMaxEnergy extends Effect {
   public FocusMaxEnergy(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         Skill sonicMastery = this.getEffected().getSkills().get(992);
         Skill focusMastery = this.getEffected().getSkills().get(993);
         int maxCharge = sonicMastery != null ? sonicMastery.getLevel() : (focusMastery != null ? focusMastery.getLevel() : 0);
         if (maxCharge != 0) {
            int count = maxCharge - this.getEffected().getActingPlayer().getCharges();
            this.getEffected().getActingPlayer().increaseCharges(count, maxCharge);
            return true;
         }
      }

      return false;
   }
}
