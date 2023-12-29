package l2e.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class FriendList extends GameServerPacket {
   private final List<FriendList.FriendInfo> _info;

   public FriendList(Player player) {
      this._info = new ArrayList<>(player.getFriendList().size());

      for(int objId : player.getFriendList()) {
         String name = CharNameHolder.getInstance().getNameById(objId);
         Player player1 = World.getInstance().getPlayer(objId);
         boolean online = false;
         int classid = 0;
         int level = 0;
         if (player1 == null) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("SELECT char_name, online, classid, level FROM characters WHERE charId = ?");
            ) {
               statement.setInt(1, objId);

               try (ResultSet rset = statement.executeQuery()) {
                  if (rset.next()) {
                     this._info.add(new FriendList.FriendInfo(objId, rset.getString(1), rset.getInt(2) == 1, rset.getInt(3), rset.getInt(4)));
                  }
               }
            } catch (Exception var67) {
            }
         } else {
            if (player1.isOnline()) {
               online = true;
            }

            classid = player1.getClassId().getId();
            level = player1.getLevel();
            this._info.add(new FriendList.FriendInfo(objId, name, online, classid, level));
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._info.size());

      for(FriendList.FriendInfo info : this._info) {
         this.writeD(info._objId);
         this.writeS(info._name);
         this.writeD(info._online ? 1 : 0);
         this.writeD(info._online ? info._objId : 0);
         this.writeD(info._classid);
         this.writeD(info._level);
      }
   }

   private static class FriendInfo {
      int _objId;
      String _name;
      boolean _online;
      int _classid;
      int _level;

      public FriendInfo(int objId, String name, boolean online, int classid, int level) {
         this._objId = objId;
         this._name = name;
         this._online = online;
         this._classid = classid;
         this._level = level;
      }
   }
}
