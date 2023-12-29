package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class SummonAgathion extends Effect {
   public SummonAgathion(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         Player player = this.getEffected().getActingPlayer();
         if (player.isInOlympiadMode()) {
            player.sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            return false;
         } else {
            player.setAgathionId(this.getSkill() == null ? 0 : this.getSkill().getNpcId());
            player.broadcastUserInfo(true);
            return true;
         }
      } else {
         return false;
      }
   }
}
