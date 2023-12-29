package l2e.gameserver.model.entity;

import gnu.trove.map.hash.TIntIntHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.FortUpdater;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.data.parser.StaticObjectsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.zone.type.FortZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Fort implements IIdentifiable {
   protected static final Logger _log = Logger.getLogger(Fort.class.getName());
   private int _fortId = 0;
   private final List<DoorInstance> _doors = new ArrayList<>();
   private StaticObjectInstance _flagPole = null;
   private String _name = "";
   private FortSiege _siege = null;
   private Calendar _siegeDate;
   private Calendar _lastOwnedTime;
   private FortZone _fortZone;
   private SiegeZone _zone;
   private Clan _fortOwner = null;
   private int _fortType = 0;
   private int _state = 0;
   private int _castleId = 0;
   private int _supplyLvL = 0;
   private final Map<Integer, Fort.FortFunction> _function;
   private final List<Skill> _residentialSkills = new CopyOnWriteArrayList<>();
   private final ScheduledFuture<?>[] _FortUpdater = new ScheduledFuture[2];
   private boolean _isSuspiciousMerchantSpawned = false;
   private final List<Spawner> _siegeNpcs = new CopyOnWriteArrayList<>();
   private final List<Spawner> _npcCommanders = new CopyOnWriteArrayList<>();
   private final List<Spawner> _specialEnvoys = new CopyOnWriteArrayList<>();
   private final TIntIntHashMap _envoyCastles = new TIntIntHashMap(2);
   private final Set<Integer> _availableCastles = new HashSet<>(1);
   public static final int FUNC_TELEPORT = 1;
   public static final int FUNC_RESTORE_HP = 2;
   public static final int FUNC_RESTORE_MP = 3;
   public static final int FUNC_RESTORE_EXP = 4;
   public static final int FUNC_SUPPORT = 5;

   public Fort(int fortId) {
      this._fortId = fortId;
      this.load();
      this.loadFlagPoles();
      this._function = new ConcurrentHashMap<>();

      for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(fortId)) {
         Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
         if (sk != null) {
            this._residentialSkills.add(sk);
         } else {
            _log.warning("Fort Id: " + fortId + " has a null residential skill Id: " + s.getId() + " level: " + s.getLvl() + "!");
         }
      }

      if (this.getOwnerClan() != null) {
         this.setVisibleFlag(true);
         this.loadFunctions();
      }

      this.initNpcs();
      this.initSiegeNpcs();
      this.initNpcCommanders();
      this.spawnNpcCommanders();
      this.initSpecialEnvoys();
      if (this.getOwnerClan() != null && this.getFortState() == 0) {
         this.spawnSpecialEnvoys();
         ThreadPoolManager.getInstance().schedule(new Fort.ScheduleSpecialEnvoysDeSpawn(this), 3600000L);
      }
   }

   public Fort.FortFunction getFunction(int type) {
      return this._function.get(type) != null ? this._function.get(type) : null;
   }

   public void endOfSiege(Clan clan) {
      ThreadPoolManager.getInstance().schedule(new Fort.endFortressSiege(this, clan), 1000L);
   }

   public void engrave(Clan clan) {
      this.setOwner(clan, true);
   }

   public void banishForeigners() {
      this.getFortZone().banishForeigners(this.getOwnerClan().getId());
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this.getZone().isInsideZone(x, y, z);
   }

   public SiegeZone getZone() {
      if (this._zone == null) {
         for(SiegeZone zone : ZoneManager.getInstance().getAllZones(SiegeZone.class)) {
            if (zone.getSiegeObjectId() == this.getId()) {
               this._zone = zone;
               break;
            }
         }
      }

      return this._zone;
   }

   public FortZone getFortZone() {
      if (this._fortZone == null) {
         for(FortZone zone : ZoneManager.getInstance().getAllZones(FortZone.class)) {
            if (zone.getFortId() == this.getId()) {
               this._fortZone = zone;
               break;
            }
         }
      }

      return this._fortZone;
   }

   public double getDistance(GameObject obj) {
      return this.getZone().getDistanceToZone(obj);
   }

   public void closeDoor(Player activeChar, int doorId) {
      this.openCloseDoor(activeChar, doorId, false);
   }

   public void openDoor(Player activeChar, int doorId) {
      this.openCloseDoor(activeChar, doorId, true);
   }

   public void openCloseDoor(Player activeChar, int doorId, boolean open) {
      if (activeChar.getClan() == this.getOwnerClan()) {
         DoorInstance door = this.getDoor(doorId);
         if (door != null) {
            if (open) {
               door.openMe();
            } else {
               door.closeMe();
            }
         }
      }
   }

   public void removeUpgrade() {
      this.removeDoorUpgrade();
   }

   public boolean setOwner(Clan clan, boolean updateClansReputation) {
      if (clan == null) {
         _log.warning(this.getClass().getSimpleName() + ": Updating Fort owner with null clan!!!");
         return false;
      } else {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
         sm.addCastleId(this.getId());
         this.getSiege().announceToPlayer(sm);
         Clan oldowner = this.getOwnerClan();
         if (oldowner != null && clan != oldowner) {
            this.updateClansReputation(oldowner, true);

            try {
               Player oldleader = oldowner.getLeader().getPlayerInstance();
               if (oldleader != null && oldleader.getMountType() == MountType.WYVERN) {
                  oldleader.dismount();
               }
            } catch (Exception var11) {
               _log.log(Level.WARNING, "Exception in setOwner: " + var11.getMessage(), (Throwable)var11);
            }

            this.removeOwner(true);
         }

         this.setFortState(0, 0);
         if (clan.getCastleId() > 0) {
            this.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
            return false;
         } else {
            if (updateClansReputation) {
               this.updateClansReputation(clan, false);
            }

            this.spawnSpecialEnvoys();
            ThreadPoolManager.getInstance().schedule(new Fort.ScheduleSpecialEnvoysDeSpawn(this), 3600000L);
            if (clan.getFortId() > 0) {
               FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
            }

            this.setSupplyLvL(0);
            this.setOwnerClan(clan);
            this.updateOwnerInDB();
            this.saveFortVariables();
            if (this.getSiege().getIsInProgress()) {
               this.getSiege().endSiege();
            }

            for(Player member : clan.getOnlineMembers(0)) {
               this.giveResidentialSkills(member);
               member.sendSkillList(false);
               if (Config.ALLOW_DAILY_TASKS && member.getActiveDailyTasks() != null) {
                  for(PlayerTaskTemplate taskTemplate : member.getActiveDailyTasks()) {
                     if (taskTemplate.getType().equalsIgnoreCase("Siege") && !taskTemplate.isComplete()) {
                        DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                        if (task.getSiegeFort()) {
                           taskTemplate.setIsComplete(true);
                           member.updateDailyStatus(taskTemplate);
                           IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                           if (vch != null) {
                              vch.useVoicedCommand("missions", member, null);
                           }
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   public void removeOwner(boolean updateDB) {
      Clan clan = this.getOwnerClan();
      if (clan != null) {
         for(Player member : clan.getOnlineMembers(0)) {
            this.removeResidentialSkills(member);
            member.sendSkillList(false);
         }

         clan.setFortId(0);
         clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
         this.setOwnerClan(null);
         this.setSupplyLvL(0);
         this.saveFortVariables();
         this.removeAllFunctions();
         if (updateDB) {
            this.updateOwnerInDB();
         }
      }
   }

   public void raiseSupplyLvL() {
      ++this._supplyLvL;
      if (this._supplyLvL > Config.FS_MAX_SUPPLY_LEVEL) {
         this._supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
      }
   }

   public void setSupplyLvL(int val) {
      if (val <= Config.FS_MAX_SUPPLY_LEVEL) {
         this._supplyLvL = val;
      }
   }

   public int getSupplyLvL() {
      return this._supplyLvL;
   }

   public void saveFortVariables() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE fort SET supplyLvL=? WHERE id = ?");
      ) {
         ps.setInt(1, this._supplyLvL);
         ps.setInt(2, this.getId());
         ps.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: saveFortVariables(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public void setVisibleFlag(boolean val) {
      StaticObjectInstance flagPole = this.getFlagPole();
      if (flagPole != null) {
         flagPole.setMeshIndex(val ? 1 : 0);
      }
   }

   public void resetDoors() {
      for(DoorInstance door : this._doors) {
         if (door.getOpen()) {
            door.closeMe();
         }

         if (door.isDead()) {
            door.doRevive();
         }

         if (door.getCurrentHp() < door.getMaxHp()) {
            door.setCurrentHp(door.getMaxHp());
         }
      }

      this.loadDoorUpgrade();
   }

   public void upgradeDoor(int doorId, int hp, int pDef, int mDef) {
      DoorInstance door = this.getDoor(doorId);
      if (door != null && door.getDoorId() == doorId) {
         door.setCurrentHp(door.getMaxHp() + (double)hp);
         this.saveDoorUpgrade(doorId, hp, pDef, mDef);
      }
   }

   private void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM fort WHERE id = ?");
         statement.setInt(1, this.getId());
         ResultSet rs = statement.executeQuery();

         int ownerId;
         for(ownerId = 0; rs.next(); this._supplyLvL = rs.getInt("supplyLvL")) {
            this._name = rs.getString("name");
            this._siegeDate = Calendar.getInstance();
            this._lastOwnedTime = Calendar.getInstance();
            this._siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
            this._lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
            ownerId = rs.getInt("owner");
            this._fortType = rs.getInt("fortType");
            this._state = rs.getInt("state");
            this._castleId = rs.getInt("castleId");
         }

         rs.close();
         statement.close();
         if (ownerId > 0) {
            Clan clan = ClanHolder.getInstance().getClan(ownerId);
            clan.setFortId(this.getId());
            this.setOwnerClan(clan);
            int runCount = this.getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
            long initial = System.currentTimeMillis() - this._lastOwnedTime.getTimeInMillis();

            while(initial > (long)Config.FS_UPDATE_FRQ * 60000L) {
               initial -= (long)Config.FS_UPDATE_FRQ * 60000L;
            }

            initial = (long)Config.FS_UPDATE_FRQ * 60000L - initial;
            if (Config.FS_MAX_OWN_TIME > 0 && this.getOwnedTime() >= Config.FS_MAX_OWN_TIME * 3600) {
               this._FortUpdater[1] = ThreadPoolManager.getInstance().schedule(new FortUpdater(this, clan, 0, FortUpdater.UpdaterType.MAX_OWN_TIME), 60000L);
            } else {
               this._FortUpdater[0] = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(
                     new FortUpdater(this, clan, runCount, FortUpdater.UpdaterType.PERIODIC_UPDATE), initial, (long)Config.FS_UPDATE_FRQ * 60000L
                  );
               if (Config.FS_MAX_OWN_TIME > 0) {
                  this._FortUpdater[1] = ThreadPoolManager.getInstance()
                     .scheduleAtFixedRate(new FortUpdater(this, clan, runCount, FortUpdater.UpdaterType.MAX_OWN_TIME), 3600000L, 3600000L);
               }
            }
         } else {
            this.setOwnerClan(null);
         }
      } catch (Exception var20) {
         _log.log(Level.WARNING, "Exception: loadFortData(): " + var20.getMessage(), (Throwable)var20);
      }
   }

   private void loadFunctions() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_functions WHERE fort_id = ?");
         statement.setInt(1, this.getId());
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this._function
               .put(
                  rs.getInt("type"),
                  new Fort.FortFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true)
               );
         }

         rs.close();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Exception: Fort.loadFunctions(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   public void removeFunction(int functionType) {
      this._function.remove(functionType);

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM fort_functions WHERE fort_id=? AND type=?");
         statement.setInt(1, this.getId());
         statement.setInt(2, functionType);
         statement.execute();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Exception: Fort.removeFunctions(int functionType): " + var15.getMessage(), (Throwable)var15);
      }
   }

   private void removeAllFunctions() {
      for(int id : this._function.keySet()) {
         this.removeFunction(id);
      }
   }

   public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate, boolean addNew) {
      if (player == null) {
         return false;
      } else if (lease > 0 && !player.destroyItemByItemId("Consume", 57, (long)lease, null, true)) {
         return false;
      } else {
         if (addNew) {
            this._function.put(type, new Fort.FortFunction(type, lvl, lease, 0, rate, 0L, false));
         } else if (lvl == 0 && lease == 0) {
            this.removeFunction(type);
         } else {
            int diffLease = lease - this._function.get(type).getLease();
            if (diffLease > 0) {
               this._function.remove(type);
               this._function.put(type, new Fort.FortFunction(type, lvl, lease, 0, rate, -1L, false));
            } else {
               this._function.get(type).setLease(lease);
               this._function.get(type).setLvl(lvl);
               this._function.get(type).dbSave();
            }
         }

         return true;
      }
   }

   public void activateInstance() {
      this.loadDoor();
   }

   private void loadDoor() {
      for(DoorInstance door : DoorParser.getInstance().getDoors()) {
         if (door.getFort() != null && door.getFort().getId() == this.getId()) {
            this._doors.add(door);
         }
      }
   }

   private void loadFlagPoles() {
      for(StaticObjectInstance obj : StaticObjectsParser.getInstance().getStaticObjects()) {
         if (obj.getType() == 3 && obj.getName().startsWith(this._name)) {
            this._flagPole = obj;
            break;
         }
      }

      if (this._flagPole == null) {
         throw new NullPointerException("Can't find flagpole for Fort " + this);
      }
   }

   private void loadDoorUpgrade() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_doorupgrade WHERE fortId = ?");
         statement.setInt(1, this.getId());
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
         }

         rs.close();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.WARNING, "Exception: loadFortDoorUpgrade(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   private void removeDoorUpgrade() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM fort_doorupgrade WHERE fortId = ?");
         statement.setInt(1, this.getId());
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Exception: removeDoorUpgrade(): " + var14.getMessage(), (Throwable)var14);
      }
   }

   private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) VALUES (?,?,?,?)");
         statement.setInt(1, doorId);
         statement.setInt(2, hp);
         statement.setInt(3, pDef);
         statement.setInt(4, mDef);
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, "Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + var18.getMessage(), (Throwable)var18);
      }
   }

   private void updateOwnerInDB() {
      Clan clan = this.getOwnerClan();
      int clanId = 0;
      if (clan != null) {
         clanId = clan.getId();
         this._lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
      } else {
         this._lastOwnedTime.setTimeInMillis(0L);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE fort SET owner=?,lastOwnedTime=?,state=?,castleId=? WHERE id = ?");
      ) {
         ps.setInt(1, clanId);
         ps.setLong(2, this._lastOwnedTime.getTimeInMillis());
         ps.setInt(3, 0);
         ps.setInt(4, 0);
         ps.setInt(5, this.getId());
         ps.execute();
         if (clan != null) {
            clan.setFortId(this.getId());
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2);
            sm.addString(clan.getName());
            sm.addCastleId(this.getId());
            World.getInstance().getAllPlayers().forEach(p -> p.sendPacket(sm));
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
            clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
            if (this._FortUpdater[0] != null) {
               this._FortUpdater[0].cancel(false);
            }

            if (this._FortUpdater[1] != null) {
               this._FortUpdater[1].cancel(false);
            }

            this._FortUpdater[0] = ThreadPoolManager.getInstance()
               .scheduleAtFixedRate(
                  new FortUpdater(this, clan, 0, FortUpdater.UpdaterType.PERIODIC_UPDATE),
                  (long)Config.FS_UPDATE_FRQ * 60000L,
                  (long)Config.FS_UPDATE_FRQ * 60000L
               );
            if (Config.FS_MAX_OWN_TIME > 0) {
               this._FortUpdater[1] = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(new FortUpdater(this, clan, 0, FortUpdater.UpdaterType.MAX_OWN_TIME), 3600000L, 3600000L);
            }
         } else {
            if (this._FortUpdater[0] != null) {
               this._FortUpdater[0].cancel(false);
            }

            this._FortUpdater[0] = null;
            if (this._FortUpdater[1] != null) {
               this._FortUpdater[1].cancel(false);
            }

            this._FortUpdater[1] = null;
         }
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Exception: updateOwnerInDB(Clan clan): " + var35.getMessage(), (Throwable)var35);
      }
   }

   @Override
   public final int getId() {
      return this._fortId;
   }

   public final Clan getOwnerClan() {
      return this._fortOwner;
   }

   public final void setOwnerClan(Clan clan) {
      this.setVisibleFlag(clan != null);
      this._fortOwner = clan;
   }

   public final DoorInstance getDoor(int doorId) {
      if (doorId <= 0) {
         return null;
      } else {
         for(DoorInstance door : this.getDoors()) {
            if (door.getDoorId() == doorId) {
               return door;
            }
         }

         return null;
      }
   }

   public final List<DoorInstance> getDoors() {
      return this._doors;
   }

   public final StaticObjectInstance getFlagPole() {
      return this._flagPole;
   }

   public final FortSiege getSiege() {
      if (this._siege == null) {
         this._siege = new FortSiege(this);
      }

      return this._siege;
   }

   public final Calendar getSiegeDate() {
      return this._siegeDate;
   }

   public final void setSiegeDate(Calendar siegeDate) {
      this._siegeDate = siegeDate;
   }

   public final int getOwnedTime() {
      return this._lastOwnedTime.getTimeInMillis() == 0L ? 0 : (int)((System.currentTimeMillis() - this._lastOwnedTime.getTimeInMillis()) / 1000L);
   }

   public final int getTimeTillRebelArmy() {
      return this._lastOwnedTime.getTimeInMillis() == 0L
         ? 0
         : (int)((this._lastOwnedTime.getTimeInMillis() + (long)Config.FS_MAX_OWN_TIME * 3600000L - System.currentTimeMillis()) / 1000L);
   }

   public final long getTimeTillNextFortUpdate() {
      return this._FortUpdater[0] == null ? 0L : this._FortUpdater[0].getDelay(TimeUnit.SECONDS);
   }

   public final String getName() {
      return this._name;
   }

   public void updateClansReputation(Clan owner, boolean removePoints) {
      if (owner != null) {
         if (removePoints) {
            owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
         } else {
            owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
         }
      }
   }

   public final int getFortState() {
      return this._state;
   }

   public final void setFortState(int state, int castleId) {
      this._state = state;
      this._castleId = castleId;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE fort SET state=?,castleId=? WHERE id = ?");
         statement.setInt(1, this.getFortState());
         statement.setInt(2, this.getContractedCastleId());
         statement.setInt(3, this.getId());
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Exception: setFortState(int state, int castleId): " + var16.getMessage(), (Throwable)var16);
      }
   }

   public final int getFortType() {
      return this._fortType;
   }

   public final int getCastleIdByAmbassador(int npcId) {
      return this._envoyCastles.get(npcId);
   }

   public final Castle getCastleByAmbassador(int npcId) {
      return CastleManager.getInstance().getCastleById(this.getCastleIdByAmbassador(npcId));
   }

   public final int getContractedCastleId() {
      return this._castleId;
   }

   public final Castle getContractedCastle() {
      return CastleManager.getInstance().getCastleById(this.getContractedCastleId());
   }

   public final boolean isBorderFortress() {
      return this._availableCastles.size() > 1;
   }

   public final int getFortSize() {
      return this.getFortType() == 0 ? 3 : 5;
   }

   public void spawnSuspiciousMerchant() {
      if (!this._isSuspiciousMerchantSpawned) {
         this._isSuspiciousMerchantSpawned = true;

         for(Spawner spawnDat : this._siegeNpcs) {
            spawnDat.doSpawn();
            spawnDat.startRespawn();
         }
      }
   }

   public void despawnSuspiciousMerchant() {
      if (this._isSuspiciousMerchantSpawned) {
         this._isSuspiciousMerchantSpawned = false;

         for(Spawner spawnDat : this._siegeNpcs) {
            spawnDat.stopRespawn();
            spawnDat.getLastSpawn().deleteMe();
         }
      }
   }

   public void spawnNpcCommanders() {
      for(Spawner spawnDat : this._npcCommanders) {
         spawnDat.doSpawn();
         spawnDat.startRespawn();
      }
   }

   public void despawnNpcCommanders() {
      for(Spawner spawnDat : this._npcCommanders) {
         spawnDat.stopRespawn();
         spawnDat.getLastSpawn().deleteMe();
      }
   }

   public void spawnSpecialEnvoys() {
      for(Spawner spawnDat : this._specialEnvoys) {
         spawnDat.doSpawn();
         spawnDat.startRespawn();
      }
   }

   public void despawnSpecialEnvoys() {
      for(Spawner spawnDat : this._specialEnvoys) {
         spawnDat.stopRespawn();
         spawnDat.getLastSpawn().deleteMe();
      }
   }

   private void initNpcs() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = ?");
         statement.setInt(1, this.getId());
         statement.setInt(2, 0);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(rset.getInt("npcId"));
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(rset.getInt("x"));
               spawnDat.setY(rset.getInt("y"));
               spawnDat.setZ(rset.getInt("z"));
               spawnDat.setHeading(rset.getInt("heading"));
               spawnDat.setRespawnDelay(60);
               SpawnParser.getInstance().addNewSpawn(spawnDat);
               spawnDat.doSpawn();
               spawnDat.startRespawn();
            } else {
               _log.warning("Fort " + this.getId() + " initNpcs: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Fort " + this.getId() + " initNpcs: Spawn could not be initialized: " + var17.getMessage(), (Throwable)var17);
      }
   }

   private void initSiegeNpcs() {
      this._siegeNpcs.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"
         );
         statement.setInt(1, this.getId());
         statement.setInt(2, 2);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(rset.getInt("npcId"));
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(rset.getInt("x"));
               spawnDat.setY(rset.getInt("y"));
               spawnDat.setZ(rset.getInt("z"));
               spawnDat.setHeading(rset.getInt("heading"));
               spawnDat.setRespawnDelay(60);
               this._siegeNpcs.add(spawnDat);
            } else {
               _log.warning("Fort " + this.getId() + " initSiegeNpcs: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Fort " + this.getId() + " initSiegeNpcs: Spawn could not be initialized: " + var17.getMessage(), (Throwable)var17);
      }
   }

   private void initNpcCommanders() {
      this._npcCommanders.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"
         );
         statement.setInt(1, this.getId());
         statement.setInt(2, 1);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(rset.getInt("npcId"));
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(rset.getInt("x"));
               spawnDat.setY(rset.getInt("y"));
               spawnDat.setZ(rset.getInt("z"));
               spawnDat.setHeading(rset.getInt("heading"));
               spawnDat.setRespawnDelay(60);
               this._npcCommanders.add(spawnDat);
            } else {
               _log.warning("Fort " + this.getId() + " initNpcCommanders: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Fort " + this.getId() + " initNpcCommanders: Spawn could not be initialized: " + var17.getMessage(), (Throwable)var17);
      }
   }

   private void initSpecialEnvoys() {
      this._specialEnvoys.clear();
      this._envoyCastles.clear();
      this._availableCastles.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT id, npcId, x, y, z, heading, castleId FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"
         );
         statement.setInt(1, this.getId());
         statement.setInt(2, 3);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int castleId = rset.getInt("castleId");
            int npcId = rset.getInt("npcId");
            NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
            if (template != null) {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(rset.getInt("x"));
               spawnDat.setY(rset.getInt("y"));
               spawnDat.setZ(rset.getInt("z"));
               spawnDat.setHeading(rset.getInt("heading"));
               spawnDat.setRespawnDelay(60);
               this._specialEnvoys.add(spawnDat);
               this._envoyCastles.put(npcId, castleId);
               this._availableCastles.add(castleId);
            } else {
               _log.warning("Fort " + this.getId() + " initSpecialEnvoys: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var19) {
         _log.log(Level.WARNING, "Fort " + this.getId() + " initSpecialEnvoys: Spawn could not be initialized: " + var19.getMessage(), (Throwable)var19);
      }
   }

   public List<Skill> getResidentialSkills() {
      return this._residentialSkills;
   }

   public void giveResidentialSkills(Player player) {
      if (this._residentialSkills != null && !this._residentialSkills.isEmpty()) {
         for(Skill sk : this._residentialSkills) {
            player.addSkill(sk, false);
         }
      }
   }

   public void removeResidentialSkills(Player player) {
      if (this._residentialSkills != null && !this._residentialSkills.isEmpty()) {
         for(Skill sk : this._residentialSkills) {
            player.removeSkill(sk, false, true);
         }
      }
   }

   @Override
   public String toString() {
      return this._name + "(" + this._fortId + ")";
   }

   public class FortFunction {
      private final int _type;
      private int _lvl;
      protected int _fee;
      protected int _tempFee;
      private final long _rate;
      private long _endDate;
      protected boolean _inDebt;
      public boolean _cwh;

      public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh) {
         this._type = type;
         this._lvl = lvl;
         this._fee = lease;
         this._tempFee = tempLease;
         this._rate = rate;
         this._endDate = time;
         this.initializeTask(cwh);
      }

      public int getType() {
         return this._type;
      }

      public int getLvl() {
         return this._lvl;
      }

      public int getLease() {
         return this._fee;
      }

      public long getRate() {
         return this._rate;
      }

      public long getEndTime() {
         return this._endDate;
      }

      public void setLvl(int lvl) {
         this._lvl = lvl;
      }

      public void setLease(int lease) {
         this._fee = lease;
      }

      public void setEndTime(long time) {
         this._endDate = time;
      }

      private void initializeTask(boolean cwh) {
         if (Fort.this.getOwnerClan() != null) {
            long currentTime = System.currentTimeMillis();
            if (this._endDate > currentTime) {
               ThreadPoolManager.getInstance().schedule(new Fort.FortFunction.FunctionTask(cwh), this._endDate - currentTime);
            } else {
               ThreadPoolManager.getInstance().schedule(new Fort.FortFunction.FunctionTask(cwh), 0L);
            }
         }
      }

      public void dbSave() {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("REPLACE INTO fort_functions (fort_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
            statement.setInt(1, Fort.this.getId());
            statement.setInt(2, this.getType());
            statement.setInt(3, this.getLvl());
            statement.setInt(4, this.getLease());
            statement.setLong(5, this.getRate());
            statement.setLong(6, this.getEndTime());
            statement.execute();
            statement.close();
         } catch (Exception var14) {
            Fort._log
               .log(
                  Level.SEVERE,
                  "Exception: Fort.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + var14.getMessage(),
                  (Throwable)var14
               );
         }
      }

      private class FunctionTask implements Runnable {
         public FunctionTask(boolean cwh) {
            FortFunction.this._cwh = cwh;
         }

         @Override
         public void run() {
            try {
               if (Fort.this.getOwnerClan() == null) {
                  return;
               }

               if (Fort.this.getOwnerClan().getWarehouse().getAdena() < (long)FortFunction.this._fee && FortFunction.this._cwh) {
                  Fort.this.removeFunction(FortFunction.this.getType());
               } else {
                  int fee = FortFunction.this._fee;
                  if (FortFunction.this.getEndTime() == -1L) {
                     fee = FortFunction.this._tempFee;
                  }

                  FortFunction.this.setEndTime(System.currentTimeMillis() + FortFunction.this.getRate());
                  FortFunction.this.dbSave();
                  if (FortFunction.this._cwh) {
                     Fort.this.getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", 57, (long)fee, null, null);
                  }

                  ThreadPoolManager.getInstance().schedule(FortFunction.this.new FunctionTask(true), FortFunction.this.getRate());
               }
            } catch (Throwable var2) {
            }
         }
      }
   }

   public static class ScheduleSpecialEnvoysDeSpawn implements Runnable {
      private final Fort _fortInst;

      public ScheduleSpecialEnvoysDeSpawn(Fort pFort) {
         this._fortInst = pFort;
      }

      @Override
      public void run() {
         try {
            if (this._fortInst.getFortState() == 0) {
               this._fortInst.setFortState(1, 0);
            }

            this._fortInst.despawnSpecialEnvoys();
         } catch (Exception var2) {
            Fort._log
               .log(Level.WARNING, "Exception: ScheduleSpecialEnvoysSpawn() for Fort " + this._fortInst.getName() + ": " + var2.getMessage(), (Throwable)var2);
         }
      }
   }

   private static class endFortressSiege implements Runnable {
      private final Fort _f;
      private final Clan _clan;

      public endFortressSiege(Fort f, Clan clan) {
         this._f = f;
         this._clan = clan;
      }

      @Override
      public void run() {
         try {
            this._f.engrave(this._clan);
         } catch (Exception var2) {
            Fort._log.log(Level.WARNING, "Exception in endFortressSiege " + var2.getMessage(), (Throwable)var2);
         }
      }
   }
}
