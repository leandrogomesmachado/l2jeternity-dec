package l2e.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.FriendAddRequestResult;
import l2e.gameserver.network.serverpackets.L2Friend;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendAddReply extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Player requestor = player.getActiveRequester();
         if (requestor == null) {
            return;
         }

         if (this._response == 1) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId) VALUES (?, ?), (?, ?)");
            ) {
               statement.setInt(1, requestor.getObjectId());
               statement.setInt(2, player.getObjectId());
               statement.setInt(3, player.getObjectId());
               statement.setInt(4, requestor.getObjectId());
               statement.execute();
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
               requestor.sendPacket(msg);
               msg = SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
               msg.addString(player.getName());
               requestor.sendPacket(msg);
               requestor.getFriendList().add(player.getObjectId());
               msg = SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
               msg.addString(requestor.getName());
               player.sendPacket(msg);
               player.getFriendList().add(requestor.getObjectId());
               player.sendPacket(new L2Friend(true, requestor.getObjectId()));
               requestor.sendPacket(new L2Friend(true, player.getObjectId()));
            } catch (Exception var35) {
               _log.log(Level.WARNING, "Could not add friend objectid: " + var35.getMessage(), (Throwable)var35);
            }
         } else {
            requestor.broadcastPacket(FriendAddRequestResult.STATIC_PACKET);
         }

         player.setActiveRequester(null);
         requestor.onTransactionResponse();
      }
   }
}
