package l2e.gameserver.handler.communityhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.L2Friend;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityFriend extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityFriend() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_friendlist_0_", "Friends"};
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (command.equals("_friendlist_0_")) {
         this.showFriendsList(activeChar);
      } else if (command.startsWith("_friendlist_0_;playerdelete;")) {
         StringTokenizer st = new StringTokenizer(command, ";");
         st.nextToken();
         st.nextToken();
         String name = st.nextToken();
         this.deleteFriend(activeChar, name);
         this.showFriendsList(activeChar);
      } else if (command.startsWith("_friendlist_0_;playerinfo;")) {
         StringTokenizer st = new StringTokenizer(command, ";");
         st.nextToken();
         st.nextToken();
         String name = st.nextToken();
         this.showFriendsInfo(activeChar, name);
      } else {
         ShowBoard sb = new ShowBoard(
            "<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar
         );
         activeChar.sendPacket(sb);
         activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
         activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
      }
   }

   private void deleteFriend(Player activeChar, String name) {
      int id = CharNameHolder.getInstance().getIdByName(name);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (charId=? AND friendId=?) OR (charId=? AND friendId=?)");
      ) {
         statement.setInt(1, activeChar.getObjectId());
         statement.setInt(2, id);
         statement.setInt(3, id);
         statement.setInt(4, activeChar.getObjectId());
         statement.execute();
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
         sm.addString(name);
         activeChar.sendPacket(sm);
         activeChar.getFriendList().remove(Integer.valueOf(id));
         activeChar.sendPacket(new L2Friend(false, id));
      } catch (Exception var36) {
         _log.warning("could not del friend objectid: " + var36.getMessage());
      }
   }

   private void showFriendsInfo(Player activeChar, String name) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/friends/friendinfo.htm");
      String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/friends/friendinfo-template.htm");
      String block = "";
      String list = "";
      Player player = World.getInstance().getPlayer(name);
      if (player != null) {
         block = template.replace("%friendName%", player.getName());
         block = block.replace(
            "%sex%",
            player.getAppearance().getSex()
               ? ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.FEMALE")
               : ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.MALE")
         );
         block = block.replace("%class%", ClassListParser.getInstance().getClass(player.getClassId()).getClientCode());
         block = block.replace("%level%", String.valueOf(player.getLevel()));
         block = block.replace(
            "%clan%",
            player.getClan() != null ? player.getClan().getName() : ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN")
         );
         list = list + block;
      } else {
         list = "<center>" + ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.NOT_PLAYER") + " " + name + "!</center>";
      }

      html = html.replace("%info%", list);
      separateAndSend(html, activeChar);
   }

   private void showFriendsList(Player activeChar) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/friends/friendslist.htm");
      String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/friends/friends-template.htm");
      String block = "";
      String list = "";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=?");
      ) {
         statement.setInt(1, activeChar.getObjectId());

         for(ResultSet rset = statement.executeQuery(); rset.next(); list = list + block) {
            int friendId = rset.getInt("friendId");
            String friendName = CharNameHolder.getInstance().getNameById(friendId);
            Player friend = friendId != 0 ? (Player)World.getInstance().findObject(friendId) : World.getInstance().getPlayer(friendName);
            block = template.replace(
               "%friendName%", friend == null ? friendName : "<a action=\"bypass _friendlist_0_;playerinfo;" + friendName + "\">" + friendName + "</a>"
            );
            block = block.replace(
               "%status%",
               friend == null
                  ? "<font color=\"D70000\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.OFF") + "</font>"
                  : "<font color=\"00CC00\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.ON") + "</font>"
            );
            block = block.replace(
               "%action%",
               friend == null
                  ? ""
                  : "<button value=\""
                     + ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.DELETE")
                     + "\" action=\"bypass _friendlist_0_;playerdelete;"
                     + friendName
                     + "\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"
            );
         }
      } catch (Exception var41) {
         list = ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.CAN_T_SHOW");
      }

      if (list == "" || list.isEmpty()) {
         list = ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.LIST_EMPTY");
      }

      html = html.replace("%list%", list);
      separateAndSend(html, activeChar);
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
      if (command.equals("Friends")) {
         if (ar1.equals("PM")) {
            try {
               Player reciever = World.getInstance().getPlayer(ar2);
               if (reciever == null) {
                  activeChar.sendMessage(ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.NOT_FOUND"));
                  this.onBypassCommand("_friendlist_0_;playerinfo;" + ar2, activeChar);
                  return;
               }

               if (activeChar.isChatBanned()) {
                  activeChar.sendMessage(ServerStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BANNED"));
                  this.onBypassCommand("_friendlist_0_;playerinfo;" + reciever.getName(), activeChar);
                  return;
               }

               if (!reciever.getMessageRefusal()) {
                  reciever.sendPacket(new CreatureSay(0, 2, activeChar.getName(), ar3));
                  activeChar.sendPacket(new CreatureSay(0, 2, activeChar.getName(), ar3));
                  this.onBypassCommand("_friendlist_0_;playerinfo;" + reciever.getName(), activeChar);
               } else {
                  activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
                  this.onBypassCommand("_friendlist_0_;playerinfo;" + reciever.getName(), activeChar);
               }
            } catch (StringIndexOutOfBoundsException var9) {
            }
         } else {
            ShowBoard sb = new ShowBoard(
               "<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", "101", activeChar
            );
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
            activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
         }
      }
   }

   public static CommunityFriend getInstance() {
      return CommunityFriend.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityFriend _instance = new CommunityFriend();
   }
}
