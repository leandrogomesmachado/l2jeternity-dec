package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Invincible extends Effect {
   public Invincible(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.INVUL.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.INVINCIBLE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startHealBlocked(true);
      this.getEffected().setIsInvul(true);
      return super.onStart();
   }

   @Override
   public boolean onActionTime() {
      return false;
   }

   @Override
   public void onExit() {
      this.getEffected().startHealBlocked(false);
      if (this.getEffected().getFirstEffect(EffectType.PETRIFICATION) == null) {
         this.getEffected().setIsInvul(false);
      }

      super.onExit();
   }
}
