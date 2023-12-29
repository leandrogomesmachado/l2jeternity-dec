package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class RebalanceHP extends Effect {
   public RebalanceHP(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.REBALANCE_HP;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector().isPlayer() && this.getEffector().isInParty()) {
         double fullHP = 0.0;
         double currentHPs = 0.0;
         Party party = this.getEffector().getParty();

         for(Player member : party.getMembers()) {
            if (!member.isDead() && Util.checkIfInRange(this.getSkill().getAffectRange(), this.getEffector(), member, true)) {
               fullHP += member.getMaxHp();
               currentHPs += member.getCurrentHp();
               if (member.hasSummon()) {
                  Summon summon = member.getSummon();
                  if (summon != null && !summon.isDead() && Util.checkIfInRange(this.getSkill().getAffectRange(), this.getEffector(), summon, true)) {
                     fullHP += summon.getMaxHp();
                     currentHPs += summon.getCurrentHp();
                  }
               }
            }
         }

         double percentHP = currentHPs / fullHP;

         for(Player member : party.getMembers()) {
            if (!member.isDead() && Util.checkIfInRange(this.getSkill().getAffectRange(), this.getEffector(), member, true)) {
               if (member.hasSummon()) {
                  Summon summon = member.getSummon();
                  if (summon != null && !summon.isDead() && Util.checkIfInRange(this.getSkill().getAffectRange(), this.getEffector(), summon, true)) {
                     double newHP = summon.getMaxHp() * percentHP;
                     if (newHP > summon.getCurrentHp()) {
                        if (summon.getCurrentHp() > (double)summon.getMaxRecoverableHp()) {
                           newHP = summon.getCurrentHp();
                        } else if (newHP > (double)summon.getMaxRecoverableHp()) {
                           newHP = (double)summon.getMaxRecoverableHp();
                        }
                     }

                     summon.setCurrentHp(newHP);
                  }
               }

               double newHP = member.getMaxHp() * percentHP;
               if (newHP > member.getCurrentHp()) {
                  if (member.getCurrentHp() > (double)member.getMaxRecoverableHp()) {
                     newHP = member.getCurrentHp();
                  } else if (newHP > (double)member.getMaxRecoverableHp()) {
                     newHP = (double)member.getMaxRecoverableHp();
                  }
               }

               member.setCurrentHp(newHP);
               StatusUpdate su = new StatusUpdate(member);
               su.addAttribute(9, (int)member.getCurrentHp());
               member.sendPacket(su);
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
