package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;

public class SiegeFlagStatus extends NpcStatus {
   public SiegeFlagStatus(SiegeFlagInstance activeChar) {
      super(activeChar);
   }

   @Override
   public void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
      if (this.getActiveChar().isAdvancedHeadquarter()) {
         value /= 2.0;
      }

      super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
   }

   public SiegeFlagInstance getActiveChar() {
      return (SiegeFlagInstance)super.getActiveChar();
   }
}
