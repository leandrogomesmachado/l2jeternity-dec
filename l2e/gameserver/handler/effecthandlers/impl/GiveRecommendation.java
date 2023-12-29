package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class GiveRecommendation extends Effect {
   private final int _amount;

   public GiveRecommendation(Env env, EffectTemplate template) {
      super(env, template);
      this._amount = template.getParameters().getInteger("amount", 0);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      Player target = this.getEffected() instanceof Player ? (Player)this.getEffected() : null;
      if (target != null) {
         int recommendationsGiven = this._amount;
         if (target.getRecommendation().getRecomHave() + this._amount >= 255) {
            recommendationsGiven = 255 - target.getRecommendation().getRecomHave();
         }

         if (recommendationsGiven > 0) {
            target.getRecommendation().setRecomHave(target.getRecommendation().getRecomHave() + recommendationsGiven);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
            sm.addNumber(recommendationsGiven);
            target.sendPacket(sm);
            target.sendUserInfo();
            target.sendVoteSystemInfo();
         } else {
            Player player = this.getEffector() instanceof Player ? (Player)this.getEffector() : null;
            if (player != null) {
               player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
            }
         }
      }

      return true;
   }
}
