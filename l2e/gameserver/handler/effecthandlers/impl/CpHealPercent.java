package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CpHealPercent extends Effect {
   public CpHealPercent(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CPHEAL_PERCENT;
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
         amount = full ? target.getMaxCp() : target.getMaxCp() * power / 100.0;
         amount = Math.max(Math.min(amount, (double)target.getMaxRecoverableCp() - target.getCurrentCp()), 0.0);
         if (amount != 0.0) {
            target.setCurrentCp(amount + target.getCurrentCp());
            StatusUpdate su = new StatusUpdate(target);
            su.addAttribute(33, (int)target.getCurrentCp());
            target.sendPacket(su);
         }

         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
         sm.addNumber((int)amount);
         target.sendPacket(sm);
         return true;
      } else {
         return false;
      }
   }
}
