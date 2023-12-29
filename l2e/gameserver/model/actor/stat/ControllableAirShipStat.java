package l2e.gameserver.model.actor.stat;

import l2e.gameserver.model.actor.instance.ControllableAirShipInstance;

public class ControllableAirShipStat extends VehicleStat {
   public ControllableAirShipStat(ControllableAirShipInstance activeChar) {
      super(activeChar);
   }

   public ControllableAirShipInstance getActiveChar() {
      return (ControllableAirShipInstance)super.getActiveChar();
   }

   @Override
   public double getMoveSpeed() {
      return !this.getActiveChar().isInDock() && this.getActiveChar().getFuel() <= 0 ? super.getMoveSpeed() * 0.05F : super.getMoveSpeed();
   }
}
