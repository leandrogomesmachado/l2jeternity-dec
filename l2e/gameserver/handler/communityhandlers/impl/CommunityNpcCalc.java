package l2e.gameserver.handler.communityhandlers.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.htm.ImagesCache;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.handler.bypasshandlers.BypassHandler;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.reward.CalculateRewardChances;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.RadarControl;

public class CommunityNpcCalc extends AbstractCommunity implements ICommunityBoardHandler {
   private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);

   public CommunityNpcCalc() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all drop calculator functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "_bbsdropCalc",
         "_bbsdropItemsByName",
         "_bbsdropMonstersByItem",
         "_bbsdropMonsterDetailsByItem",
         "_bbsdropMonstersByName",
         "_bbsdropMonsterDetailsByName"
      };
   }

   @Override
   public void onBypassCommand(String bypass, Player player) {
      StringTokenizer st = new StringTokenizer(bypass, "_");
      String cmd = st.nextToken();
      switch(cmd) {
         case "bbsdropCalc":
            showMainPage(player);
            break;
         case "bbsdropItemsByName":
            if (!st.hasMoreTokens()) {
               showMainPage(player);
               return;
            }

            String itemName = "";

            while(st.countTokens() > 1) {
               itemName = itemName + " " + st.nextToken();
            }

            int itemsPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            showDropItemsByNamePage(player, itemName.trim(), itemsPage);
            break;
         case "bbsdropMonstersByItem":
            int itemId = Integer.parseInt(st.nextToken());
            int monstersPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            showDropMonstersByItem(player, itemId, monstersPage);
            break;
         case "bbsdropMonsterDetailsByItem":
            int monsterId = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
               manageButton(player, Integer.parseInt(st.nextToken()), monsterId);
            }

            showdropMonsterDetailsByItem(player, monsterId);
            break;
         case "bbsdropMonstersByName":
            if (!st.hasMoreTokens()) {
               showMainPage(player);
               return;
            }

            String monsterName = "";

            while(st.countTokens() > 1) {
               monsterName = monsterName + " " + st.nextToken();
            }

            int monsterPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            showDropMonstersByName(player, monsterName.trim(), monsterPage);
            break;
         case "bbsdropMonsterDetailsByName":
            int chosenMobId = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
               manageButton(player, Integer.parseInt(st.nextToken()), chosenMobId);
            }

            showDropMonsterDetailsByName(player, chosenMobId);
      }
   }

   private static void showMainPage(Player player) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropCalcMain.htm");
      separateAndSend(html, player);
   }

   private static void showDropItemsByNamePage(Player player, String itemName, int page) {
      player.addQuickVar("DCItemName", itemName);
      player.addQuickVar("DCItemsPage", page);
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropItemsByName.htm");
      html = replaceItemsByNamePage(player, html, itemName, page);
      separateAndSend(html, player);
   }

   private static String replaceItemsByNamePage(Player player, String html, String itemName, int page) {
      String newHtml = html;
      List<Item> itemsByName = ItemsParser.getInstance().getItemsByNameContainingString(itemName, true);
      itemsByName.sort(new CommunityNpcCalc.ItemComparator(itemName));
      int itemIndex = 0;

      for(int i = 0; i < 8; ++i) {
         itemIndex = i + (page - 1) * 8;
         Item item = itemsByName.size() > itemIndex ? itemsByName.get(itemIndex) : null;
         newHtml = newHtml.replace("%itemIcon" + i + '%', (CharSequence)(item != null ? getItemIcon(item) : "<br>"));
         newHtml = newHtml.replace("%itemName" + i + '%', (CharSequence)(item != null ? getName(player.getItemName(item)) : "<br>"));
         newHtml = newHtml.replace("%itemGrade" + i + '%', (CharSequence)(item != null ? getItemGradeIcon(item) : "<br>"));
         newHtml = newHtml.replace(
            "%dropLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getId(), true)) : "<br>"
         );
         newHtml = newHtml.replace(
            "%spoilLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getId(), false)) : "<br>"
         );
         newHtml = newHtml.replace(
            "%showMonsters" + i + '%',
            item != null
               ? "<button value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.SHOW_MONSTERS")
                  + "\" action=\"bypass _bbsdropMonstersByItem_%itemChosenId"
                  + i
                  + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
               : "<br>"
         );
         newHtml = newHtml.replace("%itemChosenId" + i + '%', item != null ? String.valueOf(item.getId()) : "<br>");
      }

      newHtml = newHtml.replace(
         "%previousButton%",
         page > 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.PREV")
               + "\" action=\"bypass _bbsdropItemsByName_"
               + itemName
               + "_"
               + (page - 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace(
         "%nextButton%",
         itemsByName.size() > itemIndex + 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.NEXT")
               + "\" action=\"bypass _bbsdropItemsByName_"
               + itemName
               + "_"
               + (page + 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace("%searchItem%", itemName);
      return newHtml.replace("%page%", String.valueOf(page));
   }

   private static void showDropMonstersByItem(Player player, int itemId, int page) {
      player.addQuickVar("DCItemId", itemId);
      player.addQuickVar("DCMonstersPage", page);
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropMonstersByItem.htm");
      html = replaceMonstersByItemPage(player, html, itemId, page);
      separateAndSend(html, player);
   }

   private static String replaceMonstersByItemPage(Player player, String html, int itemId, int page) {
      String newHtml = html;
      List<CalculateRewardChances.NpcTemplateDrops> templates = CalculateRewardChances.getNpcsByDropOrSpoil(itemId);
      templates.sort(new CommunityNpcCalc.ItemChanceComparator(player, itemId));
      int npcIndex = 0;

      for(int i = 0; i < 10; ++i) {
         npcIndex = i + (page - 1) * 10;
         CalculateRewardChances.NpcTemplateDrops drops = templates.size() > npcIndex ? templates.get(npcIndex) : null;
         NpcTemplate npc = templates.size() > npcIndex ? templates.get(npcIndex)._template : null;
         newHtml = newHtml.replace("%monsterName" + i + '%', (CharSequence)(npc != null ? getName(player.getNpcName(npc)) : "<br>"));
         newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.getLevel()) : "<br>");
         newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? Util.boolToString(player, npc.getAggroRange() > 0) : "<br>");
         newHtml = newHtml.replace(
            "%monsterType" + i + '%',
            npc != null
               ? (
                  drops._dropNoSpoil
                     ? "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.DROP") + ""
                     : "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.SPOIL") + ""
               )
               : "<br>"
         );
         newHtml = newHtml.replace("%monsterCount" + i + '%', npc != null ? String.valueOf(getDropCount(player, npc, itemId, drops._dropNoSpoil)) : "<br>");
         newHtml = newHtml.replace("%monsterChance" + i + '%', npc != null ? String.valueOf(getDropChance(player, npc, itemId, drops._dropNoSpoil)) : "<br>");
         newHtml = newHtml.replace(
            "%showDetails" + i + '%',
            npc != null
               ? "<button value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.SHOW_DETAILS")
                  + "\" action=\"bypass _bbsdropMonsterDetailsByItem_%monsterId"
                  + i
                  + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
               : "<br>"
         );
         newHtml = newHtml.replace("%monsterId" + i + '%', npc != null ? String.valueOf(npc.getId()) : "<br>");
      }

      newHtml = newHtml.replace(
         "%previousButton%",
         page > 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.PREV")
               + "\" action=\"bypass _bbsdropMonstersByItem_%itemChosenId%_"
               + (page - 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace(
         "%nextButton%",
         templates.size() > npcIndex + 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.NEXT")
               + "\" action=\"bypass _bbsdropMonstersByItem_%itemChosenId%_"
               + (page + 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace("%searchItem%", player.getQuickVarS("DCItemName"));
      newHtml = newHtml.replace("%searchItemPage%", String.valueOf(player.getQuickVarI("DCItemsPage")));
      newHtml = newHtml.replace("%itemChosenId%", String.valueOf(itemId));
      return newHtml.replace("%monsterPage%", String.valueOf(page));
   }

   private static void showdropMonsterDetailsByItem(Player player, int monsterId) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropMonsterDetailsByItem.htm");
      html = replaceMonsterDetails(player, html, monsterId);
      separateAndSend(html, player);
   }

   private static String replaceMonsterDetails(Player player, String html, int monsterId) {
      int itemId = player.getQuickVarI("DCItemId");
      NpcTemplate template = NpcsParser.getInstance().getTemplate(monsterId);
      if (template == null) {
         return html;
      } else {
         String newHtml = html.replace("%searchName%", String.valueOf(player.getQuickVarS("DCMonsterName")));
         newHtml = newHtml.replace("%itemChosenId%", String.valueOf(player.getQuickVarI("DCItemId")));
         newHtml = newHtml.replace("%monsterPage%", String.valueOf(player.getQuickVarI("DCMonstersPage")));
         newHtml = newHtml.replace("%monsterId%", String.valueOf(monsterId));
         newHtml = newHtml.replace("%monsterName%", getName(player.getNpcName(template)));
         newHtml = newHtml.replace("%monsterLevel%", String.valueOf(template.getLevel()));
         newHtml = newHtml.replace("%monsterAggro%", Util.boolToString(player, template.getAggroRange() > 0));
         if (itemId > 0) {
            newHtml = newHtml.replace("%monsterDropSpecific%", String.valueOf(getDropChance(player, template, itemId, true)));
            newHtml = newHtml.replace("%monsterSpoilSpecific%", String.valueOf(getDropChance(player, template, itemId, false)));
         }

         newHtml = newHtml.replace("%monsterDropAll%", String.valueOf(CalculateRewardChances.getDrops(template, true, false).size()));
         newHtml = newHtml.replace("%monsterSpoilAll%", String.valueOf(CalculateRewardChances.getDrops(template, false, true).size()));
         newHtml = newHtml.replace("%spawnCount%", String.valueOf(SpawnParser.getInstance().getSpawnedCountByNpc(monsterId)));
         newHtml = newHtml.replace("%minions%", String.valueOf(template.getMinionData().size()));
         newHtml = newHtml.replace("%expReward%", String.valueOf(template.getRewardExp()));
         newHtml = newHtml.replace("%maxHp%", String.valueOf((int)template.getBaseHpMax()));
         newHtml = newHtml.replace("%maxMP%", String.valueOf((int)template.getBaseMpMax()));
         newHtml = newHtml.replace("%pAtk%", String.valueOf(template.getBasePAtk()));
         newHtml = newHtml.replace("%mAtk%", String.valueOf(template.getBaseMAtk()));
         newHtml = newHtml.replace("%pDef%", String.valueOf(template.getBasePDef()));
         newHtml = newHtml.replace("%mDef%", String.valueOf(template.getBaseMDef()));
         newHtml = newHtml.replace("%atkSpd%", String.valueOf(template.getBasePAtkSpd()));
         newHtml = newHtml.replace("%castSpd%", String.valueOf(template.getBaseMAtkSpd()));
         newHtml = newHtml.replace("%runSpd%", String.valueOf((int)template.getBaseMoveSpeed(MoveType.RUN)));
         newHtml = newHtml.replace("%serverId%", String.valueOf(Config.REQUEST_ID));
         newHtml = newHtml.replace("%monsterId%", String.valueOf(monsterId));
         ImagesCache.getInstance().sendImageToPlayer(player, monsterId);
         return newHtml;
      }
   }

   private static void showDropMonstersByName(Player player, String monsterName, int page) {
      if (!monsterName.isEmpty() && (monsterName.equalsIgnoreCase("Treasure Chest") || monsterName.equalsIgnoreCase("Сундук с Сокровищами"))) {
         monsterName = monsterName.substring(0, monsterName.length() - 1);
      }

      player.addQuickVar("DCMonsterName", monsterName);
      player.addQuickVar("DCMonstersPage", page);
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropMonstersByName.htm");
      html = replaceMonstersByName(player, html, monsterName, page);
      separateAndSend(html, player);
   }

   private static String replaceMonstersByName(Player player, String html, String monsterName, int page) {
      String newHtml = html;
      List<NpcTemplate> npcTemplates = CalculateRewardChances.getNpcsContainingString(monsterName);
      npcTemplates = sortMonsters(npcTemplates, monsterName, player.getLang());
      int npcIndex = 0;

      for(int i = 0; i < 10; ++i) {
         npcIndex = i + (page - 1) * 10;
         NpcTemplate npc = npcTemplates.size() > npcIndex ? npcTemplates.get(npcIndex) : null;
         newHtml = newHtml.replace("%monsterName" + i + '%', (CharSequence)(npc != null ? getName(player.getNpcName(npc)) : "<br>"));
         newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.getLevel()) : "<br>");
         newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? Util.boolToString(player, npc.getAggroRange() > 0) : "<br>");
         newHtml = newHtml.replace("%monsterDrops" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, true, false).size()) : "<br>");
         newHtml = newHtml.replace("%monsterSpoils" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, false, true).size()) : "<br>");
         newHtml = newHtml.replace(
            "%showDetails" + i + '%',
            npc != null
               ? "< button value =\""
                  + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.SHOW_DETAILS")
                  + "\" action=\"bypass _bbsdropMonsterDetailsByName_"
                  + npc.getId()
                  + "\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
               : "<br>"
         );
      }

      newHtml = newHtml.replace(
         "%previousButton%",
         page > 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.PREV")
               + "\" action=\"bypass _bbsdropMonstersByName_%searchName%_"
               + (page - 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace(
         "%nextButton%",
         npcTemplates.size() > npcIndex + 1
            ? "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.NEXT")
               + "\" action=\"bypass _bbsdropMonstersByName_%searchName%_"
               + (page + 1)
               + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            : "<br>"
      );
      newHtml = newHtml.replace("%searchName%", monsterName);
      return newHtml.replace("%page%", String.valueOf(page));
   }

   private static void showDropMonsterDetailsByName(Player player, int monsterId) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/npc/dropMonsterDetailsByName.htm");
      html = replaceMonsterDetails(player, html, monsterId);
      separateAndSend(html, player);
   }

   private static void manageButton(Player player, int buttonId, int monsterId) {
      switch(buttonId) {
         case 1:
            List<Location> locs = SpawnParser.getInstance().getRandomSpawnsByNpc(monsterId);
            if (locs == null || locs.isEmpty()) {
               return;
            }

            for(int id : Config.BLOCKED_RAID_LIST) {
               if (id == monsterId) {
                  return;
               }
            }

            player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
            player.sendPacket(
               new CreatureSay(player.getObjectId(), 15, "", "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityNpcCalc.LOCATION") + "")
            );

            for(Location loc : locs) {
               player.sendPacket(new RadarControl(0, 1, loc.getX(), loc.getY(), loc.getZ()));
            }
            break;
         case 2:
            IBypassHandler handler = BypassHandler.getInstance().getHandler("drop");
            if (handler != null) {
               handler.useBypass("drop 1 " + monsterId + "", player, null);
            }
            break;
         case 3:
            IBypassHandler hndlr = BypassHandler.getInstance().getHandler("spoil");
            if (hndlr != null) {
               hndlr.useBypass("spoil 1 " + monsterId + "", player, null);
            }
      }
   }

   private static CharSequence getItemIcon(Item template) {
      return "<img src=\"" + template.getIcon() + "\" width=32 height=32>";
   }

   private static CharSequence getItemGradeIcon(Item template) {
      return template.getCrystalType() == 0
         ? ""
         : "<img src=\"L2UI_CT1.Icon_DF_ItemGrade_" + template.getItemsGrade(template.getCrystalType()).replace("S8", "8") + "\" width=16 height=16>";
   }

   private static CharSequence getName(String name) {
      return name.length() > 24 ? "</font><font color=c47e0f>" + name : name;
   }

   private static String getDropCount(Player player, NpcTemplate monster, int itemId, boolean drop) {
      double[] counts = CalculateRewardChances.getAmountAndChanceById(player, monster, 1.0, drop, itemId, null);
      String formattedCounts = "[" + counts[0] + "..." + counts[1] + ']';
      if (formattedCounts.length() > 20) {
         formattedCounts = "</font><font color=c47e0f>" + formattedCounts;
      }

      return formattedCounts;
   }

   private static String getDropChance(Player player, NpcTemplate monster, int itemId, boolean drop) {
      double[] chance = CalculateRewardChances.getAmountAndChanceById(player, monster, 1.0, drop, itemId, null);
      return pf.format(chance[2]);
   }

   public static String formatDropChance(String chance) {
      String realChance = chance;
      if (chance.length() - chance.indexOf(46) > 6) {
         realChance = chance.substring(0, chance.indexOf(46) + 7);
      }

      if (realChance.endsWith(".0")) {
         realChance = realChance.substring(0, realChance.length() - 2);
      }

      return realChance + '%';
   }

   private static List<NpcTemplate> sortMonsters(List<NpcTemplate> npcTemplates, String monsterName, String lang) {
      Collections.sort(npcTemplates, new CommunityNpcCalc.MonsterComparator(monsterName, lang));
      return npcTemplates;
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityNpcCalc getInstance() {
      return CommunityNpcCalc.SingletonHolder._instance;
   }

   static {
      pf.setMaximumFractionDigits(4);
   }

   private static class ItemChanceComparator implements Comparator<CalculateRewardChances.NpcTemplateDrops>, Serializable {
      private static final long serialVersionUID = 6323413829869254438L;
      private final int _itemId;
      private final Player _player;

      private ItemChanceComparator(Player player, int itemId) {
         this._itemId = itemId;
         this._player = player;
      }

      public int compare(CalculateRewardChances.NpcTemplateDrops o1, CalculateRewardChances.NpcTemplateDrops o2) {
         BigDecimal maxDrop1 = BigDecimal.valueOf(
            CalculateRewardChances.getAmountAndChanceById(this._player, o1._template, 1.0, o1._dropNoSpoil, this._itemId, null)[1]
         );
         BigDecimal maxDrop2 = BigDecimal.valueOf(
            CalculateRewardChances.getAmountAndChanceById(this._player, o2._template, 1.0, o2._dropNoSpoil, this._itemId, null)[1]
         );
         BigDecimal chance1 = new BigDecimal(
            CalculateRewardChances.getAmountAndChanceById(this._player, o1._template, 1.0, o1._dropNoSpoil, this._itemId, null)[2]
         );
         BigDecimal chance2 = new BigDecimal(
            CalculateRewardChances.getAmountAndChanceById(this._player, o2._template, 1.0, o2._dropNoSpoil, this._itemId, null)[2]
         );
         int compare = chance2.multiply(maxDrop2).compareTo(chance1.multiply(maxDrop1));
         if (compare != 0) {
            return compare;
         } else {
            return this._player.getLang() != null && !this._player.getLang().equalsIgnoreCase("en")
               ? o2._template.getNameRu().compareTo(o1._template.getNameRu())
               : o2._template.getName().compareTo(o1._template.getName());
         }
      }
   }

   private static class ItemComparator implements Comparator<Item>, Serializable {
      private static final long serialVersionUID = -6389059445439769861L;
      private final String _search;

      private ItemComparator(String search) {
         this._search = search;
      }

      public int compare(Item o1, Item o2) {
         if (o1.equals(o2)) {
            return 0;
         } else if (o1.getNameRu().equalsIgnoreCase(this._search) || o1.getNameEn().equalsIgnoreCase(this._search)) {
            return -1;
         } else {
            return !o2.getNameRu().equalsIgnoreCase(this._search) && !o2.getNameEn().equalsIgnoreCase(this._search)
               ? Integer.compare(
                  CalculateRewardChances.getDroplistsCountByItemId(o2.getId(), true), CalculateRewardChances.getDroplistsCountByItemId(o1.getId(), true)
               )
               : 1;
         }
      }
   }

   private static class MonsterComparator implements Comparator<NpcTemplate>, Serializable {
      private static final long serialVersionUID = 2116090903265145828L;
      private final String _search;
      private final String _lang;

      private MonsterComparator(String search, String lang) {
         this._search = search;
         this._lang = lang;
      }

      public int compare(NpcTemplate o1, NpcTemplate o2) {
         if (o1.equals(o2)) {
            return 0;
         } else if (o1.getName().equalsIgnoreCase(this._search) || o1.getNameRu().equalsIgnoreCase(this._search)) {
            return 1;
         } else if (!o2.getName().equalsIgnoreCase(this._search) && !o2.getNameRu().equalsIgnoreCase(this._search)) {
            return this._lang != null && !this._lang.equalsIgnoreCase("en") ? o2.getNameRu().compareTo(o2.getNameRu()) : o2.getName().compareTo(o2.getName());
         } else {
            return -1;
         }
      }
   }

   private static class SingletonHolder {
      protected static final CommunityNpcCalc _instance = new CommunityNpcCalc();
   }
}
