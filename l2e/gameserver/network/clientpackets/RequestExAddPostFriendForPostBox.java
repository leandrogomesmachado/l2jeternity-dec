package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExConfirmAddingPostFriend;

public class RequestExAddPostFriendForPostBox extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      if (Config.ALLOW_MAIL) {
         if (this._name != null) {
            Player activeChar = this.getClient().getActiveChar();
            if (activeChar != null) {
               boolean charAdded = activeChar.getContactList().add(this._name);
               activeChar.sendPacket(new ExConfirmAddingPostFriend(this._name, charAdded));
            }
         }
      }
   }
}
