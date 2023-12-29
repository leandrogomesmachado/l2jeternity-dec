package l2e.gameserver.model.entity.mods;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.HtmlUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterSellBuffsDAO;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SellBuffsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SellBuffHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.ExPrivateStorePackageMsg;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class SellBuffsManager {
   protected SellBuffsManager() {
   }

   public static void sendSellMenu(Player player) {
      String html = HtmCache.getInstance()
         .getHtm(player, player.getLang(), "data/html/mods/sellBuffs/" + (player.isSellingBuffs() ? "already.htm" : "buffmenu.htm"));
      NpcHtmlMessage htm = new NpcHtmlMessage(0);
      htm.setHtml(player, html.toString());
      player.sendPacket(htm);
   }

   public static void sendBuffChoiceMenu(Player player, int page) {
      List<Skill> skillList = new ArrayList<>();

      for(Skill skill : player.getAllSkills()) {
         if (skill != null && SellBuffsParser.getInstance().getSellBuffs().contains(skill.getId()) && !isInSellList(player, skill)) {
            skillList.add(skill);
         }
      }

      if (skillList.isEmpty()) {
         player.sendMessage(new ServerMessage("SellBuff.NO_BUFFS", player.getLang()).toString());
      } else {
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/choice.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/choice-template.htm");
         String block = "";
         String list = "";
         int perpage = 5;
         int counter = 0;
         int curPage = page;
         int totalSize = skillList.size();
         boolean isThereNextPage = totalSize > 5;
         String currency = "";

         for(String itemName : Config.SELLBUFF_CURRECY_LIST.keySet()) {
            if (itemName != null && !itemName.isEmpty()) {
               currency = currency + itemName + ";";
            }
         }

         if (isThereNextPage && 5 * page - totalSize >= 5) {
            curPage = page - 1;
         }

         for(int i = (curPage - 1) * 5; i < totalSize; ++i) {
            Skill skill = skillList.get(i);
            if (skill != null) {
               block = template.replace("%name%", player.getSkillName(skill));
               block = block.replace("%icon%", skill.getIcon());
               block = block.replace(
                  "%level%",
                  skill.getLevel() > 100
                     ? "<font color=\"LEVEL\"> + " + skill.getLevel() % 100 + "</font>"
                     : "<font color=\"ae9977\">" + skill.getLevel() + "</font>"
               );
               block = block.replace("%currency%", "<combobox width=80 var=\"currency_" + skill.getId() + "\" list=\"" + currency + "\">");
               block = block.replace("%editVar%", "<edit var=\"price_" + skill.getId() + "\" width=60 height=10 type=\"number\">");
               block = block.replace(
                  "%bypass%", "sellbuffaddskill " + skill.getId() + " $price_" + skill.getId() + " $currency_" + skill.getId() + " " + curPage + ""
               );
               list = list + block;
            }

            if (++counter >= 5) {
               break;
            }
         }

         int count = (int)Math.ceil((double)totalSize / 5.0);
         html = html.replace("{list}", list);
         html = html.replace("{navigation}", Util.getNavigationBlock(count, curPage, totalSize, 5, isThereNextPage, "sellbuffadd %s"));
         Util.setHtml(html, player);
      }
   }

   public static void sendBuffEditMenu(Player player, int page) {
      if (player.getSellingBuffs() != null && !player.getSellingBuffs().isEmpty()) {
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/choice.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/choice-editTemplate.htm");
         String block = "";
         String list = "";
         List<Skill> sellList = new ArrayList<>();

         for(SellBuffHolder holder : player.getSellingBuffs()) {
            Skill skill = player.getKnownSkill(holder.getId());
            if (skill != null) {
               sellList.add(skill);
            }
         }

         if (sellList != null && !sellList.isEmpty()) {
            int perpage = 5;
            int counter = 0;
            int curPage = page;
            int totalSize = sellList.size();
            boolean isThereNextPage = totalSize > 5;
            String currency = "";

            for(String itemName : Config.SELLBUFF_CURRECY_LIST.keySet()) {
               if (itemName != null && !itemName.isEmpty()) {
                  currency = currency + itemName + ";";
               }
            }

            if (isThereNextPage && 5 * page - totalSize >= 5) {
               curPage = page - 1;
            }

            for(int i = (curPage - 1) * 5; i < totalSize; ++i) {
               Skill skill = sellList.get(i);
               if (skill != null) {
                  block = template.replace("%name%", player.getSkillName(skill));
                  block = block.replace("%icon%", skill.getIcon());
                  block = block.replace(
                     "%level%",
                     skill.getLevel() > 100
                        ? "<font color=\"LEVEL\"> + " + skill.getLevel() % 100 + "</font>"
                        : "<font color=\"ae9977\">" + skill.getLevel() + "</font>"
                  );
                  block = block.replace("%itemId%", buffItemId(player, player, skill.getId()));
                  block = block.replace("%price%", Util.formatAdena((long)buffPrice(player, skill.getId())));
                  block = block.replace("%editVar%", "<edit var=\"price_" + skill.getId() + "\" width=60 height=10 type=\"number\">");
                  block = block.replace("%currency%", "<combobox width=80 var=\"currency_" + skill.getId() + "\" list=\"" + currency + "\">");
                  block = block.replace(
                     "%bypass%", "sellbuffchangeprice " + skill.getId() + " $price_" + skill.getId() + " $currency_" + skill.getId() + " " + curPage + ""
                  );
                  block = block.replace("%delBypass%", "sellbuffremove " + skill.getId() + " " + curPage + "");
                  list = list + block;
               }

               if (++counter >= 5) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)totalSize / 5.0);
            html = html.replace("{list}", list);
            html = html.replace("{navigation}", Util.getNavigationBlock(count, curPage, totalSize, 5, isThereNextPage, "sellbuffedit %s"));
            Util.setHtml(html, player);
         } else {
            player.sendMessage(new ServerMessage("SellBuff.NO_BUFFS", player.getLang()).toString());
         }
      } else {
         player.sendMessage(new ServerMessage("SellBuff.NO_BUFFS", player.getLang()).toString());
      }
   }

   public static void sendBuffMenu(Player player, Player seller, int page) {
      if (seller.isSellingBuffs() && !seller.getSellingBuffs().isEmpty()) {
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/buymenu.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/sellBuffs/template.htm");
         String block = "";
         String list = "";
         List<Skill> sellList = new ArrayList<>();

         for(SellBuffHolder holder : seller.getSellingBuffs()) {
            Skill skill = seller.getKnownSkill(holder.getId());
            if (skill != null) {
               sellList.add(skill);
            }
         }

         int perpage = 6;
         int counter = 0;
         int totalSize = sellList.size();
         boolean isThereNextPage = totalSize > 6;

         for(int i = (page - 1) * 6; i < totalSize; ++i) {
            Skill skill = sellList.get(i);
            if (skill != null) {
               block = template.replace("%name%", player.getSkillName(skill));
               block = block.replace("%icon%", skill.getIcon());
               block = block.replace(
                  "%level%",
                  skill.getLevel() > 100
                     ? "<font color=\"LEVEL\"> + " + skill.getLevel() % 100 + "</font>"
                     : "<font color=\"ae9977\">" + skill.getLevel() + "</font>"
               );
               block = block.replace("%item%", buffItemId(player, seller, skill.getId()));
               block = block.replace("%amount%", Util.formatAdena((long)buffPrice(seller, skill.getId())));
               block = block.replace("%mpAmount%", Config.SELLBUFF_USED_MP ? "(<font color=\"1E90FF\">" + skill.getMpConsume() + " MP</font>)" : "");
               block = block.replace("%bypass%", "sellbuffbuyskill " + seller.getObjectId() + " " + skill.getId() + " " + page + "");
               list = list + block;
            }

            if (++counter >= 6) {
               break;
            }
         }

         int count = (int)Math.ceil((double)totalSize / 6.0);
         if (Config.SELLBUFF_USED_MP) {
            html = html.replace("%mp%", "<br>" + HtmlUtil.getMpGauge(250, (long)seller.getCurrentMp(), (long)((int)seller.getMaxMp()), false) + " <br>");
         } else {
            html = html.replace("%mp%", "");
         }

         html = html.replace("{list}", list);
         html = html.replace(
            "{navigation}", Util.getNavigationBlock(count, page, totalSize, 6, isThereNextPage, "sellbuffbuymenu " + seller.getObjectId() + " %s")
         );
         Util.setHtml(html, player);
      }
   }

   public static int buffPrice(Player seller, int skillId) {
      for(SellBuffHolder holder : seller.getSellingBuffs()) {
         if (holder.getId() == skillId) {
            return (int)holder.getPrice();
         }
      }

      return 0;
   }

   public static String buffItemId(Player player, Player seller, int skillId) {
      for(SellBuffHolder holder : seller.getSellingBuffs()) {
         if (holder.getId() == skillId) {
            return Util.getItemName(player, holder.getItemId());
         }
      }

      return "";
   }

   public static void startSellBuffs(Player player, String title) {
      player.sitDown();
      player.setIsSellingBuffs(true);
      CharacterSellBuffsDAO.getInstance().saveSellBuffList(player);
      player.setPrivateStoreType(8);
      player.setIsInStoreNow(true);
      player.getSellList().setTitle(title);
      player.setVar("sellstorename", player.getSellList().getTitle(), -1L);
      player.getSellList().setPackaged(true);
      player.broadcastCharInfo();
      player.broadcastPacket(new ExPrivateStorePackageMsg(player));
      sendSellMenu(player);
   }

   public static void stopSellBuffs(Player player) {
      player.setIsSellingBuffs(false);
      player.setPrivateStoreType(0);
      CharacterSellBuffsDAO.getInstance().cleanSellBuffList(player);
      player.standUp();
      player.broadcastCharInfo();
      sendSellMenu(player);
   }

   public static boolean isInSellList(Player player, Skill skill) {
      for(SellBuffHolder holder : player.getSellingBuffs()) {
         if (holder != null && holder.getId() == skill.getId()) {
            return true;
         }
      }

      return false;
   }
}
