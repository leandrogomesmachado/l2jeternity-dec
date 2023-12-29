package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Menu implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"menu", "cfg"};
   private static final String _ONText = "<font color=\"00FF00\">ON</font>";
   private static final String _OFFText = "<font color=\"FF0000\">OFF</font>";

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_MENU_COMMAND) {
         return false;
      } else {
         if (command.equalsIgnoreCase("menu") || command.equalsIgnoreCase("cfg")) {
            this.showConfigMenu(activeChar, null);
         } else if (command.startsWith("menu") || command.startsWith("cfg")) {
            String[] params = command.split(" ");
            if (params.length == 4 && params[1].equalsIgnoreCase("set_var")) {
               activeChar.setVar(params[2], params[3]);
               if (params[2].equalsIgnoreCase("useHideTraders@")) {
                  if (params[3].equalsIgnoreCase("1")) {
                     activeChar.hidePrivateStores();
                  } else {
                     activeChar.restorePrivateStores();
                  }
               } else if (params[2].equalsIgnoreCase("visualBlock")) {
                  activeChar.broadcastCharInfoAround();
               }

               this.showConfigMenu(activeChar, null);
            } else if (params.length == 3 && params[1].equalsIgnoreCase("edit_var")) {
               this.showConfigMenu(activeChar, params[2]);
            } else if (params.length == 3 && params[1].equalsIgnoreCase("set_logout")) {
               String percent = null;

               try {
                  percent = params[2];
               } catch (Exception var8) {
               }

               if (percent != null) {
                  int per = 0;

                  try {
                     per = Integer.parseInt(percent);
                     if (per > 120) {
                        per = 120;
                     }

                     if (per < 0) {
                        per = 0;
                     }
                  } catch (NumberFormatException var9) {
                     if (params[1].equalsIgnoreCase("set_logout")) {
                        per = activeChar.getVarInt("logoutTime", Config.DISCONNECT_TIMEOUT);
                     }
                  }

                  if (params[1].equalsIgnoreCase("set_logout")) {
                     activeChar.setVar("logoutTime", per);
                  }
               }

               this.showConfigMenu(activeChar, null);
            } else {
               this.showConfigMenu(activeChar, null);
            }
         }

         return true;
      }
   }

   private void showConfigMenu(Player activeChar, String editCmd) {
      String lang = activeChar.getLang();
      String language = "";
      String autoloot = "";
      String autolootHerbs = "";
      String blockExp = "";
      String blockBuffs = "";
      String hideTrades = "";
      String hideBuffs = "";
      String blockTrades = "";
      String blockPartys = "";
      String blockFriends = "";
      String useBlockPartyRecall = "";
      String noCarrier = "";
      String blockVisual = "";
      if (Config.MULTILANG_ENABLE && Config.MULTILANG_ALLOWED.size() > 1) {
         language = language + "<tr>";
         language = language + "<td width=5></td>";
         language = language + "<td width=180>" + ServerStorage.getInstance().getString(lang, "Menu.STRING_LANGUAGE") + ":</td>";
         language = language + "<td width=41><font color=\"LEVEL\">" + lang.toUpperCase() + "</font></td>";
         if (!Config.MULTILANG_VOICED_ALLOW) {
            language = language + "<td width=40 align=center>" + ServerStorage.getInstance().getString(lang, "Menu.STRING_NOT_AVAILABLE") + "</td>";
         } else {
            language = language + "<td width=40>";

            for(String lng : Config.MULTILANG_ALLOWED) {
               if (!lang.equalsIgnoreCase(lng.toString())) {
                  language = language
                     + "<button width=40 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var lang@ "
                     + lng
                     + "\" value=\""
                     + lng.toUpperCase()
                     + "\">";
               }
            }

            language = language + "</td>";
         }

         language = language + "</tr>";
      }

      if (Config.ALLOW_AUTOLOOT_COMMAND) {
         autoloot = autoloot
            + this.getBooleanFrame(
               activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT"), "useAutoLoot@", Config.ALLOW_AUTOLOOT_COMMAND
            );
      } else {
         autoloot = autoloot + this.getManualBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT"), false);
      }

      if (Config.ALLOW_AUTOLOOT_COMMAND) {
         autolootHerbs = autolootHerbs
            + this.getBooleanFrame(
               activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT_HERBS"), "useAutoLootHerbs@", Config.ALLOW_AUTOLOOT_COMMAND
            );
      } else {
         autolootHerbs = autolootHerbs
            + this.getManualBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT_HERBS"), false);
      }

      if (Config.DISCONNECT_SYSTEM_ENABLED) {
         noCarrier = noCarrier
            + getBooleanFrame(
               activeChar,
               ServerStorage.getInstance().getString(lang, "Menu.STRING_DISCONNECT"),
               editCmd != null && editCmd.equals("editLogout") ? editCmd : null,
               "editLogout",
               "logoutTime",
               Config.DISCONNECT_TIMEOUT
            );
      } else {
         noCarrier = noCarrier + this.getManualBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_DISCONNECT"), false);
      }

      blockExp = blockExp
         + this.getBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_XP"), "blockedEXP@", Config.ALLOW_EXP_GAIN_COMMAND);
      blockBuffs = blockBuffs
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_BUFFS"), "useBlockBuffs@", Config.ALLOW_BLOCKBUFFS_COMMAND
         );
      hideTrades = hideTrades
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_HIDE_TRADES"), "useHideTraders@", Config.ALLOW_HIDE_TRADERS_COMMAND
         );
      hideBuffs = hideBuffs
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_HIDE_BUFFS"), "useHideBuffs@", Config.ALLOW_HIDE_BUFFS_ANIMATION_COMMAND
         );
      blockTrades = blockTrades
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_TRADES"), "useBlockTrade@", Config.ALLOW_BLOCK_TRADERS_COMMAND
         );
      blockPartys = blockPartys
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_PARTYS"), "useBlockParty@", Config.ALLOW_BLOCK_PARTY_COMMAND
         );
      blockFriends = blockFriends
         + this.getBooleanFrame(
            activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_FRIENDS"), "useBlockFriend@", Config.ALLOW_BLOCK_FRIEND_COMMAND
         );
      blockVisual = blockVisual
         + this.getBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_VISUAL"), "visualBlock", true);
      useBlockPartyRecall = useBlockPartyRecall
         + this.getBooleanFrame(activeChar, ServerStorage.getInstance().getString(lang, "Menu.STRING_PARTY_RECALL"), "useBlockPartyRecall@", true);
      NpcHtmlMessage htm = new NpcHtmlMessage(6);
      htm.setFile(activeChar, activeChar.getLang(), "data/html/mods/menu.htm");
      htm.replace("%lang%", language.equals("") ? "" : language);
      htm.replace("%autoLoot%", autoloot.equals("") ? "" : autoloot);
      htm.replace("%autoHerbs%", autolootHerbs.equals("") ? "" : autolootHerbs);
      htm.replace("%blockExp%", blockExp.equals("") ? "" : blockExp);
      htm.replace("%blockbuffs%", blockBuffs.equals("") ? "" : blockBuffs);
      htm.replace("%hidetraders%", hideTrades.equals("") ? "" : hideTrades);
      htm.replace("%hidebuffs%", hideBuffs.equals("") ? "" : hideBuffs);
      htm.replace("%blocktrades%", blockTrades.equals("") ? "" : blockTrades);
      htm.replace("%blockpartys%", blockPartys.equals("") ? "" : blockPartys);
      htm.replace("%blockfriends%", blockFriends.equals("") ? "" : blockFriends);
      htm.replace("%blockPartyRecall%", useBlockPartyRecall.equals("") ? "" : useBlockPartyRecall);
      htm.replace("%blockVisual%", blockVisual.equals("") ? "" : blockVisual);
      htm.replace("%noCarrier%", noCarrier.equals("") ? "" : noCarrier);
      activeChar.sendPacket(htm);
   }

   private static String getBooleanFrame(Player player, String configTitle, String editCmd, String editeVar, String playerEditeVar, int defaultVar) {
      String info = "<tr>";
      info = info + "<td width=5></td>";
      info = info + "<td width=180>" + configTitle + ":</td>";
      if (editCmd == null || editCmd.isEmpty()) {
         info = info
            + "<td aling=center width=41><font color=c1b33a>"
            + player.getVarInt(playerEditeVar, defaultVar)
            + " "
            + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_SEC")
            + "</font></td>";
         info = info
            + "<td width=40><button width=40 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu edit_var "
            + editeVar
            + "\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "Menu.EDIT")
            + "\"></td>";
      } else if (editCmd.equals("editLogout")) {
         info = info + "<td width=41><edit var=\"" + editCmd + "\" width=34 height=12></td>";
         info = info
            + "<td width=40><button width=40 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_logout $editLogout\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "Menu.SAVE")
            + "\"></td>";
      }

      return info + "</tr>";
   }

   private String getBooleanFrame(Player activeChar, String configTitle, String configName, boolean allowtomod) {
      String info = "<tr>";
      info = info + "<td width=5></td>";
      info = info + "<td width=180>" + configTitle + ":</td>";
      info = info + "<td width=41>" + (activeChar.getVarB(configName) ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>") + "</td>";
      if (allowtomod) {
         if (activeChar.getVarB(configName)) {
            info = info
               + "<td width=40><button width=40 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var "
               + configName
               + " 0\" value=\""
               + ServerStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_OFF")
               + "\"></td>";
         } else {
            info = info
               + "<td width=40><button width=40 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var "
               + configName
               + " 1\" value=\""
               + ServerStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_ON")
               + "\"></td>";
         }
      } else {
         info = info + "<td width=40 align=center>" + ServerStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_NOT_AVAILABLE") + "</td>";
      }

      return info + "</tr>";
   }

   private String getManualBooleanFrame(Player activeChar, String configTitle, boolean isON) {
      String info = "<tr>";
      info = info + "<td width=5></td>";
      info = info + "<td width=180>" + configTitle + ":</td>";
      info = info + "<td width=41>" + (isON ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>") + "</td>";
      info = info + "<td width=40 align=center>" + ServerStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_NOT_AVAILABLE") + "</td>";
      return info + "</tr>";
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
