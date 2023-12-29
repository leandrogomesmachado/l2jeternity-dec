package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExPrivateStorePackageMsg;

public class SetPrivateStoreWholeMsg extends GameClientPacket {
   private static final int MAX_MSG_LENGTH = 29;
   private String _msg;

   @Override
   protected void readImpl() {
      this._msg = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null && player.getSellList() != null) {
         if (this._msg != null && this._msg.length() > 29) {
            Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to overflow private store whole message");
         } else {
            player.getSellList().setTitle(this._msg);
            this.sendPacket(new ExPrivateStorePackageMsg(player));
         }
      }
   }
}
