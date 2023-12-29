package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class NetPing extends GameClientPacket {
   protected int _clientID;
   protected int _ping;

   @Override
   protected void readImpl() {
      this._clientID = this.readD();
      this._ping = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.setPing(this._ping);
      }
   }
}
