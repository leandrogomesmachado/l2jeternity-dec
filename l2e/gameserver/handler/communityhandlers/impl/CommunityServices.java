package l2e.gameserver.handler.communityhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityServices extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityServices() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbs_service"};
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (command.startsWith("_bbs_service;nickname")) {
         this.changeName(activeChar);
      } else if (command.startsWith("_bbs_service;pledgename")) {
         this.changePledgeName(activeChar);
      } else if (command.startsWith("_bbs_service;nickcolor")) {
         this.changeNameColor(activeChar);
      } else if (command.startsWith("_bbs_service;titlecolor")) {
         this.changeTitleColor(activeChar);
      }

      StringTokenizer st = new StringTokenizer(command, " ");
      String curCommand = st.nextToken();
      if (curCommand.startsWith("_bbs_service;changenickname")) {
         String name = null;

         try {
            name = st.nextToken();
         } catch (Exception var13) {
         }

         if (name != null) {
            this.playerSetNickName(activeChar, name);
         } else {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.NOT_ENTER_NAME", activeChar.getLang()).toString());
         }
      } else if (command.startsWith("_bbs_service;changepledgename")) {
         String name = null;

         try {
            name = st.nextToken();
         } catch (Exception var12) {
         }

         if (name != null) {
            this.pledgeSetName(activeChar, name);
         } else {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.NOT_ENTER_NAME", activeChar.getLang()).toString());
         }
      } else if (curCommand.startsWith("_bbs_service;changenickcolor")) {
         String color = null;
         String days = null;

         try {
            color = st.nextToken();
         } catch (Exception var11) {
         }

         try {
            days = st.nextToken();
         } catch (Exception var10) {
         }

         if (color != null && days != null) {
            this.playerSetColor(activeChar, color, Integer.parseInt(days), 1);
         }
      } else if (curCommand.startsWith("_bbs_service;changetitlecolor")) {
         String color = null;
         String days = null;

         try {
            color = st.nextToken();
         } catch (Exception var9) {
         }

         try {
            days = st.nextToken();
         } catch (Exception var8) {
         }

         if (color != null && days != null) {
            this.playerSetColor(activeChar, color, Integer.parseInt(days), 2);
         }
      }
   }

   private void playerSetColor(Player activeChar, String color, int days, int type) {
      String colorh = new String("FFFFFF");
      if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.GREEN") + "")) {
         colorh = "00FF00";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YELLOW") + "")) {
         colorh = "00FFFF";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.ORANGE") + "")) {
         colorh = "0099FF";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BLUE") + "")) {
         colorh = "FF0000";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BLACK") + "")) {
         colorh = "000000";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BROWN") + "")) {
         colorh = "006699";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.LIGHT_PINK") + "")) {
         colorh = "FF66FF";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.PINK") + "")) {
         colorh = "FF00FF";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.LIGHT_BLUE") + "")) {
         colorh = "FFFF66";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.TURQUOSE") + "")) {
         colorh = "999900";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.LIME") + "")) {
         colorh = "99FF99";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.GRAY") + "")) {
         colorh = "999999";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.DARK_GREEN") + "")) {
         colorh = "339900";
      } else if (color.equalsIgnoreCase("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.PURPLE") + "")) {
         colorh = "FF3399";
      }

      int itemId = 0;
      long amount = 0L;
      boolean found = false;
      long expireTime = System.currentTimeMillis() + (long)days * 86400000L;
      switch(type) {
         case 1:
            for(int day : Config.CHANGE_COLOR_NAME_LIST.keySet()) {
               if (day == days) {
                  found = true;
                  String[] price = Config.CHANGE_COLOR_NAME_LIST.get(day).split(":");
                  if (price != null && price.length == 2) {
                     itemId = Integer.parseInt(price[0]);
                     amount = Long.parseLong(price[1]);
                  }
                  break;
               }
            }

            if (found) {
               if (itemId != 0) {
                  if (activeChar.getInventory().getItemByItemId(itemId) == null) {
                     haveNoItems(activeChar, itemId, amount);
                     this.changeNameColor(activeChar);
                     return;
                  }

                  if (activeChar.getInventory().getItemByItemId(itemId).getCount() < amount) {
                     haveNoItems(activeChar, itemId, amount);
                     this.changeNameColor(activeChar);
                     return;
                  }

                  activeChar.destroyItemByItemId("BBSColorName", itemId, amount, activeChar, false);
                  Util.addServiceLog(activeChar.getName() + " buy color name service!");
               }

               int curColor = Integer.decode("0x" + colorh);
               activeChar.setVar("namecolor", Integer.toString(curColor), expireTime);
               activeChar.getAppearance().setNameColor(curColor);
               activeChar.broadcastUserInfo(true);
               activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_NAME") + " " + color);
            }
            break;
         case 2:
            for(int day : Config.CHANGE_COLOR_TITLE_LIST.keySet()) {
               if (day == days) {
                  found = true;
                  String[] price = Config.CHANGE_COLOR_TITLE_LIST.get(day).split(":");
                  if (price != null && price.length == 2) {
                     itemId = Integer.parseInt(price[0]);
                     amount = Long.parseLong(price[1]);
                  }
                  break;
               }
            }

            if (found) {
               if (itemId != 0) {
                  if (activeChar.getInventory().getItemByItemId(itemId) == null) {
                     haveNoItems(activeChar, itemId, amount);
                     this.changeTitleColor(activeChar);
                     return;
                  }

                  if (activeChar.getInventory().getItemByItemId(itemId).getCount() < amount) {
                     haveNoItems(activeChar, itemId, amount);
                     this.changeTitleColor(activeChar);
                     return;
                  }

                  activeChar.destroyItemByItemId("BBSColorTitle", itemId, amount, activeChar, false);
                  Util.addServiceLog(activeChar.getName() + " buy color title service!");
               }

               int curColor1 = Integer.decode("0x" + colorh);
               activeChar.getAppearance().setTitleColor(curColor1);
               activeChar.setVar("titlecolor", Integer.toString(curColor1), expireTime);
               activeChar.broadcastUserInfo(true);
               activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_TITLE_COLOR") + " " + color);
            }
      }
   }

   private void playerSetNickName(Player activeChar, String name) {
      if (name.length() < 3 || name.length() > 16 || !this.isValidName(name, true)) {
         activeChar.sendMessage(new ServerMessage("ServiceBBS.CHANGE_NAME_COLOR", activeChar.getLang()).toString());
         this.changeName(activeChar);
      } else if (activeChar.getInventory().getItemByItemId(Config.SERVICES_NAMECHANGE_ITEM[0]) != null
         && activeChar.getInventory().getItemByItemId(Config.SERVICES_NAMECHANGE_ITEM[0]).getCount() >= (long)Config.SERVICES_NAMECHANGE_ITEM[1]) {
         int existing = 0;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE char_name=?");
            statement.setString(1, name);
            ResultSet rset = statement.executeQuery();

            while(rset.next()) {
               existing = rset.getInt("charId");
            }
         } catch (Exception var18) {
            System.out.println("Error in check nick " + var18);
         }

         if (existing == 0) {
            activeChar.setName(name);
            activeChar.destroyItemByItemId("BBSChangeName", Config.SERVICES_NAMECHANGE_ITEM[0], (long)Config.SERVICES_NAMECHANGE_ITEM[1], activeChar, false);
            Util.addServiceLog(activeChar.getName() + " buy change name service!");
            activeChar.broadcastUserInfo(true);
            activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YOUR_NAME") + " " + name);
            activeChar.store();
            this.changeName(activeChar);
         } else {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.ALREADY_USE", activeChar.getLang()).toString());
            this.changeName(activeChar);
         }
      } else {
         haveNoItems(activeChar, Config.SERVICES_NAMECHANGE_ITEM[0], (long)Config.SERVICES_NAMECHANGE_ITEM[1]);
         this.changeName(activeChar);
      }
   }

   private void changeName(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/changename.htm");
      activeChar.sendPacket(html);
   }

   private void changeNameColor(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/colorname.htm");
      activeChar.sendPacket(html);
   }

   private void changePledgeName(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/changepledgename.htm");
      activeChar.sendPacket(html);
   }

   private void changeTitleColor(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/colortitle.htm");
      activeChar.sendPacket(html);
   }

   private boolean isValidName(String text, boolean isCharName) {
      boolean result = true;

      Pattern pattern;
      try {
         pattern = Pattern.compile(isCharName ? Config.SERVICES_NAMECHANGE_TEMPLATE : Config.CLAN_NAME_TEMPLATE);
      } catch (PatternSyntaxException var7) {
         _log.warning("ERROR : Character name pattern of config is wrong!");
         pattern = Pattern.compile(".*");
      }

      Matcher regexp = pattern.matcher(text);
      if (!regexp.matches()) {
         result = false;
      }

      return result;
   }

   private void pledgeSetName(Player activeChar, String name) {
      if (activeChar.getClan() != null) {
         if (activeChar.getClan().getLeaderId() == activeChar.getObjectId()) {
            if (name.length() < 3 || name.length() > 16 || !Util.isAlphaNumeric(name) || !this.isValidName(name, false)) {
               activeChar.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
               this.changePledgeName(activeChar);
            } else if (ClanHolder.getInstance().getClanByName(name) != null) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
               sm.addString(name);
               activeChar.sendPacket(sm);
               this.changePledgeName(activeChar);
            } else {
               if (Config.SERVICES_CLANNAMECHANGE_ITEM[0] != 0) {
                  if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANNAMECHANGE_ITEM[0]) == null) {
                     haveNoItems(activeChar, Config.SERVICES_CLANNAMECHANGE_ITEM[0], (long)Config.SERVICES_CLANNAMECHANGE_ITEM[1]);
                     this.changeTitleColor(activeChar);
                     return;
                  }

                  if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANNAMECHANGE_ITEM[0]).getCount()
                     < (long)Config.SERVICES_CLANNAMECHANGE_ITEM[1]) {
                     haveNoItems(activeChar, Config.SERVICES_CLANNAMECHANGE_ITEM[0], (long)Config.SERVICES_CLANNAMECHANGE_ITEM[1]);
                     this.changeTitleColor(activeChar);
                     return;
                  }

                  activeChar.destroyItemByItemId(
                     "ClanNameChange", Config.SERVICES_CLANNAMECHANGE_ITEM[0], (long)Config.SERVICES_CLANNAMECHANGE_ITEM[1], activeChar, false
                  );
                  Util.addServiceLog(activeChar.getName() + " buy change clan name service!");
               }

               Clan clan = activeChar.getClan();
               if (clan != null) {
                  clan.setName(name);
                  clan.updateClanNameInDB();
                  activeChar.broadcastUserInfo(true);
                  activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YOUR_NAME") + " " + name);
               }
            }
         }
      }
   }

   private static void haveNoItems(Player player, int itemId, long amount) {
      Item template = ItemsParser.getInstance().getTemplate(itemId);
      if (template != null) {
         ServerMessage msg = new ServerMessage("Enchant.NEED_ITEMS", player.getLang());
         msg.add(amount);
         msg.add(player.getItemName(template));
         player.sendMessage(msg.toString());
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityServices getInstance() {
      return CommunityServices.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityServices _instance = new CommunityServices();
   }
}
