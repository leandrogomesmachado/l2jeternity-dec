package l2e.gameserver.handler.communityhandlers.impl;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.commons.util.StringUtil;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityClan extends AbstractCommunity implements ICommunityBoardHandler {
   private static final int CLANS_PER_PAGE = 6;
   private static final int MEMBERS_PER_PAGE = 14;
   private static final String[] ALL_CLASSES = new String[]{
      "Duelist",
      "Dreadnought",
      "PhoenixKnight",
      "HellKnight",
      "Adventurer",
      "Sagittarius",
      "Archmage",
      "Soultaker",
      "ArcanaLord",
      "Cardinal",
      "Hierophant",
      "EvaTemplar",
      "SwordMuse",
      "WindRider",
      "MoonlightSentinel",
      "MysticMuse",
      "ElementalMaster",
      "EvaSaint",
      "ShillienTemplar",
      "SpectralDancer",
      "GhostHunter",
      "GhostSentinel",
      "StormScreamer",
      "SpectralMaster",
      "ShillienSaint",
      "Titan",
      "GrandKhavatari",
      "Dominator",
      "Doomcryer",
      "FortuneSeeker",
      "Maestro"
   };
   private static final int[] SLOTS = new int[]{5, 7, 1, 6, 11, 10, 12, 23, 0, 24, 14, 13, 9, 8, 4, 15};
   private static final String[] NAMES = new String[]{
      "Weapon",
      "Shield",
      "Helmet",
      "Chest",
      "Legs",
      "Gloves",
      "Boots",
      "Cloak",
      "Shirt",
      "Belt",
      "Ring",
      " Ring",
      "Earring",
      "Earring",
      "Necklace",
      "Bracelet"
   };
   private static TIntObjectHashMap<String[]> _clanSkillDescriptions = new TIntObjectHashMap<>();
   private final CommunityClan.ClanComparator _clansComparator = new CommunityClan.ClanComparator();

   public CommunityClan() {
      _clanSkillDescriptions.put(370, new String[]{"SKILL370_1", "SKILL370_2", "SKILL370_3"});
      _clanSkillDescriptions.put(371, new String[]{"SKILL371_1", "SKILL371_2", "SKILL371_3"});
      _clanSkillDescriptions.put(372, new String[]{"SKILL372_1", "SKILL372_2", "SKILL372_3"});
      _clanSkillDescriptions.put(373, new String[]{"SKILL373_1", "SKILL373_2", "SKILL373_3"});
      _clanSkillDescriptions.put(374, new String[]{"SKILL374_1", "SKILL374_2", "SKILL374_3"});
      _clanSkillDescriptions.put(375, new String[]{"SKILL375_1", "SKILL375_2", "SKILL375_3"});
      _clanSkillDescriptions.put(376, new String[]{"SKILL376_1", "SKILL376_2", "SKILL376_3"});
      _clanSkillDescriptions.put(377, new String[]{"SKILL377_1", "SKILL377_2", "SKILL377_3"});
      _clanSkillDescriptions.put(378, new String[]{"SKILL378_1", "SKILL378_2", "SKILL378_3"});
      _clanSkillDescriptions.put(379, new String[]{"SKILL379_1", "SKILL379_2", "SKILL379_3"});
      _clanSkillDescriptions.put(380, new String[]{"SKILL380_1", "SKILL380_2", "SKILL380_3"});
      _clanSkillDescriptions.put(381, new String[]{"SKILL381_1", "SKILL381_2", "SKILL381_3"});
      _clanSkillDescriptions.put(382, new String[]{"SKILL382_1", "SKILL382_2", "SKILL382_3"});
      _clanSkillDescriptions.put(383, new String[]{"SKILL383_1", "SKILL383_2", "SKILL383_3"});
      _clanSkillDescriptions.put(384, new String[]{"SKILL384_1", "SKILL384_2", "SKILL384_3"});
      _clanSkillDescriptions.put(385, new String[]{"SKILL385_1", "SKILL385_2", "SKILL385_3"});
      _clanSkillDescriptions.put(386, new String[]{"SKILL386_1", "SKILL386_2", "SKILL386_3"});
      _clanSkillDescriptions.put(387, new String[]{"SKILL387_1", "SKILL387_2", "SKILL387_3"});
      _clanSkillDescriptions.put(388, new String[]{"SKILL388_1", "SKILL388_2", "SKILL388_3"});
      _clanSkillDescriptions.put(389, new String[]{"SKILL389_1", "SKILL389_2", "SKILL389_3"});
      _clanSkillDescriptions.put(390, new String[]{"SKILL390_1", "SKILL390_2", "SKILL390_3"});
      _clanSkillDescriptions.put(391, new String[]{"SKILL391_1"});
      _clanSkillDescriptions.put(590, new String[]{"SKILL590_1"});
      _clanSkillDescriptions.put(591, new String[]{"SKILL591_1"});
      _clanSkillDescriptions.put(592, new String[]{"SKILL592_1"});
      _clanSkillDescriptions.put(593, new String[]{"SKILL593_1"});
      _clanSkillDescriptions.put(594, new String[]{"SKILL594_1"});
      _clanSkillDescriptions.put(595, new String[]{"SKILL595_1"});
      _clanSkillDescriptions.put(596, new String[]{"SKILL596_1"});
      _clanSkillDescriptions.put(597, new String[]{"SKILL597_1"});
      _clanSkillDescriptions.put(598, new String[]{"SKILL598_1"});
      _clanSkillDescriptions.put(599, new String[]{"SKILL599_1"});
      _clanSkillDescriptions.put(600, new String[]{"SKILL600_1"});
      _clanSkillDescriptions.put(601, new String[]{"SKILL601_1"});
      _clanSkillDescriptions.put(602, new String[]{"SKILL602_1"});
      _clanSkillDescriptions.put(603, new String[]{"SKILL603_1"});
      _clanSkillDescriptions.put(604, new String[]{"SKILL604_1"});
      _clanSkillDescriptions.put(605, new String[]{"SKILL605_1"});
      _clanSkillDescriptions.put(606, new String[]{"SKILL606_1"});
      _clanSkillDescriptions.put(607, new String[]{"SKILL607_1"});
      _clanSkillDescriptions.put(608, new String[]{"SKILL608_1"});
      _clanSkillDescriptions.put(609, new String[]{"SKILL609_1"});
      _clanSkillDescriptions.put(610, new String[]{"SKILL610_1"});
      _clanSkillDescriptions.put(611, new String[]{"SKILL611_1", "SKILL611_2", "SKILL611_3"});
      _clanSkillDescriptions.put(612, new String[]{"SKILL612_1", "SKILL612_2", "SKILL612_3"});
      _clanSkillDescriptions.put(613, new String[]{"SKILL613_1", "SKILL613_2", "SKILL613_3"});
      _clanSkillDescriptions.put(614, new String[]{"SKILL614_1", "SKILL614_2", "SKILL614_3"});
      _clanSkillDescriptions.put(615, new String[]{"SKILL615_1", "SKILL615_2", "SKILL615_3"});
      _clanSkillDescriptions.put(616, new String[]{"SKILL616_1", "SKILL616_2", "SKILL616_3"});
      _clanSkillDescriptions.put(848, new String[]{"SKILL848_1"});
      _clanSkillDescriptions.put(849, new String[]{"SKILL849_1"});
      _clanSkillDescriptions.put(850, new String[]{"SKILL850_1"});
      _clanSkillDescriptions.put(851, new String[]{"SKILL851_1"});
      _clanSkillDescriptions.put(852, new String[]{"SKILL852_1"});
      _clanSkillDescriptions.put(853, new String[]{"SKILL853_1"});
      _clanSkillDescriptions.put(854, new String[]{"SKILL854_1"});
      _clanSkillDescriptions.put(855, new String[]{"SKILL855_1"});
      _clanSkillDescriptions.put(856, new String[]{"SKILL856_1"});
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "_bbsclan",
         "_bbsclclan",
         "_bbsclanlist",
         "_bbsclanmanage",
         "_bbsclanjoin",
         "_bbsclanpetitions",
         "_bbsclanplayerpetition",
         "_bbsclanplayerinventory",
         "_bbsclanmembers",
         "_bbsclanmembersingle",
         "_bbsclanskills",
         "_bbsclannoticeform",
         "_bbsclannoticeenable",
         "_bbsclannoticedisable",
         "Notice"
      };
   }

   @Override
   public void onBypassCommand(String bypass, Player player) {
      StringTokenizer st = new StringTokenizer(bypass, "_");
      String cmd = st.nextToken();
      String html = null;
      if ("bbsclan".equals(cmd)) {
         Clan clan = player.getClan();
         if (clan != null) {
            this.onBypassCommand("_bbsclclan_" + player.getClanId(), player);
         } else {
            this.onBypassCommand("_bbsclanlist_0", player);
         }
      } else {
         if ("bbsclanlist".equals(cmd)) {
            int page = Integer.parseInt(st.nextToken());
            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_list.htm");
            html = html.replace("%rank%", this.getAllClansRank(player, page));
            html = html.replace("%myClan%", player.getClan() != null ? "_bbsclclan_" + player.getClanId() : "_bbsclanlist_0");
         } else if ("bbsclclan".equals(cmd)) {
            int clanId = Integer.parseInt(st.nextToken());
            if (clanId == 0) {
               player.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
               this.onBypassCommand("_bbsclanlist_0", player);
               return;
            }

            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan == null) {
               this.onBypassCommand("_bbsclanlist_0", player);
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_index.htm");
            html = this.getMainClanPage(player, clan, html);
         } else if ("bbsclanmanage".equals(cmd)) {
            String actionToken = st.nextToken();
            int action = Integer.parseInt(actionToken.substring(0, 1));
            if (action != 0) {
               boolean shouldReturn = this.manageRecrutationWindow(player, action, actionToken);
               if (shouldReturn) {
                  return;
               }
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_recruit.htm");
            html = this.getClanRecruitmentManagePage(player, html);
         } else if ("bbsclanjoin".equals(cmd)) {
            int clanId = Integer.parseInt(st.nextToken());
            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan == null) {
               this.sendErrorMessage(player, "Such clan cannot be found!", "_bbsclanlist_0");
               return;
            }

            if (player.getClanJoinExpiryTime() > System.currentTimeMillis()) {
               player.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
               return;
            }

            String next = st.nextToken();
            if (Integer.parseInt(next.substring(0, 1)) == 1) {
               try {
                  if (!this.manageClanJoinWindow(player, clan, next.substring(2))) {
                     this.sendInfoMessage(
                        player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.ALREADY_SEND"), "_bbsclclan_" + clan.getId(), true
                     );
                     return;
                  }
               } catch (Exception var10) {
                  this.sendErrorMessage(
                     player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.INCORRECT"), "_bbsclanjoin_" + clan.getId() + "_0"
                  );
                  return;
               }

               this.sendInfoMessage(
                  player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.SUBMITTED"), "_bbsclclan_" + clan.getId(), false
               );
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_join.htm");
            html = this.getClanJoinPage(player, clan, html);
         } else if ("bbsclanpetitions".equals(cmd)) {
            int clanId = Integer.parseInt(st.nextToken());
            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan == null) {
               this.sendErrorMessage(player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NOT_FOUND"), "_bbsclanlist_0");
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_petitions.htm");
            html = this.getClanPetitionsPage(player, clan, html);
         } else if ("bbsclanplayerpetition".equals(cmd)) {
            int senderId = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
               int action = Integer.parseInt(st.nextToken());
               this.managePlayerPetition(player, senderId, action);
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_playerpetition.htm");
            Player sender = World.getInstance().getPlayer(senderId);
            if (sender != null) {
               html = this.getClanSinglePetitionPage(player, sender, html);
            } else {
               html = this.getClanSinglePetitionPage(player, senderId, html);
            }
         } else if ("bbsclanplayerinventory".equals(cmd)) {
            int senderId = Integer.parseInt(st.nextToken());
            Player sender = World.getInstance().getPlayer(senderId);
            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_playerinventory.htm");
            if (sender != null) {
               html = this.getPlayerInventoryPage(sender, html);
            } else {
               html = this.getPlayerInventoryPage(player, senderId, html);
            }
         } else if ("bbsclanmembers".equals(cmd)) {
            int clanId = Integer.parseInt(st.nextToken());
            if (clanId == 0) {
               this.sendErrorMessage(player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NOT_FOUND"), "_bbsclanlist_0");
               return;
            }

            int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan == null) {
               this.sendErrorMessage(player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NOT_FOUND"), "_bbsclanlist_0");
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_members.htm");
            html = this.getClanMembersPage(player, clan, html, page);
         } else if ("bbsclanskills".equals(cmd)) {
            int clanId = Integer.parseInt(st.nextToken());
            if (clanId == 0) {
               this.sendErrorMessage(player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NOT_FOUND"), "_bbsclanlist_0");
               return;
            }

            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan == null) {
               this.sendErrorMessage(player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NOT_FOUND"), "_bbsclanlist_0");
               return;
            }

            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_skills.htm");
            html = this.getClanSkills(player, clan, html);
         } else if ("bbsclanmembersingle".equals(cmd)) {
            int playerId = Integer.parseInt(st.nextToken());
            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/clan/clan_singlemember.htm");
            Player member = World.getInstance().getPlayer(playerId);
            if (member != null) {
               html = this.getClanSingleMemberPage(player, member, html);
            } else {
               html = this.getClanSingleMemberPage(player, playerId, html);
            }
         } else {
            if ("bbsclannoticeform".equals(cmd)) {
               if (player.getClan() != null && player.isClanLeader()) {
                  this.clanNotice(player, player.getClanId());
                  return;
               }

               this.onBypassCommand("_bbsclclan_" + player.getClanId(), player);
               return;
            }

            if ("bbsclannoticeenable".equals(cmd)) {
               if (player.getClan() == null || !player.isClanLeader()) {
                  this.onBypassCommand("_bbsclclan_" + player.getClanId(), player);
                  return;
               }

               player.getClan().setNoticeEnabled(true);
               this.clanNotice(player, player.getClanId());
            } else if ("bbsclannoticedisable".equals(cmd)) {
               if (player.getClan() == null || !player.isClanLeader()) {
                  this.onBypassCommand("_bbsclclan_" + player.getClanId(), player);
                  return;
               }

               player.getClan().setNoticeEnabled(false);
               this.clanNotice(player, player.getClanId());
            }
         }

         separateAndSend(html, player);
      }
   }

   private String getMainClanPage(Player player, Clan clan, String html) {
      html = html.replace("%clanName%", clan.getName());
      html = html.replace("%clanId%", String.valueOf(clan.getId()));
      html = html.replace("%position%", "#" + clan.getRank());
      html = html.replace("%clanLeader%", clan.getLeaderName());
      html = html.replace(
         "%allyName%", clan.getAllyId() > 0 ? clan.getAllyName() : ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO_ALLY")
      );
      html = html.replace("%crp%", Util.formatAdena((long)clan.getReputationScore()));
      html = html.replace("%membersCount%", String.valueOf(clan.getMembersCount()));
      html = html.replace("%clanLevel%", String.valueOf(clan.getLevel()));
      html = html.replace("%raidsKilled%", String.valueOf(0));
      html = html.replace("%epicsKilled%", String.valueOf(0));
      ClanHall clanHall = ClanHallManager.getInstance().getAbstractHallByOwner(clan);
      html = html.replace(
         "%clanHall%",
         clanHall != null ? Util.clanHallName(player, clanHall.getId()) : ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO")
      );
      Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
      html = html.replace("%castle%", castle != null ? castle.getName() : ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO"));
      Fort fortress = FortManager.getInstance().getFortByOwner(clan);
      html = html.replace("%fortress%", fortress != null ? fortress.getName() : ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO"));
      int[] data = this.getMainClanPageData(clan);
      html = html.replace("%pvps%", String.valueOf(data[0]));
      html = html.replace("%pks%", String.valueOf(data[1]));
      html = html.replace("%nobleCount%", String.valueOf(data[2]));
      html = html.replace("%heroCount%", String.valueOf(data[3]));
      html = html.replace("%clan_avarage_level%", Util.formatAdena((long)clan.getAverageLevel()));
      html = html.replace("%clan_online%", Util.formatAdena((long)clan.getOnlineMembers(0).size()));
      String clanCrest = "";
      if ((clan.getAllyId() <= 0 || clan.getAllyCrestId() <= 0) && clan.getCrestId() == 0) {
         clanCrest = clanCrest + "<td width=46>&nbsp;</td>";
      } else {
         clanCrest = clanCrest + "<td width=46 align=center>";
         clanCrest = clanCrest + "<table fixwidth=24 fixheight=12 cellpadding=0 cellspacing=0>";
         clanCrest = clanCrest + "<tr>";
         if (clan.getAllyId() > 0 && clan.getAllyCrestId() > 0) {
            clanCrest = clanCrest + "<td>";
            clanCrest = clanCrest
               + "<br><table height=8 cellpadding=0 cellspacing=0 background=Crest.crest_"
               + Config.REQUEST_ID
               + "_"
               + clan.getAllyCrestId()
               + ">";
            clanCrest = clanCrest + "<tr><td fixwidth=8><img height=4 width=8 src=L2UI.SquareBlack>&nbsp;</td></tr>";
            clanCrest = clanCrest + "</table></td>";
         }

         if (clan.getCrestId() != 0) {
            clanCrest = clanCrest + "<td>";
            clanCrest = clanCrest
               + "<br><table height=8 cellpadding=0 cellspacing=0 background=Crest.crest_"
               + Config.REQUEST_ID
               + "_"
               + clan.getCrestId()
               + ">";
            clanCrest = clanCrest + "<tr><td fixwidth=16><img height=4 width=16 src=L2UI.SquareBlack>&nbsp;</td></tr>";
            clanCrest = clanCrest + "</table></td>";
         }

         clanCrest = clanCrest + "</tr></table></td>";
      }

      html = html.replace("%clan_crest%", clanCrest);
      String alliances = "";
      if (clan.getAllyId() > 0) {
         alliances = "<table width=770 cellspacing=0 cellpadding=0 height=28 bgcolor=333333><tr>";
         alliances = alliances
            + "<td width=65>&nbsp;</td><td width=100 align=left><font color=LEVEL name=hs9>"
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.ALLY")
            + ":</font></td><td width=520 align=left><table width=522 cellspacing=1 cellpadding=2><tr>";

         for(Clan memberClan : ClanHolder.getInstance().getClanAllies(clan.getAllyId())) {
            alliances = alliances + "<td>";
            alliances = alliances
               + "<button action=\"bypass _bbsclanlist_0\" value=\""
               + memberClan.getName()
               + "\" width=150 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">";
            alliances = alliances + "</td>";
         }

         alliances = alliances + "</tr></table></td></tr></table>";
      }

      html = html.replace("%alliances%", alliances);
      String wars = "<tr>";
      int index = 0;

      for(Clan warClan : clan.getEnemyClans()) {
         if (index == 5) {
            wars = wars + "</tr><tr>";
            index = 0;
         }

         wars = wars + "<td align=center>";
         wars = wars
            + "<button action=\"bypass _bbsclclan_"
            + warClan.getId()
            + "\" value=\""
            + warClan.getName()
            + "\" width=130 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">";
         wars = wars + "</td>";
         ++index;
      }

      wars = wars + "</tr>";
      html = html.replace("%wars%", wars);
      String joinClan = "";
      if (player.getClan() == null) {
         joinClan = "<tr><td width=200 align=\"center\">";
         joinClan = joinClan
            + "<button action=\"bypass _bbsclanjoin_"
            + clan.getId()
            + "_0\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.JOIN_CLAN")
            + "\" width=200 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">";
         joinClan = joinClan + "</td></tr>";
      }

      html = html.replace("%joinClan%", joinClan);
      String manageRecruitment = "";
      String managePetitions = "";
      String manageNotice = "";
      if (player.getClan() != null && player.getClan().equals(clan) && player.getClan().getLeaderId() == player.getObjectId()) {
         manageRecruitment = "<tr><td width=200 align=\"center\">";
         manageRecruitment = manageRecruitment
            + "<button action=\"bypass _bbsclanmanage_0\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.MANAGE_RECRUTE")
            + "\" width=200 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">";
         manageRecruitment = manageRecruitment + "</td></tr>";
         managePetitions = "<tr><td width=200 align=\"center\">";
         managePetitions = managePetitions
            + "<button action=\"bypass _bbsclanpetitions_"
            + clan.getId()
            + "\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.MANAGE_PETITION")
            + "\" width=200 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">";
         managePetitions = managePetitions + "</td></tr>";
         manageNotice = "<tr><td width=200 align=\"center\">";
         manageNotice = manageNotice
            + "<button action=\"bypass _bbsclannoticeform\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.MANAGE_NOTICE")
            + "\" width=200 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">";
         manageNotice = manageNotice + "</td></tr>";
      }

      html = html.replace("%manageRecruitment%", manageRecruitment);
      html = html.replace("%managePetitions%", managePetitions);
      return html.replace("%manageNotice%", manageNotice);
   }

   private String getClanMembersPage(Player player, Clan clan, String html, int page) {
      html = html.replace("%clanName%", clan.getName());
      List<ClanMember> members = clan.getAllMembers();
      StringBuilder builder = new StringBuilder();
      int index = 0;
      int max = Math.min(14 + 14 * page, members.size());

      for(int i = 14 * page; i < max; ++i) {
         ClanMember member = members.get(i);
         builder.append("<table><tr>");
         builder.append("<td width=40 align=left>" + (index + 1) + ".</td>");
         builder.append("<td width=200><font color=\"FFFFFF\">").append(member.getName()).append("</font></td>");
         builder.append("<td align=center width=150>")
            .append(
               member.isOnline()
                  ? "<font color=6a9b54>" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                  : "<font color=FF6666>" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
            )
            .append("</td>");
         builder.append("<td align=center width=150>")
            .append(
               member == clan.getLeader()
                  ? "<font color=6a9b54>" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>"
                  : "<font color=FF6666>" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>"
            )
            .append("</td>");
         builder.append("<td align=center width=150><font color=\"BBFF44\">").append(this.getUnitName(player, member.getPledgeType())).append("</font></td>");
         builder.append("");
         builder.append("</tr></table>");
         builder.append("<table border=0 cellpadding=0 cellspacing=0 width=700>");
         builder.append("<tr><td><img src=\"L2UI.SquareGray\" width=700 height=1></td></tr>");
         builder.append("</table><br>");
         ++index;
      }

      html = html.replace("%members%", builder.toString());
      builder = new StringBuilder();
      builder.append("<table width=700><tr><td align=center width=350>");
      if (page > 0) {
         builder.append("<button action=\"bypass _bbsclanmembers_")
            .append(clan.getId())
            .append("_")
            .append(page - 1)
            .append(
               "\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.PREVIOUS")
                  + "\" width=140 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\">"
            );
      }

      builder.append("</td><td align=center width=350>");
      if (members.size() > 14 + 14 * page) {
         builder.append("<center><button action=\"bypass _bbsclanmembers_")
            .append(clan.getId() + "_" + (page + 1))
            .append(
               "\" value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NEXT")
                  + "\" width=140 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\"></center>"
            );
      }

      builder.append("</td></tr></table>");
      return html.replace("%nextPages%", builder.toString());
   }

   private String getClanSingleMemberPage(Player player, Player member, String html) {
      html = html.replace("%playerName%", member.getName());
      html = html.replace("%playerId%", String.valueOf(member.getObjectId()));
      html = html.replace("%clanName%", member.getClan() != null ? member.getClan().getName() : "");
      html = html.replace("%online%", "<font color=6a9b54>True</font>");
      html = html.replace("%title%", member.getTitle());
      html = html.replace("%pvpPoints%", String.valueOf(member.getPvpKills()));
      html = html.replace("%pkPoints%", String.valueOf(member.getPkKills()));
      html = html.replace("%rank%", "Level " + (member.getClan() != null ? member.getClan().getClanMember(member.getObjectId()).getPowerGrade() : 0));
      html = html.replace("%onlineTime%", TimeUtils.formatTime(player, (int)member.getTotalOnlineTime(), false));
      html = html.replace(
         "%leader%",
         member.getClan().getLeaderId() == member.getObjectId()
            ? ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE")
            : ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE")
      );
      html = html.replace("%subpledge%", this.getUnitName(player, member.getPledgeType()));
      html = html.replace(
         "%nobless%",
         member.isNoble()
            ? ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE")
            : ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE")
      );
      html = html.replace(
         "%hero%",
         member.isHero()
            ? ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE")
            : ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE")
      );
      html = html.replace("%adena%", this.getConvertedAdena(player, member.getAdena()));
      html = html.replace("%recs%", String.valueOf(member.getRecommendation().getRecomHave()));
      html = html.replace("%sevenSigns%", SevenSigns.getCabalShortName(SevenSigns.getInstance().getPlayerCabal(member.getObjectId())));
      html = html.replace("%fame%", String.valueOf(member.getFame()));
      Collection<SubClass> classes = member.getSubClasses().values();
      int subIndex = 0;

      for(SubClass sub : classes) {
         String replacement = "";
         if (sub.getClassId() == member.getBaseClass()) {
            replacement = "mainClass";
         } else {
            if (subIndex == 0) {
               replacement = "firstSub";
            } else if (subIndex == 1) {
               replacement = "secondSub";
            } else {
               replacement = "thirdSub";
            }

            ++subIndex;
         }

         html = html.replace("%" + replacement + "%", Util.className(player, ClassId.values()[sub.getClassId()].getId()) + "(" + sub.getLevel() + ")");
      }

      html = html.replace("%firstSub%", "");
      html = html.replace("%secondSub%", "");
      html = html.replace("%thirdSub%", "");
      return html.replace("%clanId%", String.valueOf(member.getClanId()));
   }

   private String getClanSingleMemberPage(Player player, int playerId, String html) {
      CommunityClan.OfflineSinglePlayerData data = this.getSinglePlayerData(playerId);
      html = html.replace("%playerName%", data.char_name);
      html = html.replace("%playerId%", String.valueOf(playerId));
      html = html.replace("%clanName%", data.clan_name);
      html = html.replace("%online%", "<font color=9b5454>" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>");
      html = html.replace("%title%", data.title == null ? "" : data.title);
      html = html.replace("%pvpPoints%", "" + data.pvpKills);
      html = html.replace("%pkPoints%", "" + data.pkKills);
      html = html.replace("%onlineTime%", TimeUtils.formatTime(player, (int)data.onlineTime, false));
      html = html.replace("%leader%", Util.boolToString(player, data.isClanLeader));
      html = html.replace("%subpledge%", this.getUnitName(player, data.pledge_type));
      html = html.replace("%nobless%", Util.boolToString(player, data.isNoble));
      html = html.replace("%hero%", Util.boolToString(player, data.isHero));
      html = html.replace("%adena%", this.getConvertedAdena(player, data.adenaCount));
      html = html.replace("%recs%", "" + data.rec_have);
      html = html.replace("%sevenSigns%", SevenSigns.getCabalShortName(data.sevenSignsSide));
      html = html.replace("%fame%", "" + data.fame);
      html = html.replace("%clanId%", "" + data.clanId);
      String[] otherSubs = new String[]{"%firstSub%", "%secondSub%", "%thirdSub%"};
      int index = 0;

      for(int[] sub : data.subClassIdLvlBase) {
         if (sub[2] == 1) {
            html = html.replace("%mainClass%", Util.className(player, ClassId.values()[sub[0]].getId()) + "(" + sub[1] + ")");
         } else {
            html = html.replace(otherSubs[0], Util.className(player, ClassId.values()[sub[0]].getId()) + "(" + sub[1] + ")");
         }
      }

      for(String sub : otherSubs) {
         html = html.replace(sub, "<br>");
      }

      return html;
   }

   private String getClanSkills(Player player, Clan clan, String html) {
      html = html.replace("%clanName%", clan.getName());
      html = html.replace("%clanId%", String.valueOf(clan.getId()));
      String skills = "";

      for(Skill clanSkill : clan.getAllSkills()) {
         skills = skills + "<tr><td width=20></td>";
         skills = skills + "<td width=50><br>";
         skills = skills + "<img src=\"" + clanSkill.getIcon() + "\" height=30 width=30>";
         skills = skills + "</td><td width=675><br><table width=675><tr><td><font color=\"00BBFF\">";
         skills = skills
            + player.getSkillName(clanSkill)
            + " - <font color=\"FFFFFF\">"
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.LEVEL")
            + " "
            + clanSkill.getLevel()
            + " </font>";
         skills = skills + "</font></td></tr><tr><td>";
         String[] descriptions = (String[])_clanSkillDescriptions.get(clanSkill.getId());
         if (descriptions != null && descriptions.length >= clanSkill.getLevel() - 1) {
            skills = skills
               + "<font color=\"FFFF11\">"
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral." + descriptions[clanSkill.getLevel() - 1] + "")
               + "</font>";
         } else {
            _log.warning("cannot find skill id:" + clanSkill.getId() + " in Clan Community Skills descriptions!");
         }

         skills = skills + "</td></tr></table></td></tr>";
      }

      return html.replace("%skills%", skills);
   }

   private String getClanSinglePetitionPage(Player leader, Player member, String html) {
      html = html.replace("%clanId%", String.valueOf(leader.getClan().getId()));
      html = html.replace("%playerId%", String.valueOf(member.getObjectId()));
      html = html.replace("%playerName%", member.getName());
      html = html.replace("%online%", "<font color=6a9b54>" + ServerStorage.getInstance().getString(leader.getLang(), "Util.TRUE") + "</font>");
      html = html.replace("%onlineTime%", TimeUtils.formatTime(leader, (int)member.getTotalOnlineTime(), false));
      html = html.replace("%pvpPoints%", String.valueOf(member.getPvpKills()));
      html = html.replace("%pkPoints%", String.valueOf(member.getPkKills()));
      html = html.replace("%fame%", String.valueOf(member.getFame()));
      html = html.replace("%adena%", this.getConvertedAdena(leader, member.getAdena()));
      Collection<SubClass> classes = member.getSubClasses().values();
      int subIndex = 0;

      for(SubClass sub : classes) {
         String replacement = "";
         if (sub.getClassId() == member.getBaseClass()) {
            replacement = "mainClass";
         } else {
            if (subIndex == 0) {
               replacement = "firstSub";
            } else if (subIndex == 1) {
               replacement = "secondSub";
            } else {
               replacement = "thirdSub";
            }

            ++subIndex;
         }

         html = html.replace(
            "%" + replacement + "%",
            Util.className(leader, ClassId.values()[sub.getClassId()].getId())
               + "("
               + ServerStorage.getInstance().getString(leader.getLang(), "CommunityClan.LEVEL")
               + ": "
               + sub.getLevel()
               + ")"
         );
      }

      html = html.replace("%firstSub%", "");
      html = html.replace("%secondSub%", "");
      html = html.replace("%thirdSub%", "");
      int index = 1;

      for(String question : leader.getClan().getQuestions()) {
         html = html.replace("%question" + index + "%", question != null && question.length() > 2 ? question + "?" : "");
         ++index;
      }

      Clan.SinglePetition petition = leader.getClan().getPetition(member.getObjectId());
      index = 1;

      for(String answer : petition.getAnswers()) {
         html = html.replace("%answer" + index + "%", answer != null && answer.length() > 2 ? answer : "");
         ++index;
      }

      return html.replace("%comment%", petition.getComment());
   }

   private String getClanSinglePetitionPage(Player leader, int playerId, String html) {
      CommunityClan.PetitionPlayerData data = this.getSinglePetitionPlayerData(playerId);
      html = html.replace("%clanId%", String.valueOf(leader.getClanId()));
      html = html.replace("%playerId%", String.valueOf(playerId));
      html = html.replace("%online%", "<font color=9b5454>" + ServerStorage.getInstance().getString(leader.getLang(), "Util.FALSE") + "</font>");
      html = html.replace("%playerName%", data.char_name);
      html = html.replace("%onlineTime%", TimeUtils.formatTime(leader, (int)data.onlineTime, false));
      html = html.replace("%pvpPoints%", "" + data.pvpKills);
      html = html.replace("%pkPoints%", "" + data.pkKills);
      html = html.replace("%fame%", "" + data.fame);
      html = html.replace("%adena%", this.getConvertedAdena(leader, data.adenaCount));
      String[] otherSubs = new String[]{"%firstSub%", "%secondSub%", "%thirdSub%"};
      int index = 0;

      for(int[] sub : data.subClassIdLvlBase) {
         if (sub[2] == 1) {
            html = html.replace("%mainClass%", Util.className(leader, ClassId.values()[sub[0]].getId()) + "(" + sub[1] + ")");
         } else {
            html = html.replace(otherSubs[index], Util.className(leader, ClassId.values()[sub[0]].getId()) + "(" + sub[1] + ")");
         }
      }

      for(String sub : otherSubs) {
         html = html.replace(sub, "<br>");
      }

      index = 1;

      for(String question : leader.getClan().getQuestions()) {
         html = html.replace("%question" + index + "%", question != null && question.length() > 2 ? question : "");
         ++index;
      }

      Clan.SinglePetition petition = leader.getClan().getPetition(playerId);
      index = 1;

      for(String answer : petition.getAnswers()) {
         html = html.replace("%answer" + index + "%", answer != null && answer.length() > 2 ? answer : "");
         ++index;
      }

      return html.replace("%comment%", petition.getComment());
   }

   private String getClanRecruitmentManagePage(Player player, String html) {
      Clan clan = player.getClan();
      if (clan == null) {
         return html;
      } else {
         html = html.replace("%clanName%", clan.getName());
         boolean firstChecked = clan.getClassesNeeded().size() == ALL_CLASSES.length;
         html = html.replace("%checked1%", firstChecked ? "_checked" : "");
         html = html.replace("%checked2%", firstChecked ? "" : "_checked");
         String[] notChoosenClasses = this.getNotChosenClasses(player, clan);
         html = html.replace("%firstClassGroup%", notChoosenClasses[0]);
         html = html.replace("%secondClassGroup%", notChoosenClasses[1]);
         String list = "<tr>";
         int index = -1;

         for(Integer clas : clan.getClassesNeeded()) {
            if (index % 4 == 3) {
               list = list + "</tr><tr>";
            }

            ++index;
            String className = Util.className(player, ALL_CLASSES[clas - 88].substring(0, 1).toLowerCase() + ALL_CLASSES[clas - 88].substring(1));
            String shortName = className.length() > 15 ? className.substring(0, 15) : className;
            list = list
               + "<td align=center width=100><button value=\""
               + shortName
               + "\" action=\"bypass  _bbsclanmanage_5 "
               + className
               + "\" back=\"l2ui_ct1.button.button_df_small_down\" width=105 height=20 fore=\"l2ui_ct1.button.button_df_small\"></td>";
         }

         list = list + "</tr>";
         html = html.replace("%choosenClasses%", list);

         for(int i = 0; i < 8; ++i) {
            String clanQuestion = clan.getQuestions()[i];
            html = html.replace("%question" + (i + 1) + "%", clanQuestion != null && clanQuestion.length() > 0 ? clanQuestion : "Question " + (i + 1) + ":");
         }

         return html.replace(
            "%recrutation%",
            clan.isRecruting()
               ? ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.STOP")
               : ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.START")
         );
      }
   }

   private String getClanJoinPage(Player player, Clan clan, String html) {
      html = html.replace("%clanId%", String.valueOf(clan.getId()));
      html = html.replace("%clanName%", clan.getName());

      for(int i = 0; i < 8; ++i) {
         String question = clan.getQuestions()[i];
         if (question != null && question.length() > 2) {
            html = html.replace("%question" + (i + 1) + "%", question);
            html = html.replace("%answer" + (i + 1) + "%", "<edit var=\"answer" + (i + 1) + "\" width=275 height=15>");
         } else {
            html = html.replace("%question" + (i + 1) + "%", "");
            html = html.replace("%answer" + (i + 1) + "%", "");
            html = html.replace("$answer" + (i + 1), " ");
         }
      }

      boolean canJoin = false;
      String classes = "<tr>";
      int index = -1;

      for(int classNeeded : clan.getClassesNeeded()) {
         if (++index == 6) {
            classes = classes + "</tr><tr>";
            index = 0;
         }

         boolean goodClass = player.getBaseClass() == classNeeded || player.getSubClasses().keySet().contains(classNeeded);
         if (goodClass) {
            canJoin = true;
         }

         classes = classes + "<td width=130><font color=\"" + (goodClass ? "00FF00" : "9b5454") + "\">";
         classes = classes + Util.className(player, ClassId.values()[classNeeded].getId());
         classes = classes + "</font></td>";
      }

      classes = classes + "</tr>";
      html = html.replace("%classes%", classes);
      if (canJoin) {
         html = html.replace(
            "%joinClanButton%",
            "<br><center><button action=\"bypass _bbsclanjoin_"
               + clan.getId()
               + "_1 | $answer1 | $answer2 | $answer3 | $answer4 | $answer5 | $answer6 | $answer7 | $answer8 | $comment |\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.SEND")
               + "\" width=320 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\"></center>"
         );
      } else {
         html = html.replace("%joinClanButton%", "");
      }

      return html;
   }

   private String getClanPetitionsPage(Player player, Clan clan, String html) {
      html = html.replace("%clanName%", clan.getName());
      String petitions = "";
      int index = 1;
      List<Clan.SinglePetition> _petitionsToRemove = new ArrayList<>();

      for(Clan.SinglePetition petition : clan.getPetitions()) {
         CommunityClan.ClanPetitionData data = this.getClanPetitionsData(player, petition.getSenderId());
         if (data == null) {
            _petitionsToRemove.add(petition);
         } else {
            petitions = petitions + "<table border=0 cellpadding=2 cellspacing=2 width=740><tr><td fixwidth=50>" + index + ".";
            petitions = petitions + "</td><td align=center fixwidth=150>";
            petitions = petitions
               + "<button action=\"bypass _bbsclanplayerpetition_"
               + petition.getSenderId()
               + "\" value=\""
               + data.char_name
               + "\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\">";
            petitions = petitions + "</td><td fixwidth=100 align=center>";
            petitions = petitions + data.online;
            petitions = petitions + "</td><td fixwidth=95 align=center><font color=\"f1b45d\">";
            petitions = petitions + data.pvpKills;
            petitions = petitions + "</font></td><td fixwidth=100 align=center><font color=\"f1b45d\">";
            petitions = petitions + TimeUtils.formatTime(player, (int)data.onlineTime, false);
            petitions = petitions + "</font></td><td fixwidth=75 align=center><font color=\"f1b45d\">";
            petitions = petitions + Util.boolToString(player, data.isNoble);
            petitions = petitions + "</font></td></tr></table>";
            petitions = petitions + "<table border=0 cellpadding=2 cellspacing=2 width=740>";
            petitions = petitions + "<tr><td><img src=\"L2UI.SquareGray\" width=740 height=1></td></tr>";
            petitions = petitions + "</table>";
            ++index;
         }
      }

      for(Clan.SinglePetition petitionToRemove : _petitionsToRemove) {
         clan.deletePetition(petitionToRemove);
      }

      return html.replace("%petitions%", petitions);
   }

   private String getPlayerInventoryPage(Player player, String html) {
      html = html.replace("%playerName%", player.getName());
      html = html.replace(
         "%back%", player.getClan() != null ? "_bbsclanmembersingle_" + player.getObjectId() : "_bbsclanplayerpetition_" + player.getObjectId()
      );
      PcInventory pcInv = player.getInventory();
      String inventory = "<tr>";

      for(int i = 0; i < SLOTS.length; ++i) {
         if (i % 2 == 0) {
            inventory = inventory + "</tr><tr>";
         }

         inventory = inventory + "<td><table><tr><td height=40>";
         inventory = inventory
            + (
               pcInv.getPaperdollItem(SLOTS[i]) != null
                  ? "<img src=" + pcInv.getPaperdollItem(SLOTS[i]).getItem().getIcon() + " width=32 height=32>"
                  : "<img src=\"Icon.low_tab\" width=32 height=32>"
            );
         inventory = inventory + "</td><td width=150><font color=\"FFFFFF\">";
         inventory = inventory
            + (
               pcInv.getPaperdollItem(SLOTS[i]) != null
                  ? player.getItemName(pcInv.getPaperdollItem(SLOTS[i]).getItem()) + " +" + pcInv.getPaperdollItem(SLOTS[i]).getEnchantLevel()
                  : ""
                     + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO")
                     + " "
                     + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan." + NAMES[i] + "")
            );
         inventory = inventory + "</font></td></tr></table></td>";
      }

      inventory = inventory + "</tr>";
      return html.replace("%inventory%", inventory);
   }

   private String getPlayerInventoryPage(Player player, int playerId, String html) {
      CommunityClan.OfflinePlayerInventoryData data = this.getPlayerInventoryData(playerId);
      html = html.replace("%playerName%", data.char_name);
      html = html.replace("%back%", data.clanId != 0 ? "_bbsclanmembersingle_" + playerId : "_bbsclanplayerpetition_" + playerId);
      String inventory = "<tr>";

      for(int i = 0; i < SLOTS.length; ++i) {
         if (i % 2 == 0) {
            inventory = inventory + "</tr><tr>";
         }

         int[] item = (int[])data.itemIdAndEnchantForSlot.get(i);
         Item template = null;
         if (item != null && item[0] > 0) {
            template = ItemsParser.getInstance().getTemplate(item[0]);
         }

         inventory = inventory + "<td><table><tr><td height=40>";
         inventory = inventory
            + (template != null ? "<img src=" + template.getIcon() + " width=32 height=32>" : "<img src=\"Icon.low_tab\" width=32 height=32>");
         inventory = inventory + "</td><td width=150><font color=\"bc7420\">";
         inventory = inventory
            + (
               template != null
                  ? player.getItemName(template) + " +" + item[1]
                  : ""
                     + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NO")
                     + " "
                     + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan." + NAMES[i] + "")
            );
         inventory = inventory + "</font></td></tr></table></td>";
      }

      inventory = inventory + "</tr>";
      return html.replace("%inventory%", inventory);
   }

   private CommunityClan.OfflinePlayerInventoryData getPlayerInventoryData(int playerId) {
      CommunityClan.OfflinePlayerInventoryData data = new CommunityClan.OfflinePlayerInventoryData();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT char_name,clanid FROM characters WHERE charId = '" + playerId + "'");
         ResultSet rset = statement.executeQuery();
      ) {
         if (rset.next()) {
            data.char_name = rset.getString("char_name");
            data.clanId = rset.getInt("clanid");
         }

         try (
            PreparedStatement statementx = con.prepareStatement(
               "SELECT item_id, loc_data, enchant_level FROM items WHERE owner_id = '" + playerId + "' AND loc='PAPERDOLL'"
            );
            ResultSet rsetx = statementx.executeQuery();
         ) {
            while(rsetx.next()) {
               int loc = rsetx.getInt("loc_data");

               for(int i = 0; i < SLOTS.length; ++i) {
                  if (loc == SLOTS[i]) {
                     int[] itemData = new int[]{rsetx.getInt("item_id"), rsetx.getInt("enchant_level")};
                     data.itemIdAndEnchantForSlot.put(i, itemData);
                  }
               }
            }
         }
      } catch (Exception var130) {
         _log.log(Level.SEVERE, "Error in getPlayerInventoryData:", (Throwable)var130);
      }

      return data;
   }

   private int[] getMainClanPageData(Clan clan) {
      int[] data = new int[5];

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT SUM(pvpkills), SUM(pkkills) FROM characters WHERE characters.clanid = '" + clan.getId() + "'"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         if (rset.next()) {
            data[0] = rset.getInt("SUM(pvpkills)");
            data[1] = rset.getInt("SUM(pkkills)");
         }

         try (
            PreparedStatement statementx = con.prepareStatement(
               "SELECT count(characters.charId) FROM characters WHERE characters.nobless=1 AND characters.clanid =" + clan.getId() + ""
            );
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data[2] = rsetx.getInt("count(characters.charId)");
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement(
               "SELECT count(characters.charId) FROM characters JOIN heroes on characters.charId = heroes.charId WHERE characters.clanid ="
                  + clan.getId()
                  + ""
            );
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data[3] = rsetx.getInt("count(characters.charId)");
            }
         }
      } catch (Exception var227) {
         _log.log(Level.SEVERE, "Error in getMainClanPageData:", (Throwable)var227);
      }

      return data;
   }

   private CommunityClan.OfflineSinglePlayerData getSinglePlayerData(int playerId) {
      CommunityClan.OfflineSinglePlayerData data = new CommunityClan.OfflineSinglePlayerData();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name,base_class,title,pvpkills,pkkills,onlinetime,rec_have,fame,clanid FROM characters WHERE charId = '" + playerId + "'"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         if (rset.next()) {
            data.char_name = rset.getString("char_name");
            data.title = rset.getString("title");
            data.pvpKills = rset.getInt("pvpkills");
            data.pkKills = rset.getInt("pkkills");
            data.onlineTime = rset.getLong("onlinetime");
            data.rec_have = rset.getInt("rec_have");
            data.fame = rset.getInt("fame");
            data.clanId = rset.getInt("clanid");
            int[] sub = new int[]{rset.getInt("base_class"), rset.getInt("level"), 1};
            data.subClassIdLvlBase.add(sub);
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT cabal FROM seven_signs WHERE charId='" + playerId + "'");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data.sevenSignsSide = SevenSigns.getCabalNumber(rsetx.getString("cabal"));
            }
         }

         if (data.clanId > 0) {
            try (
               PreparedStatement statementx = con.prepareStatement("SELECT type,name,leader_id FROM `clan_subpledges` where `clan_id` = '" + data.clanId + "'");
               ResultSet rsetx = statementx.executeQuery();
            ) {
               if (rsetx.next()) {
                  data.clan_name = rsetx.getString("name");
                  data.pledge_type = rsetx.getInt("type");
                  data.isClanLeader = rsetx.getInt("leader_id") == playerId;
               }
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT olympiad_points FROM `olympiad_nobles` where `char_id` = '" + playerId + "'");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data.isNoble = true;
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT count FROM `heroes` where `char_id` = '" + playerId + "'");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data.isHero = true;
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT count FROM `items` where `owner_id` = '" + playerId + "' AND item_id=57");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data.adenaCount = rsetx.getLong("count");
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT class_id,level FROM `character_subclasses` where `charId` = '" + playerId + "'");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            while(rsetx.next()) {
               int[] sub = new int[]{rsetx.getInt("class_id"), rsetx.getInt("level"), 0};
               data.subClassIdLvlBase.add(sub);
            }
         }
      } catch (Exception var895) {
         _log.log(Level.SEVERE, "Error in getSinglePlayerData:", (Throwable)var895);
      }

      return data;
   }

   private CommunityClan.PetitionPlayerData getSinglePetitionPlayerData(int playerId) {
      CommunityClan.PetitionPlayerData data = new CommunityClan.PetitionPlayerData();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name,base_class,level,onlinetime,pvpkills,pkkills,fame FROM characters WHERE charId = '" + playerId + "'"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         if (rset.next()) {
            data.char_name = rset.getString("char_name");
            data.onlineTime = rset.getLong("onlinetime");
            data.pvpKills = rset.getInt("pvpkills");
            data.pkKills = rset.getInt("pkkills");
            data.fame = rset.getInt("fame");
            int[] sub = new int[]{rset.getInt("base_class"), rset.getInt("level"), 1};
            data.subClassIdLvlBase.add(sub);
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT count FROM `items` WHERE `owner_id` = '" + playerId + "' AND item_id=57");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            if (rsetx.next()) {
               data.adenaCount = rsetx.getLong("count");
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT class_id,level FROM `character_subclasses` WHERE `charId` = '" + playerId + "'");
            ResultSet rsetx = statementx.executeQuery();
         ) {
            while(rsetx.next()) {
               int[] sub = new int[]{rsetx.getInt("class_id"), rsetx.getInt("level"), 0};
               data.subClassIdLvlBase.add(sub);
            }
         }
      } catch (Exception var227) {
         _log.log(Level.SEVERE, "Error in getSinglePetitionPlayerData:", (Throwable)var227);
      }

      return data;
   }

   private CommunityClan.ClanPetitionData getClanPetitionsData(Player player, int senderId) {
      CommunityClan.ClanPetitionData data = new CommunityClan.ClanPetitionData();
      Player sender = World.getInstance().getPlayer(senderId);
      boolean haveclan = false;
      if (sender != null) {
         data.char_name = sender.getName();
         data.online = "<font color=6a9b54>" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "</font>";
         data.pvpKills = sender.getPvpKills();
         data.onlineTime = sender.getTotalOnlineTime();
         data.isNoble = sender.isNoble();
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills,onlinetime,clanid FROM characters WHERE charId = '" + senderId + "'");
            ResultSet rset = statement.executeQuery();
         ) {
            if (rset.next()) {
               data.char_name = rset.getString("char_name");
               data.online = "<font color=9b5454>" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</font>";
               data.pvpKills = rset.getInt("pvpkills");
               data.onlineTime = rset.getLong("onlinetime");
               if (rset.getInt("clanid") > 0) {
                  haveclan = true;
               }
            }

            try (
               PreparedStatement statementx = con.prepareStatement("SELECT nobless FROM characters WHERE charId = '" + senderId + "'");
               ResultSet rsetx = statementx.executeQuery();
            ) {
               if (rsetx.next() && rsetx.getInt("nobless") > 0) {
                  data.isNoble = true;
               }
            }
         } catch (Exception var133) {
            _log.log(Level.SEVERE, "Error in getClanPetitionsData:", (Throwable)var133);
         }
      }

      return haveclan ? null : data;
   }

   private String getConvertedAdena(Player player, long adena) {
      String text = "";
      String convertedAdena = String.valueOf(adena);
      int ks = (convertedAdena.length() - 1) / 3;
      long firstValue = adena / (long)Math.pow(1000.0, (double)ks);
      text = firstValue + this.getKs(player, ks);
      if ((convertedAdena.length() - 2) / 3 < ks) {
         adena -= firstValue * (long)Math.pow(1000.0, (double)ks);
         if (adena / (long)Math.pow(1000.0, (double)(ks - 1)) > 0L) {
            text = text + " " + adena / (long)((int)Math.pow(1000.0, (double)(ks - 1))) + this.getKs(player, ks - 1);
         }
      }

      return text;
   }

   private String getKs(Player player, int howMany) {
      String x = "";

      for(int i = 0; i < howMany; ++i) {
         x = x + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.K");
      }

      return x;
   }

   public String getUnitName(Player player, int type) {
      String subUnitName = "";
      switch(type) {
         case 0:
            subUnitName = ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.MAIN_CLAN");
            break;
         case 100:
         case 200:
            subUnitName = ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.ROYAL_GUARD");
            break;
         default:
            subUnitName = ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.ORDER_KNIGHT");
      }

      return subUnitName;
   }

   private void sendErrorMessage(Player player, String message, String backPage) {
      this.sendInfoMessage(player, message, backPage, true);
   }

   private void sendInfoMessage(Player player, String message, String backPage, boolean error) {
      String html = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.CLAN_RECRUIT") + "</title></head><body>";
      html = html + "<table border=0 cellpadding=0 cellspacing=0 width=700><tr><td><br><br>";
      html = html + "<center><font color = \"" + (error ? "9b5454" : "6a9b54") + "\">";
      html = html + message;
      html = html + "</font><br><br><br>";
      html = html
         + "<button action=\"bypass "
         + backPage
         + "\" value=\""
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.BACK")
         + "\" width=130 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\">";
      html = html + "</center></td></tr></table></body></html>";
      separateAndSend(html, player);
   }

   private String getMainStatsTableColor(int index) {
      return index % 2 == 0 ? "222320" : "191919";
   }

   private String getAllClansRank(Player player, int page) {
      Clan[] clans = ClanHolder.getInstance().getClans();
      Arrays.sort(clans, this._clansComparator);
      String text = "<table border=0 width=760>";
      text = text + "<tr><td align=center height=30>";
      text = text + "<table border=0 width=760 bgcolor=" + this.getMainStatsTableColor(0) + " height=30><tr>";
      text = text
         + "<td align=left width=40><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.RANK")
         + "</font></td>";
      text = text
         + "<td align=center width=220><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.CLAN_INFO")
         + "</font></td>";
      text = text
         + "<td align=center width=120><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.LEADER")
         + "</font></td>";
      text = text
         + "<td align=center width=120><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.ALLY")
         + "</font></td>";
      text = text
         + "<td align=center width=50><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.CLAN_LVL")
         + "</font></td>";
      text = text
         + "<td align=center width=230><font color=\"FFFFFF\">"
         + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.RECRUITMENT")
         + "</font></td>";
      text = text + "</tr></table></td></tr>";
      int max = Math.min(6 + 6 * page, clans.length);
      int index = 0;

      for(int i = 6 * page; i < max; ++i) {
         Clan clan = clans[i];
         text = text + "<tr><td align=center height=30>";
         text = text + "<table border=0 width=760 bgcolor=\"" + this.getMainStatsTableColor(index + 1) + "\" height=30>";
         text = text + "<tr><td width=40>";
         text = text + "<font name=\"__SYSTEMWORLDFONT\" color=FFFFFF>#" + (i + 1) + "</font></center>";
         text = text + "</td><td align=center width=210>";
         text = text + "<table cellspacing=0 cellpadding=0><tr>";
         if ((clan.getAllyId() <= 0 || clan.getAllyCrestId() <= 0) && clan.getCrestId() == 0) {
            text = text + "<td width=46>&nbsp;</td>";
         } else {
            text = text + "<td width=46 align=center>";
            text = text + "<table fixwidth=24 fixheight=12 cellpadding=0 cellspacing=0>";
            text = text + "<tr><td height=12></td></tr>";
            text = text + "<tr>";
            if (clan.getAllyId() > 0 && clan.getAllyCrestId() > 0) {
               text = text + "<td>";
               text = text + "<table height=8 cellpadding=0 cellspacing=0 background=Crest.crest_" + Config.REQUEST_ID + "_" + clan.getAllyCrestId() + ">";
               text = text + "<tr><td fixwidth=8><img height=4 width=8 src=L2UI.SquareBlack>&nbsp;</td></tr>";
               text = text + "</table></td>";
            }

            if (clan.getCrestId() != 0) {
               text = text + "<td>";
               text = text + "<table height=8 cellpadding=0 cellspacing=0 background=Crest.crest_" + Config.REQUEST_ID + "_" + clan.getCrestId() + ">";
               text = text + "<tr><td fixwidth=16><img height=4 width=16 src=L2UI.SquareBlack>&nbsp;</td></tr>";
               text = text + "</table></td>";
            }

            text = text + "</tr></table></td>";
         }

         text = text + "<td width=130 align=left valign=bottom>";
         text = text
            + "<button action=\"bypass _bbsclclan_"
            + clan.getId()
            + "\" value=\""
            + clan.getName()
            + "\" width=160 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>";
         text = text + "</td>";
         text = text + "</tr></table>";
         text = text + "</td><td width=115>";
         text = text + "<center><font name=\"__SYSTEMWORLDFONT\" color=\"FFFFFF\">" + clan.getLeaderName() + "</font></center>";
         text = text + "</td><td width=115>";
         text = text
            + "<center><font name=\"__SYSTEMWORLDFONT\" color=\"FFFFFF\">"
            + (
               clan.getAllyId() > 0
                  ? clan.getAllyName()
                  : "<font name=\"__SYSTEMWORLDFONT\" color=\"A18C70\">" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "</fomt>"
            )
            + "</font>";
         text = text + "</td><td width=60>";
         text = text + "<center><font name=\"__SYSTEMWORLDFONT\" color=\"FFFFFF\">" + clan.getLevel() + "</font></center>";
         text = text + "</td><td width=220>";
         if (!clan.isRecruting() || clan.isFull()) {
            text = text
               + "<center><button action=\"bypass _bbsclanlist_"
               + page
               + "\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.RECRUT_CLOSE")
               + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"></center>";
         } else if (player.getClan() != null) {
            text = text
               + "<center><button action=\"bypass _bbsclanlist_"
               + page
               + "\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.RECRUT_OPEN")
               + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"></center>";
         } else {
            text = text
               + "<center><button action=\"bypass _bbsclanjoin_"
               + clan.getId()
               + "_0\" value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.JOIN_CLAN")
               + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"></center>";
         }

         text = text + "</td></tr></table>";
         text = text + "</td></tr>";
         ++index;
      }

      text = text + "</table>";
      text = text + "<table width=700><tr><td width=350>";
      if (page > 0) {
         text = text
            + "<center><button action=\"bypass _bbsclanlist_"
            + (page - 1)
            + "\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.PREVIOUS")
            + "\" width=140 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\"></center>";
      }

      text = text + "</td><td width=350>";
      if (clans.length > 6 + 6 * page) {
         text = text
            + "<center><button action=\"bypass _bbsclanlist_"
            + (page + 1)
            + "\" value=\""
            + ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.NEXT")
            + "\" width=140 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.button_df\"></center>";
      }

      return text + "</td></tr></table>";
   }

   private boolean manageRecrutationWindow(Player player, int actionToken, String wholeText) {
      Clan clan;
      boolean failedAction;
      clan = player.getClan();
      failedAction = false;
      label68:
      switch(actionToken) {
         case 1:
            clan.getClassesNeeded().clear();

            for(int i = 88; i <= 118; ++i) {
               clan.addClassNeeded(i);
            }
            break;
         case 2:
            clan.getClassesNeeded().clear();
            break;
         case 3:
            if (wholeText.length() > 2) {
               String clazz = wholeText.substring(2);

               for(int i = 0; i < ALL_CLASSES.length; ++i) {
                  String className = Util.className(player, ALL_CLASSES[i].substring(0, 1).toLowerCase() + ALL_CLASSES[i].substring(1));
                  if (className.equals(clazz)) {
                     clan.addClassNeeded(88 + i);
                     break;
                  }
               }
            }
            break;
         case 4:
         default:
            failedAction = true;
            break;
         case 5:
            String clazz = wholeText.substring(2);

            for(int i = 0; i < ALL_CLASSES.length; ++i) {
               String className = Util.className(player, ALL_CLASSES[i].substring(0, 1).toLowerCase() + ALL_CLASSES[i].substring(1));
               if (className.equals(clazz)) {
                  clan.deleteClassNeeded(88 + i);
                  break label68;
               }
            }
            break;
         case 6:
            String[] questions = clan.getQuestions();
            StringTokenizer st = new StringTokenizer(wholeText.substring(2), "|");

            for(int i = 0; i < 8; ++i) {
               String question = st.nextToken();
               if (question.length() > 3) {
                  questions[i] = question;
               }

               clan.setQuestions(questions);
            }
            break;
         case 7:
            clan.setRecrutating(!clan.isRecruting());
      }

      if (!failedAction) {
         clan.updateRecrutationData();
      }

      return false;
   }

   private boolean manageClanJoinWindow(Player player, Clan clan, String text) {
      StringTokenizer st = new StringTokenizer(text, "|");
      String[] answers = new String[8];

      for(int i = 0; i < 8; ++i) {
         String answer = st.nextToken();
         answers[i] = answer;
      }

      String comment = st.nextToken();
      return clan.addPetition(player.getObjectId(), answers, comment);
   }

   private void managePlayerPetition(Player player, int senderId, int action) {
      Player sender = World.getInstance().getPlayer(senderId);
      Clan clan = player.getClan();
      switch(action) {
         case 1:
            int type = -1;

            for(ClanMember unit : clan.getMembers()) {
               if (clan.getSubPledgeMembersCount(unit.getPledgeType()) < clan.getMaxNrOfMembers(unit.getPledgeType())) {
                  type = unit.getPledgeType();
               }
            }

            if (type == -1) {
               this.sendErrorMessage(
                  player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.CLAN_FULL"), "_bbsclanplayerpetition_" + senderId
               );
               return;
            }

            if (sender != null) {
               sender.setPledgeType(type);
               if (type == -1) {
                  sender.setPowerGrade(9);
                  sender.setLvlJoinedAcademy(sender.getLevel());
               } else {
                  sender.setPowerGrade(5);
               }

               clan.addClanMember(sender);
               sender.setClanPrivileges(clan.getRankPrivs(sender.getPowerGrade()));
               sender.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
               sm.addString(sender.getName());
               clan.broadcastToOnlineMembers(sm);
               SystemMessage var42 = null;
               if (clan.getCastleId() > 0) {
                  CastleManager.getInstance().getCastleByOwner(clan).giveResidentialSkills(sender);
               }

               if (clan.getFortId() > 0) {
                  FortManager.getInstance().getFortByOwner(clan).giveResidentialSkills(sender);
               }

               sender.sendSkillList(false);
               clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(sender), sender);
               clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
               sender.sendPacket(new PledgeShowMemberListAll(clan, sender));
               sender.setClanJoinExpiryTime(0L);
               sender.broadcastCharInfo();
            } else {
               int powerGrade = 5;
               if (type == -1) {
                  powerGrade = 9;
               }

               try (
                  Connection con = DatabaseFactory.getInstance().getConnection();
                  PreparedStatement statement = con.prepareStatement(
                     "UPDATE characters SET clanid="
                        + clan.getId()
                        + ", subpledge="
                        + type
                        + ", power_grade="
                        + powerGrade
                        + " WHERE charId="
                        + senderId
                        + " AND clanid=0"
                  );
               ) {
                  statement.execute();
               } catch (Exception var40) {
                  _log.log(Level.SEVERE, "Error in managePlayerPetition:", (Throwable)var40);
               }

               clan.addClanMember(this.getSubUnitMember(clan, type, senderId));
            }

            this.sendInfoMessage(
               player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.HAS_BEED_ADD"), "_bbsclanpetitions_" + clan.getId(), false
            );
         case 2:
            clan.deletePetition(senderId);
            if (action == 2) {
               this.sendInfoMessage(
                  player, ServerStorage.getInstance().getString(player.getLang(), "CommunityClan.PETITION_DELETE"), "_bbsclanpetitions_" + clan.getId(), false
               );
            }
      }
   }

   protected ClanMember getSubUnitMember(Clan clan, int type, int memberId) {
      ClanMember member = null;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name,level,classid,pvpkills,charId,subpledge,title,power_grade,apprentice,sponsor,sex,race FROM characters WHERE charId=?"
         );
      ) {
         statement.setInt(1, memberId);

         try (ResultSet rset = statement.executeQuery()) {
            if (rset.next()) {
               member = new ClanMember(clan, rset);
            }
         }
      } catch (Exception var63) {
         _log.log(Level.SEVERE, "Error in managePlayerPetition:", (Throwable)var63);
      }

      return member;
   }

   private String[] getNotChosenClasses(Player player, Clan clan) {
      String[] splited = new String[]{"", ""};
      ArrayList<Integer> classes = clan.getClassesNeeded();

      for(int i = 0; i < ALL_CLASSES.length; ++i) {
         if (!classes.contains(i + 88)) {
            int x = 1;
            if (i % 2 == 0) {
               x = 0;
            }

            if (!splited[x].equals("")) {
               splited[x] = splited[x] + ";";
            }

            splited[x] = splited[x] + Util.className(player, ALL_CLASSES[i].substring(0, 1).toLowerCase() + ALL_CLASSES[i].substring(1));
         }
      }

      return splited;
   }

   private void clanNotice(Player player, int clanId) {
      Clan cl = ClanHolder.getInstance().getClan(clanId);
      if (cl != null) {
         if (cl.getLevel() < 2) {
            player.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN);
            this.onBypassCommand("_bbsclclan_" + player.getClanId(), player);
         } else {
            StringBuilder html = StringUtil.startAppend(
               2000,
               "<html><body><center><br><table border=0 cellspacing=0 cellpadding=0 width=755><tr><td width=755 height=38 align=center valign=top><font name=hs12 color=FF6633>"
                  + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.CLAN_NOTICE")
                  + "</font></td></tr></table><img src=\"L2UI.SquareGray\" width=770 height=1><table border=0 cellspacing=0 cellpadding=0><tr><td height=365><center>"
            );
            StringUtil.append(
               html,
               "<br><br><center><table width=610 border=0 cellspacing=0 cellpadding=0><tr><td fixwidth=610><font color=\"AAAAAA\">"
                  + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.NOTICE_FOR_CL")
                  + "</font> </td></tr><tr><td height=20></td></tr>"
            );
            if (player.getClan().isNoticeEnabled()) {
               StringUtil.append(
                  html,
                  "<tr><td fixwidth=610>"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.NOTICE_FUNCTION")
                     + ":&nbsp;&nbsp;&nbsp;"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.ON")
                     + "&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclannoticedisable\">"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.OFF")
                     + "</a>"
               );
            } else {
               StringUtil.append(
                  html,
                  "<tr><td fixwidth=610>"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.NOTICE_FUNCTION")
                     + ":&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclannoticeenable\">"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.ON")
                     + "</a>&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;"
                     + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.OFF")
                     + ""
               );
            }

            StringUtil.append(
               html,
               "</td></tr></table><br><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\"><br> <br><table width=610 border=0 cellspacing=2 cellpadding=0><tr><td>"
                  + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.NOTICE_EDITE")
                  + ": </td></tr><tr><td height=5></td></tr><tr><td><MultiEdit var =\"Content\" width=610 height=150></td></tr></table><br><table width=610 border=0 cellspacing=0 cellpadding=0><tr><td height=5></td></tr><tr><td align=center FIXWIDTH=65><button value=\""
                  + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.CHANGE")
                  + "\" action=\"Write Notice Set _ Content Content Content\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td><td align=center FIXWIDTH=65><button value = \""
                  + ServerStorage.getInstance().getString(player.getLang(), "ClanBBS.BACK")
                  + "\" action=\"bypass _bbsclclan_"
                  + clanId
                  + "\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\"></td><td align=center FIXWIDTH=500></td></tr></table></td></tr></table></center></center></body></html>"
            );
            this.send1001(html.toString(), player);
            this.send1002(player, player.getClan().getNotice(), " ", "0");
         }
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
      if (command.equals("Notice") && ar1.equals("Set")) {
         player.getClan().setNotice(ar4);
         player.sendPacket(SystemMessageId.CLAN_NOTICE_SAVED);
         this.onBypassCommand("_bbsclannoticeform", player);
      }
   }

   public static CommunityClan getInstance() {
      return CommunityClan.SingletonHolder._instance;
   }

   private class ClanComparator implements Comparator<Clan> {
      private ClanComparator() {
      }

      public int compare(Clan o1, Clan o2) {
         if (o1.getLevel() > o2.getLevel()) {
            return -1;
         } else if (o2.getLevel() > o1.getLevel()) {
            return 1;
         } else if (o1.getReputationScore() > o2.getReputationScore()) {
            return -1;
         } else {
            return o2.getReputationScore() > o1.getReputationScore() ? 1 : 0;
         }
      }
   }

   private class ClanPetitionData {
      String char_name;
      String online;
      int pvpKills;
      long onlineTime;
      boolean isNoble;

      private ClanPetitionData() {
      }
   }

   private class OfflinePlayerInventoryData {
      String char_name;
      int clanId;
      Map<Integer, int[]> itemIdAndEnchantForSlot = new HashMap<>();

      private OfflinePlayerInventoryData() {
      }
   }

   private class OfflineSinglePlayerData {
      String char_name;
      String title = "";
      int pvpKills;
      int pkKills;
      long onlineTime;
      int rec_have;
      int sevenSignsSide = 0;
      int fame;
      int clanId;
      String clan_name = "";
      int pledge_type = 0;
      boolean isClanLeader = false;
      boolean isNoble = false;
      boolean isHero = false;
      long adenaCount = 0L;
      List<int[]> subClassIdLvlBase = new ArrayList<>();

      private OfflineSinglePlayerData() {
      }
   }

   private class PetitionPlayerData {
      String char_name;
      long onlineTime;
      int pvpKills;
      int pkKills;
      int fame;
      long adenaCount = 0L;
      List<int[]> subClassIdLvlBase = new ArrayList<>();

      private PetitionPlayerData() {
      }
   }

   private static class SingletonHolder {
      protected static final CommunityClan _instance = new CommunityClan();
   }
}
