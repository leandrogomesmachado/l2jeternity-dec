package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeGuardManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.listener.events.FortSiegeEvent;
import l2e.gameserver.listener.events.FortSiegeListener;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.CombatFlag;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BackupPowerUnitInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.FortCommanderInstance;
import l2e.gameserver.model.actor.instance.PowerControlUnitInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.spawn.SpawnFortSiege;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FortSiege implements Siegable {
   protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());
   private static List<FortSiegeListener> fortSiegeListeners = new LinkedList<>();
   private static final String DELETE_FORT_SIEGECLANS_BY_CLAN_ID = "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?";
   private static final String DELETE_FORT_SIEGECLANS = "DELETE FROM fortsiege_clans WHERE fort_id = ?";
   private final List<SiegeClan> _attackerClans = new CopyOnWriteArrayList<>();
   protected List<Spawner> _commanders = new CopyOnWriteArrayList<>();
   protected List<Spawner> _powerUnits = new CopyOnWriteArrayList<>();
   protected List<Spawner> _controlUnits = new CopyOnWriteArrayList<>();
   protected List<Spawner> _mainMachines = new CopyOnWriteArrayList<>();
   protected final Fort _fort;
   private boolean _isControlDisabled = true;
   private boolean _isControlDoorsOpen = false;
   private boolean _isInProgress = false;
   private FortSiegeGuardManager _siegeGuardManager;
   ScheduledFuture<?> _siegeEnd = null;
   ScheduledFuture<?> _siegeRestore = null;
   ScheduledFuture<?> _siegeStartTask = null;
   protected int _firstOwnerClanId = -1;

   public FortSiege(Fort fort) {
      this._fort = fort;
      this.checkAutoTask();
      FortSiegeManager.getInstance().addSiege(this);
   }

   @Override
   public void endSiege() {
      if (this.getIsInProgress()) {
         this._isInProgress = false;
         this._isControlDisabled = true;
         this._isControlDoorsOpen = false;
         this.removeFlags();
         this.unSpawnFlags();
         this.updatePlayerSiegeStateFlags(true);
         int ownerId = -1;
         if (this.getFort().getOwnerClan() != null) {
            ownerId = this.getFort().getOwnerClan().getId();
            if (ownerId == this._firstOwnerClanId) {
               RewardManager.getInstance().checkFortDefenceReward(this.getFort().getOwnerClan());
               if (AchievementManager.getInstance().isActive() && this.getFort().getOwnerClan() != null) {
                  for(ClanMember member : this.getFort().getOwnerClan().getMembers()) {
                     if (member != null && member.isOnline()) {
                        member.getPlayerInstance().getCounters().addAchivementInfo("fortSiegesDefend", 0, -1L, false, false, false);
                     }
                  }
               }
            } else {
               RewardManager.getInstance().checkFortCaptureReward(this.getFort().getOwnerClan());
            }
         }

         this.getFort().getZone().banishForeigners(ownerId);
         this.getFort().getZone().setIsActive(false);
         this.getFort().getZone().updateZoneStatusForCharactersInside();
         this.getFort().getZone().setSiegeInstance(null);
         this.saveFortSiege();
         this.clearSiegeClan();
         this.removeCommanders();
         this.removePowerUnits();
         this.removeControlUnits();
         this.removeMainMachine();
         this.getFort().spawnNpcCommanders();
         this.getSiegeGuardManager().unspawnSiegeGuard();
         this.getFort().resetDoors();
         ThreadPoolManager.getInstance()
            .schedule(new FortSiege.ScheduleSuspiciousMerchantSpawn(), (long)(FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60) * 1000L);
         this.setSiegeDateTime(true);
         if (this._siegeEnd != null) {
            this._siegeEnd.cancel(true);
            this._siegeEnd = null;
         }

         if (this._siegeRestore != null) {
            this._siegeRestore.cancel(true);
            this._siegeRestore = null;
         }

         if (this.getFort().getOwnerClan() != null && this.getFort().getFlagPole().getMeshIndex() == 0) {
            this.getFort().setVisibleFlag(true);
         }

         _log.info("Siege of " + this.getFort().getName() + " fort finished.");
         this.fireFortSiegeEventListeners(ScriptListener.EventStage.END);
      }
   }

   @Override
   public void startSiege() {
      if (!this.getIsInProgress()) {
         if (!this.fireFortSiegeEventListeners(ScriptListener.EventStage.START)) {
            return;
         }

         if (this._siegeStartTask != null) {
            this._siegeStartTask.cancel(true);
            this.getFort().despawnSuspiciousMerchant();
         }

         this._siegeStartTask = null;
         if (this.getAttackerClans().isEmpty()) {
            return;
         }

         this._isInProgress = true;
         this._isControlDisabled = true;
         this._isControlDoorsOpen = false;
         this.loadSiegeClan();
         this.updatePlayerSiegeStateFlags(false);
         this.teleportPlayer(FortSiege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
         if (this.getFort().getOwnerClan() != null) {
            this._firstOwnerClanId = this.getFort().getOwnerClan().getId();
         }

         this.getFort().despawnNpcCommanders();
         this.spawnCommanders();
         this.spawnControlUnits();
         this.getFort().resetDoors();
         this.spawnSiegeGuard();
         this.getFort().setVisibleFlag(false);
         this.getFort().getZone().setSiegeInstance(this);
         this.getFort().getZone().setIsActive(true);
         this.getFort().getZone().updateZoneStatusForCharactersInside();
         this._siegeEnd = ThreadPoolManager.getInstance()
            .schedule(new FortSiege.ScheduleEndSiegeTask(), (long)(FortSiegeManager.getInstance().getSiegeLength() * 60) * 1000L);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_S1_HAS_BEGUN);
         sm.addCastleId(this.getFort().getId());
         this.announceToPlayer(sm);
         this.saveFortSiege();
         _log.info("Siege of " + this.getFort().getName() + " fort started.");
      }
   }

   public void announceToPlayer(SystemMessage sm) {
      for(SiegeClan siegeclan : this.getAttackerClans()) {
         Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());

         for(Player member : clan.getOnlineMembers(0)) {
            if (member != null) {
               member.sendPacket(sm);
            }
         }
      }

      if (this.getFort().getOwnerClan() != null) {
         Clan clan = ClanHolder.getInstance().getClan(this.getFort().getOwnerClan().getId());

         for(Player member : clan.getOnlineMembers(0)) {
            if (member != null) {
               member.sendPacket(sm);
            }
         }
      }
   }

   public void announceToPlayer(SystemMessage sm, String s) {
      sm.addString(s);
      this.announceToPlayer(sm);
   }

   public void updatePlayerSiegeStateFlags(boolean clear) {
      for(SiegeClan siegeclan : this.getAttackerClans()) {
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
                  member.setSiegeSide(this.getFort().getId());
                  if (this.checkIfInZone(member)) {
                     member.setIsInSiege(true);
                     member.startFameTask((long)(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000), Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
                  }
               }

               member.broadcastUserInfo(true);
            }
         }
      }

      if (this.getFort().getOwnerClan() != null) {
         Clan clan = ClanHolder.getInstance().getClan(this.getFort().getOwnerClan().getId());

         for(Player member : clan.getOnlineMembers(0)) {
            if (member != null) {
               if (clear) {
                  member.setSiegeState((byte)0);
                  member.setSiegeSide(0);
                  member.setIsInSiege(false);
                  member.stopFameTask();
               } else {
                  member.setSiegeState((byte)2);
                  member.setSiegeSide(this.getFort().getId());
                  if (this.checkIfInZone(member)) {
                     member.setIsInSiege(true);
                     member.startFameTask((long)(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000), Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
                  }
               }

               member.broadcastUserInfo(true);
            }
         }
      }
   }

   public boolean checkIfInZone(GameObject object) {
      return this.checkIfInZone(object.getX(), object.getY(), object.getZ());
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this.getIsInProgress() && this.getFort().checkIfInZone(x, y, z);
   }

   @Override
   public boolean checkIsAttacker(Clan clan) {
      return this.getAttackerClan(clan) != null;
   }

   @Override
   public boolean checkIsDefender(Clan clan) {
      return clan != null && this.getFort().getOwnerClan() == clan;
   }

   public void clearSiegeClan() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
      ) {
         ps.setInt(1, this.getFort().getId());
         ps.execute();
         if (this.getFort().getOwnerClan() != null) {
            try (PreparedStatement delete = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?")) {
               delete.setInt(1, this.getFort().getOwnerClan().getId());
               delete.execute();
            }
         }

         this.getAttackerClans().clear();
         if (this.getIsInProgress()) {
            this.endSiege();
         }

         if (this._siegeStartTask != null) {
            this._siegeStartTask.cancel(true);
            this._siegeStartTask = null;
         }
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Exception: clearSiegeClan(): " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void clearSiegeDate() {
      this.getFort().getSiegeDate().setTimeInMillis(0L);
   }

   @Override
   public List<Player> getAttackersInZone() {
      List<Player> players = new LinkedList<>();

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

   public List<Player> getPlayersInZone() {
      return this.getFort().getZone().getPlayersInside();
   }

   public List<Player> getOwnersInZone() {
      List<Player> players = new LinkedList<>();
      if (this.getFort().getOwnerClan() != null) {
         Clan clan = ClanHolder.getInstance().getClan(this.getFort().getOwnerClan().getId());
         if (clan != this.getFort().getOwnerClan()) {
            return null;
         }

         for(Player player : clan.getOnlineMembers(0)) {
            if (player != null && player.isInSiege()) {
               players.add(player);
            }
         }
      }

      return players;
   }

   public void killedCommander(FortCommanderInstance instance) {
      if (this._commanders != null && this.getFort() != null && this._commanders.size() != 0) {
         Spawner spawn = instance.getSpawn();
         if (spawn != null) {
            for(SpawnFortSiege spawn2 : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getId())) {
               if (spawn2.getId() == spawn.getId()) {
                  NpcStringId npcString = null;
                  switch(spawn2.getId()) {
                     case 1:
                        npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
                        break;
                     case 2:
                        npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
                        break;
                     case 3:
                        npcString = NpcStringId.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
                        break;
                     case 4:
                        npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
                  }

                  if (npcString != null) {
                     instance.broadcastPacket(new NpcSay(instance.getObjectId(), 23, instance.getId(), npcString));
                  }
               }
            }

            this._commanders.remove(spawn);
            if (this._commanders.isEmpty()) {
               this.checkCommanders();
            } else if (this._siegeRestore == null) {
               this.getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
               this._siegeRestore = ThreadPoolManager.getInstance()
                  .schedule(new FortSiege.ScheduleSiegeRestore(), (long)(FortSiegeManager.getInstance().getCountDownLength() * 60) * 1000L);
            } else {
               this.getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
            }
         } else {
            _log.warning(
               "FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: "
                  + instance.getId()
                  + " FortId: "
                  + this.getFort().getId()
            );
         }
      }
   }

   public void killedControlUnit(BackupPowerUnitInstance instance) {
      if (this._controlUnits != null && this.getFort() != null && this._controlUnits.size() != 0) {
         Spawner spawn = instance.getSpawn();
         if (spawn != null) {
            this._controlUnits.remove(spawn);
         }
      }
   }

   public void killedPowerUnit(PowerControlUnitInstance instance) {
      if (this._powerUnits != null && this.getFort() != null && this._powerUnits.size() != 0) {
         Spawner spawn = instance.getSpawn();
         if (spawn != null) {
            this._powerUnits.remove(spawn);
         }
      }
   }

   public void checkCommanders() {
      if (this._commanders.isEmpty() && this._isControlDisabled) {
         this.spawnFlag(this.getFort().getId());
         if (this._siegeRestore != null) {
            this._siegeRestore.cancel(true);
         }

         for(DoorInstance door : this.getFort().getDoors()) {
            if (!door.getIsShowHp()) {
               door.openMe();
            }
         }

         this.getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.ALL_BARRACKS_OCCUPIED));
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

   public boolean registerAttacker(Player player, boolean force) {
      if (player.getClan() == null) {
         return false;
      } else if (!force && !this.checkIfCanRegister(player)) {
         return false;
      } else {
         this.saveSiegeClan(player.getClan());
         if (this.getAttackerClans().size() == 1) {
            if (!force) {
               player.reduceAdena("siege", 250000L, null, true);
            }

            this.startAutoTask(true);
         }

         return true;
      }
   }

   private void removeSiegeClan(int clanId) {
      String query = clanId != 0 ? "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?" : "DELETE FROM fortsiege_clans WHERE fort_id = ?";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(query);
      ) {
         statement.setInt(1, this.getFort().getId());
         if (clanId != 0) {
            statement.setInt(2, clanId);
         }

         statement.execute();
         this.loadSiegeClan();
         if (this.getAttackerClans().isEmpty()) {
            if (this.getIsInProgress()) {
               this.endSiege();
            } else {
               this.saveFortSiege();
            }

            if (this._siegeStartTask != null) {
               this._siegeStartTask.cancel(true);
               this._siegeStartTask = null;
            }
         }
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Exception on removeSiegeClan: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public void removeSiegeClan(Clan clan) {
      if (clan != null && clan.getFortId() != this.getFort().getId() && FortSiegeManager.getInstance().checkIsRegistered(clan, this.getFort().getId())) {
         this.removeSiegeClan(clan.getId());
      }
   }

   public void checkAutoTask() {
      if (this._siegeStartTask == null) {
         long delay = this.getFort().getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
         if (delay < 0L) {
            this.saveFortSiege();
            this.clearSiegeClan();
            ThreadPoolManager.getInstance().execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
         } else {
            this.loadSiegeClan();
            if (this.getAttackerClans().isEmpty()) {
               ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleSuspiciousMerchantSpawn(), delay);
            } else {
               if (delay > 3600000L) {
                  ThreadPoolManager.getInstance().execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
                  this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(3600), delay - 3600000L);
               }

               if (delay > 600000L) {
                  ThreadPoolManager.getInstance().execute(new FortSiege.ScheduleSuspiciousMerchantSpawn());
                  this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(600), delay - 600000L);
               } else if (delay > 300000L) {
                  this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(300), delay - 300000L);
               } else if (delay > 60000L) {
                  this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(60), delay - 60000L);
               } else {
                  this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(60), 0L);
               }

               _log.info("Siege of " + this.getFort().getName() + " fort: " + this.getFort().getSiegeDate().getTime());
            }
         }
      }
   }

   public void startAutoTask(boolean setTime) {
      if (this._siegeStartTask == null) {
         if (setTime) {
            this.setSiegeDateTime(false);
         }

         if (this.getFort().getOwnerClan() != null) {
            this.getFort().getOwnerClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
         }

         this._siegeStartTask = ThreadPoolManager.getInstance().schedule(new FortSiege.ScheduleStartSiegeTask(3600), 0L);
      }
   }

   public void teleportPlayer(FortSiege.TeleportWhoType teleportWho, TeleportWhereType teleportWhere) {
      List<Player> players;
      switch(teleportWho) {
         case Owner:
            players = this.getOwnersInZone();
            break;
         case Attacker:
            players = this.getAttackersInZone();
            break;
         default:
            players = this.getPlayersInZone();
      }

      for(Player player : players) {
         if (!player.canOverrideCond(PcCondOverride.FORTRESS_CONDITIONS) && !player.isJailed()) {
            player.teleToLocation(teleportWhere, true);
         }
      }
   }

   private void addAttacker(int clanId) {
      this.getAttackerClans().add(new SiegeClan(clanId, SiegeClan.SiegeClanType.ATTACKER));
   }

   public boolean checkIfCanRegister(Player player) {
      boolean b = true;
      if (player.getClan() == null || player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel()) {
         b = false;
         player.sendMessage(
            "Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fortress siege."
         );
      } else if ((player.getClanPrivileges() & 262144) != 262144) {
         b = false;
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else if (player.getClan() == this.getFort().getOwnerClan()) {
         b = false;
         player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
      } else if (this.getFort().getOwnerClan() != null
         && player.getClan().getCastleId() > 0
         && player.getClan().getCastleId() == this.getFort().getContractedCastleId()) {
         b = false;
         player.sendPacket(SystemMessageId.CANT_REGISTER_TO_SIEGE_DUE_TO_CONTRACT);
      } else if (this.getFort().getTimeTillRebelArmy() > 0 && this.getFort().getTimeTillRebelArmy() <= 7200) {
         b = false;
         player.sendMessage("You cannot register for the fortress siege 2 hours prior to rebel army attack.");
      } else if (this.getFort().getSiege().getAttackerClans().isEmpty() && player.getInventory().getAdena() < 250000L) {
         b = false;
         player.sendMessage("You need 250,000 adena to register");
      } else if (System.currentTimeMillis() >= TerritoryWarManager.getInstance().getTWStartTimeInMillis()
         || !TerritoryWarManager.getInstance().getIsRegistrationOver()) {
         if (System.currentTimeMillis() > TerritoryWarManager.getInstance().getTWStartTimeInMillis() && TerritoryWarManager.getInstance().isTWChannelOpen()) {
            b = false;
            player.sendMessage("This is not a good time. You cannot register.");
         } else {
            for(Fort fort : FortManager.getInstance().getForts()) {
               if (fort.getSiege().getAttackerClan(player.getClanId()) != null) {
                  b = false;
                  player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
                  break;
               }

               if (fort.getOwnerClan() == player.getClan() && (fort.getSiege().getIsInProgress() || fort.getSiege()._siegeStartTask != null)) {
                  b = false;
                  player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
                  break;
               }
            }

            for(Castle castle : CastleManager.getInstance().getCastles()) {
               if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getId()) && castle.getSiege().getIsInProgress()) {
                  b = false;
                  player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
                  break;
               }
            }
         }
      } else {
         b = false;
         player.sendMessage("This is not a good time. You cannot register.");
      }

      return b;
   }

   public boolean checkIfAlreadyRegisteredForSameDay(Clan clan) {
      for(FortSiege siege : FortSiegeManager.getInstance().getSieges()) {
         if (siege != this && siege.getSiegeDate().get(7) == this.getSiegeDate().get(7)) {
            if (siege.checkIsAttacker(clan)) {
               return true;
            }

            if (siege.checkIsDefender(clan)) {
               return true;
            }
         }
      }

      return false;
   }

   private void setSiegeDateTime(boolean merchant) {
      Calendar newDate = Calendar.getInstance();
      if (merchant) {
         newDate.add(12, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
      } else {
         newDate.add(12, 60);
      }

      this.getFort().setSiegeDate(newDate);
      this.saveSiegeDate();
   }

   private void loadSiegeClan() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         this.getAttackerClans().clear();
         PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");
         statement.setInt(1, this.getFort().getId());
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.addAttacker(rs.getInt("clan_id"));
         }

         rs.close();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.WARNING, "Exception: loadSiegeClan(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   private void removePowerUnits() {
      if (this._powerUnits != null && !this._powerUnits.isEmpty()) {
         for(Spawner spawn : this._powerUnits) {
            if (spawn != null) {
               spawn.stopRespawn();
               if (spawn.getLastSpawn() != null) {
                  spawn.getLastSpawn().deleteMe();
               }
            }
         }

         this._powerUnits.clear();
      }
   }

   private void removeControlUnits() {
      if (this._controlUnits != null && !this._controlUnits.isEmpty()) {
         for(Spawner spawn : this._controlUnits) {
            if (spawn != null) {
               spawn.stopRespawn();
               if (spawn.getLastSpawn() != null) {
                  spawn.getLastSpawn().deleteMe();
               }
            }
         }

         this._controlUnits.clear();
      }
   }

   private void removeMainMachine() {
      if (this._mainMachines != null && !this._mainMachines.isEmpty()) {
         for(Spawner spawn : this._mainMachines) {
            if (spawn != null) {
               spawn.stopRespawn();
               if (spawn.getLastSpawn() != null) {
                  spawn.getLastSpawn().deleteMe();
               }
            }
         }

         this._mainMachines.clear();
      }
   }

   private void removeCommanders() {
      if (this._commanders != null && !this._commanders.isEmpty()) {
         for(Spawner spawn : this._commanders) {
            if (spawn != null) {
               spawn.stopRespawn();
               if (spawn.getLastSpawn() != null) {
                  spawn.getLastSpawn().deleteMe();
               }
            }
         }

         this._commanders.clear();
      }
   }

   private void removeFlags() {
      for(SiegeClan sc : this.getAttackerClans()) {
         if (sc != null) {
            sc.removeFlags();
         }
      }
   }

   private void saveFortSiege() {
      this.clearSiegeDate();
      this.saveSiegeDate();
   }

   private void saveSiegeDate() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?");
      ) {
         ps.setLong(1, this.getSiegeDate().getTimeInMillis());
         ps.setInt(2, this.getFort().getId());
         ps.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: saveSiegeDate(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   private void saveSiegeClan(Clan clan) {
      if (this.getAttackerClans().size() < FortSiegeManager.getInstance().getAttackerMaxClans()) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)");
         ) {
            statement.setInt(1, clan.getId());
            statement.setInt(2, this.getFort().getId());
            statement.execute();
            this.addAttacker(clan.getId());
         } catch (Exception var34) {
            _log.log(Level.WARNING, "Exception: saveSiegeClan(Clan clan): " + var34.getMessage(), (Throwable)var34);
         }
      }
   }

   private void spawnCommanders() {
      try {
         this._commanders.clear();

         for(SpawnFortSiege _sp : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getId())) {
            NpcTemplate template1 = NpcsParser.getInstance().getTemplate(_sp.getId());
            if (template1 != null) {
               Spawner spawnDat = new Spawner(template1);
               spawnDat.setAmount(1);
               spawnDat.setX(_sp.getLocation().getX());
               spawnDat.setY(_sp.getLocation().getY());
               spawnDat.setZ(_sp.getLocation().getZ());
               spawnDat.setHeading(_sp.getLocation().getHeading());
               spawnDat.setRespawnDelay(60);
               spawnDat.doSpawn();
               spawnDat.stopRespawn();
               this._commanders.add(spawnDat);
            } else {
               _log.warning("FortSiege.spawnCommander: Data missing in NPC table for ID: " + _sp.getId() + ".");
            }
         }
      } catch (Exception var5) {
         _log.log(Level.WARNING, "FortSiege.spawnCommander: Spawn could not be initialized: " + var5.getMessage(), (Throwable)var5);
      }
   }

   private void spawnControlUnits() {
      if (FortSiegeManager.getInstance().getControlUnitSpawnList(this.getFort().getId()) != null
         && !FortSiegeManager.getInstance().getControlUnitSpawnList(this.getFort().getId()).isEmpty()) {
         try {
            this._controlUnits.clear();

            for(SpawnFortSiege _sp : FortSiegeManager.getInstance().getControlUnitSpawnList(this.getFort().getId())) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(_sp.getId());
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(1);
                  spawnDat.setX(_sp.getLocation().getX());
                  spawnDat.setY(_sp.getLocation().getY());
                  spawnDat.setZ(_sp.getLocation().getZ());
                  spawnDat.setHeading(_sp.getLocation().getHeading());
                  spawnDat.setRespawnDelay(60);
                  spawnDat.doSpawn();
                  spawnDat.stopRespawn();
                  this._controlUnits.add(spawnDat);
               } else {
                  _log.warning("FortSiege.spawnControlUnit: Data missing in NPC table for ID: " + _sp.getId() + ".");
               }
            }

            this._isControlDisabled = false;
            this._isControlDoorsOpen = false;
         } catch (Exception var5) {
            _log.log(Level.WARNING, "FortSiege.spawnControlUnit: Spawn could not be initialized: " + var5.getMessage(), (Throwable)var5);
         }
      }
   }

   public void spawnPowerUnits() {
      if (FortSiegeManager.getInstance().getPowerUnitSpawnList(this.getFort().getId()) != null
         && !FortSiegeManager.getInstance().getPowerUnitSpawnList(this.getFort().getId()).isEmpty()) {
         try {
            this._powerUnits.clear();

            for(SpawnFortSiege _sp : FortSiegeManager.getInstance().getPowerUnitSpawnList(this.getFort().getId())) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(_sp.getId());
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(1);
                  spawnDat.setX(_sp.getLocation().getX());
                  spawnDat.setY(_sp.getLocation().getY());
                  spawnDat.setZ(_sp.getLocation().getZ());
                  spawnDat.setHeading(_sp.getLocation().getHeading());
                  spawnDat.setRespawnDelay(60);
                  spawnDat.doSpawn();
                  spawnDat.stopRespawn();
                  this._powerUnits.add(spawnDat);
               } else {
                  _log.warning("FortSiege.spawnPowerUnit: Data missing in NPC table for ID: " + _sp.getId() + ".");
               }
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "FortSiege.spawnPowerUnit: Spawn could not be initialized: " + var5.getMessage(), (Throwable)var5);
         }
      }
   }

   public void spawnMainMachine() {
      if (FortSiegeManager.getInstance().getMainMachineSpawnList(this.getFort().getId()) != null
         && !FortSiegeManager.getInstance().getMainMachineSpawnList(this.getFort().getId()).isEmpty()) {
         try {
            this._mainMachines.clear();

            for(SpawnFortSiege _sp : FortSiegeManager.getInstance().getMainMachineSpawnList(this.getFort().getId())) {
               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(_sp.getId());
               if (template1 != null) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(1);
                  spawnDat.setX(_sp.getLocation().getX());
                  spawnDat.setY(_sp.getLocation().getY());
                  spawnDat.setZ(_sp.getLocation().getZ());
                  spawnDat.setHeading(_sp.getLocation().getHeading());
                  spawnDat.setRespawnDelay(60);
                  spawnDat.doSpawn();
                  spawnDat.stopRespawn();
                  this._mainMachines.add(spawnDat);
               } else {
                  _log.warning("FortSiege.spawnMainMachine: Data missing in NPC table for ID: " + _sp.getId() + ".");
               }
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "FortSiege.spawnMainMachine: Spawn could not be initialized: " + var5.getMessage(), (Throwable)var5);
         }
      }
   }

   private void spawnFlag(int Id) {
      for(CombatFlag cf : FortSiegeManager.getInstance().getFlagList(Id)) {
         cf.spawnMe();
      }
   }

   private void unSpawnFlags() {
      if (FortSiegeManager.getInstance().getFlagList(this.getFort().getId()) != null) {
         for(CombatFlag cf : FortSiegeManager.getInstance().getFlagList(this.getFort().getId())) {
            cf.unSpawnMe();
         }
      }
   }

   private void spawnSiegeGuard() {
      this.getSiegeGuardManager().spawnSiegeGuard();
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
      return this._attackerClans;
   }

   public final Fort getFort() {
      return this._fort;
   }

   public final boolean getIsInProgress() {
      return this._isInProgress;
   }

   @Override
   public final Calendar getSiegeDate() {
      return this.getFort().getSiegeDate();
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

   public final FortSiegeGuardManager getSiegeGuardManager() {
      if (this._siegeGuardManager == null) {
         this._siegeGuardManager = new FortSiegeGuardManager(this.getFort());
      }

      return this._siegeGuardManager;
   }

   public void resetSiege() {
      this.removeCommanders();
      this.removePowerUnits();
      this.removeControlUnits();
      this.removeMainMachine();
      this._isControlDisabled = true;
      this._isControlDoorsOpen = false;
      this.spawnCommanders();
      this.spawnControlUnits();
      this.getFort().resetDoors();
   }

   public List<Spawner> getCommanders() {
      return this._commanders;
   }

   public List<Spawner> getControlUnits() {
      return this._controlUnits;
   }

   public List<Spawner> getPowerUnits() {
      return this._powerUnits;
   }

   public List<Spawner> getMainMachine() {
      return this._mainMachines;
   }

   public void disablePower(boolean disable) {
      this._isControlDisabled = disable;
   }

   public void setOpenControlDoors(boolean open) {
      this._isControlDoorsOpen = open;
   }

   public boolean isControlDoorsOpen() {
      return this._isControlDoorsOpen;
   }

   public void openControlDoors(int id) {
      switch(id) {
         case 102:
            DoorParser.getInstance().getDoor(19240003).openMe();
            DoorParser.getInstance().getDoor(19240004).openMe();
         case 103:
         case 105:
         case 106:
         case 108:
         case 111:
         case 114:
         case 115:
         default:
            break;
         case 104:
            DoorParser.getInstance().getDoor(23210002).openMe();
            DoorParser.getInstance().getDoor(23210003).openMe();
            break;
         case 107:
            DoorParser.getInstance().getDoor(25190002).openMe();
            DoorParser.getInstance().getDoor(25190003).openMe();
            break;
         case 109:
            DoorParser.getInstance().getDoor(24150009).openMe();
            DoorParser.getInstance().getDoor(24150010).openMe();
            break;
         case 110:
            DoorParser.getInstance().getDoor(22160002).openMe();
            DoorParser.getInstance().getDoor(22160003).openMe();
            break;
         case 112:
            DoorParser.getInstance().getDoor(20220017).openMe();
            DoorParser.getInstance().getDoor(20220018).openMe();
            break;
         case 113:
            DoorParser.getInstance().getDoor(18200004).openMe();
            DoorParser.getInstance().getDoor(18200005).openMe();
            break;
         case 116:
            DoorParser.getInstance().getDoor(22200010).openMe();
            DoorParser.getInstance().getDoor(22200011).openMe();
            break;
         case 117:
            DoorParser.getInstance().getDoor(23170006).openMe();
            DoorParser.getInstance().getDoor(23170007).openMe();
            break;
         case 118:
            DoorParser.getInstance().getDoor(23200004).openMe();
            DoorParser.getInstance().getDoor(23200005).openMe();
      }

      this._isControlDoorsOpen = true;
   }

   @Override
   public SiegeClan getDefenderClan(int clanId) {
      return null;
   }

   @Override
   public SiegeClan getDefenderClan(Clan clan) {
      return null;
   }

   @Override
   public List<SiegeClan> getDefenderClans() {
      return null;
   }

   @Override
   public boolean giveFame() {
      return true;
   }

   @Override
   public int getFameFrequency() {
      return Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
   }

   @Override
   public int getFameAmount() {
      return Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
   }

   @Override
   public void updateSiege() {
   }

   private boolean fireFortSiegeEventListeners(ScriptListener.EventStage stage) {
      if (!fortSiegeListeners.isEmpty()) {
         FortSiegeEvent event = new FortSiegeEvent();
         event.setSiege(this);
         event.setStage(stage);
         switch(stage) {
            case START:
               for(FortSiegeListener listener : fortSiegeListeners) {
                  if (!listener.onStart(event)) {
                     return false;
                  }
               }
               break;
            case END:
               for(FortSiegeListener listener : fortSiegeListeners) {
                  listener.onEnd(event);
               }
         }
      }

      return true;
   }

   public static void addFortSiegeListener(FortSiegeListener listener) {
      if (!fortSiegeListeners.contains(listener)) {
         fortSiegeListeners.add(listener);
      }
   }

   public static void removeFortSiegeListener(FortSiegeListener listener) {
      fortSiegeListeners.remove(listener);
   }

   public class ScheduleEndSiegeTask implements Runnable {
      @Override
      public void run() {
         if (FortSiege.this.getIsInProgress()) {
            try {
               FortSiege.this._siegeEnd = null;
               FortSiege.this.endSiege();
            } catch (Exception var2) {
               FortSiege._log
                  .log(
                     Level.WARNING, "Exception: ScheduleEndSiegeTask() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), (Throwable)var2
                  );
            }
         }
      }
   }

   public class ScheduleSiegeRestore implements Runnable {
      @Override
      public void run() {
         if (FortSiege.this.getIsInProgress()) {
            try {
               FortSiege.this._siegeRestore = null;
               FortSiege.this.resetSiege();
               FortSiege.this.announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.BARRACKS_FUNCTION_RESTORED));
            } catch (Exception var2) {
               FortSiege._log
                  .log(
                     Level.WARNING, "Exception: ScheduleSiegeRestore() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(), (Throwable)var2
                  );
            }
         }
      }
   }

   public class ScheduleStartSiegeTask implements Runnable {
      private final Fort _fortInst = FortSiege.this._fort;
      private final int _time;

      public ScheduleStartSiegeTask(int time) {
         this._time = time;
      }

      @Override
      public void run() {
         if (!FortSiege.this.getIsInProgress()) {
            try {
               if (this._time == 3600) {
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(600), 3000000L);
               } else if (this._time == 600) {
                  FortSiege.this.getFort().despawnSuspiciousMerchant();
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(10);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(300), 300000L);
               } else if (this._time == 300) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(5);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(60), 240000L);
               } else if (this._time == 60) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(1);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(30), 30000L);
               } else if (this._time == 30) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(30);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(10), 20000L);
               } else if (this._time == 10) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(10);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(5), 5000L);
               } else if (this._time == 5) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(5);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(1), 4000L);
               } else if (this._time == 1) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
                  sm.addNumber(1);
                  FortSiege.this.announceToPlayer(sm);
                  ThreadPoolManager.getInstance().schedule(FortSiege.this.new ScheduleStartSiegeTask(0), 1000L);
               } else if (this._time == 0) {
                  this._fortInst.getSiege().startSiege();
               } else {
                  FortSiege._log.warning("Exception: ScheduleStartSiegeTask(): unknown siege time: " + String.valueOf(this._time));
               }
            } catch (Exception var2) {
               FortSiege._log
                  .log(Level.WARNING, "Exception: ScheduleStartSiegeTask() for Fort: " + this._fortInst.getName() + " " + var2.getMessage(), (Throwable)var2);
            }
         }
      }
   }

   public class ScheduleSuspiciousMerchantSpawn implements Runnable {
      @Override
      public void run() {
         if (!FortSiege.this.getIsInProgress()) {
            try {
               FortSiege.this._fort.spawnSuspiciousMerchant();
            } catch (Exception var2) {
               FortSiege._log
                  .log(
                     Level.WARNING,
                     "Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + FortSiege.this._fort.getName() + " " + var2.getMessage(),
                     (Throwable)var2
                  );
            }
         }
      }
   }

   public static enum TeleportWhoType {
      All,
      Attacker,
      Owner;
   }
}
