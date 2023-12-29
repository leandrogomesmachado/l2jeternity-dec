package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ManaHealPercent extends Effect {
   public ManaHealPercent(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MANAHEAL_PERCENT;
   }

   @Override
   public boolean onStart() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (!target.isHealBlocked() && !target.isDead()) {
         double amount = 0.0;
         double power = this.calc();
         boolean full = power == 100.0;
         amount = full ? target.getMaxMp() : target.getMaxMp() * power / 100.0;
         amount = Math.max(Math.min(amount, (double)target.getMaxRecoverableMp() - target.getCurrentMp()), 0.0);
         if (amount != 0.0) {
            target.setCurrentMp(amount + target.getCurrentMp());
            StatusUpdate su = new StatusUpdate(target);
            su.addAttribute(11, (int)target.getCurrentMp());
            target.sendPacket(su);
         }

         SystemMessage sm;
         if (this.getEffector().getObjectId() != target.getObjectId()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
            sm.addCharName(this.getEffector());
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
         }

         sm.addNumber((int)amount);
         target.sendPacket(sm);
         return true;
      } else {
         return false;
      }
   }
}
