package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class L2FriendList extends GameServerPacket {
   private final List<L2FriendList.FriendInfo> _info;

   public L2FriendList(Player player) {
      this._info = new ArrayList<>(player.getFriendList().size());

      for(int objId : player.getFriendList()) {
         String name = CharNameHolder.getInstance().getNameById(objId);
         Player player1 = World.getInstance().getPlayer(objId);
         boolean online = false;
         if (player1 != null && player1.isOnline()) {
            online = true;
         }

         this._info.add(new L2FriendList.FriendInfo(objId, name, online));
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._info.size());

      for(L2FriendList.FriendInfo info : this._info) {
         this.writeD(info._objId);
         this.writeS(info._name);
         this.writeD(info._online ? 1 : 0);
         this.writeD(info._online ? info._objId : 0);
      }
   }

   private static class FriendInfo {
      int _objId;
      String _name;
      boolean _online;

      public FriendInfo(int objId, String name, boolean online) {
         this._objId = objId;
         this._name = name;
         this._online = online;
      }
   }
}
