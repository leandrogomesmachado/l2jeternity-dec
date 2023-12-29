package l2e.gameserver.handler.communityhandlers.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.htm.ImagesCache;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.bypasshandlers.BypassHandler;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.impl.model.NpcUtils;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.RadarControl;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.apache.commons.lang3.StringUtils;

public class CommunityRaidBoss extends AbstractCommunity implements ICommunityBoardHandler {
   private static final int BOSSES_PER_PAGE = 10;

   public CommunityRaidBoss() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsboss", "_bbsepic", "_bbsbosslist", "_bbsepiclist"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      if (command.startsWith("_bbsbosslist")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();
         int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "1");
         int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
         sendBossListPage(player, getSortByIndex(sort), page, search);
      } else if (command.startsWith("_bbsepiclist")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();
         int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "1");
         int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
         sendEpicBossListPage(player, getSortByIndex(sort), page, search);
      } else if (command.startsWith("_bbsboss")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();
         int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "3");
         int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
         int bossId = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "25044");
         int buttonClick = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         manageButtons(player, buttonClick, bossId);
         if (buttonClick != 5) {
            sendBossDetails(player, getSortByIndex(sort), page, search, bossId);
         }
      } else if (command.startsWith("_bbsepic")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();
         int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "3");
         int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
         int bossId = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "25044");
         int buttonClick = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
         manageButtons(player, buttonClick, bossId);
         if (buttonClick != 5) {
            sendEpicBossDetails(player, getSortByIndex(sort), page, search, bossId);
         }
      }
   }

   private static void sendBossListPage(Player player, CommunityRaidBoss.SortType sort, int page, String search) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/bosses/boss_list.htm");
      Map<Integer, StatsSet> allBosses = getSearchedBosses(sort, search, player.getLang());
      Map<Integer, StatsSet> bossesToShow = getBossesToShow(allBosses, page);
      boolean isThereNextPage = allBosses.size() > bossesToShow.size();
      html = getBossListReplacements(player, html, page, bossesToShow, isThereNextPage);
      html = getNormalReplacements(html, page, sort, search, -1);
      separateAndSend(html, player);
   }

   private static void sendEpicBossListPage(Player player, CommunityRaidBoss.SortType sort, int page, String search) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/bosses/epic_list.htm");
      Map<Integer, StatsSet> allBosses = getSearchedEpicBosses(sort, search, player.getLang());
      Map<Integer, StatsSet> bossesToShow = getBossesToShow(allBosses, page);
      boolean isThereNextPage = allBosses.size() > bossesToShow.size();
      html = getEpicBossListReplacements(player, html, page, bossesToShow, isThereNextPage);
      html = getNormalReplacements(html, page, sort, search, -1);
      separateAndSend(html, player);
   }

   private static String getBossListReplacements(Player player, String html, int page, Map<Integer, StatsSet> allBosses, boolean nextPage) {
      String newHtml = html;
      int i = 0;

      for(Entry<Integer, StatsSet> entry : allBosses.entrySet()) {
         StatsSet boss = entry.getValue();
         NpcTemplate temp = NpcsParser.getInstance().getTemplate(entry.getKey());
         boolean isAlive = isBossAlive(boss);
         newHtml = newHtml.replace("<?name_" + i + "?>", player.getNpcName(temp));
         newHtml = newHtml.replace("<?level_" + i + "?>", String.valueOf(temp.getLevel()));
         newHtml = newHtml.replace(
            "<?status_" + i + "?>",
            isAlive
               ? "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.ALIVE") + ""
               : (
                  Config.ALLOW_BOSS_RESPAWN_TIME
                     ? getRespawnTime(boss)
                     : "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.DEAD") + ""
               )
         );
         newHtml = newHtml.replace("<?status_color_" + i + "?>", getTextColor(isAlive));
         newHtml = newHtml.replace(
            "<?bp_" + i + "?>",
            "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.SHOW")
               + "\" action=\"bypass _bbsboss_<?sort?>_"
               + page
               + "_ <?search?> _"
               + entry.getKey()
               + "\" width=50 height=16 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">"
         );
         ++i;
      }

      for(int j = i; j < 10; ++j) {
         newHtml = newHtml.replace("<?name_" + j + "?>", "...");
         newHtml = newHtml.replace("<?level_" + j + "?>", "...");
         newHtml = newHtml.replace("<?status_" + j + "?>", "...");
         newHtml = newHtml.replace("<?status_color_" + j + "?>", "FFFFFF");
         newHtml = newHtml.replace("<?bp_" + j + "?>", "...");
      }

      newHtml = newHtml.replace(
         "<?previous?>",
         page > 0
            ? "<button action=\"bypass _bbsbosslist_<?sort?>_"
               + (page - 1)
               + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\">"
            : "<br>"
      );
      newHtml = newHtml.replace(
         "<?next?>",
         nextPage && i == 10
            ? "<button action=\"bypass _bbsbosslist_<?sort?>_"
               + (page + 1)
               + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\">"
            : "<br>"
      );
      return newHtml.replace("<?pages?>", String.valueOf(page + 1));
   }

   private static String getEpicBossListReplacements(Player player, String html, int page, Map<Integer, StatsSet> allBosses, boolean nextPage) {
      String newHtml = html;
      int i = 0;

      for(Entry<Integer, StatsSet> entry : allBosses.entrySet()) {
         StatsSet boss = entry.getValue();
         NpcTemplate temp = NpcsParser.getInstance().getTemplate(entry.getKey());
         boolean isAlive = isBossAlive(boss);
         newHtml = newHtml.replace("<?name_" + i + "?>", player.getNpcName(temp));
         newHtml = newHtml.replace("<?level_" + i + "?>", String.valueOf(temp.getLevel()));
         newHtml = newHtml.replace(
            "<?status_" + i + "?>",
            isAlive
               ? "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.ALIVE") + ""
               : (
                  Config.ALLOW_BOSS_RESPAWN_TIME
                     ? getRespawnTime(boss)
                     : "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.DEAD") + ""
               )
         );
         newHtml = newHtml.replace("<?status_color_" + i + "?>", getTextColor(isAlive));
         newHtml = newHtml.replace(
            "<?bp_" + i + "?>",
            "<button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.SHOW")
               + "\" action=\"bypass _bbsepic_<?sort?>_"
               + page
               + "_ <?search?> _"
               + entry.getKey()
               + "\" width=50 height=16 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">"
         );
         ++i;
      }

      for(int j = i; j < 10; ++j) {
         newHtml = newHtml.replace("<?name_" + j + "?>", "...");
         newHtml = newHtml.replace("<?level_" + j + "?>", "...");
         newHtml = newHtml.replace("<?status_" + j + "?>", "...");
         newHtml = newHtml.replace("<?status_color_" + j + "?>", "FFFFFF");
         newHtml = newHtml.replace("<?bp_" + j + "?>", "...");
      }

      newHtml = newHtml.replace(
         "<?previous?>",
         page > 0
            ? "<button action=\"bypass _bbsepiclist_<?sort?>_"
               + (page - 1)
               + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\">"
            : "<br>"
      );
      newHtml = newHtml.replace(
         "<?next?>",
         nextPage && i == 10
            ? "<button action=\"bypass _bbsepiclist_<?sort?>_"
               + (page + 1)
               + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\">"
            : "<br>"
      );
      return newHtml.replace("<?pages?>", String.valueOf(page + 1));
   }

   private static Map<Integer, StatsSet> getBossesToShow(Map<Integer, StatsSet> allBosses, int page) {
      Map<Integer, StatsSet> bossesToShow = new LinkedHashMap<>();
      int i = 0;

      for(Entry<Integer, StatsSet> entry : allBosses.entrySet()) {
         if (i < page * 10) {
            ++i;
         } else {
            StatsSet boss = entry.getValue();
            NpcTemplate temp = NpcsParser.getInstance().getTemplate(entry.getKey());
            if (boss != null && temp != null) {
               ++i;
               bossesToShow.put(entry.getKey(), entry.getValue());
               if (i > page * 10 + 10 - 1) {
                  return bossesToShow;
               }
            }
         }
      }

      return bossesToShow;
   }

   private static void sendBossDetails(Player player, CommunityRaidBoss.SortType sort, int page, CharSequence search, int bossId) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/bosses/boss_details.htm");
      StatsSet bossSet = RaidBossSpawnManager.getInstance().getStoredInfo().get(bossId);
      if (bossSet == null) {
         separateAndSend(html, player);
      } else {
         NpcTemplate bossTemplate = NpcsParser.getInstance().getTemplate(bossId);
         RaidBossInstance bossInstance = getAliveBoss(bossId);
         html = getDetailedBossReplacements(player, html, bossSet, bossTemplate, bossInstance);
         html = getNormalReplacements(html, page, sort, search, bossId);
         ImagesCache.getInstance().sendImageToPlayer(player, bossId);
         separateAndSend(html, player);
      }
   }

   private static void sendEpicBossDetails(Player player, CommunityRaidBoss.SortType sort, int page, CharSequence search, int bossId) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/bosses/epic_details.htm");
      StatsSet bossSet = EpicBossManager.getInstance().getStoredInfo().get(bossId);
      if (bossSet == null) {
         separateAndSend(html, player);
      } else {
         NpcTemplate bossTemplate = NpcsParser.getInstance().getTemplate(bossId);
         GrandBossInstance bossInstance = getAliveEpicBoss(bossId);
         html = getDetailedEpicBossReplacements(player, html, bossSet, bossTemplate, bossInstance);
         html = getNormalReplacements(html, page, sort, search, bossId);
         ImagesCache.getInstance().sendImageToPlayer(player, bossId);
         separateAndSend(html, player);
      }
   }

   private static void manageButtons(Player player, int buttonIndex, int bossId) {
      switch(buttonIndex) {
         case 1:
            RaidBossSpawnManager.getInstance().showBossLocation(player, bossId);
            break;
         case 2:
            IBypassHandler handler = BypassHandler.getInstance().getHandler("drop");
            if (handler != null) {
               handler.useBypass("drop 1 " + bossId + "", player, null);
            }
            break;
         case 3:
            NpcUtils.showNpcSkillList(player, NpcsParser.getInstance().getTemplate(bossId));
            break;
         case 4:
            for(int id : Config.BLOCKED_RAID_LIST) {
               if (id == bossId) {
                  return;
               }
            }

            player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
            break;
         case 5:
            if (!Config.ALLOW_TELEPORT_TO_RAID
               || (AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
                  && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               return;
            }

            if (player.getUCState() > 0
               || player.isCursedWeaponEquipped()
               || player.isInFightEvent()
               || player.getKarma() > 0
               || player.getPvpFlag() > 0
               || player.isInCombat()
               || player.isInDuel()
               || player.getReflectionId() > 0
               || player.isInStoreMode()
               || player.isInOfflineMode()
               || player.isJailed()
               || player.inObserverMode()
               || player.isInOlympiadMode()
               || player.isBlocked()
               || OlympiadManager.getInstance().isRegistered(player)) {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               return;
            }

            if (!player.isInsideZone(ZoneId.PEACE)) {
               player.sendMessage(new ServerMessage("CommunityRaidBoss.CANT_USE", player.getLang()).toString());
               return;
            }

            for(int id : Config.BLOCKED_RAID_LIST) {
               if (id == bossId) {
                  player.sendMessage(new ServerMessage("CommunityRaidBoss.BLOCK_BOSS", player.getLang()).toString());
                  return;
               }
            }

            Spawner spawn = RaidBossSpawnManager.getInstance().getSpawns().get(bossId);
            if (spawn != null) {
               Location loc = Location.findPointToStay(
                  spawn.calcSpawnRangeLoc(spawn.getGeoIndex(), spawn.getTemplate()), 100, 150, player.getGeoIndex(), false
               );
               if (loc != null) {
                  if (Config.TELEPORT_TO_RAID_PRICE[0] > 0) {
                     if (player.getInventory().getItemByItemId(Config.TELEPORT_TO_RAID_PRICE[0]) == null) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     if (player.getInventory().getItemByItemId(Config.TELEPORT_TO_RAID_PRICE[0]).getCount() < (long)Config.TELEPORT_TO_RAID_PRICE[1]) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     player.destroyItemByItemId("TeleToRaid", Config.TELEPORT_TO_RAID_PRICE[0], (long)Config.TELEPORT_TO_RAID_PRICE[1], player, true);
                  }

                  player.sendPacket(new ShowBoard());
                  if (BotFunctions.getInstance().isAutoTpToRaidEnable(player)) {
                     BotFunctions.getInstance().getAutoTeleportToRaid(player, player.getLocation(), new Location(loc.getX(), loc.getY(), loc.getZ()));
                     return;
                  }

                  player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
               } else {
                  player.sendMessage(new ServerMessage("CommunityRaidBoss.EMPTY_LOC", player.getLang()).toString());
               }
            }
      }
   }

   private static String getDetailedBossReplacements(Player player, String html, StatsSet bossSet, NpcTemplate bossTemplate, RaidBossInstance bossInstance) {
      boolean isAlive = isBossAlive(bossSet);
      String newHtml = html.replace("<?name?>", player.getNpcName(bossTemplate));
      newHtml = newHtml.replace("<?level?>", String.valueOf(bossTemplate.getLevel()));
      newHtml = newHtml.replace(
         "<?status?>",
         isAlive
            ? "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.ALIVE") + ""
            : (
               Config.ALLOW_BOSS_RESPAWN_TIME
                  ? getRespawnTime(bossSet)
                  : "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.DEAD") + ""
            )
      );
      newHtml = newHtml.replace("<?status_color?>", getTextColor(isAlive));
      newHtml = newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));
      newHtml = newHtml.replace("<?currentHp?>", Util.formatAdena(bossInstance != null ? (long)((int)bossInstance.getCurrentHp()) : 0L));
      newHtml = newHtml.replace("<?maxHp?>", Util.formatAdena((long)((int)bossTemplate.getBaseHpMax())));
      return newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));
   }

   protected static String getDetailedEpicBossReplacements(
      Player player, String html, StatsSet bossSet, NpcTemplate bossTemplate, GrandBossInstance bossInstance
   ) {
      boolean isAlive = isBossAlive(bossSet);
      String newHtml = html.replace("<?name?>", player.getNpcName(bossTemplate));
      newHtml = newHtml.replace("<?level?>", String.valueOf(bossTemplate.getLevel()));
      newHtml = newHtml.replace(
         "<?status?>",
         isAlive
            ? "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.ALIVE") + ""
            : (
               Config.ALLOW_BOSS_RESPAWN_TIME
                  ? getRespawnTime(bossSet)
                  : "" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRaidBoss.DEAD") + ""
            )
      );
      newHtml = newHtml.replace("<?status_color?>", getTextColor(isAlive));
      newHtml = newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));
      newHtml = newHtml.replace("<?currentHp?>", Util.formatAdena(bossInstance != null ? (long)((int)bossInstance.getCurrentHp()) : 0L));
      newHtml = newHtml.replace("<?maxHp?>", Util.formatAdena((long)((int)bossTemplate.getBaseHpMax())));
      return newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));
   }

   private static String getNormalReplacements(String html, int page, CommunityRaidBoss.SortType sort, CharSequence search, int bossId) {
      String newHtml = html.replace("<?page?>", String.valueOf(page));
      newHtml = newHtml.replace("<?sort?>", String.valueOf(sort.index));
      newHtml = newHtml.replace("<?serverId?>", String.valueOf(Config.REQUEST_ID));
      newHtml = newHtml.replace("<?bossId?>", String.valueOf(bossId));
      newHtml = newHtml.replace("<?search?>", search);

      for(int i = 1; i <= 6; ++i) {
         if (Math.abs(sort.index) == i) {
            newHtml = newHtml.replace("<?sort" + i + "?>", String.valueOf(-sort.index));
         } else {
            newHtml = newHtml.replace("<?sort" + i + "?>", String.valueOf(i));
         }
      }

      return newHtml;
   }

   private static boolean isBossAlive(StatsSet set) {
      return set.getLong("respawnTime") < Calendar.getInstance().getTimeInMillis();
   }

   private static String getRespawnTime(StatsSet set) {
      if (set.getLong("respawnTime") < Calendar.getInstance().getTimeInMillis()) {
         return "isAlive";
      } else {
         long delay = (set.getLong("respawnTime") - Calendar.getInstance().getTimeInMillis()) / TimeUnit.SECONDS.toMillis(1L);
         int hours = (int)(delay / 60L / 60L);
         int mins = (int)((delay - (long)(hours * 60 * 60)) / 60L);
         int secs = (int)(delay - (long)(hours * 60 * 60 + mins * 60));
         String Strhours = hours < 10 ? "0" + hours : "" + hours;
         String Strmins = mins < 10 ? "0" + mins : "" + mins;
         String Strsecs = secs < 10 ? "0" + secs : "" + secs;
         return "<font color=\"b02e31\">" + Strhours + ":" + Strmins + ":" + Strsecs + "</font>";
      }
   }

   private static RaidBossInstance getAliveBoss(int bossId) {
      RaidBossInstance boss = RaidBossSpawnManager.getInstance().getBossStatus(bossId);
      return boss != null ? (RaidBossInstance)World.getInstance().findObject(boss.getObjectId()) : null;
   }

   private static GrandBossInstance getAliveEpicBoss(int bossId) {
      GrandBossInstance boss = EpicBossManager.getInstance().getBoss(bossId);
      return boss != null ? (GrandBossInstance)World.getInstance().findObject(boss.getObjectId()) : null;
   }

   private static int getMinionsCount(NpcTemplate template) {
      int minionsCount = 0;
      if (template.getMinionData().isEmpty()) {
         return minionsCount;
      } else {
         if (template.isRandomMinons()) {
            MinionData data = template.getMinionData().size() > 1
               ? template.getMinionData().get(Rnd.get(template.getMinionData().size()))
               : template.getMinionData().get(0);
            if (data != null) {
               for(MinionTemplate tpl : data.getMinions()) {
                  minionsCount += tpl.getAmount();
               }
            }
         } else {
            for(MinionData minion : template.getMinionData()) {
               for(MinionTemplate tpl : minion.getMinions()) {
                  minionsCount += tpl.getAmount();
               }
            }
         }

         return minionsCount;
      }
   }

   private static String getTextColor(boolean alive) {
      return alive ? "259a30" : "b02e31";
   }

   private static Map<Integer, StatsSet> getSearchedBosses(CommunityRaidBoss.SortType sort, String search, String lang) {
      Map<Integer, StatsSet> result = getBossesMapBySearch(search);

      for(int id : Config.BBS_BOSSES_TO_NOT_SHOW) {
         result.remove(id);
      }

      return sortResults(result, sort, lang);
   }

   private static Map<Integer, StatsSet> getSearchedEpicBosses(CommunityRaidBoss.SortType sort, String search, String lang) {
      Map<Integer, StatsSet> result = getEpicBossesMapBySearch(search);

      for(int id : Config.BBS_BOSSES_TO_NOT_SHOW) {
         result.remove(id);
      }

      return sortResults(result, sort, lang);
   }

   private static Map<Integer, StatsSet> getBossesMapBySearch(String search) {
      Map<Integer, StatsSet> finalResult = new HashMap<>();
      if (search.isEmpty()) {
         finalResult = RaidBossSpawnManager.getInstance().getStoredInfo();
      } else {
         for(Entry<Integer, StatsSet> entry : RaidBossSpawnManager.getInstance().getStoredInfo().entrySet()) {
            NpcTemplate temp = NpcsParser.getInstance().getTemplate(entry.getKey());
            if (StringUtils.containsIgnoreCase(temp.getName(), search) || StringUtils.containsIgnoreCase(temp.getNameRu(), search)) {
               finalResult.put(entry.getKey(), entry.getValue());
            }
         }
      }

      return finalResult;
   }

   private static Map<Integer, StatsSet> getEpicBossesMapBySearch(String search) {
      Map<Integer, StatsSet> finalResult = new HashMap<>();
      if (search.isEmpty()) {
         finalResult = EpicBossManager.getInstance().getStoredInfo();
      } else {
         for(Entry<Integer, StatsSet> entry : EpicBossManager.getInstance().getStoredInfo().entrySet()) {
            NpcTemplate temp = NpcsParser.getInstance().getTemplate(entry.getKey());
            if (StringUtils.containsIgnoreCase(temp.getName(), search) || StringUtils.containsIgnoreCase(temp.getNameRu(), search)) {
               finalResult.put(entry.getKey(), entry.getValue());
            }
         }
      }

      return finalResult;
   }

   private static Map<Integer, StatsSet> sortResults(Map<Integer, StatsSet> result, CommunityRaidBoss.SortType sort, String lang) {
      CommunityRaidBoss.ValueComparator bvc = new CommunityRaidBoss.ValueComparator(result, sort, lang);
      Map<Integer, StatsSet> sortedMap = new TreeMap<>(bvc);
      sortedMap.putAll(result);
      return sortedMap;
   }

   private static CommunityRaidBoss.SortType getSortByIndex(int i) {
      for(CommunityRaidBoss.SortType type : CommunityRaidBoss.SortType.values()) {
         if (type.index == i) {
            return type;
         }
      }

      return CommunityRaidBoss.SortType.NAME_ASC;
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityRaidBoss getInstance() {
      return CommunityRaidBoss.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityRaidBoss _instance = new CommunityRaidBoss();
   }

   private static enum SortType {
      NAME_ASC(1),
      NAME_DESC(-1),
      LEVEL_ASC(2),
      LEVEL_DESC(-2),
      STATUS_ASC(3),
      STATUS_DESC(-3);

      public final int index;

      private SortType(int index) {
         this.index = index;
      }
   }

   private static class ValueComparator implements Comparator<Integer>, Serializable {
      private static final long serialVersionUID = 4782405190873267622L;
      private final Map<Integer, StatsSet> _base;
      private final CommunityRaidBoss.SortType _sortType;
      private final String _lang;

      private ValueComparator(Map<Integer, StatsSet> base, CommunityRaidBoss.SortType sortType, String lang) {
         this._base = base;
         this._sortType = sortType;
         this._lang = lang;
      }

      public int compare(Integer o1, Integer o2) {
         int sortResult = this.sortById(o1, o2, this._sortType, this._lang);
         if (sortResult == 0 && !o1.equals(o2) && Math.abs(this._sortType.index) != 1) {
            sortResult = this.sortById(o1, o2, CommunityRaidBoss.SortType.NAME_ASC, this._lang);
         }

         return sortResult;
      }

      private int sortById(Integer a, Integer b, CommunityRaidBoss.SortType sorting, String lang) {
         NpcTemplate temp1 = NpcsParser.getInstance().getTemplate(a);
         NpcTemplate temp2 = NpcsParser.getInstance().getTemplate(b);
         StatsSet set1 = this._base.get(a);
         StatsSet set2 = this._base.get(b);
         switch(sorting) {
            case NAME_ASC:
               return lang != null && !lang.equalsIgnoreCase("en")
                  ? temp1.getNameRu().compareTo(temp2.getNameRu())
                  : temp1.getName().compareTo(temp2.getName());
            case NAME_DESC:
               return lang != null && !lang.equalsIgnoreCase("en")
                  ? temp2.getNameRu().compareTo(temp1.getNameRu())
                  : temp2.getName().compareTo(temp1.getName());
            case LEVEL_ASC:
               return Integer.compare(temp1.getLevel(), temp2.getLevel());
            case LEVEL_DESC:
               return Integer.compare(temp2.getLevel(), temp1.getLevel());
            case STATUS_ASC:
               return Integer.compare((int)set1.getLong("respawnTime"), (int)set2.getLong("respawnTime"));
            case STATUS_DESC:
               return Integer.compare((int)set2.getLong("respawnTime"), (int)set1.getLong("respawnTime"));
            default:
               return 0;
         }
      }
   }
}
