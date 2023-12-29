package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ManaHealByLevel extends Effect {
   public ManaHealByLevel(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MANAHEAL_BY_LEVEL;
   }

   @Override
   public boolean onStart() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (!target.isHealBlocked() && !target.isDead() && !target.isInvul()) {
         double amount = this.calc();
         amount = target.calcStat(Stats.MANA_CHARGE, amount, null, null);
         if (target.getLevel() > this.getSkill().getMagicLevel()) {
            int lvlDiff = target.getLevel() - this.getSkill().getMagicLevel();
            if (lvlDiff == 6) {
               amount *= 0.9;
            } else if (lvlDiff == 7) {
               amount *= 0.8;
            } else if (lvlDiff == 8) {
               amount *= 0.7;
            } else if (lvlDiff == 9) {
               amount *= 0.6;
            } else if (lvlDiff == 10) {
               amount *= 0.5;
            } else if (lvlDiff == 11) {
               amount *= 0.4;
            } else if (lvlDiff == 12) {
               amount *= 0.3;
            } else if (lvlDiff == 13) {
               amount *= 0.2;
            } else if (lvlDiff == 14) {
               amount *= 0.1;
            } else if (lvlDiff >= 15) {
               amount = 0.0;
            }
         }

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
