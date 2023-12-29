package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.SiegeGuardManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.listener.events.SiegeEvent;
import l2e.gameserver.listener.events.SiegeListener;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControlTowerInstance;
import l2e.gameserver.model.actor.instance.FlameTowerInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Siege implements Siegable {
   protected static final Logger _log = Logger.getLogger(Siege.class.getName());
   private static final List<SiegeListener> siegeListeners = new LinkedList<>();
   public static final byte OWNER = -1;
   public static final byte DEFENDER = 0;
   public static final byte ATTACKER = 1;
   public static final byte DEFENDER_NOT_APPROWED = 2;
   private int _controlTowerCount;
   private int _controlTowerMaxCount;
   private int _flameTowerCount;
   private int _flameTowerMaxCount;
   private final List<SiegeClan> _attackerClans = new CopyOnWriteArrayList<>();
   private final List<SiegeClan> _defenderClans = new CopyOnWriteArrayList<>();
   private final List<SiegeClan> _defenderWaitingClans = new CopyOnWriteArrayList<>();
   private List<ControlTowerInstance> _controlTowers = new ArrayList<>();
   private List<FlameTowerInstance> _flameTowers = new ArrayList<>();
   private final Castle[] _castle;
   private boolean _isInProgress = false;
   private boolean _isNormalSide = true;
   protected boolean _isRegistrationOver = false;
   protected Calendar _siegeEndDate;
   private SiegeGuardManager _siegeGuardManager;
   protected ScheduledFuture<?> _scheduledStartSiegeTask = null;
   protected int _firstOwnerClanId = -1;

   public Siege(Castle[] castle) {
      this._castle = castle;
      this._siegeGuardManager = new SiegeGuardManager(this.getCastle());
      this.startAutoTask();
   }

   @Override
   public void endSiege() {
      if (this.getIsInProgress()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
         sm.addCastleId(this.getCastle().getId());
         Announcements.getInstance().announceToAll(sm);
         if (this.getCastle().getOwnerId() > 0) {
            Clan clan = ClanHolder.getInstance().getClan(this.getCastle().getOwnerId());
            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
            sm.addString(clan.getName());
            sm.addCastleId(this.getCastle().getId());
            Announcements.getInstance().announceToAll(sm);
            if (clan.getId() == this._firstOwnerClanId) {
               clan.increaseBloodAllianceCount();
               RewardManager.getInstance().checkCastleDefenceReward(clan);
               if (AchievementManager.getInstance().isActive()) {
                  for(ClanMember member : clan.getMembers()) {
                     if (member != null && member.isOnline()) {
                        member.getPlayerInstance().getCounters().addAchivementInfo("castleSiegesDefend", 0, -1L, false, false, false);
                     }
                  }
               }
            } else {
               RewardManager.getInstance().checkCastleCaptureReward(clan);
               this.getCastle().setTicketBuyCount(0);

               for(ClanMember member : clan.getMembers()) {
                  if (member != null) {
                     Player player = member.getPlayerInstance();
                     if (player != null) {
                        if (player.isOnline()) {
                           member.getPlayerInstance().getCounters().addAchivementInfo("castleSiegesWon", 0, -1L, false, false, false);
                        }

                        if (player.isNoble()) {
                           Hero.getInstance().setCastleTaken(player.getObjectId(), this.getCastle().getId());
                        }
                     }
                  }
               }
            }
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
            sm.addCastleId(this.getCastle().getId());
            Announcements.getInstance().announceToAll(sm);
         }

         for(SiegeClan attackerClan : this.getAttackerClans()) {
            Clan clan = ClanHolder.getInstance().getClan(attackerClan.getClanId());
            if (clan != null) {
               clan.clearSiegeKills();
               clan.clearSiegeDeaths();
            }
         }

         for(SiegeClan defenderClan : this.getDefenderClans()) {
            Clan clan = ClanHolder.getInstance().getClan(defenderClan.getClanId());
            if (clan != null) {
               clan.clearSiegeKills();
               clan.clearSiegeDeaths();
            }
         }

         this.getCastle().updateClansReputation();
         this.removeFlags();
         this.teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
         this.teleportPlayer(Siege.TeleportWhoType.DefenderNotOwner, TeleportWhereType.TOWN);
         this.teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.TOWN);
         this._isInProgress = false;
         this.updatePlayerSiegeStateFlags(true);
         this.saveCastleSiege();
         this.clearSiegeClan();
         this.removeControlTower();
         this.removeFlameTower();
         this._siegeGuardManager.unspawnSiegeGuard();
         if (this.getCastle().getOwnerId() > 0) {
            this._siegeGuardManager.removeMercs();
         }

         this.getCastle().spawnDoor();
         this.getCastle().getZone().setIsActive(false);
         this.getCastle().getZone().updateZoneStatusForCharactersInside();
         this.getCastle().getZone().setSiegeInstance(null);
         this.fireSiegeListeners(ScriptListener.EventStage.END);
      }
   }

   private void removeDefender(SiegeClan sc) {
      if (sc != null) {
         this.getDefenderClans().remove(sc);
      }
   }

   private void removeAttacker(SiegeClan sc) {
      if (sc != null) {
         this.getAttackerClans().remove(sc);
      }
   }

   private void addDefender(SiegeClan sc, SiegeClan.SiegeClanType type) {
      if (sc != null) {
         sc.setType(type);
         this.getDefenderClans().add(sc);
      }
   }

   private void addAttacker(SiegeClan sc) {
      if (sc != null) {
         sc.setType(SiegeClan.SiegeClanType.ATTACKER);
         this.getAttackerClans().add(sc);
      }
   }

   public synchronized void midVictory() {
      if (this.getIsInProgress()) {
         if (this.getCastle().getOwnerId() > 0) {
            this._siegeGuardManager.removeMercs();
         }

         if (this.getDefenderClans().isEmpty() && this.getAttackerClans().size() == 1) {
            SiegeClan sc_newowner = this.getAttackerClan(this.getCastle().getOwnerId());
            this.removeAttacker(sc_newowner);
            this.addDefender(sc_newowner, SiegeClan.SiegeClanType.OWNER);
            this.endSiege();
            return;
         }

         if (this.getCastle().getOwnerId() > 0) {
            int allyId = ClanHolder.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId();
            if (this.getDefenderClans().isEmpty() && allyId != 0) {
               boolean allinsamealliance = true;

               for(SiegeClan sc : this.getAttackerClans()) {
                  if (sc != null && ClanHolder.getInstance().getClan(sc.getClanId()).getAllyId() != allyId) {
                     allinsamealliance = false;
                  }
               }

               if (allinsamealliance) {
                  SiegeClan sc_newowner = this.getAttackerClan(this.getCastle().getOwnerId());
                  this.removeAttacker(sc_newowner);
                  this.addDefender(sc_newowner, SiegeClan.SiegeClanType.OWNER);
                  this.endSiege();
                  return;
               }
            }

            for(SiegeClan sc : this.getDefenderClans()) {
               if (sc != null) {
                  this.removeDefender(sc);
                  this.addAttacker(sc);
               }
            }

            SiegeClan sc_newowner = this.getAttackerClan(this.getCastle().getOwnerId());
            this.removeAttacker(sc_newowner);
            this.addDefender(sc_newowner, SiegeClan.SiegeClanType.OWNER);

            for(Clan clan : ClanHolder.getInstance().getClanAllies(allyId)) {
               SiegeClan sc = this.getAttackerClan(clan.getId());
               if (sc != null) {
                  this.removeAttacker(sc);
                  this.addDefender(sc, SiegeClan.SiegeClanType.DEFENDER);
               }
            }

            this.teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.SIEGEFLAG);
            this.teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.TOWN);
            this.removeDefenderFlags();
            this.getCastle().removeUpgrade();
            this.getCastle().spawnDoor(true);
            this.removeControlTower();
            this.removeFlameTower();
            this._controlTowerCount = 0;
            this._controlTowerMaxCount = 0;
            this._flameTowerCount = 0;
            this._flameTowerMaxCount = 0;
            this.spawnControlTower(this.getCastle().getId());
            this.spawnFlameTower(this.getCastle().getId());
            this.updatePlayerSiegeStateFlags(false);
            this.fireSiegeListeners(ScriptListener.EventStage.CONTROL_CHANGE);
         }
      }
   }

   @Override
   public void startSiege() {
      if (!this.getIsInProgress()) {
         if (!this.fireSiegeListeners(ScriptListener.EventStage.START)) {
            return;
         }

         this._firstOwnerClanId = this.getCastle().getOwnerId();
         if (this.getAttackerClans().isEmpty()) {
            SystemMessage sm;
            if (this._firstOwnerClanId <= 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
               Clan ownerClan = ClanHolder.getInstance().getClan(this._firstOwnerClanId);
               ownerClan.increaseBloodAllianceCount();
            }

            sm.addCastleId(this.getCastle().getId());
            Announcements.getInstance().announceToAll(sm);
            this.saveCastleSiege();
            return;
         }

         this._isNormalSide = true;
         this._isInProgress = true;
         this.loadSiegeClan(true);
         this.updatePlayerSiegeStateFlags(false);
         this.teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
         this._controlTowerCount = 0;
         this._controlTowerMaxCount = 0;
         this.spawnControlTower(this.getCastle().getId());
         this.spawnFlameTower(this.getCastle().getId());
         this.getCastle().spawnDoor();
         this.spawnSiegeGuard();
         MercTicketManager.getInstance().deleteTickets(this.getCastle().getId());
         this.getCastle().getZone().setSiegeInstance(this);
         this.getCastle().getZone().setIsActive(true);
         this.getCastle().getZone().updateZoneStatusForCharactersInside();
         this._siegeEndDate = Calendar.getInstance();
         this._siegeEndDate.add(12, SiegeManager.getInstance().getSiegeLength());
         ThreadPoolManager.getInstance().schedule(new Siege.ScheduleEndSiegeTask(this.getCastle()), 1000L);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED);
         sm.addCastleId(this.getCastle().getId());
         Announcements.getInstance().announceToAll(sm);
      }
   }

   public void announceToPlayer(SystemMessage message, boolean bothSides) {
      for(SiegeClan siegeClans : this.getDefenderClans()) {
         Clan clan = ClanHolder.getInstance().getClan(siegeClans.getClanId());

         for(Player member : clan.getOnlineMembers(0)) {
            if (member != null) {
               member.sendPacket(message);
            }
         }
      }

      if (bothSides) {
         for(SiegeClan siegeClans : this.getAttackerClans()) {
            Clan clan = ClanHolder.getInstance().getClan(siegeClans.getClanId());

            for(Player member : clan.getOnlineMembers(0)) {
               if (member != null) {
                  member.sendPacket(message);
               }
            }
         }
      }
   }

   public void updatePlayerSiegeStateFlags(boolean clear) {
      for(SiegeClan siegeclan : this.getAttackerClans()) {
         if (siegeclan != null) {
            Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());

            for(Player member : clan.getOnlineMembers(0)) {
               if (member != null) {
                  if (clear) {
                     member.setSiegeState((byte)0);
                     member.setSiegeSide(0);
                     member.setIsInSiege(false);
                     member.stopFameTask();
                  } else {
                     member.setSiegeState((byte)1);
                     member.setSiegeSide(this.getCastle().getId());
                     if (this.checkIfInZone(member)) {
                        member.setIsInSiege(true);
                        member.startFameTask((long)(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000), Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
                     }
                  }

                  member.sendUserInfo();
                  member.broadcastRelationChanged();
               }
            }
         }
      }

      for(SiegeClan siegeclan : this.getDefenderClans()) {
         if (siegeclan != null) {
            Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());

            for(Player member : clan.getOnlineMembers(0)) {
               if (member != null) {
                  if (clear) {
                     member.setSiegeState((byte)0);
                     member.setSiegeSide(0);
                     member.setIsInSiege(false);
                     member.stopFameTask();
                  } else {
                     member.setSiegeState((byte)2);
                     member.setSiegeSide(this.getCastle().getId());
                     if (this.checkIfInZone(member)) {
                        member.setIsInSiege(true);
                        member.startFameTask((long)(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000), Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
                     }
                  }

                  member.sendUserInfo();
                  member.broadcastRelationChanged();
               }
            }
         }
      }
   }

   public void approveSiegeDefenderClan(int clanId) {
      if (clanId > 0) {
         this.saveSiegeClan(ClanHolder.getInstance().getClan(clanId), (byte)0, true);
         this.loadSiegeClan(false);
      }
   }

   public boolean checkIfInZone(GameObject object) {
      return this.checkIfInZone(object.getX(), object.getY(), object.getZ());
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this.getIsInProgress() && this.getCastle().checkIfInZone(x, y, z);
   }

   @Override
   public boolean checkIsAttacker(Clan clan) {
      return this.getAttackerClan(clan) != null;
   }

   @Override
   public boolean checkIsDefender(Clan clan) {
      return this.getDefenderClan(clan) != null;
   }

   public boolean checkIsDefenderWaiting(Clan clan) {
      return this.getDefenderWaitingClan(clan) != null;
   }

   public void clearSiegeClan() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
      ) {
         statement.setInt(1, this.getCastle().getId());
         statement.execute();
         if (this.getCastle().getOwnerId() > 0) {
            try (PreparedStatement delete = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?")) {
               delete.setInt(1, this.getCastle().getOwnerId());
               delete.execute();
            }
         }

         this.getAttackerClans().clear();
         this.getDefenderClans().clear();
         this.getDefenderWaitingClans().clear();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Exception: clearSiegeClan(): " + var59.getMessage(), (Throwable)var59);
      }
   }

   public void clearSiegeWaitingClan() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
      ) {
         statement.setInt(1, this.getCastle().getId());
         statement.execute();
         this.getDefenderWaitingClans().clear();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: clearSiegeWaitingClan(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   @Override
   public List<Player> getAttackersInZone() {
      List<Player> players = new ArrayList<>();

      for(SiegeClan siegeclan : this.getAttackerClans()) {
         Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());

         for(Player player : clan.getOnlineMembers(0)) {
            if (player != null && player.isInSiege()) {
               players.add(player);
            }
         }
      }

      return players;
   }

   public List<Player> getDefendersButNotOwnersInZone() {
      List<Player> players = new ArrayList<>();

      for(SiegeClan siegeclan : this.getDefenderClans()) {
         Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
         if (clan.getId() != this.getCastle().getOwnerId()) {
            for(Player player : clan.getOnlineMembers(0)) {
               if (player != null && player.isInSiege()) {
                  players.add(player);
               }
            }
         }
      }

      return players;
   }

   public List<Player> getPlayersInZone() {
      return this.getCastle().getZone().getPlayersInside();
   }

   public List<Player> getOwnersInZone() {
      List<Player> players = new ArrayList<>();

      for(SiegeClan siegeclan : this.getDefenderClans()) {
         Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
         if (clan.getId() == this.getCastle().getOwnerId()) {
            for(Player player : clan.getOnlineMembers(0)) {
               if (player != null && player.isInSiege()) {
                  players.add(player);
               }
            }
         }
      }

      return players;
   }

   public List<Player> getSpectatorsInZone() {
      List<Player> players = new ArrayList<>();

      for(Player player : this.getCastle().getZone().getPlayersInside()) {
         if (player != null && !player.isInSiege()) {
            players.add(player);
         }
      }

      return players;
   }

   public void killedCT(Npc ct) {
      --this._controlTowerCount;
      if (this._controlTowerCount < 0) {
         this._controlTowerCount = 0;
      }
   }

   public void killedFlag(Npc flag) {
      if (flag != null) {
         for(SiegeClan clan : this.getAttackerClans()) {
            if (clan.removeFlag(flag)) {
               return;
            }
         }
      }
   }

   public void listRegisterClan(Player player) {
      player.sendPacket(new CastleSiegeInfo(this.getCastle()));
   }

   public void registerAttacker(Player player) {
      this.registerAttacker(player, false);
   }

   public void registerAttacker(Player player, boolean force) {
      if (player.getClan() != null) {
         int allyId = 0;
         if (this.getCastle().getOwnerId() != 0) {
            allyId = ClanHolder.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId();
         }

         if (allyId != 0 && player.getClan().getAllyId() == allyId && !force) {
            player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
         } else if (force) {
            if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), this.getCastle().getId())) {
               player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
            } else {
               this.saveSiegeClan(player.getClan(), (byte)1, false);
            }
         } else {
            if (this.checkIfCanRegister(player, (byte)1)) {
               this.saveSiegeClan(player.getClan(), (byte)1, false);
            }
         }
      }
   }

   public void registerDefender(Player player) {
      this.registerDefender(player, false);
   }

   public void registerDefender(Player player, boolean force) {
      if (this.getCastle().getOwnerId() <= 0) {
         player.sendMessage("You cannot register as a defender because " + this.getCastle().getName() + " is owned by NPC.");
      } else if (force) {
         if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), this.getCastle().getId())) {
            player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
         } else {
            this.saveSiegeClan(player.getClan(), (byte)2, false);
         }
      } else {
         if (this.checkIfCanRegister(player, (byte)2)) {
            this.saveSiegeClan(player.getClan(), (byte)2, false);
         }
      }
   }

   public void removeSiegeClan(int clanId) {
      if (clanId > 0) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");
         ) {
            statement.setInt(1, this.getCastle().getId());
            statement.setInt(2, clanId);
            statement.execute();
            this.loadSiegeClan(false);
         } catch (Exception var34) {
            _log.log(Level.WARNING, "Exception: removeSiegeClan(): " + var34.getMessage(), (Throwable)var34);
         }
      }
   }

   public void removeSiegeClan(Clan clan) {
      if (clan != null && clan.getCastleId() != this.getCastle().getId() && SiegeManager.getInstance().checkIsRegistered(clan, this.getCastle().getId())) {
         this.removeSiegeClan(clan.getId());
      }
   }

   public void removeSiegeClan(Player player) {
      this.removeSiegeClan(player.getClan());
   }

   public void startAutoTask() {
      this.correctSiegeDateTime();
      _log.info(this.getCastle().getName() + " Siege: Will begin at " + this.getCastle().getSiegeDate().getTime());
      this.loadSiegeClan(false);
      if (this._scheduledStartSiegeTask != null) {
         this._scheduledStartSiegeTask.cancel(false);
      }

      this._scheduledStartSiegeTask = ThreadPoolManager.getInstance().schedule(new Siege.ScheduleStartSiegeTask(this.getCastle()), 1000L);
   }

   public void teleportPlayer(Siege.TeleportWhoType teleportWho, TeleportWhereType teleportWhere) {
      List<Player> players;
      switch(teleportWho) {
         case Owner:
            players = this.getOwnersInZone();
            break;
         case Attacker:
            players = this.getAttackersInZone();
            break;
         case DefenderNotOwner:
            players = this.getDefendersButNotOwnersInZone();
            break;
         case Spectator:
            players = this.getSpectatorsInZone();
            break;
         default:
            players = this.getPlayersInZone();
      }

      for(Player player : players) {
         if (!player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) && !player.isJailed()) {
            player.teleToLocation(teleportWhere, true);
         }
      }
   }

   private void addAttacker(int clanId) {
      this.getAttackerClans().add(new SiegeClan(clanId, SiegeClan.SiegeClanType.ATTACKER));
   }

   private void addDefender(int clanId) {
      this.getDefenderClans().add(new SiegeClan(clanId, SiegeClan.SiegeClanType.DEFENDER));
   }

   private void addDefender(int clanId, SiegeClan.SiegeClanType type) {
      this.getDefenderClans().add(new SiegeClan(clanId, type));
   }

   private void addDefenderWaiting(int clanId) {
      this.getDefenderWaitingClans().add(new SiegeClan(clanId, SiegeClan.SiegeClanType.DEFENDER_PENDING));
   }

   private boolean checkIfCanRegister(Player player, byte typeId) {
      if (this.getIsRegistrationOver()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
         sm.addCastleId(this.getCastle().getId());
         player.sendPacket(sm);
      } else if (this.getIsInProgress()) {
         player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
      } else if (player.getClan() == null || player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()) {
         player.sendPacket(SystemMessageId.ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE);
      } else if (player.getClan().getId() == this.getCastle().getOwnerId()) {
         player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
      } else if (player.getClan().getCastleId() > 0) {
         player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
      } else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), this.getCastle().getId())) {
         player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
      } else if (this.checkIfAlreadyRegisteredForSameDay(player.getClan())) {
         player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
      } else if (typeId == 1 && this.getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans()) {
         player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
      } else {
         if (typeId != 0 && typeId != 2 && typeId != -1
            || this.getDefenderClans().size() + this.getDefenderWaitingClans().size() < SiegeManager.getInstance().getDefenderMaxClans()) {
            return true;
         }

         player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
      }

      return false;
   }

   public boolean checkIfAlreadyRegisteredForSameDay(Clan clan) {
      for(Siege siege : SiegeManager.getInstance().getSieges()) {
         if (siege != this && siege.getSiegeDate().get(7) == this.getSiegeDate().get(7)) {
            if (siege.checkIsAttacker(clan)) {
               return true;
            }

            if (siege.checkIsDefender(clan)) {
               return true;
            }

            if (siege.checkIsDefenderWaiting(clan)) {
               return true;
            }
         }
      }

      return false;
   }

   public void correctSiegeDateTime() {
      boolean corrected = false;
      if (this.getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
         corrected = true;
         this.setNextSiegeDate();
      }

      if (!SevenSigns.getInstance().isDateInSealValidPeriod(this.getCastle().getSiegeDate())) {
         corrected = true;
         this.setNextSiegeDate();
      }

      if (corrected) {
         this.saveSiegeDate();
      }
   }

   private void loadSiegeClan(boolean checkForts) {
      List<Integer> clanList = new ArrayList<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
      ) {
         this.getAttackerClans().clear();
         this.getDefenderClans().clear();
         this.getDefenderWaitingClans().clear();
         if (this.getCastle().getOwnerId() > 0) {
            this.addDefender(this.getCastle().getOwnerId(), SiegeClan.SiegeClanType.OWNER);
            clanList.add(this.getCastle().getOwnerId());
         }

         statement.setInt(1, this.getCastle().getId());

         try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
               int typeId = rs.getInt("type");
               if (typeId == 0) {
                  this.addDefender(rs.getInt("clan_id"));
               } else if (typeId == 1) {
                  this.addAttacker(rs.getInt("clan_id"));
               } else if (typeId == 2) {
                  this.addDefenderWaiting(rs.getInt("clan_id"));
               }

               if (checkForts) {
                  clanList.add(rs.getInt("clan_id"));
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Exception: loadSiegeClan(): " + var61.getMessage(), (Throwable)var61);
      }

      if (checkForts && !clanList.isEmpty()) {
         for(int clanId : clanList) {
            Clan clan = ClanHolder.getInstance().getClan(clanId);
            if (clan != null) {
               for(Fort fort : FortManager.getInstance().getForts()) {
                  if (fort != null
                     && fort.getSiege().getAttackerClan(clan) != null
                     && (fort.getSiege().getIsInProgress() || fort.getSiege().getSiegeDate().getTimeInMillis() < System.currentTimeMillis() + 7200000L)) {
                     fort.getSiege().removeSiegeClan(clan);
                  }
               }
            }
         }
      }
   }

   private void removeControlTower() {
      if (this._controlTowers != null && !this._controlTowers.isEmpty()) {
         for(ControlTowerInstance ct : this._controlTowers) {
            if (ct != null) {
               try {
                  ct.deleteMe();
               } catch (Exception var4) {
                  _log.log(Level.WARNING, "Exception: removeControlTower(): " + var4.getMessage(), (Throwable)var4);
               }
            }
         }

         this._controlTowers.clear();
         this._controlTowers = null;
      }
   }

   private void removeFlameTower() {
      if (this._flameTowers != null && !this._flameTowers.isEmpty()) {
         for(FlameTowerInstance ct : this._flameTowers) {
            if (ct != null) {
               try {
                  ct.deleteMe();
               } catch (Exception var4) {
                  _log.log(Level.WARNING, "Exception: removeFlamelTower(): " + var4.getMessage(), (Throwable)var4);
               }
            }
         }

         this._flameTowers.clear();
         this._flameTowers = null;
      }
   }

   private void removeFlags() {
      for(SiegeClan sc : this.getAttackerClans()) {
         if (sc != null) {
            sc.removeFlags();
         }
      }

      for(SiegeClan sc : this.getDefenderClans()) {
         if (sc != null) {
            sc.removeFlags();
         }
      }
   }

   private void removeDefenderFlags() {
      for(SiegeClan sc : this.getDefenderClans()) {
         if (sc != null) {
            sc.removeFlags();
         }
      }
   }

   private void saveCastleSiege() {
      this.setNextSiegeDate();
      this.getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
      this.getTimeRegistrationOverDate().add(5, 1);
      this.getCastle().setIsTimeRegistrationOver(false);
      this.saveSiegeDate();
      this.startAutoTask();
   }

   public void saveSiegeDate() {
      if (this._scheduledStartSiegeTask != null) {
         this._scheduledStartSiegeTask.cancel(true);
         this._scheduledStartSiegeTask = ThreadPoolManager.getInstance().schedule(new Siege.ScheduleStartSiegeTask(this.getCastle()), 1000L);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?  WHERE id = ?");
      ) {
         statement.setLong(1, this.getSiegeDate().getTimeInMillis());
         statement.setLong(2, this.getTimeRegistrationOverDate().getTimeInMillis());
         statement.setString(3, String.valueOf(this.getIsTimeRegistrationOver()));
         statement.setInt(4, this.getCastle().getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: saveSiegeDate(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   private void saveSiegeClan(Clan clan, byte typeId, boolean isUpdateRegistration) {
      if (clan.getCastleId() <= 0) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            if (typeId != 0 && typeId != 2 && typeId != -1) {
               if (this.getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans()) {
                  return;
               }
            } else if (this.getDefenderClans().size() + this.getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans()) {
               return;
            }

            if (!isUpdateRegistration) {
               try (PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)")) {
                  statement.setInt(1, clan.getId());
                  statement.setInt(2, this.getCastle().getId());
                  statement.setInt(3, typeId);
                  statement.execute();
               }
            } else {
               try (PreparedStatement statement = con.prepareStatement("UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?")) {
                  statement.setInt(1, typeId);
                  statement.setInt(2, this.getCastle().getId());
                  statement.setInt(3, clan.getId());
                  statement.execute();
               }
            }

            if (typeId != 0 && typeId != -1) {
               if (typeId == 1) {
                  this.addAttacker(clan.getId());
               } else if (typeId == 2) {
                  this.addDefenderWaiting(clan.getId());
                  return;
               }
            } else {
               this.addDefender(clan.getId());
            }
         } catch (Exception var62) {
            _log.log(Level.WARNING, "Exception: saveSiegeClan(Clan clan, int typeId, boolean isUpdateRegistration): " + var62.getMessage(), (Throwable)var62);
         }
      }
   }

   private void setNextSiegeDate() {
      while(this.getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
         String siegeDate = SiegeManager.getInstance().getCastleSiegeDate(this.getCastle().getId());
         if (siegeDate != null && !siegeDate.isEmpty()) {
            SchedulingPattern cronTime;
            try {
               cronTime = new SchedulingPattern(siegeDate);
            } catch (SchedulingPattern.InvalidPatternException var5) {
               return;
            }

            long nextTime = cronTime.next(System.currentTimeMillis());
            this.getCastle().getSiegeDate().setTimeInMillis(nextTime);
         } else {
            if (this.getCastle().getSiegeDate().get(7) != 7 && this.getCastle().getSiegeDate().get(7) != 1) {
               this.getCastle().getSiegeDate().set(7, 7);
            }

            if (this.getCastle().getSiegeDate().get(7) == 7) {
               this.getCastle().getSiegeDate().set(7, 1);
            }

            this.getCastle().getSiegeDate().set(11, 20);
            this.getCastle().getSiegeDate().set(12, 0);
            this.getCastle().getSiegeDate().set(13, 0);
            this.getCastle().getSiegeDate().add(5, 7);
         }
      }

      if (SiegeManager.getInstance().isCheckSevenSignStatus() && !SevenSigns.getInstance().isDateInSealValidPeriod(this.getCastle().getSiegeDate())) {
         this.getCastle().getSiegeDate().add(5, 7);
      }

      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
      sm.addCastleId(this.getCastle().getId());
      Broadcast.toAllOnlinePlayers(sm);
      this._isRegistrationOver = false;
   }

   private void spawnControlTower(int Id) {
      if (this._controlTowers == null) {
         this._controlTowers = new ArrayList<>();
      }

      for(SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id)) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(_sp.getNpcId());
         ControlTowerInstance ct = new ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
         ct.setCurrentHpMp((double)_sp.getHp(), ct.getMaxMp());
         ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
         ++this._controlTowerCount;
         ++this._controlTowerMaxCount;
         this._controlTowers.add(ct);
      }
   }

   private void spawnFlameTower(int Id) {
      if (this._flameTowers == null) {
         this._flameTowers = new ArrayList<>();
      }

      for(SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getFlameTowerSpawnList(Id)) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(_sp.getNpcId());
         FlameTowerInstance ct = new FlameTowerInstance(IdFactory.getInstance().getNextId(), template);
         ct.setCurrentHpMp((double)_sp.getHp(), ct.getMaxMp());
         ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
         ++this._flameTowerCount;
         ++this._flameTowerMaxCount;
         this._flameTowers.add(ct);
      }

      if (this._flameTowerCount == 0) {
         this._flameTowerCount = 1;
      }
   }

   private void spawnSiegeGuard() {
      this.getSiegeGuardManager().spawnSiegeGuard();
      if (!this.getSiegeGuardManager().getSiegeGuardSpawn().isEmpty() && !this._controlTowers.isEmpty()) {
         double distanceClosest = 0.0;

         for(Spawner spawn : this.getSiegeGuardManager().getSiegeGuardSpawn()) {
            if (spawn != null) {
               ControlTowerInstance closestCt = null;
               distanceClosest = 2.147483647E9;
               int x = spawn.getX();
               int y = spawn.getY();
               int z = spawn.getZ();

               for(ControlTowerInstance ct : this._controlTowers) {
                  if (ct != null) {
                     double distance = ct.getDistanceSq(x, y, z);
                     if (distance < distanceClosest) {
                        closestCt = ct;
                        distanceClosest = distance;
                     }
                  }
               }

               if (closestCt != null) {
                  closestCt.registerGuard(spawn);
               }
            }
         }
      }
   }

   @Override
   public final SiegeClan getAttackerClan(Clan clan) {
      return clan == null ? null : this.getAttackerClan(clan.getId());
   }

   @Override
   public final SiegeClan getAttackerClan(int clanId) {
      for(SiegeClan sc : this.getAttackerClans()) {
         if (sc != null && sc.getClanId() == clanId) {
            return sc;
         }
      }

      return null;
   }

   @Override
   public final List<SiegeClan> getAttackerClans() {
      return this._isNormalSide ? this._attackerClans : this._defenderClans;
   }

   public final int getAttackerRespawnDelay() {
      return SiegeManager.getInstance().getAttackerRespawnDelay();
   }

   public final Castle getCastle() {
      return this._castle != null && this._castle.length > 0 ? this._castle[0] : null;
   }

   @Override
   public final SiegeClan getDefenderClan(Clan clan) {
      return clan == null ? null : this.getDefenderClan(clan.getId());
   }

   @Override
   public final SiegeClan getDefenderClan(int clanId) {
      for(SiegeClan sc : this.getDefenderClans()) {
         if (sc != null && sc.getClanId() == clanId) {
            return sc;
         }
      }

      return null;
   }

   @Override
   public final List<SiegeClan> getDefenderClans() {
      return this._isNormalSide ? this._defenderClans : this._attackerClans;
   }

   public final SiegeClan getDefenderWaitingClan(Clan clan) {
      return clan == null ? null : this.getDefenderWaitingClan(clan.getId());
   }

   public final SiegeClan getDefenderWaitingClan(int clanId) {
      for(SiegeClan sc : this.getDefenderWaitingClans()) {
         if (sc != null && sc.getClanId() == clanId) {
            return sc;
         }
      }

      return null;
   }

   public final List<SiegeClan> getDefenderWaitingClans() {
      return this._defenderWaitingClans;
   }

   public final boolean getIsInProgress() {
      return this._isInProgress;
   }

   public final boolean getIsRegistrationOver() {
      return this._isRegistrationOver;
   }

   public final boolean getIsTimeRegistrationOver() {
      return this.getCastle().getIsTimeRegistrationOver();
   }

   @Override
   public final Calendar getSiegeDate() {
      return this.getCastle().getSiegeDate();
   }

   public final Calendar getTimeRegistrationOverDate() {
      return this.getCastle().getTimeRegistrationOverDate();
   }

   public void endTimeRegistration(boolean automatic) {
      this.getCastle().setIsTimeRegistrationOver(true);
      if (!automatic) {
         this.saveSiegeDate();
      }
   }

   @Override
   public List<Npc> getFlag(Clan clan) {
      if (clan != null) {
         SiegeClan sc = this.getAttackerClan(clan);
         if (sc != null) {
            return sc.getFlag();
         }
      }

      return null;
   }

   public final SiegeGuardManager getSiegeGuardManager() {
      if (this._siegeGuardManager == null) {
         this._siegeGuardManager = new SiegeGuardManager(this.getCastle());
      }

      return this._siegeGuardManager;
   }

   public int getControlTowerCount() {
      return this._controlTowerCount;
   }

   public int getControlTowerMaxCount() {
      return this._controlTowerMaxCount;
   }

   public int getFlameTowerMaxCount() {
      return this._flameTowerMaxCount;
   }

   public void disableTraps() {
      --this._flameTowerCount;
   }

   public boolean isTrapsActive() {
      return this._flameTowerCount > 0;
   }

   @Override
   public boolean giveFame() {
      return true;
   }

   @Override
   public int getFameFrequency() {
      return Config.CASTLE_ZONE_FAME_TASK_FREQUENCY;
   }

   @Override
   public int getFameAmount() {
      return Config.CASTLE_ZONE_FAME_AQUIRE_POINTS;
   }

   @Override
   public void updateSiege() {
   }

   private boolean fireSiegeListeners(ScriptListener.EventStage stage) {
      if (!siegeListeners.isEmpty()) {
         SiegeEvent event = new SiegeEvent();
         event.setSiege(this);
         event.setStage(stage);
         switch(stage) {
            case START:
               for(SiegeListener listener : siegeListeners) {
                  if (!listener.onStart(event)) {
                     return false;
                  }
               }
               break;
            case END:
               for(SiegeListener listener : siegeListeners) {
                  listener.onEnd(event);
               }
               break;
            case CONTROL_CHANGE:
               for(SiegeListener listener : siegeListeners) {
                  listener.onControlChange(event);
               }
         }
      }

      return true;
   }

   protected int getArtifactCount(int casleId) {
      return casleId != 7 && casleId != 9 ? 1 : 2;
   }

   public static void addSiegeListener(SiegeListener listener) {
      if (!siegeListeners.contains(listener)) {
         siegeListeners.add(listener);
      }
   }

   public static void removeSiegeListener(SiegeListener listener) {
      siegeListeners.remove(listener);
   }

   public class ScheduleEndSiegeTask implements Runnable {
      private final Castle _castleInst;

      public ScheduleEndSiegeTask(Castle pCastle) {
         this._castleInst = pCastle;
      }

      @Override
      public void run() {
         if (Siege.this.getIsInProgress()) {
            try {
               long timeRemaining = Siege.this._siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
               if (timeRemaining > 3600000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION);
                  sm.addNumber(2);
                  Siege.this.announceToPlayer(sm, true);
                  ThreadPoolManager.getInstance().schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 3600000L);
               } else if (timeRemaining <= 3600000L && timeRemaining > 600000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
                  sm.addNumber((int)timeRemaining / 60000);
                  Siege.this.announceToPlayer(sm, true);
                  ThreadPoolManager.getInstance().schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 600000L);
               } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
                  sm.addNumber((int)timeRemaining / 60000);
                  Siege.this.announceToPlayer(sm, true);
                  ThreadPoolManager.getInstance().schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 300000L);
               } else if (timeRemaining <= 300000L && timeRemaining > 10000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
                  sm.addNumber((int)timeRemaining / 60000);
                  Siege.this.announceToPlayer(sm, true);
                  ThreadPoolManager.getInstance().schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining - 10000L);
               } else if (timeRemaining <= 10000L && timeRemaining > 0L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT);
                  sm.addNumber((int)timeRemaining / 1000);
                  Siege.this.announceToPlayer(sm, true);
                  ThreadPoolManager.getInstance().schedule(Siege.this.new ScheduleEndSiegeTask(this._castleInst), timeRemaining);
               } else {
                  this._castleInst.getSiege().endSiege();
               }
            } catch (Exception var4) {
               Siege._log.log(Level.SEVERE, "", (Throwable)var4);
            }
         }
      }
   }

   public class ScheduleStartSiegeTask implements Runnable {
      private final Castle _castleInst;

      public ScheduleStartSiegeTask(Castle pCastle) {
         this._castleInst = pCastle;
      }

      @Override
      public void run() {
         Siege.this._scheduledStartSiegeTask.cancel(false);
         if (!Siege.this.getIsInProgress()) {
            try {
               if (!Siege.this.getIsTimeRegistrationOver()) {
                  long regTimeRemaining = Siege.this.getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                  if (regTimeRemaining > 0L) {
                     Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                        .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), regTimeRemaining);
                     return;
                  }

                  Siege.this.endTimeRegistration(true);
               }

               long timeRemaining = Siege.this.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
               if (timeRemaining > 86400000L) {
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 86400000L);
               } else if (timeRemaining <= 86400000L && timeRemaining > 13600000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
                  sm.addCastleId(Siege.this.getCastle().getId());
                  Announcements.getInstance().announceToAll(sm);
                  Siege.this._isRegistrationOver = true;
                  Siege.this.clearSiegeWaitingClan();
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 13600000L);
               } else if (timeRemaining <= 13600000L && timeRemaining > 600000L) {
                  Siege.this._isRegistrationOver = true;
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 600000L);
               } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
                  Siege.this._isRegistrationOver = true;
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 300000L);
               } else if (timeRemaining <= 300000L && timeRemaining > 10000L) {
                  Siege.this._isRegistrationOver = true;
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining - 10000L);
               } else if (timeRemaining <= 10000L && timeRemaining > 0L) {
                  Siege.this._isRegistrationOver = true;
                  Siege.this._scheduledStartSiegeTask = ThreadPoolManager.getInstance()
                     .schedule(Siege.this.new ScheduleStartSiegeTask(this._castleInst), timeRemaining);
               } else {
                  this._castleInst.getSiege().startSiege();
               }
            } catch (Exception var4) {
               Siege._log.log(Level.SEVERE, "", (Throwable)var4);
            }
         }
      }
   }

   public static enum TeleportWhoType {
      All,
      Attacker,
      DefenderNotOwner,
      Owner,
      Spectator;
   }
}
