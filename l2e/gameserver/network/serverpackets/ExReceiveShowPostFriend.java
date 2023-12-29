package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.model.actor.Player;

public class ExReceiveShowPostFriend extends GameServerPacket {
   private final List<String> _contacts;

   public ExReceiveShowPostFriend(Player player) {
      this._contacts = player.getContactList().getAllContacts();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._contacts.size());

      for(String name : this._contacts) {
         this.writeS(name);
      }
   }
}
