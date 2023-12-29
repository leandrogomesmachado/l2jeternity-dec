package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyMsg;

public final class SetPrivateStoreBuyMsg extends GameClientPacket {
   private static final int MAX_MSG_LENGTH = 29;
   private String _storeMsg;

   @Override
   protected void readImpl() {
      this._storeMsg = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null && player.getBuyList() != null) {
         if (this._storeMsg != null && this._storeMsg.length() > 29) {
            Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to overflow private store buy message");
         } else {
            player.getBuyList().setTitle(this._storeMsg);
            player.sendPacket(new PrivateStoreBuyMsg(player));
         }
      }
   }
}
