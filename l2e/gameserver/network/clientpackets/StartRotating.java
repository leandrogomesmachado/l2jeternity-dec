package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.StartRotation;

public final class StartRotating extends GameClientPacket {
   private int _degree;
   private int _side;

   @Override
   protected void readImpl() {
      this._degree = this.readD();
      this._side = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar)) {
            StartRotation br = new StartRotation(activeChar.getAirShip().getObjectId(), this._degree, this._side, 0);
            activeChar.getAirShip().broadcastPacket(br);
         } else {
            StartRotation br = new StartRotation(activeChar.getObjectId(), this._degree, this._side, 0);
            activeChar.broadcastPacket(br);
         }
      }
   }
}
