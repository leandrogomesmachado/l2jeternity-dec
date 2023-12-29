package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.FinishRotatings;

public final class FinishRotating extends GameClientPacket {
   private int _degree;
   protected int _unknown;

   @Override
   protected void readImpl() {
      this._degree = this.readD();
      this._unknown = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar)) {
            activeChar.getAirShip().setHeading(this._degree);
            FinishRotatings sr = new FinishRotatings(activeChar.getAirShip().getObjectId(), this._degree, 0);
            activeChar.getAirShip().broadcastPacket(sr);
         } else {
            FinishRotatings sr = new FinishRotatings(activeChar.getObjectId(), this._degree, 0);
            activeChar.broadcastPacket(sr);
         }
      }
   }
}
