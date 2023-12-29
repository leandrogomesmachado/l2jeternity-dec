package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class RecoBonus extends Effect {
   public RecoBonus(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NEVIT_HOURGLASS;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffected().isPlayer()) {
         return false;
      } else {
         this.getEffected().getActingPlayer().getRecommendation().stopRecBonus();
         return true;
      }
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().getActingPlayer().getRecommendation().startRecBonus();
         this.getEffected().getActingPlayer().sendUserInfo(true);
         this.getEffected().getActingPlayer().sendVoteSystemInfo();
      }
   }

   @Override
   public boolean onActionTime() {
      return false;
   }
}
