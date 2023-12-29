package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Heal extends Effect {
   public Heal(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.HEAL;
   }

   @Override
   public boolean onStart() {
      Creature target = this.getEffected();
      Creature activeChar = this.getEffector();
      if (target != null && !target.isDead() && !target.isHealBlocked() && !target.isInvul()) {
         if (activeChar.isPlayer()
            && activeChar.getActingPlayer().isInFightEvent()
            && !activeChar.getActingPlayer().getFightEvent().canUsePositiveMagic(activeChar, target)) {
            return false;
         } else {
            double amount = this.calc();
            double staticShotBonus = 0.0;
            int mAtkMul = 1;
            boolean sps = this.getSkill().isMagic() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
            boolean bss = this.getSkill().isMagic() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
            if ((!sps && !bss || !activeChar.isPlayer() || !activeChar.getActingPlayer().isMageClass()) && !activeChar.isSummon()) {
               if ((sps || bss) && activeChar.isNpc()) {
                  staticShotBonus = 2.4 * (double)this.getSkill().getMpConsume();
                  mAtkMul = 4;
               } else {
                  ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
                  if (weaponInst != null) {
                     mAtkMul = weaponInst.getItem().getItemGrade() == 7 ? 4 : (weaponInst.getItem().getItemGrade() == 6 ? 2 : 1);
                  }

                  mAtkMul = bss ? mAtkMul * 4 : mAtkMul + 1;
               }
            } else {
               staticShotBonus = (double)this.getSkill().getMpConsume();
               mAtkMul = bss ? 4 : 2;
               staticShotBonus *= bss ? 2.4 : 1.0;
            }

            if (!this.getSkill().isStatic()) {
               amount += staticShotBonus + Math.sqrt((double)mAtkMul * activeChar.getMAtk(activeChar, null));
               amount = target.calcStat(Stats.HEAL_EFFECT, amount, null, null);
               if (this.getSkill().isMagic() && Formulas.calcMCrit(activeChar.getMCriticalHit(target, this.getSkill()))) {
                  amount *= 3.0;
               }
            }

            amount = Math.max(Math.min(amount, (double)target.getMaxRecoverableHp() - target.getCurrentHp()), 0.0);
            if (amount != 0.0) {
               target.setCurrentHp(amount + target.getCurrentHp());
               StatusUpdate su = new StatusUpdate(target);
               su.addAttribute(9, (int)target.getCurrentHp());
               target.sendPacket(su);
            }

            if (target.isPlayer()) {
               if (this.getSkill().getId() == 4051) {
                  target.sendPacket(SystemMessageId.REJUVENATING_HP);
               } else if (activeChar.isPlayer() && activeChar != target) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
                  sm.addString(activeChar.getName());
                  sm.addNumber((int)amount);
                  target.sendPacket(sm);
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
                  sm.addNumber((int)amount);
                  target.sendPacket(sm);
               }
            }

            activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
            return true;
         }
      } else {
         return false;
      }
   }
}
