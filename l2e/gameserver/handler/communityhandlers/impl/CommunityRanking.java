package l2e.gameserver.handler.communityhandlers.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerStorage;

public class CommunityRanking extends AbstractCommunity implements ICommunityBoardHandler {
   private static CommunityRanking.RankingManager RankingManagerStats = new CommunityRanking.RankingManager();
   private long update = 0L;
   private final int time_update = Config.INTERVAL_STATS_UPDATE;
   private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

   public CommunityRanking() {
      this.selectRankingPK();
      this.selectRankingPVP();
      this.selectRankingPcBang();
      this.selectRankingHero();
      this.selectRankingClan();
      this.selectRankingAdena();
      this.selectRankingCastle();
      this.selectRankingOnline();
      this.selectRankingRebirth();
      this.selectRankingRaidPoints();
      this.selectRankingClanRaidPoints();
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsranking", "_bbsloc"};
   }

   @Override
   public void onBypassCommand(String bypass, Player player) {
      if (this.update + (long)(this.time_update * 60) * 1000L < Calendar.getInstance().getTimeInMillis()) {
         this.selectRankingPK();
         this.selectRankingPVP();
         this.selectRankingPcBang();
         this.selectRankingHero();
         this.selectRankingClan();
         this.selectRankingAdena();
         this.selectRankingCastle();
         this.selectRankingOnline();
         this.selectRankingRebirth();
         this.selectRankingRaidPoints();
         this.selectRankingClanRaidPoints();
         this.update = Calendar.getInstance().getTimeInMillis();
         _log.info("Ranking in the commynity board has been updated.");
      }

      if (bypass.equals("_bbsloc")) {
         this.onBypassCommand("_bbsranking:pvp", player);
      } else if (bypass.equals("_bbsranking:pk")) {
         this.show(player, 1);
      } else if (bypass.equals("_bbsranking:pvp")) {
         this.show(player, 2);
      } else if (bypass.equals("_bbsranking:pcbang")) {
         this.show(player, 3);
      } else if (bypass.equals("_bbsranking:hero")) {
         this.show(player, 4);
      } else if (bypass.equals("_bbsranking:clan")) {
         this.show(player, 5);
      } else if (bypass.equals("_bbsranking:adena")) {
         this.show(player, 6);
      } else if (bypass.equals("_bbsranking:castle")) {
         this.show(player, 7);
      } else if (bypass.equals("_bbsranking:online")) {
         this.show(player, 8);
      } else if (bypass.equals("_bbsranking:rebirth")) {
         this.show(player, 9);
      } else if (bypass.equals("_bbsranking:raidPoints")) {
         this.show(player, 10);
      } else if (bypass.equals("_bbsranking:clanRaidPoints")) {
         this.show(player, 11);
      }
   }

   private void show(Player player, int page) {
      int number = 0;
      String html = null;
      switch(page) {
         case 1:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/pk.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingPkName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingPkName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingPkClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingPkClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingPkClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingPkOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingPk[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 2:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/pvp.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingPvPName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingPvPName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingPvPClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingPvPClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingPvPClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingPvPOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingPvP[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 3:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/pcbang.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingPcbangName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingPcbangName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingPcbangClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingPcbangClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingPcbangClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingPcbangOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingPcbang[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 4:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/hero.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingHeroName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingHeroName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingHeroClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingHeroClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingHeroClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingHeroOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingHero[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 5:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/clan.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingClanName[number] != null) {
                  html = html.replace("<?clanName_" + number + "?>", RankingManagerStats.RankingClanName[number]);
                  html = html.replace(
                     "<?clanAlly_" + number + "?>",
                     RankingManagerStats.RankingClanAlly[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_ALLY") + "</font>"
                        : RankingManagerStats.RankingClanAlly[number]
                  );
                  html = html.replace("<?clanRep_" + number + "?>", Integer.toString(RankingManagerStats.RankingClanReputation[number]));
                  html = html.replace("<?clanLvl_" + number + "?>", Integer.toString(RankingManagerStats.RankingClanLvl[number]));
                  html = html.replace("<?clanLeader_" + number + "?>", RankingManagerStats.RankingClanLeader[number]);
               } else {
                  html = html.replace("<?clanName_" + number + "?>", "...");
                  html = html.replace("<?clanAlly_" + number + "?>", "...");
                  html = html.replace("<?clanRep_" + number + "?>", "...");
                  html = html.replace("<?clanLvl_" + number + "?>", "...");
                  html = html.replace("<?clanLeader_" + number + "?>", "...");
               }
            }
            break;
         case 6:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/adena.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingAdenaName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingAdenaName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingAdenaClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingAdenaClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingAdenaClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingAdenaOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Long.toString(RankingManagerStats.RankingAdena[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 7:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/castle.htm"); number < 9; ++number) {
               if (RankingManagerStats.RankingCastleName[number] != null) {
                  Date nextDate = new Date(RankingManagerStats.RankingCastleDate[number]);
                  String DATE_FORMAT = "dd-MMM-yyyy HH:mm";
                  SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingCastleName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingCastleClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingCastleClan[number]
                  );
                  html = html.replace("<?level_" + number + "?>", Integer.toString(RankingManagerStats.RankingCastleClanLvl[number]));
                  html = html.replace("<?tax_" + number + "?>", "" + RankingManagerStats.RankingCastleTax[number] + " %");
                  html = html.replace("<?date_" + number + "?>", String.valueOf(sdf.format(nextDate)));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?level_" + number + "?>", "...");
                  html = html.replace("<?tax_" + number + "?>", "...");
                  html = html.replace("<?date_" + number + "?>", "...");
               }
            }
            break;
         case 8:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/online.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingOnlineName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingOnlineName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingOnlineClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingOnlineClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingOnlineClass[number]));
                  html = html.replace("<?count_" + number + "?>", TimeUtils.formatTime(player, (int)RankingManagerStats.RankingOnline[number], false));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 9:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/rebirth.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingRebirthName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingRebirthName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingRebirthClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingRebirthClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingRebirthClass[number]));
                  html = html.replace("<?count_" + number + "?>", String.valueOf(RankingManagerStats.RankingRebirthAmount[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 10:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/raidPoints.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingRpName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingRpName[number]);
                  html = html.replace(
                     "<?clan_" + number + "?>",
                     RankingManagerStats.RankingRpClan[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_CLAN") + "</font>"
                        : RankingManagerStats.RankingRpClan[number]
                  );
                  html = html.replace("<?class_" + number + "?>", Util.className(player, RankingManagerStats.RankingRpClass[number]));
                  html = html.replace(
                     "<?on_" + number + "?>",
                     RankingManagerStats.RankingRpOn[number] == 1
                        ? "<font color=\"66FF33\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                        : "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
                  );
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingRp[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clan_" + number + "?>", "...");
                  html = html.replace("<?class_" + number + "?>", "...");
                  html = html.replace("<?on_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         case 11:
            for(html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/clanRaidPoints.htm"); number < 10; ++number) {
               if (RankingManagerStats.RankingCRpName[number] != null) {
                  html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingCRpName[number]);
                  html = html.replace(
                     "<?clanAlly_" + number + "?>",
                     RankingManagerStats.RankingCRpAlly[number] == null
                        ? "<font color=\"B59A75\">" + ServerStorage.getInstance().getString(player.getLang(), "CommunityRanking.NO_ALLY") + "</font>"
                        : RankingManagerStats.RankingCRpAlly[number]
                  );
                  html = html.replace("<?clanLvl_" + number + "?>", Integer.toString(RankingManagerStats.RankingCRpLvl[number]));
                  html = html.replace("<?count_" + number + "?>", Integer.toString(RankingManagerStats.RankingCRp[number]));
               } else {
                  html = html.replace("<?name_" + number + "?>", "...");
                  html = html.replace("<?clanAlly_" + number + "?>", "...");
                  html = html.replace("<?clanLvl_" + number + "?>", "...");
                  html = html.replace("<?count_" + number + "?>", "...");
               }
            }
            break;
         default:
            _log.warning("Unknown page: " + page + " - " + player.getName());
      }

      html = html.replace("<?update?>", String.valueOf(this.time_update));
      html = html.replace("<?last_update?>", String.valueOf(time(this.update)));
      html = html.replace("<?ranking_menu?>", HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/ranking/menu.htm"));
      separateAndSend(html, player);
   }

   private static String time(long time) {
      return TIME_FORMAT.format(new Date(time));
   }

   private void selectRankingPVP() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, online, pvpkills FROM characters WHERE accesslevel = 0 AND pvpkills > 0 ORDER BY pvpkills DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingPvPName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingPvPClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingPvPClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingPvPOn[number] = rset.getInt("online");
               RankingManagerStats.RankingPvP[number] = rset.getInt("pvpkills");
            } else {
               RankingManagerStats.RankingPvPName[number] = null;
               RankingManagerStats.RankingPvPClan[number] = null;
               RankingManagerStats.RankingPvPClass[number] = 0;
               RankingManagerStats.RankingPvPOn[number] = 0;
               RankingManagerStats.RankingPvP[number] = 0;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingPK() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, online, pkkills FROM characters WHERE accesslevel = 0 AND pkkills > 0 ORDER BY pkkills DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingPkName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingPkClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingPkClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingPkOn[number] = rset.getInt("online");
               RankingManagerStats.RankingPk[number] = rset.getInt("pkkills");
            } else {
               RankingManagerStats.RankingPkName[number] = null;
               RankingManagerStats.RankingPkClan[number] = null;
               RankingManagerStats.RankingPkClass[number] = 0;
               RankingManagerStats.RankingPkOn[number] = 0;
               RankingManagerStats.RankingPk[number] = 0;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingPcBang() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, online, pccafe_points FROM characters WHERE accesslevel = 0 AND pccafe_points > 0 ORDER BY pccafe_points DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingPcbangName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingPcbangClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingPcbangClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingPcbangOn[number] = rset.getInt("online");
               RankingManagerStats.RankingPcbang[number] = rset.getInt("pccafe_points");
            } else {
               RankingManagerStats.RankingPcbangName[number] = null;
               RankingManagerStats.RankingPcbangClan[number] = null;
               RankingManagerStats.RankingPcbangClass[number] = 0;
               RankingManagerStats.RankingPcbangOn[number] = 0;
               RankingManagerStats.RankingPcbang[number] = 0;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingHero() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT h.count, ch.char_name, ch.base_class, ch.online, ch.clanid FROM heroes h LEFT JOIN characters ch ON ch.charId=h.charId ORDER BY h.count DESC, ch.char_name ASC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingHeroName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingHeroClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingHeroClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingHeroOn[number] = rset.getInt("online");
               RankingManagerStats.RankingHero[number] = rset.getInt("count");
            } else {
               RankingManagerStats.RankingHeroName[number] = null;
               RankingManagerStats.RankingHeroClan[number] = null;
               RankingManagerStats.RankingHeroClass[number] = 0;
               RankingManagerStats.RankingHeroOn[number] = 0;
               RankingManagerStats.RankingHero[number] = 0;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingClan() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT clan_name, clan_level, reputation_score, ally_name, leader_id FROM clan_data WHERE clan_level > 0 ORDER BY clan_level DESC LIMIT 10"
         );
      ) {
         for(ResultSet rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("clan_name").isEmpty()) {
               RankingManagerStats.RankingClanName[number] = rset.getString("clan_name");
               String ally = rset.getString("ally_name");
               RankingManagerStats.RankingClanAlly[number] = ally == null ? null : ally;
               RankingManagerStats.RankingClanReputation[number] = rset.getInt("reputation_score");
               RankingManagerStats.RankingClanLvl[number] = rset.getInt("clan_level");
               RankingManagerStats.RankingClanLeader[number] = CharNameHolder.getInstance().getNameById(rset.getInt("leader_id"));
            } else {
               RankingManagerStats.RankingClanName[number] = null;
               RankingManagerStats.RankingClanAlly[number] = null;
               RankingManagerStats.RankingClanReputation[number] = 0;
               RankingManagerStats.RankingClanLvl[number] = 0;
               RankingManagerStats.RankingClanLeader[number] = null;
            }
         }
      } catch (Exception var35) {
         var35.printStackTrace();
      }
   }

   private void selectRankingAdena() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, online, it.count FROM characters AS c LEFT JOIN items AS it ON (c.charId=it.owner_id) WHERE it.item_id=57 AND accesslevel = 0 ORDER BY it.count DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingAdenaName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingAdenaClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingAdenaClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingAdenaOn[number] = rset.getInt("online");
               RankingManagerStats.RankingAdena[number] = rset.getLong("count");
            } else {
               RankingManagerStats.RankingAdenaName[number] = null;
               RankingManagerStats.RankingAdenaClan[number] = null;
               RankingManagerStats.RankingAdenaClass[number] = 0;
               RankingManagerStats.RankingAdenaOn[number] = 0;
               RankingManagerStats.RankingAdena[number] = 0L;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingCastle() {
      int number = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         for(int i = 1; i <= 9; ++i) {
            PreparedStatement statement = con.prepareStatement("SELECT clan_name, clan_level FROM clan_data WHERE hasCastle=" + i + ";");
            ResultSet result = statement.executeQuery();
            PreparedStatement statement2 = con.prepareStatement("SELECT name, siegeDate, taxPercent FROM castle WHERE id=" + i + ";");

            for(ResultSet result2 = statement2.executeQuery(); result.next(); ++number) {
               if (!result.getString("clan_name").isEmpty()) {
                  while(result2.next()) {
                     RankingManagerStats.RankingCastleName[number] = result2.getString("name");
                     RankingManagerStats.RankingCastleClan[number] = result.getString("clan_name");
                     RankingManagerStats.RankingCastleClanLvl[number] = result.getInt("clan_level");
                     RankingManagerStats.RankingCastleTax[number] = result2.getInt("taxPercent");
                     RankingManagerStats.RankingCastleDate[number] = result2.getLong("siegeDate");
                  }
               } else {
                  RankingManagerStats.RankingCastleName[number] = null;
                  RankingManagerStats.RankingCastleClan[number] = null;
                  RankingManagerStats.RankingCastleClanLvl[number] = 0;
                  RankingManagerStats.RankingCastleTax[number] = 0;
                  RankingManagerStats.RankingCastleDate[number] = 0L;
               }
            }
         }
      } catch (Exception var19) {
         var19.printStackTrace();
      }
   }

   private void selectRankingOnline() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, onlinetime FROM characters WHERE accesslevel = 0 ORDER BY onlinetime DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingOnlineName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingOnlineClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingOnlineClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingOnline[number] = rset.getLong("onlinetime");
            } else {
               RankingManagerStats.RankingOnlineName[number] = null;
               RankingManagerStats.RankingOnlineClan[number] = null;
               RankingManagerStats.RankingOnlineClass[number] = 0;
               RankingManagerStats.RankingOnline[number] = 0L;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingRebirth() {
      int number = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name, base_class, clanid, cv.value FROM characters AS c LEFT JOIN character_variables AS cv ON (c.charId=cv.obj_id) WHERE cv.name=\"rebirth\" AND accesslevel = 0 ORDER BY convert(cv.value, decimal) DESC LIMIT 10"
         );
      ) {
         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++number) {
            if (!rset.getString("char_name").isEmpty()) {
               RankingManagerStats.RankingRebirthName[number] = rset.getString("char_name");
               int clan_id = rset.getInt("clanid");
               Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
               RankingManagerStats.RankingRebirthClan[number] = clan == null ? null : clan.getName();
               RankingManagerStats.RankingRebirthClass[number] = rset.getInt("base_class");
               RankingManagerStats.RankingRebirthAmount[number] = rset.getLong("value");
            } else {
               RankingManagerStats.RankingRebirthName[number] = null;
               RankingManagerStats.RankingRebirthClan[number] = null;
               RankingManagerStats.RankingRebirthClass[number] = 0;
               RankingManagerStats.RankingRebirthAmount[number] = 0L;
            }
         }

         rset.close();
      } catch (Exception var36) {
         var36.printStackTrace();
      }
   }

   private void selectRankingRaidPoints() {
      List<CommunityRanking.CharPointsInfo> charPointList = new ArrayList<>();

      for(Entry<Integer, Map<Integer, Integer>> charPoints : RaidBossSpawnManager.getInstance().getPoints().entrySet()) {
         Map<Integer, Integer> tmpPoint = charPoints.getValue();
         int totalPoints = 0;

         for(Entry<Integer, Integer> e : tmpPoint.entrySet()) {
            switch(e.getKey()) {
               case 0:
                  totalPoints += e.getValue();
            }
         }

         if (totalPoints != 0) {
            charPointList.add(new CommunityRanking.CharPointsInfo(charPoints.getKey(), totalPoints));
         }
      }

      if (charPointList != null && !charPointList.isEmpty()) {
         Comparator<CommunityRanking.CharPointsInfo> statsComparator = new CommunityRanking.SortCharPointsInfo();
         Collections.sort(charPointList, statsComparator);
         int count = 0;

         for(int i = 0; i < charPointList.size(); ++i) {
            CommunityRanking.CharPointsInfo data = charPointList.get(i);
            if (data != null) {
               Player player = World.getInstance().getPlayer(data.getCharId());
               if (player != null) {
                  RankingManagerStats.RankingRpName[i] = player.getName();
                  RankingManagerStats.RankingRpClan[i] = player.getClan() == null ? null : player.getClan().getName();
                  RankingManagerStats.RankingRpClass[i] = player.getBaseClass();
                  RankingManagerStats.RankingRpOn[i] = 1;
                  RankingManagerStats.RankingRp[i] = data.getPoints();
               } else {
                  try (
                     Connection con = DatabaseFactory.getInstance().getConnection();
                     PreparedStatement statement = con.prepareStatement(
                        "SELECT char_name, base_class, clanid FROM characters WHERE charId = '" + data.getCharId() + "'"
                     );
                     ResultSet rset = statement.executeQuery();
                  ) {
                     if (rset.next()) {
                        RankingManagerStats.RankingRpName[i] = rset.getString("char_name");
                        int clan_id = rset.getInt("clanid");
                        Clan clan = clan_id == 0 ? null : ClanHolder.getInstance().getClan(clan_id);
                        RankingManagerStats.RankingRpClan[i] = clan == null ? null : clan.getName();
                        RankingManagerStats.RankingRpClass[i] = rset.getInt("base_class");
                        RankingManagerStats.RankingRpOn[i] = 0;
                        RankingManagerStats.RankingRp[i] = data.getPoints();
                     }
                  } catch (Exception var66) {
                     _log.log(Level.SEVERE, "Error restore char data:", (Throwable)var66);
                  }
               }
            } else {
               RankingManagerStats.RankingRpName[i] = null;
               RankingManagerStats.RankingRpClan[i] = null;
               RankingManagerStats.RankingRpClass[i] = 0;
               RankingManagerStats.RankingRpOn[i] = 0;
               RankingManagerStats.RankingRp[i] = 0;
            }

            if (++count >= 10) {
               break;
            }
         }
      }
   }

   private void selectRankingClanRaidPoints() {
      List<CommunityRanking.ClanPointsInfo> clanPointList = new ArrayList<>();

      for(Integer clanId : RaidBossSpawnManager.getInstance().getClanPoints().keySet()) {
         int points = RaidBossSpawnManager.getInstance().getClanPoints().get(clanId);
         if (points != 0) {
            clanPointList.add(new CommunityRanking.ClanPointsInfo(clanId, points));
         }
      }

      if (clanPointList != null && !clanPointList.isEmpty()) {
         Comparator<CommunityRanking.ClanPointsInfo> statsComparator = new CommunityRanking.SortClanPointsInfo();
         Collections.sort(clanPointList, statsComparator);
         int count = 0;

         for(int i = 0; i < clanPointList.size(); ++i) {
            CommunityRanking.ClanPointsInfo data = clanPointList.get(i);
            if (data != null) {
               Clan clan = ClanHolder.getInstance().getClan(data.getClanId());
               if (clan != null) {
                  RankingManagerStats.RankingCRpName[i] = clan.getName();
                  RankingManagerStats.RankingCRpLvl[i] = clan.getLevel();
                  String ally = clan.getAllyName();
                  RankingManagerStats.RankingCRpAlly[i] = ally == null ? null : ally;
                  RankingManagerStats.RankingCRp[i] = data.getPoints();
               } else {
                  RankingManagerStats.RankingCRpName[i] = null;
                  RankingManagerStats.RankingCRpLvl[i] = 0;
                  RankingManagerStats.RankingCRpAlly[i] = null;
                  RankingManagerStats.RankingCRp[i] = 0;
               }
            } else {
               RankingManagerStats.RankingCRpName[i] = null;
               RankingManagerStats.RankingCRpLvl[i] = 0;
               RankingManagerStats.RankingCRpAlly[i] = null;
               RankingManagerStats.RankingCRp[i] = 0;
            }

            if (++count >= 10) {
               break;
            }
         }
      }
   }

   public static CommunityRanking getInstance() {
      return CommunityRanking.SingletonHolder._instance;
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
   }

   protected static class CharPointsInfo {
      private final int _charId;
      private final int _points;

      public CharPointsInfo(int charId, int points) {
         this._charId = charId;
         this._points = points;
      }

      public int getCharId() {
         return this._charId;
      }

      public int getPoints() {
         return this._points;
      }
   }

   protected static class ClanPointsInfo {
      private final int _clanId;
      private final int _points;

      public ClanPointsInfo(int charId, int points) {
         this._clanId = charId;
         this._points = points;
      }

      public int getClanId() {
         return this._clanId;
      }

      public int getPoints() {
         return this._points;
      }
   }

   private static class RankingManager {
      private final String[] RankingPvPName = new String[10];
      private final String[] RankingPvPClan = new String[10];
      private final int[] RankingPvPClass = new int[10];
      private final int[] RankingPvPOn = new int[10];
      private final int[] RankingPvP = new int[10];
      private final String[] RankingPkName = new String[10];
      private final String[] RankingPkClan = new String[10];
      private final int[] RankingPkClass = new int[10];
      private final int[] RankingPkOn = new int[10];
      private final int[] RankingPk = new int[10];
      private final String[] RankingPcbangName = new String[10];
      private final String[] RankingPcbangClan = new String[10];
      private final int[] RankingPcbangClass = new int[10];
      private final int[] RankingPcbangOn = new int[10];
      private final int[] RankingPcbang = new int[10];
      private final String[] RankingHeroName = new String[10];
      private final String[] RankingHeroClan = new String[10];
      private final int[] RankingHeroClass = new int[10];
      private final int[] RankingHeroOn = new int[10];
      private final int[] RankingHero = new int[10];
      private final String[] RankingClanName = new String[10];
      private final String[] RankingClanAlly = new String[10];
      private final int[] RankingClanReputation = new int[10];
      private final int[] RankingClanLvl = new int[10];
      private final String[] RankingClanLeader = new String[10];
      private final String[] RankingAdenaName = new String[10];
      private final String[] RankingAdenaClan = new String[10];
      private final int[] RankingAdenaClass = new int[10];
      private final int[] RankingAdenaOn = new int[10];
      private final long[] RankingAdena = new long[10];
      private final String[] RankingCastleName = new String[10];
      private final String[] RankingCastleClan = new String[10];
      private final int[] RankingCastleClanLvl = new int[10];
      private final int[] RankingCastleTax = new int[10];
      private final long[] RankingCastleDate = new long[10];
      private final String[] RankingOnlineName = new String[10];
      private final String[] RankingOnlineClan = new String[10];
      private final int[] RankingOnlineClass = new int[10];
      private final long[] RankingOnline = new long[10];
      private final String[] RankingRebirthName = new String[10];
      private final String[] RankingRebirthClan = new String[10];
      private final int[] RankingRebirthClass = new int[10];
      private final long[] RankingRebirthAmount = new long[10];
      private final String[] RankingRpName = new String[10];
      private final String[] RankingRpClan = new String[10];
      private final int[] RankingRpClass = new int[10];
      private final int[] RankingRpOn = new int[10];
      private final int[] RankingRp = new int[10];
      private final String[] RankingCRpName = new String[10];
      private final int[] RankingCRpLvl = new int[10];
      private final String[] RankingCRpAlly = new String[10];
      private final int[] RankingCRp = new int[10];

      private RankingManager() {
      }
   }

   private static class SingletonHolder {
      protected static final CommunityRanking _instance = new CommunityRanking();
   }

   private static class SortCharPointsInfo implements Comparator<CommunityRanking.CharPointsInfo>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortCharPointsInfo() {
      }

      public int compare(CommunityRanking.CharPointsInfo o1, CommunityRanking.CharPointsInfo o2) {
         return Integer.compare(o2.getPoints(), o1.getPoints());
      }
   }

   private static class SortClanPointsInfo implements Comparator<CommunityRanking.ClanPointsInfo>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortClanPointsInfo() {
      }

      public int compare(CommunityRanking.ClanPointsInfo o1, CommunityRanking.ClanPointsInfo o2) {
         return Integer.compare(o2.getPoints(), o1.getPoints());
      }
   }
}
