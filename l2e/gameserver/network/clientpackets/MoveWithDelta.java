package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;

public class MoveWithDelta extends GameClientPacket {
   protected int _dx;
   protected int _dy;
   protected int _dz;

   @Override
   protected void readImpl() {
      this._dx = this.readD();
      this._dy = this.readD();
      this._dz = this.readD();
   }

   @Override
   protected void runImpl() {
      if (Config.PACKET_HANDLER_DEBUG) {
         _log.warning("MoveWithDelta: Not support for this packet!!!");
      }
   }
}
