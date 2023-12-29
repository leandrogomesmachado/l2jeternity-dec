package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public final class CanNotMoveAnymore extends GameClientPacket {
   private int _x;
   private int _y;
   private int _z;
   private int _heading;

   @Override
   protected void readImpl() {
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
      this._heading = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (Config.DEBUG) {
            _log.fine(
               "client: x:" + this._x + " y:" + this._y + " z:" + this._z + " server x:" + player.getX() + " y:" + player.getY() + " z:" + player.getZ()
            );
         }

         if (player.getAI() != null) {
            player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new Location(this._x, this._y, this._z, this._heading));
         }
      }
   }
}
