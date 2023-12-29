package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Acp implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"acp"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String target) {
      if (!Config.AUTO_POINTS_SYSTEM) {
         return false;
      } else {
         if (command.equalsIgnoreCase("acp")) {
            showHtmlMenu(player, null);
         } else if (command.startsWith("acp")) {
            String[] params = command.split(" ");
            if (params.length == 4 && params[1].equalsIgnoreCase("set_var")) {
               player.setVar(params[2], params[3]);
               if (params[2].equalsIgnoreCase("useAutoHpPotions@")) {
                  if (params[3].equalsIgnoreCase("1")) {
                     player.startHpPotionTask();
                  } else {
                     player.stopHpPotionTask();
                  }
               } else if (params[2].equalsIgnoreCase("useAutoMpPotions@")) {
                  if (params[3].equalsIgnoreCase("1")) {
                     player.startMpPotionTask();
                  } else {
                     player.stopMpPotionTask();
                  }
               } else if (params[2].equalsIgnoreCase("useAutoCpPotions@")) {
                  if (params[3].equalsIgnoreCase("1")) {
                     player.startCpPotionTask();
                  } else {
                     player.stopCpPotionTask();
                  }
               } else if (params[2].equalsIgnoreCase("useAutoSoulPotions@")) {
                  if (params[3].equalsIgnoreCase("1")) {
                     player.startSoulPotionTask();
                  } else {
                     player.stopSoulPotionTask();
                  }
               }

               showHtmlMenu(player, null);
            } else if (params.length == 3 && params[1].equalsIgnoreCase("edit_var")) {
               showHtmlMenu(player, params[2]);
            } else if (params.length == 3
               && (
                  params[1].equalsIgnoreCase("set_hp")
                     || params[1].equalsIgnoreCase("set_mp")
                     || params[1].equalsIgnoreCase("set_cp")
                     || params[1].equalsIgnoreCase("set_soul")
               )) {
               String percent = null;

               try {
                  percent = params[2];
               } catch (Exception var10) {
               }

               if (percent != null) {
                  int per = 0;

                  try {
                     per = Integer.parseInt(percent);
                     if (per > 100) {
                        per = 100;
                     }

                     if (per < 0) {
                        per = 0;
                     }
                  } catch (NumberFormatException var11) {
                     if (params[1].equalsIgnoreCase("set_hp")) {
                        per = player.getVarInt("hpPercent", Config.DEFAULT_HP_PERCENT);
                     } else if (params[1].equalsIgnoreCase("set_mp")) {
                        per = player.getVarInt("mpPercent", Config.DEFAULT_MP_PERCENT);
                     } else if (params[1].equalsIgnoreCase("set_cp")) {
                        per = player.getVarInt("cpPercent", Config.DEFAULT_CP_PERCENT);
                     } else if (params[1].equalsIgnoreCase("set_soul")) {
                        per = player.getVarInt("soulPercent", Config.DEFAULT_SOUL_AMOUNT);
                     }
                  }

                  if (params[1].equalsIgnoreCase("set_hp")) {
                     player.setVar("hpPercent", per);
                  } else if (params[1].equalsIgnoreCase("set_mp")) {
                     player.setVar("mpPercent", per);
                  } else if (params[1].equalsIgnoreCase("set_cp")) {
                     player.setVar("cpPercent", per);
                  } else if (params[1].equalsIgnoreCase("set_soul")) {
                     player.setVar("soulPercent", per);
                  }
               }

               showHtmlMenu(player, null);
            } else if (params.length != 2
               || !params[1].equalsIgnoreCase("set_itemHp")
                  && !params[1].equalsIgnoreCase("set_itemMp")
                  && !params[1].equalsIgnoreCase("set_itemCp")
                  && !params[1].equalsIgnoreCase("set_itemSoul")) {
               if (params.length == 3
                  && (
                     params[1].equalsIgnoreCase("set_hpPotion")
                        || params[1].equalsIgnoreCase("set_mpPotion")
                        || params[1].equalsIgnoreCase("set_cpPotion")
                        || params[1].equalsIgnoreCase("set_soulPotion")
                  )) {
                  String item = null;

                  try {
                     item = params[2];
                  } catch (Exception var9) {
                  }

                  if (item != null) {
                     int itemId = 0;

                     try {
                        itemId = Integer.parseInt(item);
                     } catch (NumberFormatException var8) {
                     }

                     if (itemId == 0) {
                        return false;
                     }

                     if (params[1].equalsIgnoreCase("set_hpPotion")) {
                        player.setVar("autoHpItemId", itemId);
                     } else if (params[1].equalsIgnoreCase("set_mpPotion")) {
                        player.setVar("autoMpItemId", itemId);
                     } else if (params[1].equalsIgnoreCase("set_cpPotion")) {
                        player.setVar("autoCpItemId", itemId);
                     } else if (params[1].equalsIgnoreCase("set_soulPotion")) {
                        player.setVar("autoSoulItemId", itemId);
                     }
                  }

                  showHtmlMenu(player, null);
               } else {
                  showHtmlMenu(player, null);
               }
            } else if (params[1].equalsIgnoreCase("set_itemHp")) {
               getPotions(player, Config.AUTO_HP_VALID_ITEMS, "set_hpPotion");
            } else if (params[1].equalsIgnoreCase("set_itemMp")) {
               getPotions(player, Config.AUTO_MP_VALID_ITEMS, "set_mpPotion");
            } else if (params[1].equalsIgnoreCase("set_itemCp")) {
               getPotions(player, Config.AUTO_CP_VALID_ITEMS, "set_cpPotion");
            } else if (params[1].equalsIgnoreCase("set_itemSoul")) {
               getPotions(player, Config.AUTO_SOUL_VALID_ITEMS, "set_soulPotion");
            }
         }

         return true;
      }
   }

   private static void showHtmlMenu(Player player, String editCmd) {
      String autoHpPotions = "";
      String autoMpPotions = "";
      String autoCpPotions = "";
      String autoSoulPotions = "";
      autoHpPotions = autoHpPotions
         + getBooleanFrame(
            player,
            player.getVarInt("autoHpItemId", 0),
            "useAutoHpPotions@",
            editCmd != null && editCmd.equals("editHp") ? editCmd : null,
            "editHp",
            "hpPercent",
            Config.DEFAULT_HP_PERCENT
         );
      autoMpPotions = autoMpPotions
         + getBooleanFrame(
            player,
            player.getVarInt("autoMpItemId", 0),
            "useAutoMpPotions@",
            editCmd != null && editCmd.equals("editMp") ? editCmd : null,
            "editMp",
            "mpPercent",
            Config.DEFAULT_MP_PERCENT
         );
      autoCpPotions = autoCpPotions
         + getBooleanFrame(
            player,
            player.getVarInt("autoCpItemId", 0),
            "useAutoCpPotions@",
            editCmd != null && editCmd.equals("editCp") ? editCmd : null,
            "editCp",
            "cpPercent",
            Config.DEFAULT_CP_PERCENT
         );
      autoSoulPotions = autoSoulPotions
         + getBooleanFrame(
            player,
            player.getVarInt("autoSoulItemId", 0),
            "useAutoSoulPotions@",
            editCmd != null && editCmd.equals("editSoul") ? editCmd : null,
            "editSoul",
            "soulPercent",
            Config.DEFAULT_SOUL_AMOUNT
         );
      NpcHtmlMessage htm = new NpcHtmlMessage(6);
      htm.setFile(player, player.getLang(), "data/html/mods/acp.htm");
      htm.replace("%autoHpPotions%", autoHpPotions.equals("") ? "" : autoHpPotions);
      htm.replace("%autoMpPotions%", autoMpPotions.equals("") ? "" : autoMpPotions);
      htm.replace("%autoCpPotions%", autoCpPotions.equals("") ? "" : autoCpPotions);
      if ((int)player.calcStat(Stats.MAX_SOULS, 0.0, null, null) > 0) {
         htm.replace("%autoSoulPotions%", autoSoulPotions.equals("") ? "" : autoSoulPotions);
      } else {
         htm.replace("%autoSoulPotions%", "");
      }

      player.sendPacket(htm);
   }

   private static String getBooleanFrame(Player player, int itemId, String configName, String editCmd, String editeVar, String playerEditeVar, int defaultVar) {
      String info = "<tr>";
      info = info + "<td width=2></td>";
      Item item = ItemsParser.getInstance().getTemplate(itemId);
      if (editeVar.equals("editHp")) {
         info = info
            + "<td width=36><table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\""
            + getItemIcon(item)
            + "\"><tr><td><button action=\"bypass -h voiced_acp set_itemHp\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"></td></tr></table></td>";
         if (item != null) {
            info = info + "<td width=80><font color=c1b33a name=hs12>x" + getPotionAmount(player, itemId) + "</font></td>";
         } else {
            info = info + "<td width=80>" + ServerStorage.getInstance().getString(player.getLang(), "Menu.SELECT_HP") + "</td>";
         }
      } else if (editeVar.equals("editMp")) {
         info = info
            + "<td width=36><table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\""
            + getItemIcon(item)
            + "\"><tr><td><button action=\"bypass -h voiced_acp set_itemMp\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"></td></tr></table></td>";
         if (item != null) {
            info = info + "<td width=80><font color=c1b33a name=hs12>x" + getPotionAmount(player, itemId) + "</font></td>";
         } else {
            info = info + "<td width=80>" + ServerStorage.getInstance().getString(player.getLang(), "Menu.SELECT_MP") + "</td>";
         }
      } else if (editeVar.equals("editCp")) {
         info = info
            + "<td width=36><table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\""
            + getItemIcon(item)
            + "\"><tr><td><button action=\"bypass -h voiced_acp set_itemCp\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"></td></tr></table></td>";
         if (item != null) {
            info = info + "<td width=80><font color=c1b33a name=hs12>x" + getPotionAmount(player, itemId) + "</font></td>";
         } else {
            info = info + "<td width=80>" + ServerStorage.getInstance().getString(player.getLang(), "Menu.SELECT_CP") + "</td>";
         }
      } else if (editeVar.equals("editSoul")) {
         info = info
            + "<td width=36><table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\""
            + getItemIcon(item)
            + "\"><tr><td><button action=\"bypass -h voiced_acp set_itemSoul\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"></td></tr></table></td>";
         if (item != null) {
            info = info + "<td width=80><font color=c1b33a name=hs12>x" + getPotionAmount(player, itemId) + "</font></td>";
         } else {
            info = info + "<td width=80>" + ServerStorage.getInstance().getString(player.getLang(), "Menu.SELECT_SOUL") + "</td>";
         }
      }

      info = info + "<td width=41>" + (player.getVarB(configName) ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>") + "</td>";
      if (editCmd != null && !editCmd.isEmpty()) {
         if (editCmd.equals("editHp")) {
            info = info + "<td width=40><br><edit var=\"" + editCmd + "\" width=34 height=12></td>";
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_hp $editHp\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.SAVE")
               + "\"></td>";
            if (player.getVarB(configName)) {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 0\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_OFF")
                  + "\"></td>";
            } else {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 1\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_ON")
                  + "\"></td>";
            }
         } else if (editCmd.equals("editMp")) {
            info = info + "<td width=40><br><edit var=\"" + editCmd + "\" width=34 height=12></td>";
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_mp $editMp\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.SAVE")
               + "\"></td>";
            if (player.getVarB(configName)) {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 0\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_OFF")
                  + "\"></td>";
            } else {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 1\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_ON")
                  + "\"></td>";
            }
         } else if (editCmd.equals("editCp")) {
            info = info + "<td width=40><br><edit var=\"" + editCmd + "\" width=34 height=12></td>";
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_cp $editCp\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.SAVE")
               + "\"></td>";
            if (player.getVarB(configName)) {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 0\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_OFF")
                  + "\"></td>";
            } else {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 1\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_ON")
                  + "\"></td>";
            }
         } else if (editCmd.equals("editSoul")) {
            info = info + "<td width=40><br><edit var=\"" + editCmd + "\" width=34 height=12></td>";
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_soul $editSoul\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.SAVE")
               + "\"></td>";
            if (player.getVarB(configName)) {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 0\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_OFF")
                  + "\"></td>";
            } else {
               info = info
                  + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
                  + configName
                  + " 1\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_ON")
                  + "\"></td>";
            }
         }
      } else {
         if (playerEditeVar.equalsIgnoreCase("soulPercent")) {
            info = info + "<td aling=center width=40><font color=c1b33a>" + player.getVarInt(playerEditeVar, defaultVar) + "</font></td>";
         } else {
            info = info + "<td aling=center width=40><font color=c1b33a>" + player.getVarInt(playerEditeVar, defaultVar) + "%</font></td>";
         }

         info = info
            + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp edit_var "
            + editeVar
            + "\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "Menu.EDIT")
            + "\"></td>";
         if (player.getVarB(configName)) {
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
               + configName
               + " 0\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_OFF")
               + "\"></td>";
         } else {
            info = info
               + "<td width=40><br><button width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_acp set_var "
               + configName
               + " 1\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "Menu.STRING_ON")
               + "\"></td>";
         }
      }

      return info + "</tr>";
   }

   private static CharSequence getItemIcon(Item item) {
      return item != null ? item.getIcon() : "icon.etc_question_mark_i00";
   }

   public static long getPotionAmount(Player player, int itemId) {
      for(ItemInstance item : player.getInventory().getItems()) {
         if (item != null && item.getId() == itemId) {
            return item.getCount();
         }
      }

      return 0L;
   }

   public static void getPotions(Player player, List<Integer> validItems, String cmd) {
      List<ItemInstance> potions = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if (item != null && validItems.contains(item.getId())) {
            potions.add(item);
         }
      }

      if (!potions.isEmpty()) {
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/acp-items.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/acp-itemTemplate.htm");
         String block = "";
         String list = "";

         for(ItemInstance item : potions) {
            if (item != null) {
               block = template.replace("%bypass%", "bypass -h voiced_acp " + cmd + " " + item.getId());
               block = block.replace("%name%", player.getItemName(item.getItem()));
               block = block.replace("%icon%", Util.getItemIcon(item.getId()));
               list = list + block;
            }
         }

         html = html.replace("{list}", list);
         Util.setHtml(html, player);
      } else {
         player.sendMessage(new ServerMessage("Menu.HAVE_NO_POTIONS", player.getLang()).toString());
         showHtmlMenu(player, null);
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
