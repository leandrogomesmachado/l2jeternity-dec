package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class AttackableStatus extends NpcStatus {
   public AttackableStatus(Attackable activeChar) {
      super(activeChar);
   }

   @Override
   public final void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
      if (!this.getActiveChar().isDead()) {
         if (value > 0.0) {
            if (this.getActiveChar().isOverhit()) {
               this.getActiveChar().setOverhitValues(attacker, value);
            } else {
               this.getActiveChar().overhitEnabled(false);
            }
         } else {
            this.getActiveChar().overhitEnabled(false);
         }

         super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
         if (!this.getActiveChar().isDead()) {
            this.getActiveChar().overhitEnabled(false);
         }
      }
   }

   public Attackable getActiveChar() {
      return (Attackable)super.getActiveChar();
   }
}
