package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class BigHead extends Effect {
   public BigHead(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startAbnormalEffect(AbnormalEffect.BIG_HEAD);
      return true;
   }

   @Override
   public void onExit() {
      this.getEffected().stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
   }
}
