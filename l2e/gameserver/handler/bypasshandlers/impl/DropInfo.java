package l2e.gameserver.handler.bypasshandlers.impl;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.reward.CalculateRewardChances;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class DropInfo implements IBypassHandler {
   private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
   private static final String[] COMMANDS = new String[]{"drop", "spoil", "info"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      try {
         NpcHtmlMessage htm = new NpcHtmlMessage(5);
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         String var7 = actualCommand.toLowerCase();
         switch(var7) {
            case "drop":
               try {
                  NpcTemplate tpl = null;
                  String npcId = null;
                  ChampionTemplate championTemplate = null;
                  Npc npc = null;
                  int page = Integer.parseInt(st.nextToken());

                  try {
                     npcId = st.nextToken();
                  } catch (Exception var30) {
                  }

                  if (npcId != null) {
                     tpl = NpcsParser.getInstance().getTemplate(Integer.parseInt(npcId));
                  } else {
                     GameObject targetmob = activeChar.getTarget();
                     npc = (Npc)targetmob;
                     tpl = npc.getTemplate();
                     championTemplate = npc.getChampionTemplate();
                  }

                  if (tpl.getRewards().isEmpty()) {
                     htm.setFile(activeChar, activeChar.getLang(), "data/html/rewardlist_empty.htm");
                     htm.replace("%npc_name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getName());
                     activeChar.sendPacket(htm);
                     return false;
                  }

                  String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/rewardlist_info.htm");
                  String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/rewardlist_info_template.htm");
                  String block = "";
                  String list = "";
                  double penaltyMod = npc != null
                     ? ExperienceParser.getInstance().penaltyModifier((long)npc.calculateLevelDiffForDrop(activeChar.getLevel()), 9.0)
                     : 1.0;
                  List<CalculateRewardChances.DropInfoTemplate> allItems = CalculateRewardChances.getAmountAndChance(
                     activeChar, tpl, penaltyMod, true, championTemplate
                  );
                  if (allItems.isEmpty()) {
                     htm.setFile(activeChar, activeChar.getLang(), "data/html/rewardlist_empty.htm");
                     htm.replace("%npc_name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getName());
                     activeChar.sendPacket(htm);
                     return false;
                  }

                  Comparator<CalculateRewardChances.DropInfoTemplate> statsComparator = new DropInfo.SortDropInfo();
                  Collections.sort(allItems, statsComparator);
                  int perpage = 7;
                  int counter = 0;
                  int totalSize = allItems.size();
                  boolean isThereNextPage = totalSize > 7;

                  for(int i = (page - 1) * 7; i < totalSize; ++i) {
                     CalculateRewardChances.DropInfoTemplate data = allItems.get(i);
                     if (data != null) {
                        String icon = data._item.getItem().getIcon();
                        if (icon == null || icon.equals("")) {
                           icon = "icon.etc_question_mark_i00";
                        }

                        block = template.replace("{name}", activeChar.getItemName(data._item.getItem()));
                        block = block.replace("{icon}", icon);
                        block = block.replace(
                           "{count}", data._maxCount > data._minCount ? "" + data._minCount + " - " + data._maxCount + "" : String.valueOf(data._minCount)
                        );
                        block = block.replace("{chance}", pf.format(data._chance));
                        list = list + block;
                     }

                     if (++counter >= 7) {
                        break;
                     }
                  }

                  double pages = (double)totalSize / 7.0;
                  int count = (int)Math.ceil(pages);
                  html = html.replace("{list}", list);
                  if (npcId != null) {
                     html = html.replace(
                        "{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "drop %s " + Integer.parseInt(npcId) + "")
                     );
                  } else {
                     html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "drop %s"));
                  }

                  html = html.replace(
                     "%npc_name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getName()
                  );
                  allItems.clear();
                  Util.setHtml(html, activeChar);
               } catch (Exception var32) {
                  activeChar.sendMessage("Something went wrong with the drop preview.");
               }
               break;
            case "spoil":
               try {
                  NpcTemplate tpl = null;
                  String npcId = null;
                  Npc npc = null;
                  ChampionTemplate championTemplate = null;
                  int page = Integer.parseInt(st.nextToken());

                  try {
                     npcId = st.nextToken();
                  } catch (Exception var29) {
                  }

                  if (npcId != null) {
                     tpl = NpcsParser.getInstance().getTemplate(Integer.parseInt(npcId));
                  } else {
                     GameObject targetmob = activeChar.getTarget();
                     npc = (Npc)targetmob;
                     tpl = npc.getTemplate();
                     championTemplate = npc.getChampionTemplate();
                  }

                  double penaltyMod = npc != null
                     ? ExperienceParser.getInstance().penaltyModifier((long)npc.calculateLevelDiffForDrop(activeChar.getLevel()), 9.0)
                     : 1.0;
                  List<CalculateRewardChances.DropInfoTemplate> allItems = CalculateRewardChances.getAmountAndChance(
                     activeChar, tpl, penaltyMod, false, championTemplate
                  );
                  if (allItems.isEmpty()) {
                     htm.setFile(activeChar, activeChar.getLang(), "data/html/spoillist_empty.htm");
                     htm.replace("%npc_name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getName());
                     activeChar.sendPacket(htm);
                     return false;
                  }

                  Comparator<CalculateRewardChances.DropInfoTemplate> statsComparator = new DropInfo.SortDropInfo();
                  Collections.sort(allItems, statsComparator);
                  String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/rewardlist_info.htm");
                  String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/rewardlist_info_template.htm");
                  String block = "";
                  String list = "";
                  int perpage = 7;
                  int counter = 0;
                  int totalSize = allItems.size();
                  boolean isThereNextPage = totalSize > 7;

                  for(int i = (page - 1) * 7; i < totalSize; ++i) {
                     CalculateRewardChances.DropInfoTemplate data = allItems.get(i);
                     if (data != null) {
                        String icon = data._item.getItem().getIcon();
                        if (icon == null || icon.equals("")) {
                           icon = "icon.etc_question_mark_i00";
                        }

                        block = template.replace("{name}", activeChar.getItemName(data._item.getItem()));
                        block = block.replace("{icon}", icon);
                        block = block.replace(
                           "{count}", data._maxCount > data._minCount ? "" + data._minCount + " - " + data._maxCount + "" : String.valueOf(data._minCount)
                        );
                        block = block.replace("{chance}", pf.format(data._chance));
                        list = list + block;
                     }

                     if (++counter >= 7) {
                        break;
                     }
                  }

                  double pages = (double)totalSize / 7.0;
                  int count = (int)Math.ceil(pages);
                  html = html.replace("{list}", list);
                  if (npcId != null) {
                     html = html.replace(
                        "{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "spoil %s " + Integer.parseInt(npcId) + "")
                     );
                  } else {
                     html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "spoil %s"));
                  }

                  html = html.replace(
                     "%npc_name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getName()
                  );
                  allItems.clear();
                  Util.setHtml(html, activeChar);
               } catch (Exception var31) {
                  activeChar.sendMessage("Something went wrong with the spoil preview.");
               }
         }
      } catch (Exception var33) {
         activeChar.sendMessage("You cant use this option with this target.");
      }

      return false;
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }

   static {
      pf.setMaximumFractionDigits(4);
   }

   private static class SortDropInfo implements Comparator<CalculateRewardChances.DropInfoTemplate>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortDropInfo() {
      }

      public int compare(CalculateRewardChances.DropInfoTemplate o1, CalculateRewardChances.DropInfoTemplate o2) {
         return Double.compare(o2._chance, o1._chance);
      }
   }
}
