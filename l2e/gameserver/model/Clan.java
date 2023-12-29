package l2e.gameserver.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.ClanDAO;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.listener.clan.ClanCreationListener;
import l2e.gameserver.listener.clan.ClanMembershipListener;
import l2e.gameserver.listener.events.ClanCreationEvent;
import l2e.gameserver.listener.events.ClanJoinEvent;
import l2e.gameserver.listener.events.ClanLeaderChangeEvent;
import l2e.gameserver.listener.events.ClanLeaveEvent;
import l2e.gameserver.listener.events.ClanLevelUpEvent;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExSubPledgeSkillAdd;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.PledgeSkillListAdd;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Clan implements IIdentifiable {
   private static final Logger _log = Logger.getLogger(Clan.class.getName());
   private static final String INSERT_CLAN_DATA = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String SELECT_CLAN_DATA = "SELECT * FROM clan_data where clan_id=?";
   public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
   public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
   public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
   public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
   public static final int CP_NOTHING = 0;
   public static final int CP_CL_JOIN_CLAN = 2;
   public static final int CP_CL_GIVE_TITLE = 4;
   public static final int CP_CL_VIEW_WAREHOUSE = 8;
   public static final int CP_CL_MANAGE_RANKS = 16;
   public static final int CP_CL_PLEDGE_WAR = 32;
   public static final int CP_CL_DISMISS = 64;
   public static final int CP_CL_REGISTER_CREST = 128;
   public static final int CP_CL_APPRENTICE = 256;
   public static final int CP_CL_TROOPS_FAME = 512;
   public static final int CP_CL_SUMMON_AIRSHIP = 1024;
   public static final int CP_CH_OPEN_DOOR = 2048;
   public static final int CP_CH_OTHER_RIGHTS = 4096;
   public static final int CP_CH_AUCTION = 8192;
   public static final int CP_CH_DISMISS = 16384;
   public static final int CP_CH_SET_FUNCTIONS = 32768;
   public static final int CP_CS_OPEN_DOOR = 65536;
   public static final int CP_CS_MANOR_ADMIN = 131072;
   public static final int CP_CS_MANAGE_SIEGE = 262144;
   public static final int CP_CS_USE_FUNCTIONS = 524288;
   public static final int CP_CS_DISMISS = 1048576;
   public static final int CP_CS_TAXES = 2097152;
   public static final int CP_CS_MERCENARIES = 4194304;
   public static final int CP_CS_SET_FUNCTIONS = 8388608;
   public static final int CP_ALL = 16777214;
   public static final int SUBUNIT_ACADEMY = -1;
   public static final int SUBUNIT_ROYAL1 = 100;
   public static final int SUBUNIT_ROYAL2 = 200;
   public static final int SUBUNIT_KNIGHT1 = 1001;
   public static final int SUBUNIT_KNIGHT2 = 1002;
   public static final int SUBUNIT_KNIGHT3 = 2001;
   public static final int SUBUNIT_KNIGHT4 = 2002;
   private static List<ClanCreationListener> clanCreationListeners = new LinkedList<>();
   private static List<ClanMembershipListener> clanMembershipListeners = new LinkedList<>();
   private String _name;
   private int _clanId;
   private ClanMember _leader;
   private final Map<Integer, ClanMember> _members = new ConcurrentHashMap<>();
   private String _allyName;
   private int _allyId;
   private int _level;
   private int _castleId;
   private int _fortId;
   private int _hideoutId;
   private int _hiredGuards;
   private int _crestId;
   private int _crestLargeId;
   private int _allyCrestId;
   private int _auctionBiddedAt = 0;
   private long _allyPenaltyExpiryTime;
   private int _allyPenaltyType;
   private long _charPenaltyExpiryTime;
   private long _dissolvingExpiryTime;
   private int _bloodAllianceCount;
   private int _bloodOathCount;
   private final ItemContainer _warehouse = new ClanWarehouse(this);
   private final Set<Integer> _atWarWith = ConcurrentHashMap.newKeySet();
   private final Set<Integer> _atWarAttackers = ConcurrentHashMap.newKeySet();
   private final Map<Integer, Skill> _skills = new ConcurrentHashMap<>();
   private final Map<Integer, Clan.RankPrivs> _privs = new ConcurrentHashMap<>();
   private final Map<Integer, Clan.SubPledge> _subPledges = new ConcurrentHashMap<>();
   private final Map<Integer, Skill> _subPledgeSkills = new ConcurrentHashMap<>();
   private int _reputationScore = 0;
   private String _notice;
   private boolean _noticeEnabled = false;
   private static final int MAX_NOTICE_LENGTH = 8192;
   private int _newLeaderId;
   private AtomicInteger _siegeKills;
   private AtomicInteger _siegeDeaths;
   private boolean _recruting = false;
   private final ArrayList<Integer> _classesNeeded = new ArrayList<>();
   private String[] _questions = new String[8];
   private final ArrayList<Clan.SinglePetition> _petitions = new ArrayList<>();
   private static final Clan.ClanReputationComparator REPUTATION_COMPARATOR = new Clan.ClanReputationComparator();
   private static final int REPUTATION_PLACES = 100;

   public Clan(int clanId) {
      this._clanId = clanId;
      this.initializePrivs();
      this.restore();
      this.getWarehouse().restore();
   }

   public Clan(int clanId, String clanName) {
      this._clanId = clanId;
      this._name = clanName;
      this.initializePrivs();
      this.fireClanCreationListeners();
   }

   @Override
   public int getId() {
      return this._clanId;
   }

   public void setClanId(int clanId) {
      this._clanId = clanId;
   }

   public int getLeaderId() {
      return this._leader != null ? this._leader.getObjectId() : 0;
   }

   public ClanMember getLeader() {
      return this._leader;
   }

   public void setLeader(ClanMember leader) {
      this._leader = leader;
      this._members.put(leader.getObjectId(), leader);
   }

   public void setNewLeader(ClanMember member) {
      Player newLeader = member.getPlayerInstance();
      ClanMember exMember = this.getLeader();
      Player exLeader = exMember.getPlayerInstance();
      if (this.fireClanLeaderChangeListeners(newLeader, exLeader)) {
         if (exLeader != null) {
            if (exLeader.isFlying()) {
               exLeader.dismount();
            }

            if (this.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel()) {
               SiegeManager.getInstance().removeSiegeSkills(exLeader);
            }

            exLeader.setClanPrivileges(0);
            exLeader.broadcastCharInfo();
         } else {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?");
            ) {
               statement.setInt(1, 0);
               statement.setInt(2, this.getLeaderId());
               statement.execute();
            } catch (Exception var97) {
               _log.log(Level.WARNING, "Couldn't update clan privs for old clan leader", (Throwable)var97);
            }
         }

         this.setLeader(member);
         if (this.getNewLeaderId() != 0) {
            this.setNewLeaderId(0, true);
         }

         this.updateClanInDB();
         if (exLeader != null) {
            exLeader.setPledgeClass(ClanMember.calculatePledgeClass(exLeader));
            exLeader.broadcastCharInfo();
            exLeader.checkItemRestriction();
         }

         if (newLeader != null) {
            newLeader.setPledgeClass(ClanMember.calculatePledgeClass(newLeader));
            newLeader.setClanPrivileges(16777214);
            if (this.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel()) {
               SiegeManager.getInstance().addSiegeSkills(newLeader);
            }

            newLeader.broadcastCharInfo();
         } else {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?");
            ) {
               statement.setInt(1, 16777214);
               statement.setInt(2, this.getLeaderId());
               statement.execute();
            } catch (Exception var93) {
               _log.log(Level.WARNING, "Couldn't update clan privs for new clan leader", (Throwable)var93);
            }
         }

         this.broadcastClanStatus();
         this.broadcastToOnlineMembers(
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1).addString(member.getName())
         );
         _log.log(Level.INFO, "Leader of Clan: " + this.getName() + " changed to: " + member.getName() + " ex leader: " + exMember.getName());
      }
   }

   public String getLeaderName() {
      if (this._leader == null) {
         _log.warning(Clan.class.getName() + ": Clan " + this.getName() + " without clan leader!");
         return "";
      } else {
         return this._leader.getName();
      }
   }

   public String getName() {
      return this._name;
   }

   public void setName(String name) {
      this._name = name;
   }

   public void addClanMember(ClanMember member) {
      this._members.put(member.getObjectId(), member);
   }

   public void addClanMember(Player player) {
      if (this.fireClanJoinListeners(player)) {
         ClanMember member = new ClanMember(this, player);
         this.addClanMember(member);
         member.setPlayerInstance(player);
         player.setClan(this);
         player.setPledgeClass(ClanMember.calculatePledgeClass(player));
         player.sendPacket(new PledgeShowMemberListUpdate(player));
         player.sendPacket(new PledgeSkillList(this));
         this.addSkillEffects(player);
      }
   }

   public void updateClanMember(Player player) {
      ClanMember member = new ClanMember(player.getClan(), player);
      if (player.isClanLeader()) {
         this.setLeader(member);
      }

      this.addClanMember(member);
   }

   public ClanMember getClanMember(String name) {
      for(ClanMember temp : this._members.values()) {
         if (temp.getName().equals(name)) {
            return temp;
         }
      }

      return null;
   }

   public ClanMember getClanMember(int objectId) {
      return this._members.get(objectId);
   }

   public void removeClanMember(int objectId, long clanJoinExpiryTime) {
      if (this.fireClanLeaveListeners(objectId)) {
         ClanMember exMember = this._members.remove(objectId);
         if (exMember == null) {
            _log.warning("Member Object ID: " + objectId + " not found in clan while trying to remove");
         } else {
            int leadssubpledge = this.getLeaderSubPledge(objectId);
            if (leadssubpledge != 0) {
               this.getSubPledge(leadssubpledge).setLeaderId(0);
               this.updateSubPledgeInDB(leadssubpledge);
            }

            if (exMember.getApprentice() != 0) {
               ClanMember apprentice = this.getClanMember(exMember.getApprentice());
               if (apprentice != null) {
                  if (apprentice.getPlayerInstance() != null) {
                     apprentice.getPlayerInstance().setSponsor(0);
                  } else {
                     apprentice.setApprenticeAndSponsor(0, 0);
                  }

                  apprentice.saveApprenticeAndSponsor(0, 0);
               }
            }

            if (exMember.getSponsor() != 0) {
               ClanMember sponsor = this.getClanMember(exMember.getSponsor());
               if (sponsor != null) {
                  if (sponsor.getPlayerInstance() != null) {
                     sponsor.getPlayerInstance().setApprentice(0);
                  } else {
                     sponsor.setApprenticeAndSponsor(0, 0);
                  }

                  sponsor.saveApprenticeAndSponsor(0, 0);
               }
            }

            exMember.saveApprenticeAndSponsor(0, 0);
            if (Config.REMOVE_CASTLE_CIRCLETS) {
               CastleManager.getInstance().removeCirclet(exMember, this.getCastleId());
            }

            if (exMember.isOnline()) {
               Player player = exMember.getPlayerInstance();
               if (!player.isNoble()) {
                  player.setTitle("");
               }

               player.setApprentice(0);
               player.setSponsor(0);
               if (player.isClanLeader()) {
                  SiegeManager.getInstance().removeSiegeSkills(player);
                  player.setClanCreateExpiryTime(System.currentTimeMillis() + (long)Config.ALT_CLAN_CREATE_DAYS * 3600000L);
               }

               this.removeSkillEffects(player);
               if (player.getClan().getCastleId() > 0) {
                  Castle castle = CastleManager.getInstance().getCastleById(player.getClan().getCastleId());
                  if (castle != null) {
                     TerritoryWarManager.Territory territory = castle.getTerritory();
                     if (territory != null && territory.getLordObjectId() == player.getObjectId()) {
                        castle.getTerritory().changeOwner(null);
                     }
                  }

                  CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
               }

               if (player.getClan().getFortId() > 0) {
                  FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
               }

               player.sendSkillList(false);
               player.setClan(null);
               if (exMember.getPledgeType() != -1) {
                  player.setClanJoinExpiryTime(clanJoinExpiryTime);
               }

               player.setPledgeClass(ClanMember.calculatePledgeClass(player));
               player.broadcastUserInfo(true);
               player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
            } else {
               this.removeMemberInDatabase(
                  exMember,
                  clanJoinExpiryTime,
                  this.getLeaderId() == objectId ? System.currentTimeMillis() + (long)Config.ALT_CLAN_CREATE_DAYS * 3600000L : 0L
               );
            }
         }
      }
   }

   public ClanMember[] getMembers() {
      return this._members.values().toArray(new ClanMember[this._members.size()]);
   }

   public List<ClanMember> getAllMembers() {
      List<ClanMember> members = new ArrayList<>(this._members.size());

      for(ClanMember unit : this.getMembers()) {
         members.add(unit);
      }

      return members;
   }

   public int getAverageLevel() {
      int size = 0;
      int level = 0;

      for(ClanMember member : this.getMembers()) {
         ++size;
         level += member.getLevel();
      }

      return level / size;
   }

   public int getMembersCount() {
      return this._members.size();
   }

   public int getSubPledgeMembersCount(int subpl) {
      int result = 0;

      for(ClanMember temp : this._members.values()) {
         if (temp.getPledgeType() == subpl) {
            ++result;
         }
      }

      return result;
   }

   public int getMaxNrOfMembers(int pledgeType) {
      int limit = 0;
      switch(pledgeType) {
         case -1:
            limit = 20;
            break;
         case 0:
            switch(this.getLevel()) {
               case 0:
                  int limitxxxx = 10;
                  return limitxxxx;
               case 1:
                  int limitxxx = 15;
                  return limitxxx;
               case 2:
                  int limitxx = 20;
                  return limitxx;
               case 3:
                  int limitx = 30;
                  return limitx;
               default:
                  int limitxxxxx = 40;
                  return limitxxxxx;
            }
         case 100:
         case 200:
            switch(this.getLevel()) {
               case 11:
                  int limitx = 30;
                  return limitx;
               default:
                  int limitxx = 20;
                  return limitxx;
            }
         case 1001:
         case 1002:
         case 2001:
         case 2002:
            switch(this.getLevel()) {
               case 9:
               case 10:
               case 11:
                  limit = 25;
                  break;
               default:
                  limit = 10;
            }
      }

      return limit;
   }

   public List<Player> getOnlineMembers(int exclude) {
      List<Player> onlineMembers = new ArrayList<>();

      for(ClanMember temp : this._members.values()) {
         if (temp != null && temp.isOnline() && temp.getObjectId() != exclude) {
            onlineMembers.add(temp.getPlayerInstance());
         }
      }

      return onlineMembers;
   }

   public Player[] getOnlineMembers() {
      List<Player> list = new ArrayList<>();

      for(ClanMember temp : this._members.values()) {
         if (temp != null && temp.isOnline()) {
            list.add(temp.getPlayerInstance());
         }
      }

      return list.toArray(new Player[list.size()]);
   }

   public int getOnlineMembersCount() {
      int count = 0;

      for(ClanMember temp : this._members.values()) {
         if (temp != null && temp.isOnline()) {
            ++count;
         }
      }

      return count;
   }

   public int getAllyId() {
      return this._allyId;
   }

   public String getAllyName() {
      return this._allyName;
   }

   public void setAllyCrestId(int allyCrestId) {
      this._allyCrestId = allyCrestId;
   }

   public int getAllyCrestId() {
      return this._allyCrestId;
   }

   public int getLevel() {
      return this._level;
   }

   public void setLevel(int level) {
      this._level = level;
   }

   public int getCastleId() {
      return this._castleId;
   }

   public int getFortId() {
      return this._fortId;
   }

   public int getHideoutId() {
      return this._hideoutId;
   }

   public void setCrestId(int crestId) {
      this._crestId = crestId;
   }

   public int getCrestId() {
      return this._crestId;
   }

   public void setCrestLargeId(int crestLargeId) {
      this._crestLargeId = crestLargeId;
   }

   public int getCrestLargeId() {
      return this._crestLargeId;
   }

   public void setAllyId(int allyId) {
      this._allyId = allyId;
   }

   public void setAllyName(String allyName) {
      this._allyName = allyName;
   }

   public void setCastleId(int castleId) {
      this._castleId = castleId;
   }

   public void setFortId(int fortId) {
      this._fortId = fortId;
   }

   public void setHideoutId(int hideoutId) {
      this._hideoutId = hideoutId;
   }

   public boolean isMember(int id) {
      return id == 0 ? false : this._members.containsKey(id);
   }

   public int getBloodAllianceCount() {
      return this._bloodAllianceCount;
   }

   public void increaseBloodAllianceCount() {
      this._bloodAllianceCount += SiegeManager.getInstance().getBloodAllianceReward();
      this.updateBloodAllianceCountInDB();
   }

   public void resetBloodAllianceCount() {
      this._bloodAllianceCount = 0;
      this.updateBloodAllianceCountInDB();
   }

   public void updateBloodAllianceCountInDB() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET blood_alliance_count=? WHERE clan_id=?");
      ) {
         statement.setInt(1, this.getBloodAllianceCount());
         statement.setInt(2, this.getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public int getBloodOathCount() {
      return this._bloodOathCount;
   }

   public void increaseBloodOathCount() {
      this._bloodOathCount += Config.FS_BLOOD_OATH_COUNT;
      this.updateBloodOathCountInDB();
   }

   public void resetBloodOathCount() {
      this._bloodOathCount = 0;
      this.updateBloodOathCountInDB();
   }

   public void updateBloodOathCountInDB() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_oath_count=? WHERE clan_id=?");
      ) {
         ps.setInt(1, this.getBloodOathCount());
         ps.setInt(2, this.getId());
         ps.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public void updateClanScoreInDB() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
      ) {
         statement.setInt(1, this.getReputationScore());
         statement.setInt(2, this.getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception on updateClanScoreInDb(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public void updateClanNameInDB() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_name=? WHERE clan_id=?");
         statement.setString(1, this.getName());
         statement.setInt(2, this.getId());
         statement.execute();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Error saving clan name: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void updateClanInDB() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=?,new_leader_id=? WHERE clan_id=?"
         );
         statement.setInt(1, this.getLeaderId());
         statement.setInt(2, this.getAllyId());
         statement.setString(3, this.getAllyName());
         statement.setInt(4, this.getReputationScore());
         statement.setLong(5, this.getAllyPenaltyExpiryTime());
         statement.setInt(6, this.getAllyPenaltyType());
         statement.setLong(7, this.getCharPenaltyExpiryTime());
         statement.setLong(8, this.getDissolvingExpiryTime());
         statement.setInt(9, this.getNewLeaderId());
         statement.setInt(10, this.getId());
         statement.execute();
         statement.close();
         if (Config.DEBUG) {
            _log.fine("New clan leader saved in db: " + this.getId());
         }
      } catch (Exception var14) {
         _log.log(Level.SEVERE, "Error saving clan: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void store() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)"
         );
      ) {
         ps.setInt(1, this.getId());
         ps.setString(2, this.getName());
         ps.setInt(3, this.getLevel());
         ps.setInt(4, this.getCastleId());
         ps.setInt(5, this.getBloodAllianceCount());
         ps.setInt(6, this.getBloodOathCount());
         ps.setInt(7, this.getAllyId());
         ps.setString(8, this.getAllyName());
         ps.setInt(9, this.getLeaderId());
         ps.setInt(10, this.getCrestId());
         ps.setInt(11, this.getCrestLargeId());
         ps.setInt(12, this.getAllyCrestId());
         ps.setInt(13, this.getNewLeaderId());
         ps.execute();
         if (Config.DEBUG) {
            _log.fine("New clan saved in db: " + this.getId());
         }
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Error saving new clan: " + var33.getMessage(), (Throwable)var33);
      }
   }

   private void removeMemberInDatabase(ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime) {
      if (member.getClan().getCastleId() > 0) {
         Castle castle = CastleManager.getInstance().getCastleById(member.getClan().getCastleId());
         if (castle != null) {
            TerritoryWarManager.Territory territory = castle.getTerritory();
            if (territory != null && territory.getLordObjectId() == member.getObjectId()) {
               castle.getTerritory().changeOwner(null);
            }
         }
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?"
         );
         statement.setString(1, "");
         statement.setLong(2, clanJoinExpiryTime);
         statement.setLong(3, clanCreateExpiryTime);
         statement.setInt(4, member.getObjectId());
         statement.execute();
         statement.close();
         if (Config.DEBUG) {
            _log.fine("clan member removed in db: " + this.getId());
         }

         statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
         statement.setInt(1, member.getObjectId());
         statement.execute();
         statement.close();
         statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
         statement.setInt(1, member.getObjectId());
         statement.execute();
         statement.close();
      } catch (Exception var19) {
         _log.log(Level.SEVERE, "Error removing clan member: " + var19.getMessage(), (Throwable)var19);
      }
   }

   private void restore() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_data where clan_id=?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet clanData = statement.executeQuery()) {
            if (clanData.next()) {
               this.setName(clanData.getString("clan_name"));
               this.setLevel(clanData.getInt("clan_level"));
               this.setCastleId(clanData.getInt("hasCastle"));
               this._bloodAllianceCount = clanData.getInt("blood_alliance_count");
               this._bloodOathCount = clanData.getInt("blood_oath_count");
               this.setAllyId(clanData.getInt("ally_id"));
               this.setAllyName(clanData.getString("ally_name"));
               this.setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
               if (this.getAllyPenaltyExpiryTime() < System.currentTimeMillis()) {
                  this.setAllyPenaltyExpiryTime(0L, 0);
               }

               this.setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
               if (this.getCharPenaltyExpiryTime() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L < System.currentTimeMillis()) {
                  this.setCharPenaltyExpiryTime(0L);
               }

               this.setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
               this.setCrestId(clanData.getInt("crest_id"));
               this.setCrestLargeId(clanData.getInt("crest_large_id"));
               this.setAllyCrestId(clanData.getInt("ally_crest_id"));
               this.setReputationScore(clanData.getInt("reputation_score"), false);
               this.setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
               this.setNewLeaderId(clanData.getInt("new_leader_id"), false);
               int leaderId = clanData.getInt("leader_id");
               statement.clearParameters();

               try (PreparedStatement select = con.prepareStatement(
                     "SELECT char_name,level,pvpkills,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"
                  )) {
                  select.setInt(1, this.getId());

                  try (ResultSet clanMember = select.executeQuery()) {
                     ClanMember member = null;

                     while(clanMember.next()) {
                        member = new ClanMember(this, clanMember);
                        if (member.getObjectId() == leaderId) {
                           this.setLeader(member);
                        } else {
                           this.addClanMember(member);
                        }
                     }
                  }
               }
            }
         }

         if (Config.DEBUG && this.getName() != null) {
            _log.info("Restored clan data for \"" + this.getName() + "\" from database.");
         }

         this.restoreSubPledges();
         this.restoreRankPrivs();
         this.restoreSkills();
         this.restoreClanRecruitment();
         this.restoreNotice();
      } catch (Exception var133) {
         _log.log(Level.SEVERE, "Error restoring clan data: " + var133.getMessage(), (Throwable)var133);
      }
   }

   private void restoreNotice() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet noticeData = statement.executeQuery()) {
            while(noticeData.next()) {
               this._noticeEnabled = noticeData.getBoolean("enabled");
               this._notice = noticeData.getString("notice");
            }
         }
      } catch (Exception var59) {
         _log.log(Level.SEVERE, "Error restoring clan notice: " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void storeNotice(String notice, boolean enabled) {
      if (notice == null) {
         notice = "";
      }

      if (notice.length() > 8192) {
         notice = notice.substring(0, 8191);
      }

      notice = notice.replace("<a", "");
      notice = notice.replace("</a>", "");
      notice = notice.replace("bypass", ".1.");

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO clan_notices (clan_id,notice,enabled) values (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?"
         );
      ) {
         statement.setInt(1, this.getId());
         statement.setString(2, notice);
         if (enabled) {
            statement.setString(3, "true");
         } else {
            statement.setString(3, "false");
         }

         statement.setString(4, notice);
         if (enabled) {
            statement.setString(5, "true");
         } else {
            statement.setString(5, "false");
         }

         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Error could not store clan notice: " + var35.getMessage(), (Throwable)var35);
      }

      this._notice = notice;
      this._noticeEnabled = enabled;
   }

   public void setNoticeEnabled(boolean enabled) {
      this.storeNotice(this._notice, enabled);
   }

   public void setNotice(String notice) {
      this.storeNotice(notice, this._noticeEnabled);
   }

   public boolean isNoticeEnabled() {
      return this._noticeEnabled;
   }

   public String getNotice() {
      return this._notice == null ? "" : this._notice;
   }

   private void restoreSkills() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,sub_pledge_id FROM clan_skills WHERE clan_id=?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int id = rset.getInt("skill_id");
               int level = rset.getInt("skill_level");
               Skill skill = SkillsParser.getInstance().getInfo(id, level);
               int subType = rset.getInt("sub_pledge_id");
               if (subType == -2) {
                  this._skills.put(skill.getId(), skill);
               } else if (subType == 0) {
                  this._subPledgeSkills.put(skill.getId(), skill);
               } else {
                  Clan.SubPledge subunit = this._subPledges.get(subType);
                  if (subunit != null) {
                     subunit.addNewSkill(skill);
                  } else {
                     _log.info("Missing subpledge " + subType + " for clan " + this + ", skill skipped.");
                  }
               }
            }
         }
      } catch (Exception var63) {
         _log.log(Level.SEVERE, "Error restoring clan skills: " + var63.getMessage(), (Throwable)var63);
      }
   }

   public final Skill[] getAllSkills() {
      return this._skills == null ? new Skill[0] : this._skills.values().toArray(new Skill[this._skills.values().size()]);
   }

   public Map<Integer, Skill> getSkills() {
      return this._skills;
   }

   public Skill addSkill(Skill newSkill) {
      Skill oldSkill = null;
      if (newSkill != null) {
         oldSkill = this._skills.put(newSkill.getId(), newSkill);
      }

      return oldSkill;
   }

   public Skill addNewSkill(Skill newSkill) {
      return this.addNewSkill(newSkill, -2);
   }

   public Skill addNewSkill(Skill newSkill, int subType) {
      Skill oldSkill = null;
      if (newSkill != null) {
         if (subType == -2) {
            oldSkill = this._skills.put(newSkill.getId(), newSkill);
         } else if (subType == 0) {
            oldSkill = this._subPledgeSkills.put(newSkill.getId(), newSkill);
         } else {
            Clan.SubPledge subunit = this.getSubPledge(subType);
            if (subunit == null) {
               _log.log(Level.WARNING, "Subpledge " + subType + " does not exist for clan " + this);
               return oldSkill;
            }

            oldSkill = subunit.addNewSkill(newSkill);
         }

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            if (oldSkill != null) {
               try (PreparedStatement statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?")) {
                  statement.setInt(1, newSkill.getLevel());
                  statement.setInt(2, oldSkill.getId());
                  statement.setInt(3, this.getId());
                  statement.execute();
               }
            } else {
               try (PreparedStatement statement = con.prepareStatement(
                     "INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,sub_pledge_id) VALUES (?,?,?,?,?)"
                  )) {
                  statement.setInt(1, this.getId());
                  statement.setInt(2, newSkill.getId());
                  statement.setInt(3, newSkill.getLevel());
                  statement.setString(4, newSkill.getNameEn());
                  statement.setInt(5, subType);
                  statement.execute();
               }
            }
         } catch (Exception var60) {
            _log.log(Level.WARNING, "Error could not store clan skills: " + var60.getMessage(), (Throwable)var60);
         }

         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
         sm.addSkillName(newSkill.getId());

         for(ClanMember temp : this._members.values()) {
            if (temp != null && temp.getPlayerInstance() != null && temp.isOnline()) {
               if (subType == -2) {
                  if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()) {
                     temp.getPlayerInstance().addSkill(newSkill, false);
                     temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
                     temp.getPlayerInstance().sendPacket(sm);
                     temp.getPlayerInstance().sendSkillList(false);
                  }
               } else if (temp.getPledgeType() == subType) {
                  temp.getPlayerInstance().addSkill(newSkill, false);
                  temp.getPlayerInstance().sendPacket(new ExSubPledgeSkillAdd(subType, newSkill.getId(), newSkill.getLevel()));
                  temp.getPlayerInstance().sendPacket(sm);
                  temp.getPlayerInstance().sendSkillList(false);
               }
            }
         }
      }

      return oldSkill;
   }

   public void addSkillEffects() {
      for(Skill skill : this._skills.values()) {
         for(ClanMember temp : this._members.values()) {
            try {
               if (temp != null && temp.isOnline() && skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()) {
                  temp.getPlayerInstance().addSkill(skill, false);
               }
            } catch (NullPointerException var6) {
               _log.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         }
      }
   }

   public void addSkillEffects(Player player) {
      if (player != null) {
         for(Skill skill : this._skills.values()) {
            if (skill.getMinPledgeClass() <= player.getPledgeClass()) {
               player.addSkill(skill, false);
            }
         }

         if (player.getPledgeType() == 0) {
            for(Skill skill : this._subPledgeSkills.values()) {
               player.addSkill(skill, false);
            }
         } else {
            Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
            if (subunit == null) {
               return;
            }

            for(Skill skill : subunit.getSkills()) {
               player.addSkill(skill, false);
            }
         }

         if (this._reputationScore < 0) {
            this.skillsStatus(player, true);
         }
      }
   }

   public void removeSkillEffects(Player player) {
      if (player != null) {
         for(Skill skill : this._skills.values()) {
            player.removeSkill(skill, false);
         }

         if (player.getPledgeType() == 0) {
            for(Skill skill : this._subPledgeSkills.values()) {
               player.removeSkill(skill, false);
            }
         } else {
            Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
            if (subunit == null) {
               return;
            }

            for(Skill skill : subunit.getSkills()) {
               player.removeSkill(skill, false);
            }
         }
      }
   }

   public void skillsStatus(Player player, boolean disable) {
      if (player != null) {
         for(Skill skill : this._skills.values()) {
            if (disable) {
               player.disableSkill(skill, -1L);
            } else {
               player.enableSkill(skill);
            }
         }

         if (player.getPledgeType() == 0) {
            for(Skill skill : this._subPledgeSkills.values()) {
               if (disable) {
                  player.disableSkill(skill, -1L);
               } else {
                  player.enableSkill(skill);
               }
            }
         } else {
            Clan.SubPledge subunit = this.getSubPledge(player.getPledgeType());
            if (subunit != null) {
               for(Skill skill : subunit.getSkills()) {
                  if (disable) {
                     player.disableSkill(skill, -1L);
                  } else {
                     player.enableSkill(skill);
                  }
               }
            }
         }
      }
   }

   public void broadcastToOnlineAllyMembers(GameServerPacket packet) {
      for(Clan clan : ClanHolder.getInstance().getClanAllies(this.getAllyId())) {
         clan.broadcastToOnlineMembers(packet);
      }
   }

   public void broadcastToOnlineMembers(GameServerPacket packet) {
      for(ClanMember member : this._members.values()) {
         if (member != null && member.isOnline()) {
            member.getPlayerInstance().sendPacket(packet);
         }
      }
   }

   public void broadcastCSToOnlineMembers(CreatureSay packet, Player broadcaster) {
      for(ClanMember member : this._members.values()) {
         if (member != null && member.isOnline() && !BlockedList.isBlocked(member.getPlayerInstance(), broadcaster)) {
            member.getPlayerInstance().sendPacket(packet);
         }
      }
   }

   public void broadcastToOtherOnlineMembers(GameServerPacket packet, Player player) {
      for(ClanMember member : this._members.values()) {
         if (member != null && member.isOnline() && member.getPlayerInstance() != player) {
            member.getPlayerInstance().sendPacket(packet);
         }
      }
   }

   @Override
   public String toString() {
      return this.getName() + "[" + this.getId() + "]";
   }

   public ItemContainer getWarehouse() {
      return this._warehouse;
   }

   public boolean isAtWarWith(Integer id) {
      return this._atWarWith.contains(id);
   }

   public boolean isAtWarWith(Clan clan) {
      return clan == null ? false : this._atWarWith.contains(clan.getId());
   }

   public boolean isAtWarAttacker(Integer id) {
      return this._atWarAttackers != null && !this._atWarAttackers.isEmpty() ? this._atWarAttackers.contains(id) : false;
   }

   public void setEnemyClan(Clan clan) {
      this._atWarWith.add(clan.getId());
   }

   public List<Clan> getEnemyClans() {
      List<Clan> _clanList = new ArrayList<>();

      for(Integer i : this._atWarWith) {
         _clanList.add(ClanHolder.getInstance().getClan(i));
      }

      return _clanList;
   }

   public void setEnemyClan(Integer clan) {
      this._atWarWith.add(clan);
   }

   public void setAttackerClan(Clan clan) {
      this._atWarAttackers.add(clan.getId());
   }

   public void setAttackerClan(Integer clan) {
      this._atWarAttackers.add(clan);
   }

   public void deleteEnemyClan(Clan clan) {
      Integer id = clan.getId();
      this._atWarWith.remove(id);
   }

   public void deleteAttackerClan(Clan clan) {
      Integer id = clan.getId();
      this._atWarAttackers.remove(id);
   }

   public int getHiredGuards() {
      return this._hiredGuards;
   }

   public void incrementHiredGuards() {
      ++this._hiredGuards;
   }

   public boolean isAtWar() {
      return this._atWarWith != null && !this._atWarWith.isEmpty();
   }

   public Set<Integer> getWarList() {
      return this._atWarWith;
   }

   public Set<Integer> getAttackerList() {
      return this._atWarAttackers;
   }

   public void broadcastClanStatus() {
      for(Player member : this.getOnlineMembers(0)) {
         member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
         member.sendPacket(new PledgeShowMemberListAll(this, member));
      }
   }

   private void restoreSubPledges() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int id = rset.getInt("sub_pledge_id");
               String name = rset.getString("name");
               int leaderId = rset.getInt("leader_id");
               Clan.SubPledge pledge = new Clan.SubPledge(id, name, leaderId);
               this._subPledges.put(id, pledge);
            }
         }
      } catch (Exception var62) {
         _log.log(Level.WARNING, "Could not restore clan sub-units: " + var62.getMessage(), (Throwable)var62);
      }
   }

   public final Clan.SubPledge getSubPledge(int pledgeType) {
      return this._subPledges == null ? null : this._subPledges.get(pledgeType);
   }

   public final Clan.SubPledge getSubPledge(String pledgeName) {
      if (this._subPledges == null) {
         return null;
      } else {
         for(Clan.SubPledge sp : this._subPledges.values()) {
            if (sp.getName().equalsIgnoreCase(pledgeName)) {
               return sp;
            }
         }

         return null;
      }
   }

   public final Clan.SubPledge[] getAllSubPledges() {
      return this._subPledges == null ? new Clan.SubPledge[0] : this._subPledges.values().toArray(new Clan.SubPledge[this._subPledges.values().size()]);
   }

   public Clan.SubPledge createSubPledge(Player player, int pledgeType, int leaderId, String subPledgeName) {
      Clan.SubPledge subPledge = null;
      pledgeType = this.getAvailablePledgeTypes(pledgeType);
      if (pledgeType == 0) {
         if (pledgeType == -1) {
            player.sendPacket(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
         } else {
            player.sendMessage("You can't create any more sub-units of this type");
         }

         return null;
      } else if (this._leader.getObjectId() == leaderId) {
         player.sendMessage("Leader is not correct");
         return null;
      } else if (pledgeType == -1
         || (this.getReputationScore() >= Config.ROYAL_GUARD_COST || pledgeType >= 1001)
            && (this.getReputationScore() >= Config.KNIGHT_UNIT_COST || pledgeType <= 200)) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)");
         ) {
            statement.setInt(1, this.getId());
            statement.setInt(2, pledgeType);
            statement.setString(3, subPledgeName);
            if (pledgeType != -1) {
               statement.setInt(4, leaderId);
            } else {
               statement.setInt(4, 0);
            }

            statement.execute();
            subPledge = new Clan.SubPledge(pledgeType, subPledgeName, leaderId);
            this._subPledges.put(pledgeType, subPledge);
            if (pledgeType != -1) {
               if (pledgeType < 1001) {
                  this.setReputationScore(this.getReputationScore() - Config.ROYAL_GUARD_COST, true);
               } else {
                  this.setReputationScore(this.getReputationScore() - Config.KNIGHT_UNIT_COST, true);
               }
            }

            if (Config.DEBUG) {
               _log.fine("New sub_clan saved in db: " + this.getId() + "; " + pledgeType);
            }
         } catch (Exception var38) {
            _log.log(Level.SEVERE, "Error saving sub clan data: " + var38.getMessage(), (Throwable)var38);
         }

         this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this._leader.getClan()));
         this.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, this._leader.getClan()));
         return subPledge;
      } else {
         player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
         return null;
      }
   }

   public int getAvailablePledgeTypes(int pledgeType) {
      if (this._subPledges.get(pledgeType) != null) {
         switch(pledgeType) {
            case -1:
               return 0;
            case 100:
               pledgeType = this.getAvailablePledgeTypes(200);
               break;
            case 200:
               return 0;
            case 1001:
               pledgeType = this.getAvailablePledgeTypes(1002);
               break;
            case 1002:
               pledgeType = this.getAvailablePledgeTypes(2001);
               break;
            case 2001:
               pledgeType = this.getAvailablePledgeTypes(2002);
               break;
            case 2002:
               return 0;
         }
      }

      return pledgeType;
   }

   public void updateSubPledgeInDB(int pledgeType) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?");
      ) {
         statement.setInt(1, this.getSubPledge(pledgeType).getLeaderId());
         statement.setString(2, this.getSubPledge(pledgeType).getName());
         statement.setInt(3, this.getId());
         statement.setInt(4, pledgeType);
         statement.execute();
         if (Config.DEBUG) {
            _log.fine("Subpledge updated in db: " + this.getId());
         }
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Error updating subpledge: " + var34.getMessage(), (Throwable)var34);
      }
   }

   private void restoreRankPrivs() {
      ClanDAO.getInstance().getPrivileges(this.getId()).forEach((rank, privileges) -> this._privs.get(rank).setPrivs(privileges));
   }

   public void initializePrivs() {
      for(int i = 1; i < 10; ++i) {
         Clan.RankPrivs privs = new Clan.RankPrivs(i, 0, 0);
         this._privs.put(i, privs);
      }
   }

   public int getRankPrivs(int rank) {
      return this._privs.get(rank) != null ? this._privs.get(rank).getPrivs() : 0;
   }

   public int countMembersByRank(int rank) {
      int ret = 0;

      for(ClanMember m : this.getAllMembers()) {
         if (m.getPowerGrade() == rank) {
            ++ret;
         }
      }

      return ret;
   }

   public void setRankPrivs(int rank, int privs) {
      if (this._privs.get(rank) != null) {
         this._privs.get(rank).setPrivs(privs);
         ClanDAO.getInstance().storePrivileges(this.getId(), rank, privs);

         for(ClanMember cm : this.getMembers()) {
            if (cm.isOnline() && cm.getPowerGrade() == rank && cm.getPlayerInstance() != null) {
               cm.getPlayerInstance().setClanPrivileges(privs);
               cm.getPlayerInstance().sendUserInfo();
            }
         }

         this.broadcastClanStatus();
      } else {
         this._privs.put(rank, new Clan.RankPrivs(rank, this.countMembersByRank(rank), privs));
         ClanDAO.getInstance().storePrivileges(this.getId(), rank, privs);
      }
   }

   public final Clan.RankPrivs[] getAllRankPrivs() {
      return this._privs == null ? new Clan.RankPrivs[0] : this._privs.values().toArray(new Clan.RankPrivs[this._privs.values().size()]);
   }

   public int getLeaderSubPledge(int leaderId) {
      int id = 0;

      for(Clan.SubPledge sp : this._subPledges.values()) {
         if (sp.getLeaderId() != 0 && sp.getLeaderId() == leaderId) {
            id = sp.getId();
         }
      }

      return id;
   }

   public synchronized void addReputationScore(int value, boolean save) {
      this.setReputationScore(this.getReputationScore() + value, save);
   }

   public synchronized void takeReputationScore(int value, boolean save) {
      this.setReputationScore(this.getReputationScore() - value, save);
   }

   private void setReputationScore(int value, boolean save) {
      if (this._reputationScore >= 0 && value < 0) {
         this.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));

         for(ClanMember member : this._members.values()) {
            if (member.isOnline() && member.getPlayerInstance() != null) {
               this.skillsStatus(member.getPlayerInstance(), true);
            }
         }
      } else if (this._reputationScore < 0 && value >= 0) {
         this.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));

         for(ClanMember member : this._members.values()) {
            if (member.isOnline() && member.getPlayerInstance() != null) {
               this.skillsStatus(member.getPlayerInstance(), false);
            }
         }
      }

      this._reputationScore = value;
      if (this._reputationScore > 100000000) {
         this._reputationScore = 100000000;
      }

      if (this._reputationScore < -100000000) {
         this._reputationScore = -100000000;
      }

      this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
      if (save) {
         this.updateClanScoreInDB();
      }
   }

   public int getReputationScore() {
      return this._reputationScore;
   }

   public int getRank() {
      Clan[] clans = ClanHolder.getInstance().getClans();
      Arrays.sort(clans, REPUTATION_COMPARATOR);
      int place = 1;

      for(int i = 0; i < clans.length; ++i) {
         if (i == 100) {
            return 0;
         }

         Clan clan = clans[i];
         if (clan == this) {
            return 1 + i;
         }
      }

      return 0;
   }

   public int getAuctionBiddedAt() {
      return this._auctionBiddedAt;
   }

   public void setAuctionBiddedAt(int id, boolean storeInDb) {
      this._auctionBiddedAt = id;
      if (storeInDb) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
         ) {
            statement.setInt(1, id);
            statement.setInt(2, this.getId());
            statement.execute();
         } catch (Exception var35) {
            _log.log(Level.WARNING, "Could not store auction for clan: " + var35.getMessage(), (Throwable)var35);
         }
      }
   }

   public boolean checkClanJoinCondition(Player activeChar, Player target, int pledgeType) {
      if (activeChar == null) {
         return false;
      } else if ((activeChar.getClanPrivileges() & 2) != 2) {
         activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         return false;
      } else if (target == null) {
         activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         return false;
      } else if (activeChar.getObjectId() == target.getObjectId()) {
         activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
         return false;
      } else if (this.getCharPenaltyExpiryTime() > System.currentTimeMillis()) {
         activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
         return false;
      } else if (target.getClanId() != 0) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
         sm.addString(target.getName());
         activeChar.sendPacket(sm);
         return false;
      } else if (target.getClanJoinExpiryTime() > System.currentTimeMillis()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
         sm.addString(target.getName());
         activeChar.sendPacket(sm);
         return false;
      } else if ((target.getLevel() > 40 || target.getClassId().level() >= 2) && pledgeType == -1) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
         sm.addString(target.getName());
         activeChar.sendPacket(sm);
         activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
         return false;
      } else if (this.getSubPledgeMembersCount(pledgeType) >= this.getMaxNrOfMembers(pledgeType)) {
         if (pledgeType == 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
            sm.addString(this.getName());
            activeChar.sendPacket(sm);
         } else {
            activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean checkAllyJoinCondition(Player activeChar, Player target) {
      if (activeChar == null) {
         return false;
      } else if (activeChar.getAllyId() != 0 && activeChar.isClanLeader() && activeChar.getClanId() == activeChar.getAllyId()) {
         Clan leaderClan = activeChar.getClan();
         if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis() && leaderClan.getAllyPenaltyType() == 3) {
            activeChar.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
            return false;
         } else if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            return false;
         } else if (activeChar.getObjectId() == target.getObjectId()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
            return false;
         } else if (target.getClan() == null) {
            activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
            return false;
         } else if (!target.isClanLeader()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
            sm.addString(target.getName());
            activeChar.sendPacket(sm);
            SystemMessage var7 = null;
            return false;
         } else {
            Clan targetClan = target.getClan();
            if (target.getAllyId() != 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
               sm.addString(targetClan.getName());
               sm.addString(targetClan.getAllyName());
               activeChar.sendPacket(sm);
               SystemMessage var10 = null;
               return false;
            } else {
               if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
                  if (targetClan.getAllyPenaltyType() == 1) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
                     sm.addString(target.getClan().getName());
                     sm.addString(target.getClan().getAllyName());
                     activeChar.sendPacket(sm);
                     SystemMessage var8 = null;
                     return false;
                  }

                  if (targetClan.getAllyPenaltyType() == 2) {
                     activeChar.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
                     return false;
                  }
               }

               if (activeChar.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE)) {
                  activeChar.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
                  return false;
               } else if (leaderClan.isAtWarWith(targetClan.getId())) {
                  activeChar.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
                  return false;
               } else if (ClanHolder.getInstance().getClanAllies(activeChar.getAllyId()).size() >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY) {
                  activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
                  return false;
               } else {
                  return true;
               }
            }
         }
      } else {
         activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
         return false;
      }
   }

   public long getAllyPenaltyExpiryTime() {
      return this._allyPenaltyExpiryTime;
   }

   public int getAllyPenaltyType() {
      return this._allyPenaltyType;
   }

   public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType) {
      this._allyPenaltyExpiryTime = expiryTime;
      this._allyPenaltyType = penaltyType;
   }

   public long getCharPenaltyExpiryTime() {
      return this._charPenaltyExpiryTime;
   }

   public void setCharPenaltyExpiryTime(long time) {
      this._charPenaltyExpiryTime = time;
   }

   public long getDissolvingExpiryTime() {
      return this._dissolvingExpiryTime;
   }

   public void setDissolvingExpiryTime(long time) {
      this._dissolvingExpiryTime = time;
   }

   public void createAlly(Player player, String allyName) {
      if (null != player) {
         if (Config.DEBUG) {
            _log.fine(player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
         }

         if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
         } else if (this.getAllyId() != 0) {
            player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
         } else if (this.getLevel() < 5) {
            player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
         } else if (this.getAllyPenaltyExpiryTime() > System.currentTimeMillis() && this.getAllyPenaltyType() == 4) {
            player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
         } else if (this.getDissolvingExpiryTime() > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
         } else if (!Util.isAlphaNumeric(allyName)) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
         } else if (allyName.length() > 16 || allyName.length() < 2) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
         } else if (ClanHolder.getInstance().isAllyExists(allyName)) {
            player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
         } else {
            this.setAllyId(this.getId());
            this.setAllyName(allyName.trim());
            this.setAllyPenaltyExpiryTime(0L, 0);
            this.updateClanInDB();
            player.sendUserInfo();
            player.sendMessage("Alliance " + allyName + " has been created.");
         }
      }
   }

   public void dissolveAlly(Player player) {
      if (this.getAllyId() == 0) {
         player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
      } else if (player.isClanLeader() && this.getId() == this.getAllyId()) {
         if (player.isInsideZone(ZoneId.SIEGE)) {
            player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
         } else {
            this.broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));
            long currentTime = System.currentTimeMillis();

            for(Clan clan : ClanHolder.getInstance().getClanAllies(this.getAllyId())) {
               if (clan.getId() != this.getId()) {
                  clan.setAllyId(0);
                  clan.setAllyName(null);
                  clan.setAllyPenaltyExpiryTime(0L, 0);
                  clan.updateClanInDB();
               }
            }

            this.setAllyId(0);
            this.setAllyName(null);
            this.changeAllyCrest(0, false);
            this.setAllyPenaltyExpiryTime(currentTime + (long)Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 3600000L, 4);
            this.updateClanInDB();
            player.deathPenalty(null, false, false, false);
         }
      } else {
         player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
      }
   }

   public boolean levelUpClan(Player player) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         return false;
      } else if (System.currentTimeMillis() < this.getDissolvingExpiryTime()) {
         player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
         return false;
      } else {
         boolean increaseClanLevel = false;
         if (!this.fireClanLevelUpListeners()) {
            return false;
         } else {
            switch(this.getLevel()) {
               case 0:
                  if (player.getSp() >= 20000 && player.getAdena() >= 650000L && player.reduceAdena("ClanLvl", 650000L, player.getTarget(), true)) {
                     player.setSp(player.getSp() - 20000);
                     SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
                     sp.addNumber(20000);
                     player.sendPacket(sp);
                     SystemMessage var25 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 1:
                  if (player.getSp() >= 100000 && player.getAdena() >= 2500000L && player.reduceAdena("ClanLvl", 2500000L, player.getTarget(), true)) {
                     player.setSp(player.getSp() - 100000);
                     SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
                     sp.addNumber(100000);
                     player.sendPacket(sp);
                     SystemMessage var23 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 2:
                  if (player.getSp() >= 350000
                     && player.getInventory().getItemByItemId(1419) != null
                     && player.destroyItemByItemId("ClanLvl", 1419, 1L, player.getTarget(), false)) {
                     player.setSp(player.getSp() - 350000);
                     SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
                     sp.addNumber(350000);
                     player.sendPacket(sp);
                     SystemMessage var21 = null;
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                     sm.addItemName(1419);
                     player.sendPacket(sm);
                     SystemMessage var36 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 3:
                  if (player.getSp() >= 1000000
                     && player.getInventory().getItemByItemId(3874) != null
                     && player.destroyItemByItemId("ClanLvl", 3874, 1L, player.getTarget(), false)) {
                     player.setSp(player.getSp() - 1000000);
                     SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
                     sp.addNumber(1000000);
                     player.sendPacket(sp);
                     SystemMessage var19 = null;
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                     sm.addItemName(3874);
                     player.sendPacket(sm);
                     SystemMessage var34 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 4:
                  if (player.getSp() >= 2500000
                     && player.getInventory().getItemByItemId(3870) != null
                     && player.destroyItemByItemId("ClanLvl", 3870, 1L, player.getTarget(), false)) {
                     player.setSp(player.getSp() - 2500000);
                     SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
                     sp.addNumber(2500000);
                     player.sendPacket(sp);
                     SystemMessage var17 = null;
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                     sm.addItemName(3870);
                     player.sendPacket(sm);
                     SystemMessage var32 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 5:
                  if (this.getReputationScore() >= Config.CLAN_LEVEL_6_COST && this.getMembersCount() >= Config.CLAN_LEVEL_6_REQUIREMENT) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_6_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_6_COST);
                     player.sendPacket(cr);
                     SystemMessage var15 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 6:
                  if (this.getReputationScore() >= Config.CLAN_LEVEL_7_COST && this.getMembersCount() >= Config.CLAN_LEVEL_7_REQUIREMENT) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_7_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_7_COST);
                     player.sendPacket(cr);
                     SystemMessage var13 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 7:
                  if (this.getReputationScore() >= Config.CLAN_LEVEL_8_COST && this.getMembersCount() >= Config.CLAN_LEVEL_8_REQUIREMENT) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_8_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_8_COST);
                     player.sendPacket(cr);
                     SystemMessage var11 = null;
                     increaseClanLevel = true;
                  }
                  break;
               case 8:
                  if (this.getReputationScore() >= Config.CLAN_LEVEL_9_COST
                     && player.getInventory().getItemByItemId(9910) != null
                     && this.getMembersCount() >= Config.CLAN_LEVEL_9_REQUIREMENT
                     && player.destroyItemByItemId("ClanLvl", 9910, 150L, player.getTarget(), false)) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_9_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_9_COST);
                     player.sendPacket(cr);
                     SystemMessage var9 = null;
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                     sm.addItemName(9910);
                     sm.addItemNumber(150L);
                     player.sendPacket(sm);
                     increaseClanLevel = true;
                  }
                  break;
               case 9:
                  if (this.getReputationScore() >= Config.CLAN_LEVEL_10_COST
                     && player.getInventory().getItemByItemId(9911) != null
                     && this.getMembersCount() >= Config.CLAN_LEVEL_10_REQUIREMENT
                     && player.destroyItemByItemId("ClanLvl", 9911, 5L, player.getTarget(), false)) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_10_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_10_COST);
                     player.sendPacket(cr);
                     SystemMessage var7 = null;
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                     sm.addItemName(9911);
                     sm.addItemNumber(5L);
                     player.sendPacket(sm);
                     increaseClanLevel = true;
                  }
                  break;
               case 10:
                  boolean hasTerritory = false;

                  for(TerritoryWarManager.Territory terr : TerritoryWarManager.getInstance().getAllTerritories()) {
                     if (terr.getLordObjectId() == this.getLeaderId()) {
                        hasTerritory = true;
                        break;
                     }
                  }

                  if (hasTerritory && this.getReputationScore() >= Config.CLAN_LEVEL_11_COST && this.getMembersCount() >= Config.CLAN_LEVEL_11_REQUIREMENT) {
                     this.setReputationScore(this.getReputationScore() - Config.CLAN_LEVEL_11_COST, true);
                     SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                     cr.addNumber(Config.CLAN_LEVEL_11_COST);
                     player.sendPacket(cr);
                     SystemMessage var28 = null;
                     increaseClanLevel = true;
                  }
                  break;
               default:
                  return false;
            }

            if (!increaseClanLevel) {
               player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
               return false;
            } else {
               StatusUpdate su = new StatusUpdate(player);
               su.addAttribute(13, player.getSp());
               player.sendPacket(su);
               player.sendItemList(false);
               this.changeLevel(this.getLevel() + 1, true);
               return true;
            }
         }
      }
   }

   public boolean changeLevel(int level, boolean sendMsg) {
      if (level > 11) {
         return false;
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
         ) {
            statement.setInt(1, level);
            statement.setInt(2, this.getId());
            statement.execute();
         } catch (Exception var35) {
            _log.log(Level.WARNING, "could not increase clan level:" + var35.getMessage(), (Throwable)var35);
         }

         this.setLevel(level);
         if (this.getLeader().isOnline()) {
            Player leader = this.getLeader().getPlayerInstance();
            if (4 < level) {
               SiegeManager.getInstance().addSiegeSkills(leader);
               leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
            } else if (5 > level) {
               SiegeManager.getInstance().removeSiegeSkills(leader);
            }
         }

         if (level > 1) {
            for(ClanMember member : this.getMembers()) {
               if (member.isOnline() && member.getPlayerInstance() != null) {
                  member.getPlayerInstance().setPledgeClass(ClanMember.calculatePledgeClass(member.getPlayerInstance()));
                  member.getPlayerInstance().sendUserInfo();
               }
            }
         }

         if (sendMsg) {
            this.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
         }

         this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
         return true;
      }
   }

   public void changeClanCrest(int crestId) {
      if (this.getCrestId() != 0) {
         CrestHolder.getInstance().removeCrest(this.getCrestId());
      }

      this.setCrestId(crestId);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
      ) {
         statement.setInt(1, crestId);
         statement.setInt(2, this.getId());
         statement.executeUpdate();
      } catch (SQLException var34) {
         _log.log(Level.WARNING, "Could not update crest for clan " + this.getName() + " [" + this.getId() + "] : " + var34.getMessage(), (Throwable)var34);
      }

      for(Player member : this.getOnlineMembers(0)) {
         member.broadcastUserInfo(true);
      }
   }

   public void changeAllyCrest(int crestId, boolean onlyThisClan) {
      String sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
      int allyId = this.getId();
      if (!onlyThisClan) {
         if (this.getAllyCrestId() != 0) {
            CrestHolder.getInstance().removeCrest(this.getAllyCrestId());
         }

         sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
         allyId = this.getAllyId();
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(sqlStatement);
      ) {
         statement.setInt(1, crestId);
         statement.setInt(2, allyId);
         statement.executeUpdate();
      } catch (SQLException var37) {
         _log.log(Level.WARNING, "Could not update ally crest for ally/clan id " + allyId + " : " + var37.getMessage(), (Throwable)var37);
      }

      if (onlyThisClan) {
         this.setAllyCrestId(crestId);

         for(Player member : this.getOnlineMembers(0)) {
            member.broadcastUserInfo(true);
         }
      } else {
         for(Clan clan : ClanHolder.getInstance().getClanAllies(this.getAllyId())) {
            clan.setAllyCrestId(crestId);

            for(Player member : clan.getOnlineMembers(0)) {
               member.broadcastUserInfo(true);
            }
         }
      }
   }

   public void changeLargeCrest(int crestId) {
      if (this.getCrestLargeId() != 0) {
         CrestHolder.getInstance().removeCrest(this.getCrestLargeId());
      }

      this.setCrestLargeId(crestId);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");
      ) {
         statement.setInt(1, crestId);
         statement.setInt(2, this.getId());
         statement.executeUpdate();
      } catch (SQLException var34) {
         _log.log(
            Level.WARNING, "Could not update large crest for clan " + this.getName() + " [" + this.getId() + "] : " + var34.getMessage(), (Throwable)var34
         );
      }

      for(Player member : this.getOnlineMembers(0)) {
         member.broadcastUserInfo(true);
      }
   }

   public boolean isLearnableSubSkill(int skillId, int skillLevel) {
      Skill current = this._subPledgeSkills.get(skillId);
      if (current != null && current.getLevel() + 1 == skillLevel) {
         return true;
      } else if (current == null && skillLevel == 1) {
         return true;
      } else {
         for(Clan.SubPledge subunit : this._subPledges.values()) {
            if (subunit._id != -1) {
               current = subunit._subPledgeSkills.get(skillId);
               if (current != null && current.getLevel() + 1 == skillLevel) {
                  return true;
               }

               if (current == null && skillLevel == 1) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean isLearnableSubPledgeSkill(Skill skill, int subType) {
      if (subType == -1) {
         return false;
      } else {
         int id = skill.getId();
         Skill current;
         if (subType == 0) {
            current = this._subPledgeSkills.get(id);
         } else {
            current = this._subPledges.get(subType)._subPledgeSkills.get(id);
         }

         if (current != null && current.getLevel() + 1 == skill.getLevel()) {
            return true;
         } else {
            return current == null && skill.getLevel() == 1;
         }
      }
   }

   public PledgeSkillList.SubPledgeSkill[] getAllSubSkills() {
      List<PledgeSkillList.SubPledgeSkill> list = new LinkedList<>();

      for(Skill skill : this._subPledgeSkills.values()) {
         list.add(new PledgeSkillList.SubPledgeSkill(0, skill.getId(), skill.getLevel()));
      }

      for(Clan.SubPledge subunit : this._subPledges.values()) {
         for(Skill skill : subunit.getSkills()) {
            list.add(new PledgeSkillList.SubPledgeSkill(subunit._id, skill.getId(), skill.getLevel()));
         }
      }

      PledgeSkillList.SubPledgeSkill[] result = list.toArray(new PledgeSkillList.SubPledgeSkill[list.size()]);
      return result;
   }

   public void setNewLeaderId(int objectId, boolean storeInDb) {
      this._newLeaderId = objectId;
      if (storeInDb) {
         this.updateClanInDB();
      }
   }

   public int getNewLeaderId() {
      return this._newLeaderId;
   }

   public Player getNewLeader() {
      return World.getInstance().getPlayer(this._newLeaderId);
   }

   public String getNewLeaderName() {
      return CharNameHolder.getInstance().getNameById(this._newLeaderId);
   }

   public int getSiegeKills() {
      return this._siegeKills != null ? this._siegeKills.get() : 0;
   }

   public int getSiegeDeaths() {
      return this._siegeDeaths != null ? this._siegeDeaths.get() : 0;
   }

   public int addSiegeKill() {
      if (this._siegeKills == null) {
         synchronized(this) {
            if (this._siegeKills == null) {
               this._siegeKills = new AtomicInteger();
            }
         }
      }

      return this._siegeKills.incrementAndGet();
   }

   public int addSiegeDeath() {
      if (this._siegeDeaths == null) {
         synchronized(this) {
            if (this._siegeDeaths == null) {
               this._siegeDeaths = new AtomicInteger();
            }
         }
      }

      return this._siegeDeaths.incrementAndGet();
   }

   public void clearSiegeKills() {
      if (this._siegeKills != null) {
         this._siegeKills.set(0);
      }
   }

   public void clearSiegeDeaths() {
      if (this._siegeDeaths != null) {
         this._siegeDeaths.set(0);
      }
   }

   private void fireClanCreationListeners() {
      if (!clanCreationListeners.isEmpty()) {
         ClanCreationEvent event = new ClanCreationEvent();
         event.setClan(this);

         for(ClanCreationListener listener : clanCreationListeners) {
            listener.onClanCreate(event);
         }
      }
   }

   private boolean fireClanLeaderChangeListeners(Player newLeader, Player exLeader) {
      if (!clanMembershipListeners.isEmpty() && newLeader != null && exLeader != null) {
         ClanLeaderChangeEvent event = new ClanLeaderChangeEvent();
         event.setClan(this);
         event.setNewLeader(newLeader);
         event.setOldLeader(exLeader);

         for(ClanMembershipListener listener : clanMembershipListeners) {
            if (!listener.onLeaderChange(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanJoinListeners(Player player) {
      if (!clanMembershipListeners.isEmpty() && player != null) {
         ClanJoinEvent event = new ClanJoinEvent();
         event.setClan(this);
         event.setPlayer(player);

         for(ClanMembershipListener listener : clanMembershipListeners) {
            if (!listener.onJoin(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanLeaveListeners(int objectId) {
      if (!clanMembershipListeners.isEmpty()) {
         ClanLeaveEvent event = new ClanLeaveEvent();
         event.setPlayerId(objectId);
         event.setClan(this);

         for(ClanMembershipListener listener : clanMembershipListeners) {
            if (!listener.onLeave(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanLevelUpListeners() {
      if (!clanCreationListeners.isEmpty()) {
         ClanLevelUpEvent event = new ClanLevelUpEvent();
         event.setClan(this);
         event.setOldLevel(this._level);

         for(ClanCreationListener listener : clanCreationListeners) {
            if (!listener.onClanLevelUp(event)) {
               return false;
            }
         }
      }

      return true;
   }

   public static void addClanCreationListener(ClanCreationListener listener) {
      if (!clanCreationListeners.contains(listener)) {
         clanCreationListeners.add(listener);
      }
   }

   public static void removeClanCreationListener(ClanCreationListener listener) {
      clanCreationListeners.remove(listener);
   }

   public static void addClanMembershipListener(ClanMembershipListener listener) {
      if (!clanMembershipListeners.contains(listener)) {
         clanMembershipListeners.add(listener);
      }
   }

   public static void removeClanMembershipListener(ClanMembershipListener listener) {
      clanMembershipListeners.remove(listener);
   }

   private void restoreClanRecruitment() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_requiements where clan_id=" + this.getId());
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this._recruting = rset.getInt("recruting") == 1;

            for(String clas : rset.getString("classes").split(",")) {
               if (clas.length() > 0) {
                  this._classesNeeded.add(Integer.parseInt(clas));
               }
            }

            for(int i = 1; i <= 8; ++i) {
               this._questions[i - 1] = rset.getString("question" + i);
            }
         }

         try (
            PreparedStatement statementx = con.prepareStatement("SELECT * FROM clan_petitions where clan_id=" + this.getId());
            ResultSet rsetx = statementx.executeQuery();
         ) {
            while(rsetx.next()) {
               String[] answers = new String[8];

               for(int i = 1; i <= 8; ++i) {
                  answers[i - 1] = rsetx.getString("answer" + i);
               }

               this._petitions.add(new Clan.SinglePetition(rsetx.getInt("sender_id"), answers, rsetx.getString("comment")));
            }
         }
      } catch (SQLException | NumberFormatException var131) {
         _log.log(Level.SEVERE, "Error while restoring Clan Recruitment", (Throwable)var131);
      }
   }

   public void updateRecrutationData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO clan_requiements VALUES("
               + this.getId()
               + ",0,'','','','','','','','','') ON DUPLICATE KEY UPDATE recruting=?,classes=?,question1=?,question2=?,question3=?,question4=?,question5=?,question6=?,question7=?,question8=?"
         );
      ) {
         statement.setInt(1, this._recruting ? 1 : 0);
         statement.setString(2, this.getClassesForData());

         for(int i = 0; i < 8; ++i) {
            statement.setString(i + 3, this._questions[i] == null ? "" : this._questions[i]);
         }

         statement.execute();

         try (PreparedStatement statementx = con.prepareStatement("DELETE FROM clan_petitions WHERE clan_id=" + this.getId())) {
            statementx.execute();
         }

         for(Clan.SinglePetition petition : this.getPetitions()) {
            try (PreparedStatement statementx = con.prepareStatement("INSERT IGNORE INTO clan_petitions VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
               statementx.setInt(1, petition.getSenderId());
               statementx.setInt(2, this.getId());

               for(int i = 0; i < 8; ++i) {
                  statementx.setString(i + 3, petition.getAnswers()[i] == null ? "" : petition.getAnswers()[i]);
               }

               statementx.setString(11, petition.getComment());
               statementx.execute();
            }
         }
      } catch (SQLException var88) {
         _log.log(Level.WARNING, "Error while updating clan recruitment system on clan id '" + this._clanId + "' in db", (Throwable)var88);
      }
   }

   public void setQuestions(String[] questions) {
      this._questions = questions;
   }

   public synchronized boolean addPetition(int senderId, String[] answers, String comment) {
      if (this.getPetition(senderId) != null) {
         return false;
      } else {
         this._petitions.add(new Clan.SinglePetition(senderId, answers, comment));
         this.updateRecrutationData();
         if (World.getInstance().getPlayer(this.getLeaderId()) != null) {
            World.getInstance().getPlayer(this.getLeaderId()).sendMessage("New Clan Petition has arrived!");
         }

         return true;
      }
   }

   public Clan.SinglePetition getPetition(int senderId) {
      return this._petitions.stream().filter(petition -> petition.getSenderId() == senderId).findAny().orElse(null);
   }

   public ArrayList<Clan.SinglePetition> getPetitions() {
      return this._petitions;
   }

   public synchronized void deletePetition(int senderId) {
      Clan.SinglePetition petition = this._petitions.stream().filter(p -> p.getSenderId() == senderId).findAny().orElse(null);
      if (petition != null) {
         this._petitions.remove(petition);
         this.updateRecrutationData();
      }
   }

   public void deletePetition(Clan.SinglePetition petition) {
      this._petitions.remove(petition);
      this.updateRecrutationData();
   }

   public void setRecrutating(boolean b) {
      this._recruting = b;
   }

   public void addClassNeeded(int clas) {
      this._classesNeeded.add(clas);
   }

   public void deleteClassNeeded(int clas) {
      int indexOfClass = this._classesNeeded.indexOf(clas);
      if (indexOfClass != -1) {
         this._classesNeeded.remove(indexOfClass);
      } else {
         _log.warning("Tried removing inexistent class: " + clas);
      }
   }

   public String getClassesForData() {
      String text = "";

      for(int i = 0; i < this.getClassesNeeded().size(); ++i) {
         if (i != 0) {
            text = text + ",";
         }

         text = text + this.getClassesNeeded().get(i);
      }

      return text;
   }

   public ArrayList<Integer> getClassesNeeded() {
      return this._classesNeeded;
   }

   public boolean isRecruting() {
      return this._recruting;
   }

   public String[] getQuestions() {
      return this._questions;
   }

   public boolean isFull() {
      for(ClanMember unit : this.getMembers()) {
         if (this.getSubPledgeMembersCount(unit.getPledgeType()) < this.getMaxNrOfMembers(unit.getPledgeType())) {
            return false;
         }
      }

      return true;
   }

   private static class ClanReputationComparator implements Comparator<Clan>, Serializable {
      private static final long serialVersionUID = -240267507300918307L;

      private ClanReputationComparator() {
      }

      public int compare(Clan o1, Clan o2) {
         return o1 != null && o2 != null ? Integer.compare(o2.getReputationScore(), o1.getReputationScore()) : 0;
      }
   }

   public static class RankPrivs {
      private final int _rankId;
      private int _party;
      private int _rankPrivs;

      public RankPrivs(int rank, int party, int privs) {
         this._rankId = rank;
         this._party = party;
         this._rankPrivs = privs;
      }

      public int getRank() {
         return this._rankId;
      }

      public int getParty() {
         return this._party;
      }

      public int getPrivs() {
         return this._rankPrivs;
      }

      public void setParty(int party) {
         this._party = party;
      }

      public void setPrivs(int privs) {
         this._rankPrivs = privs;
      }
   }

   public class SinglePetition {
      int _sender;
      String[] _answers;
      String _comment;

      private SinglePetition(int sender, String[] answers, String comment) {
         this._sender = sender;
         this._answers = answers;
         this._comment = comment;
      }

      public int getSenderId() {
         return this._sender;
      }

      public String[] getAnswers() {
         return this._answers;
      }

      public String getComment() {
         return this._comment;
      }
   }

   public static class SubPledge {
      public final int _id;
      public String _subPledgeName;
      private int _leaderId;
      public final Map<Integer, Skill> _subPledgeSkills = new HashMap<>();

      public SubPledge(int id, String name, int leaderId) {
         this._id = id;
         this._subPledgeName = name;
         this._leaderId = leaderId;
      }

      public int getId() {
         return this._id;
      }

      public String getName() {
         return this._subPledgeName;
      }

      public void setName(String name) {
         this._subPledgeName = name;
      }

      public int getLeaderId() {
         return this._leaderId;
      }

      public void setLeaderId(int leaderId) {
         this._leaderId = leaderId;
      }

      public Skill addNewSkill(Skill skill) {
         return this._subPledgeSkills.put(skill.getId(), skill);
      }

      public Collection<Skill> getSkills() {
         return this._subPledgeSkills.values();
      }
   }
}
