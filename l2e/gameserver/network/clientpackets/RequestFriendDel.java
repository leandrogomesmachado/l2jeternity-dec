package l2e.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.FriendRemove;
import l2e.gameserver.network.serverpackets.L2Friend;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendDel extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         int id = CharNameHolder.getInstance().getIdByName(this._name);
         if (id == -1) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST);
            sm.addString(this._name);
            activeChar.sendPacket(sm);
         } else if (!activeChar.getFriendList().contains(id)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST);
            sm.addString(this._name);
            activeChar.sendPacket(sm);
         } else {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (charId=? AND friendId=?) OR (charId=? AND friendId=?)");
            ) {
               statement.setInt(1, activeChar.getObjectId());
               statement.setInt(2, id);
               statement.setInt(3, id);
               statement.setInt(4, activeChar.getObjectId());
               statement.execute();
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REMOVED_FROM_YOUR_FRIENDS_LIST);
               sm.addString(this._name);
               activeChar.sendPacket(sm);
               activeChar.getFriendList().remove(Integer.valueOf(id));
               activeChar.sendPacket(new FriendRemove(this._name));
               activeChar.sendPacket(new L2Friend(false, id));
               Player player = World.getInstance().getPlayer(this._name);
               if (player != null) {
                  player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(activeChar.getName()));
                  player.sendPacket(new L2Friend(false, activeChar.getObjectId()));
               }
            } catch (Exception var36) {
               _log.log(Level.WARNING, "could not del friend objectid: ", (Throwable)var36);
            }
         }
      }
   }
}
