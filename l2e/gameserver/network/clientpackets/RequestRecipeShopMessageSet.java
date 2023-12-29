package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;

public class RequestRecipeShopMessageSet extends GameClientPacket {
   private static final int MAX_MSG_LENGTH = 29;
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._name != null && this._name.length() > 29) {
            Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to overflow recipe shop message");
         } else {
            if (player.hasManufactureShop()) {
               player.setStoreName(this._name);
            }
         }
      }
   }
}
