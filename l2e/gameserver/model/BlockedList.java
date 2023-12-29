package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class BlockedList {
   private static Logger _log = Logger.getLogger(BlockedList.class.getName());
   private static Map<Integer, List<Integer>> _offlineList = new ConcurrentHashMap<>();
   private final Player _owner;
   private List<Integer> _blockList;

   public BlockedList(Player owner) {
      this._owner = owner;
      this._blockList = _offlineList.get(owner.getObjectId());
      if (this._blockList == null) {
         this._blockList = loadList(this._owner.getObjectId());
      }
   }

   private void addToBlockList(int target) {
      this._blockList.add(target);
      this.updateInDB(target, true);
   }

   private void removeFromBlockList(int target) {
      this._blockList.remove(Integer.valueOf(target));
      this.updateInDB(target, false);
   }

   public void playerLogout() {
      _offlineList.put(this._owner.getObjectId(), this._blockList);
   }

   private static List<Integer> loadList(int ObjId) {
      List<Integer> list = new ArrayList<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=? AND relation=1");
      ) {
         statement.setInt(1, ObjId);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int friendId = rset.getInt("friendId");
               if (friendId != ObjId) {
                  list.add(friendId);
               }
            }
         }
      } catch (Exception var60) {
         _log.log(Level.WARNING, "Error found in " + ObjId + " FriendList while loading BlockList: " + var60.getMessage(), (Throwable)var60);
      }

      return list;
   }

   private void updateInDB(int targetId, boolean state) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         if (state) {
            try (PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId, relation) VALUES (?, ?, 1)")) {
               statement.setInt(1, this._owner.getObjectId());
               statement.setInt(2, targetId);
               statement.execute();
            }
         } else {
            try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? AND friendId=? AND relation=1")) {
               statement.setInt(1, this._owner.getObjectId());
               statement.setInt(2, targetId);
               statement.execute();
            }
         }
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not add block player: " + var59.getMessage(), (Throwable)var59);
      }
   }

   public boolean isInBlockList(Player target) {
      return this._blockList.contains(target.getObjectId());
   }

   public boolean isInBlockList(int targetId) {
      return this._blockList.contains(targetId);
   }

   private boolean isBlockAll() {
      return this._owner.getMessageRefusal();
   }

   public static boolean isBlocked(Player listOwner, Player target) {
      BlockedList blockList = listOwner.getBlockList();
      return blockList.isBlockAll() || blockList.isInBlockList(target);
   }

   public static boolean isBlocked(Player listOwner, int targetId) {
      BlockedList blockList = listOwner.getBlockList();
      return blockList.isBlockAll() || blockList.isInBlockList(targetId);
   }

   private void setBlockAll(boolean state) {
      this._owner.setMessageRefusal(state);
   }

   public List<Integer> getBlockList() {
      return this._blockList;
   }

   public static void addToBlockList(Player listOwner, int targetId) {
      if (listOwner != null) {
         String charName = CharNameHolder.getInstance().getNameById(targetId);
         if (listOwner.getFriendList().contains(targetId)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
            sm.addString(charName);
            listOwner.sendPacket(sm);
         } else if (listOwner.getBlockList().getBlockList().contains(targetId)) {
            listOwner.sendMessage("Already in ignore list.");
         } else {
            listOwner.getBlockList().addToBlockList(targetId);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
            sm.addString(charName);
            listOwner.sendPacket(sm);
            Player player = World.getInstance().getPlayer(targetId);
            if (player != null) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
               sm.addString(listOwner.getName());
               player.sendPacket(sm);
            }
         }
      }
   }

   public static void removeFromBlockList(Player listOwner, int targetId) {
      if (listOwner != null) {
         String charName = CharNameHolder.getInstance().getNameById(targetId);
         if (!listOwner.getBlockList().getBlockList().contains(targetId)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
            listOwner.sendPacket(sm);
         } else {
            listOwner.getBlockList().removeFromBlockList(targetId);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
            sm.addString(charName);
            listOwner.sendPacket(sm);
         }
      }
   }

   public static boolean isInBlockList(Player listOwner, Player target) {
      return listOwner.getBlockList().isInBlockList(target);
   }

   public boolean isBlockAll(Player listOwner) {
      return listOwner.getBlockList().isBlockAll();
   }

   public static void setBlockAll(Player listOwner, boolean newValue) {
      listOwner.getBlockList().setBlockAll(newValue);
   }

   public static void sendListToOwner(Player listOwner) {
      int i = 1;
      listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);

      for(int playerId : listOwner.getBlockList().getBlockList()) {
         listOwner.sendMessage(i++ + ". " + CharNameHolder.getInstance().getNameById(playerId));
      }

      listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
   }

   public static boolean isInBlockList(int ownerId, int targetId) {
      Player player = World.getInstance().getPlayer(ownerId);
      if (player != null) {
         return isBlocked(player, targetId);
      } else {
         if (!_offlineList.containsKey(ownerId)) {
            _offlineList.put(ownerId, loadList(ownerId));
         }

         return _offlineList.get(ownerId).contains(targetId);
      }
   }
}
