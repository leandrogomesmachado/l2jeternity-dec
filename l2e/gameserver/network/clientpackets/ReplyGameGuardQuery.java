package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.GameClient;

public class ReplyGameGuardQuery extends GameClientPacket {
   private int _dx;

   @Override
   protected void readImpl() {
      this._dx = this.readC();
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (this._dx == 0) {
         client.setGameGuardOk(true);
      } else {
         client.setGameGuardOk(false);
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
