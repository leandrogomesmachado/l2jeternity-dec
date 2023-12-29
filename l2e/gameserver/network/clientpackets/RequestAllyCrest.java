package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.serverpackets.AllianceCrest;

public final class RequestAllyCrest extends GameClientPacket {
   private int _crestId;

   @Override
   protected void readImpl() {
      this._crestId = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._crestId != 0) {
         this.sendPacket(new AllianceCrest(this._crestId));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
