package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInfoList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);
         Player friend = null;

         for(int id : activeChar.getFriendList()) {
            String friendName = CharNameHolder.getInstance().getNameById(id);
            if (friendName != null) {
               friend = World.getInstance().getPlayer(friendName);
               SystemMessage sm;
               if (friend != null && friend.isOnline()) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE);
                  sm.addString(friendName);
               } else {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE);
                  sm.addString(friendName);
               }

               activeChar.sendPacket(sm);
            }
         }

         activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
      }
   }
}
