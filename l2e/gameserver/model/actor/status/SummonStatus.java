package l2e.gameserver.model.actor.status;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.stats.Stats;

public class SummonStatus extends PlayableStatus {
   public SummonStatus(Summon activeChar) {
      super(activeChar);
   }

   @Override
   public void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
      if (attacker != null && !this.getActiveChar().isDead()) {
         Player attackerPlayer = attacker.getActingPlayer();
         if (attackerPlayer != null && (this.getActiveChar().getOwner() == null || this.getActiveChar().getOwner().getDuelId() != attackerPlayer.getDuelId())) {
            attackerPlayer.setDuelState(4);
         }

         Player caster = this.getActiveChar().getTransferingDamageTo();
         if (this.getActiveChar().getOwner().getParty() != null) {
            if (caster != null
               && Util.checkIfInRange(1000, this.getActiveChar(), caster, true)
               && !caster.isDead()
               && this.getActiveChar().getParty().getMembers().contains(caster)) {
               int transferDmg = 0;
               transferDmg = (int)value * (int)this.getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0.0, null, null) / 100;
               transferDmg = Math.min((int)caster.getCurrentHp() - 1, transferDmg);
               if (transferDmg > 0) {
                  int membersInRange = 0;

                  for(Player member : caster.getParty().getMembers()) {
                     if (Util.checkIfInRange(1000, member, caster, false) && member != caster) {
                        ++membersInRange;
                     }
                  }

                  if (attacker instanceof Playable && caster.getCurrentCp() > 0.0) {
                     if (caster.getCurrentCp() > (double)transferDmg) {
                        caster.getStatus().reduceCp(transferDmg);
                     } else {
                        transferDmg = (int)((double)transferDmg - caster.getCurrentCp());
                        caster.getStatus().reduceCp((int)caster.getCurrentCp());
                     }
                  }

                  if (membersInRange > 0) {
                     caster.reduceCurrentHp((double)(transferDmg / membersInRange), attacker, null);
                     value -= (double)transferDmg;
                  }
               }
            }
         } else if (caster != null
            && caster == this.getActiveChar().getOwner()
            && Util.checkIfInRange(1000, this.getActiveChar(), caster, true)
            && !caster.isDead()) {
            int transferDmg = 0;
            transferDmg = (int)value * (int)this.getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0.0, null, null) / 100;
            transferDmg = Math.min((int)caster.getCurrentHp() - 1, transferDmg);
            if (transferDmg > 0) {
               if (attacker instanceof Playable && caster.getCurrentCp() > 0.0) {
                  if (caster.getCurrentCp() > (double)transferDmg) {
                     caster.getStatus().reduceCp(transferDmg);
                  } else {
                     transferDmg = (int)((double)transferDmg - caster.getCurrentCp());
                     caster.getStatus().reduceCp((int)caster.getCurrentCp());
                  }
               }

               caster.reduceCurrentHp((double)transferDmg, attacker, null);
               value -= (double)transferDmg;
            }
         }

         super.reduceHp(value, attacker, awake, isDOT, isHPConsumption);
      }
   }

   public Summon getActiveChar() {
      return (Summon)super.getActiveChar();
   }
}
