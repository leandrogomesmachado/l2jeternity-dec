package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.GameSettings;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Siegable;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TerritoryWarManager implements Siegable {
   protected static final Logger _log = Logger.getLogger(TerritoryWarManager.class.getName());
   private static final String DELETE = "DELETE FROM territory_registrations WHERE castleId = ? and registeredId = ?";
   private static final String INSERT = "INSERT INTO territory_registrations (castleId, registeredId) values (?, ?)";
   public static String qn = "TerritoryWarSuperClass";
   public static int DEFENDERMAXCLANS;
   public static int DEFENDERMAXPLAYERS;
   public static int CLANMINLEVEL;
   public static int PLAYERMINLEVEL;
   public static int MINTWBADGEFORNOBLESS;
   public static int MINTWBADGEFORSTRIDERS;
   public static int MINTWBADGEFORBIGSTRIDER;
   public static Long WARLENGTH;
   public static boolean PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE;
   public static boolean SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS;
   public static boolean RETURN_WARDS_WHEN_TW_STARTS;
   private static String _siegeDateTime;
   public static final Map<Integer, Integer> TERRITORY_ITEM_IDS = new HashMap<>();
   private final Map<Integer, List<Clan>> _registeredClans = new ConcurrentHashMap<>();
   private final Map<Integer, List<Integer>> _registeredMercenaries = new ConcurrentHashMap<>();
   private final Map<Integer, TerritoryWarManager.Territory> _territoryList = new ConcurrentHashMap<>();
   protected List<Integer> _disguisedPlayers = new CopyOnWriteArrayList<>();
   private final List<TerritoryWard> _territoryWards = new CopyOnWriteArrayList<>();
   private final Map<Integer, SiegeFlagInstance> _outposts = new ConcurrentHashMap<>();
   private final Map<Clan, SiegeFlagInstance> _clanFlags = new ConcurrentHashMap<>();
   private final Map<Integer, Integer[]> _participantPoints = new ConcurrentHashMap<>();
   protected Calendar _startTWDate = Calendar.getInstance();
   protected boolean _isRegistrationOver = true;
   protected boolean _isTWChannelOpen = false;
   private boolean _isTWInProgress = false;
   protected ScheduledFuture<?> _scheduledStartTWTask = null;
   protected ScheduledFuture<?> _scheduledEndTWTask = null;
   protected ScheduledFuture<?> _scheduledRewardOnlineTask = null;

   public static final TerritoryWarManager getInstance() {
      return TerritoryWarManager.SingletonHolder._instance;
   }

   protected TerritoryWarManager() {
      this.load();
      this.generateNextWarTime();
   }

   public int getRegisteredTerritoryId(Player player) {
      if (player != null && this._isTWChannelOpen && player.getLevel() >= PLAYERMINLEVEL) {
         if (player.getClan() != null) {
            if (player.getClan().getCastleId() > 0) {
               return player.getClan().getCastleId() + 80;
            }

            for(int cId : this._registeredClans.keySet()) {
               if (this._registeredClans.get(cId).contains(player.getClan())) {
                  return cId + 80;
               }
            }
         }

         for(int cId : this._registeredMercenaries.keySet()) {
            if (this._registeredMercenaries.get(cId).contains(player.getObjectId())) {
               return cId + 80;
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   public boolean isAllyField(Player player, int fieldId) {
      if (player != null && player.getSiegeSide() != 0) {
         if (player.getSiegeSide() - 80 == fieldId) {
            return true;
         } else {
            return fieldId > 100
               && this._territoryList.containsKey(player.getSiegeSide() - 80)
               && this._territoryList.get(player.getSiegeSide() - 80).getFortId() == fieldId;
         }
      } else {
         return false;
      }
   }

   public final boolean checkIsRegistered(int castleId, Clan clan) {
      if (clan == null) {
         return false;
      } else if (clan.getCastleId() > 0) {
         return castleId == -1 ? true : clan.getCastleId() == castleId;
      } else if (castleId == -1) {
         for(int cId : this._registeredClans.keySet()) {
            if (this._registeredClans.get(cId).contains(clan)) {
               return true;
            }
         }

         return false;
      } else {
         return this._registeredClans.get(castleId).contains(clan);
      }
   }

   public final boolean checkIsRegistered(int castleId, int objId) {
      if (castleId == -1) {
         for(int cId : this._registeredMercenaries.keySet()) {
            if (this._registeredMercenaries.get(cId).contains(objId)) {
               return true;
            }
         }

         return false;
      } else {
         return this._registeredMercenaries.get(castleId).contains(objId);
      }
   }

   public TerritoryWarManager.Territory getTerritory(int castleId) {
      return this._territoryList.get(castleId);
   }

   public List<TerritoryWarManager.Territory> getAllTerritories() {
      List<TerritoryWarManager.Territory> ret = new LinkedList<>();

      for(TerritoryWarManager.Territory t : this._territoryList.values()) {
         if (t.getOwnerClan() != null) {
            ret.add(t);
         }
      }

      return ret;
   }

   public Collection<Clan> getRegisteredClans(int castleId) {
      return this._registeredClans.get(castleId);
   }

   public void addDisguisedPlayer(int playerObjId) {
      this._disguisedPlayers.add(playerObjId);
   }

   public boolean isDisguised(int playerObjId) {
      return this._disguisedPlayers.contains(playerObjId);
   }

   public Collection<Integer> getRegisteredMercenaries(int castleId) {
      return this._registeredMercenaries.get(castleId);
   }

   public long getTWStartTimeInMillis() {
      return this._startTWDate.getTimeInMillis();
   }

   public Calendar getTWStart() {
      return this._startTWDate;
   }

   public void setTWStartTimeInMillis(long time) {
      this._startTWDate.setTimeInMillis(time);
      if (this._isTWInProgress) {
         if (this._scheduledEndTWTask != null) {
            this._scheduledEndTWTask.cancel(false);
         }

         this._scheduledEndTWTask = ThreadPoolManager.getInstance().schedule(new TerritoryWarManager.ScheduleEndTWTask(), 1000L);
      } else {
         if (this._scheduledStartTWTask != null) {
            this._scheduledStartTWTask.cancel(false);
         }

         this._scheduledStartTWTask = ThreadPoolManager.getInstance().schedule(new TerritoryWarManager.ScheduleStartTWTask(), 1000L);
      }
   }

   public boolean isTWChannelOpen() {
      return this._isTWChannelOpen;
   }

   public void registerClan(int castleId, Clan clan) {
      if (clan != null && (this._registeredClans.get(castleId) == null || !this._registeredClans.get(castleId).contains(clan))) {
         this._registeredClans.putIfAbsent(castleId, new CopyOnWriteArrayList<>());
         this._registeredClans.get(castleId).add(clan);
         this.changeRegistration(castleId, clan.getId(), false);
      }
   }

   public void registerMerc(int castleId, Player player) {
      if (player != null
         && player.getLevel() >= PLAYERMINLEVEL
         && (this._registeredMercenaries.get(castleId) == null || !this._registeredMercenaries.get(castleId).contains(player.getObjectId()))) {
         if (this._registeredMercenaries.get(castleId) == null) {
            this._registeredMercenaries.put(castleId, new CopyOnWriteArrayList<>());
         }

         this._registeredMercenaries.get(castleId).add(player.getObjectId());
         this.changeRegistration(castleId, player.getObjectId(), false);
      }
   }

   public void removeClan(int castleId, Clan clan) {
      if (clan != null) {
         if (this._registeredClans.get(castleId) != null && this._registeredClans.get(castleId).contains(clan)) {
            this._registeredClans.get(castleId).remove(clan);
            this.changeRegistration(castleId, clan.getId(), true);
         }
      }
   }

   public void removeMerc(int castleId, Player player) {
      if (player != null) {
         if (this._registeredMercenaries.get(castleId) != null && this._registeredMercenaries.get(castleId).contains(player.getObjectId())) {
            this._registeredMercenaries.get(castleId).remove(this._registeredMercenaries.get(castleId).indexOf(player.getObjectId()));
            this.changeRegistration(castleId, player.getObjectId(), true);
         }
      }
   }

   public boolean getIsRegistrationOver() {
      return this._isRegistrationOver;
   }

   public boolean isTWInProgress() {
      return this._isTWInProgress;
   }

   public void territoryCatapultDestroyed(int castleId) {
      if (this._territoryList.get(castleId) != null) {
         this._territoryList.get(castleId).changeNPCsSpawn(2, false);
      }

      for(DoorInstance door : CastleManager.getInstance().getCastleById(castleId).getDoors()) {
         door.openMe();
      }
   }

   public Npc addTerritoryWard(int territoryId, int newOwnerId, int oldOwnerId, boolean broadcastMessage) {
      Npc ret = null;
      if (this._territoryList.get(newOwnerId) != null) {
         TerritoryWarManager.Territory terNew = this._territoryList.get(newOwnerId);
         TerritoryWarManager.TerritoryNPCSpawn ward = terNew.getFreeWardSpawnPlace();
         if (ward != null) {
            ward._npcId = territoryId;
            ret = this.spawnNPC(36491 + territoryId, ward.getLocation());
            ward.setNPC(ret);
            if (!this.isTWInProgress() && !SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS) {
               ret.decayMe();
            }

            if (terNew.getOwnerClan() != null && terNew.getOwnedWardIds().contains(newOwnerId + 80)) {
               for(int wardId : terNew.getOwnedWardIds()) {
                  for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(wardId)) {
                     Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
                     if (sk != null) {
                        for(Player member : terNew.getOwnerClan().getOnlineMembers(0)) {
                           if (!member.isInOlympiadMode()) {
                              member.addSkill(sk, false);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (this._territoryList.containsKey(oldOwnerId)) {
            TerritoryWarManager.Territory terOld = this._territoryList.get(oldOwnerId);
            terOld.removeWard(territoryId);
            this.updateTerritoryData(terOld);
            this.updateTerritoryData(terNew);
            if (broadcastMessage) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_SUCCEDED_IN_CAPTURING_S2_TERRITORY_WARD);
               sm.addString(terNew.getOwnerClan().getName());
               sm.addCastleId(terNew.getTerritoryId());
               this.announceToParticipants(sm, 135000, 13500);
            }

            if (terOld.getOwnerClan() != null) {
               for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(territoryId)) {
                  Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
                  if (sk != null) {
                     for(Player member : terOld.getOwnerClan().getOnlineMembers(0)) {
                        member.removeSkill(sk, false);
                     }
                  }
               }

               if (!terOld.getOwnedWardIds().isEmpty() && !terOld.getOwnedWardIds().contains(oldOwnerId + 80)) {
                  for(int wardId : terOld.getOwnedWardIds()) {
                     for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(wardId)) {
                        Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
                        if (sk != null) {
                           for(Player member : terOld.getOwnerClan().getOnlineMembers(0)) {
                              member.removeSkill(sk, false);
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         _log.warning("TerritoryWarManager: Missing territory for new Ward owner: " + newOwnerId + ";" + territoryId);
      }

      return ret;
   }

   public SiegeFlagInstance getHQForClan(Clan clan) {
      return clan != null && clan.getCastleId() > 0 ? this._outposts.get(clan.getCastleId()) : null;
   }

   public void setHQForClan(Clan clan, SiegeFlagInstance hq) {
      if (clan != null && clan.getCastleId() > 0) {
         this._outposts.put(clan.getCastleId(), hq);
      }
   }

   public void removeHQForClan(Clan clan) {
      if (clan != null && clan.getCastleId() > 0) {
         SiegeFlagInstance flag = this._outposts.remove(clan.getCastleId());
         if (flag != null) {
            flag.deleteMe();
         }
      }
   }

   public void addClanFlag(Clan clan, SiegeFlagInstance flag) {
      this._clanFlags.put(clan, flag);
   }

   public boolean isClanHasFlag(Clan clan) {
      return this._clanFlags.containsKey(clan);
   }

   public SiegeFlagInstance getFlagForClan(Clan clan) {
      return this._clanFlags.get(clan);
   }

   public void removeClanFlag(Clan clan) {
      this._clanFlags.remove(clan);
   }

   public List<TerritoryWard> getAllTerritoryWards() {
      return this._territoryWards;
   }

   public TerritoryWard getTerritoryWardForOwner(int castleId) {
      for(TerritoryWard twWard : this._territoryWards) {
         if (twWard.getTerritoryId() == castleId) {
            return twWard;
         }
      }

      return null;
   }

   public TerritoryWard getTerritoryWard(int territoryId) {
      for(TerritoryWard twWard : this._territoryWards) {
         if (twWard.getTerritoryId() == territoryId) {
            return twWard;
         }
      }

      return null;
   }

   public TerritoryWard getTerritoryWard(Player player) {
      for(TerritoryWard twWard : this._territoryWards) {
         if (twWard.playerId == player.getObjectId()) {
            return twWard;
         }
      }

      return null;
   }

   public void dropCombatFlag(Player player, boolean isKilled, boolean isSpawnBack) {
      for(TerritoryWard twWard : this._territoryWards) {
         if (twWard.playerId == player.getObjectId()) {
            twWard.dropIt();
            if (this.isTWInProgress()) {
               if (isKilled) {
                  twWard.spawnMe();
               } else if (isSpawnBack) {
                  twWard.spawnBack();
               } else {
                  for(TerritoryWarManager.TerritoryNPCSpawn wardSpawn : this._territoryList.get(twWard.getOwnerCastleId()).getOwnedWard()) {
                     if (wardSpawn.getId() == twWard.getTerritoryId()) {
                        wardSpawn.setNPC(wardSpawn.getNpc().getSpawn().doSpawn());
                        twWard.unSpawnMe();
                        twWard.setNpc(wardSpawn.getNpc());
                     }
                  }
               }
            }

            if (isKilled) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_CHAR_THAT_ACQUIRED_S1_WARD_HAS_BEEN_KILLED);
               sm.addString(twWard.getNpc().getName().replaceAll(" Ward", ""));
               this.announceToParticipants(sm, 0, 0);
            }
         }
      }
   }

   public void giveTWQuestPoint(Player player) {
      if (!this._participantPoints.containsKey(player.getObjectId())) {
         this._participantPoints.put(player.getObjectId(), new Integer[]{player.getSiegeSide(), 0, 0, 0, 0, 0, 0});
      }

      Integer[] var2 = (Integer[])this._participantPoints.get(player.getObjectId());
      Integer var3 = var2[2];
      Integer var4 = var2[2] = var2[2] + 1;
   }

   public void giveTWPoint(Player killer, int victimSide, int type) {
      if (victimSide != 0) {
         if (killer.getParty() != null && type < 5) {
            for(Player pl : killer.getParty().getMembers()) {
               if (pl.getSiegeSide() != victimSide && pl.getSiegeSide() != 0 && Util.checkIfInRange(2000, killer, pl, false)) {
                  if (!this._participantPoints.containsKey(pl.getObjectId())) {
                     this._participantPoints.put(pl.getObjectId(), new Integer[]{pl.getSiegeSide(), 0, 0, 0, 0, 0, 0});
                  }

                  Integer[] var11 = (Integer[])this._participantPoints.get(pl.getObjectId());
                  Integer var8 = var11[type];
                  Integer var9 = var11[type] = var11[type] + 1;
               }
            }
         } else {
            if (killer.getSiegeSide() == victimSide || killer.getSiegeSide() == 0) {
               return;
            }

            if (!this._participantPoints.containsKey(killer.getObjectId())) {
               this._participantPoints.put(killer.getObjectId(), new Integer[]{killer.getSiegeSide(), 0, 0, 0, 0, 0, 0});
            }

            Integer[] var4 = (Integer[])this._participantPoints.get(killer.getObjectId());
            Integer var6 = var4[type];
            Integer var7 = var4[type] = var4[type] + 1;
         }
      }
   }

   public int[] calcReward(Player player) {
      if (this._participantPoints.containsKey(player.getObjectId())) {
         int[] reward = new int[2];
         Integer[] temp = (Integer[])this._participantPoints.get(player.getObjectId());
         reward[0] = temp[0];
         reward[1] = 0;
         if (temp[6] < 10) {
            return reward;
         } else {
            reward[1] += temp[6] > 70 ? 7 : (int)((double)temp[6].intValue() * 0.1);
            reward[1] += temp[2] * 7;
            if (temp[1] < 50) {
               reward[1] = (int)((double)reward[1] + (double)temp[1].intValue() * 0.1);
            } else if (temp[1] < 120) {
               reward[1] += 5 + (temp[1] - 50) / 14;
            } else {
               reward[1] += 10;
            }

            reward[1] += temp[3];
            reward[1] += temp[4] * 2;
            reward[1] += temp[5] > 0 ? 5 : 0;
            reward[1] += Math.min(this._territoryList.get(temp[0] - 80).getQuestDone()[0], 10);
            reward[1] += this._territoryList.get(temp[0] - 80).getQuestDone()[1];
            reward[1] += this._territoryList.get(temp[0] - 80).getOwnedWardIds().size();
            return reward;
         }
      } else {
         return new int[]{0, 0};
      }
   }

   public void debugReward(Player player) {
      player.sendMessage("Registred TerrId: " + player.getSiegeSide());
      if (this._participantPoints.containsKey(player.getObjectId())) {
         Integer[] temp = (Integer[])this._participantPoints.get(player.getObjectId());
         player.sendMessage("TerrId: " + temp[0]);
         player.sendMessage("PcKill: " + temp[1]);
         player.sendMessage("PcQuests: " + temp[2]);
         player.sendMessage("npcKill: " + temp[3]);
         player.sendMessage("CatatKill: " + temp[4]);
         player.sendMessage("WardKill: " + temp[5]);
         player.sendMessage("onlineTime: " + temp[6]);
      } else {
         player.sendMessage("No points for you!");
      }

      if (this._territoryList.containsKey(player.getSiegeSide() - 80)) {
         player.sendMessage("Your Territory's jobs:");
         player.sendMessage("npcKill: " + this._territoryList.get(player.getSiegeSide() - 80).getQuestDone()[0]);
         player.sendMessage("WardCaptured: " + this._territoryList.get(player.getSiegeSide() - 80).getQuestDone()[1]);
      }
   }

   public void resetReward(Player player) {
      if (this._participantPoints.containsKey(player.getObjectId())) {
         this._participantPoints.get(player.getObjectId())[6] = 0;
      }
   }

   public Npc spawnNPC(int npcId, Location loc) {
      NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
      if (template != null) {
         try {
            Spawner spawnDat = new Spawner(template);
            spawnDat.setAmount(1);
            spawnDat.setX(loc.getX());
            spawnDat.setY(loc.getY());
            spawnDat.setZ(loc.getZ());
            spawnDat.setHeading(loc.getHeading());
            spawnDat.stopRespawn();
            return spawnDat.spawnOne(false);
         } catch (Exception var6) {
            _log.log(Level.WARNING, "Territory War Manager: " + var6.getMessage(), (Throwable)var6);
         }
      } else {
         _log.warning("Territory War Manager: Data missing in NPC table for ID: " + npcId + ".");
      }

      return null;
   }

   private void changeRegistration(int castleId, int objId, boolean delete) {
      String query = delete
         ? "DELETE FROM territory_registrations WHERE castleId = ? and registeredId = ?"
         : "INSERT INTO territory_registrations (castleId, registeredId) values (?, ?)";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(query);
      ) {
         statement.setInt(1, castleId);
         statement.setInt(2, objId);
         statement.execute();
      } catch (Exception var37) {
         _log.log(Level.WARNING, "Exception: Territory War registration: " + var37.getMessage(), (Throwable)var37);
      }
   }

   private void updateTerritoryData(TerritoryWarManager.Territory ter) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE territories SET ownedWardIds=? WHERE territoryId=?");
         StringBuilder wardList = new StringBuilder();

         for(int i : ter.getOwnedWardIds()) {
            wardList.append(i + ";");
         }

         statement.setString(1, wardList.toString());
         statement.setInt(2, ter.getTerritoryId());
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, "Exception: Territory Data update: " + var18.getMessage(), (Throwable)var18);
      }
   }

   private void updateTerritoryLord(int territoryId, int lordObjId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE territories SET lordObjectId=? WHERE territoryId=?");
         statement.setInt(1, lordObjId);
         statement.setInt(2, territoryId);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Exception: Territory Lord Object update: " + var16.getMessage(), (Throwable)var16);
      }
   }

   private final void load() {
      GameSettings territoryWarSettings = new GameSettings();

      try (InputStream is = new FileInputStream(new File("./config/main/territorywar.ini"))) {
         territoryWarSettings.load(is);
      } catch (IOException var108) {
         _log.log(Level.WARNING, "Error while loading Territory War Manager settings!", (Throwable)var108);
      }

      DEFENDERMAXCLANS = Integer.decode(territoryWarSettings.getProperty("DefenderMaxClans", "500"));
      DEFENDERMAXPLAYERS = Integer.decode(territoryWarSettings.getProperty("DefenderMaxPlayers", "500"));
      CLANMINLEVEL = Integer.decode(territoryWarSettings.getProperty("ClanMinLevel", "0"));
      PLAYERMINLEVEL = Integer.decode(territoryWarSettings.getProperty("PlayerMinLevel", "40"));
      WARLENGTH = Long.decode(territoryWarSettings.getProperty("WarLength", "120")) * 60000L;
      PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(territoryWarSettings.getProperty("PlayerWithWardCanBeKilledInPeaceZone", "False"));
      SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS = Boolean.parseBoolean(territoryWarSettings.getProperty("SpawnWardsWhenTWIsNotInProgress", "False"));
      RETURN_WARDS_WHEN_TW_STARTS = Boolean.parseBoolean(territoryWarSettings.getProperty("ReturnWardsWhenTWStarts", "False"));
      MINTWBADGEFORNOBLESS = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForNobless", "100"));
      MINTWBADGEFORSTRIDERS = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForStriders", "50"));
      MINTWBADGEFORBIGSTRIDER = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForBigStrider", "80"));
      _siegeDateTime = territoryWarSettings.getProperty("TerritorySiegePattern", "");

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM territory_spawnlist");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            int castleId = rs.getInt("castleId");
            int npcId = rs.getInt("npcId");
            Location loc = new Location(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("heading"));
            int spawnType = rs.getInt("spawnType");
            if (!this._territoryList.containsKey(castleId)) {
               this._territoryList.put(castleId, new TerritoryWarManager.Territory(castleId));
            }

            switch(spawnType) {
               case 0:
               case 1:
               case 2:
                  this._territoryList.get(castleId).getSpawnList().add(new TerritoryWarManager.TerritoryNPCSpawn(castleId, loc, npcId, spawnType, null));
                  break;
               case 3:
                  this._territoryList.get(castleId).addWardSpawnPlace(loc);
                  break;
               default:
                  _log.warning("Territory War Manager: Unknown npc type for " + rs.getInt("id"));
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var106) {
         _log.log(Level.WARNING, "Territory War Manager: SpawnList error: " + var106.getMessage(), (Throwable)var106);
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM territories");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            int castleId = rs.getInt("castleId");
            int fortId = rs.getInt("fortId");
            String ownedWardIds = rs.getString("OwnedWardIds");
            int lordObjectId = rs.getInt("lordObjectId");
            TerritoryWarManager.Territory t = this._territoryList.get(castleId);
            if (t != null) {
               t._fortId = fortId;
               t._lordObjId = lordObjectId;
               if (CastleManager.getInstance().getCastleById(castleId).getOwnerId() > 0) {
                  t.setOwnerClan(ClanHolder.getInstance().getClan(CastleManager.getInstance().getCastleById(castleId).getOwnerId()));
                  t.changeNPCsSpawn(0, true);
               }

               if (!ownedWardIds.isEmpty()) {
                  for(String wardId : ownedWardIds.split(";")) {
                     if (Integer.parseInt(wardId) > 0) {
                        this.addTerritoryWard(Integer.parseInt(wardId), castleId, 0, false);
                     }
                  }
               }
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var103) {
         _log.log(Level.WARNING, "Territory War Manager: territory list error(): " + var103.getMessage(), (Throwable)var103);
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM territory_registrations");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            int castleId = rs.getInt("castleId");
            int registeredId = rs.getInt("registeredId");
            if (ClanHolder.getInstance().getClan(registeredId) != null) {
               if (this._registeredClans.get(castleId) == null) {
                  this._registeredClans.putIfAbsent(castleId, new CopyOnWriteArrayList<>());
               }

               this._registeredClans.get(castleId).add(ClanHolder.getInstance().getClan(registeredId));
            } else {
               if (this._registeredMercenaries.get(castleId) == null) {
                  this._registeredMercenaries.put(castleId, new CopyOnWriteArrayList<>());
               }

               this._registeredMercenaries.get(castleId).add(registeredId);
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var100) {
         _log.log(Level.WARNING, "Territory War Manager: registration list error: " + var100.getMessage(), (Throwable)var100);
      }
   }

   private boolean haveActiveTerritoryList() {
      List<TerritoryWarManager.Territory> activeTerritoryList = new LinkedList<>();

      for(TerritoryWarManager.Territory t : this._territoryList.values()) {
         Castle castle = CastleManager.getInstance().getCastleById(t.getCastleId());
         if (castle != null && castle.getOwnerId() > 0) {
            activeTerritoryList.add(t);
         }
      }

      return activeTerritoryList.size() >= 2;
   }

   protected void startTerritoryWar() {
      if (this._territoryList == null) {
         _log.warning("TerritoryWarManager: TerritoryList is NULL!");
      } else {
         List<TerritoryWarManager.Territory> activeTerritoryList = new LinkedList<>();

         for(TerritoryWarManager.Territory t : this._territoryList.values()) {
            Castle castle = CastleManager.getInstance().getCastleById(t.getCastleId());
            if (castle != null) {
               if (castle.getOwnerId() > 0) {
                  activeTerritoryList.add(t);
               }
            } else {
               _log.warning("TerritoryWarManager: Castle missing! CastleId: " + t.getCastleId());
            }
         }

         if (activeTerritoryList.size() < 2) {
            this.generateNextWarTime();
         } else {
            this._isTWInProgress = true;
            if (this.updatePlayerTWStateFlags(false)) {
               for(TerritoryWarManager.Territory t : activeTerritoryList) {
                  Castle castle = CastleManager.getInstance().getCastleById(t.getCastleId());
                  Fort fort = FortManager.getInstance().getFortById(t.getFortId());
                  if (castle != null) {
                     t.changeNPCsSpawn(2, true);
                     castle.spawnDoor();
                     castle.getZone().setSiegeInstance(this);
                     castle.getZone().setIsActive(true);
                     castle.getZone().updateZoneStatusForCharactersInside();
                  } else {
                     _log.warning("TerritoryWarManager: Castle missing! CastleId: " + t.getCastleId());
                  }

                  if (fort != null) {
                     t.changeNPCsSpawn(1, true);
                     fort.resetDoors();
                     fort.getZone().setSiegeInstance(this);
                     fort.getZone().setIsActive(true);
                     fort.getZone().updateZoneStatusForCharactersInside();
                  } else {
                     _log.warning("TerritoryWarManager: Fort missing! FortId: " + t.getFortId());
                  }

                  for(TerritoryWarManager.TerritoryNPCSpawn ward : t.getOwnedWard()) {
                     if (ward.getNpc() != null && t.getOwnerClan() != null) {
                        if (!ward.getNpc().isVisible()) {
                           ward.setNPC(ward.getNpc().getSpawn().doSpawn());
                        }

                        this._territoryWards
                           .add(
                              new TerritoryWard(
                                 ward.getId(),
                                 ward.getLocation().getX(),
                                 ward.getLocation().getY(),
                                 ward.getLocation().getZ(),
                                 0,
                                 ward.getId() + 13479,
                                 t.getCastleId(),
                                 ward.getNpc()
                              )
                           );
                     }
                  }

                  t.getQuestDone()[0] = 0;
                  t.getQuestDone()[1] = 0;
               }

               this._participantPoints.clear();
               if (RETURN_WARDS_WHEN_TW_STARTS) {
                  for(TerritoryWard ward : this._territoryWards) {
                     if (ward.getOwnerCastleId() != ward.getTerritoryId() - 80) {
                        ward.unSpawnMe();
                        ward.setNpc(this.addTerritoryWard(ward.getTerritoryId(), ward.getTerritoryId() - 80, ward.getOwnerCastleId(), false));
                        ward.setOwnerCastleId(ward.getTerritoryId() - 80);
                     }
                  }
               }

               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_HAS_BEGUN);
               Announcements.getInstance().announceToAll(sm);
            }
         }
      }
   }

   protected void endTerritoryWar() {
      this._isTWInProgress = false;
      if (this._territoryList == null) {
         _log.warning("TerritoryWarManager: TerritoryList is NULL!");
      } else {
         List<TerritoryWarManager.Territory> activeTerritoryList = new LinkedList<>();

         for(TerritoryWarManager.Territory t : this._territoryList.values()) {
            Castle castle = CastleManager.getInstance().getCastleById(t.getCastleId());
            if (castle != null) {
               if (castle.getOwnerId() > 0) {
                  activeTerritoryList.add(t);
               }
            } else {
               _log.warning("TerritoryWarManager: Castle missing! CastleId: " + t.getCastleId());
            }
         }

         if (activeTerritoryList.size() < 2) {
            this.generateNextWarTime();
         } else if (this.updatePlayerTWStateFlags(true)) {
            if (this._territoryWards != null) {
               for(TerritoryWard twWard : this._territoryWards) {
                  twWard.unSpawnMe();
               }

               this._territoryWards.clear();
            }

            for(TerritoryWarManager.Territory t : activeTerritoryList) {
               Castle castle = CastleManager.getInstance().getCastleById(t.getCastleId());
               Fort fort = FortManager.getInstance().getFortById(t.getFortId());
               if (castle != null) {
                  castle.spawnDoor();
                  t.changeNPCsSpawn(2, false);
                  castle.getZone().setIsActive(false);
                  castle.getZone().updateZoneStatusForCharactersInside();
                  castle.getZone().setSiegeInstance(null);
               } else {
                  _log.warning("TerritoryWarManager: Castle missing! CastleId: " + t.getCastleId());
               }

               if (fort != null) {
                  t.changeNPCsSpawn(1, false);
                  fort.getZone().setIsActive(false);
                  fort.getZone().updateZoneStatusForCharactersInside();
                  fort.getZone().setSiegeInstance(null);
               } else {
                  _log.warning("TerritoryWarManager: Fort missing! FortId: " + t.getFortId());
               }

               for(TerritoryWarManager.TerritoryNPCSpawn ward : t.getOwnedWard()) {
                  if (ward.getNpc() != null) {
                     if (!ward.getNpc().isVisible() && SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS) {
                        ward.setNPC(ward.getNpc().getSpawn().doSpawn());
                     } else if (ward.getNpc().isVisible() && !SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS) {
                        ward.getNpc().decayMe();
                     }
                  }
               }
            }

            for(SiegeFlagInstance flag : this._outposts.values()) {
               flag.deleteMe();
            }

            for(SiegeFlagInstance flag : this._clanFlags.values()) {
               flag.deleteMe();
            }

            this._clanFlags.clear();

            for(Integer castleId : this._registeredClans.keySet()) {
               for(Clan clan : this._registeredClans.get(castleId)) {
                  this.changeRegistration(castleId, clan.getId(), true);
               }
            }

            for(Integer castleId : this._registeredMercenaries.keySet()) {
               for(Integer pl_objId : this._registeredMercenaries.get(castleId)) {
                  this.changeRegistration(castleId, pl_objId, true);
               }
            }

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_HAS_ENDED);
            Announcements.getInstance().announceToAll(sm);
            this.generateNextWarTime();
         }
      }
   }

   protected boolean updatePlayerTWStateFlags(boolean clear) {
      Quest twQuest = QuestManager.getInstance().getQuest(qn);
      if (twQuest == null) {
         _log.warning("TerritoryWarManager: missing main Quest!");
         return false;
      } else {
         for(int castleId : this._registeredClans.keySet()) {
            for(Clan clan : this._registeredClans.get(castleId)) {
               for(Player player : clan.getOnlineMembers(0)) {
                  if (player != null) {
                     if (clear) {
                        player.setSiegeState((byte)0);
                        if (!this._isTWChannelOpen) {
                           player.setSiegeSide(0);
                        }
                     } else {
                        if (player.getLevel() < PLAYERMINLEVEL || player.getClassId().level() < 2) {
                           continue;
                        }

                        if (this._isTWInProgress) {
                           player.setSiegeState((byte)1);
                        }

                        player.setSiegeSide(80 + castleId);
                     }

                     player.broadcastUserInfo(true);
                  }
               }
            }
         }

         for(int castleId : this._registeredMercenaries.keySet()) {
            for(int objId : this._registeredMercenaries.get(castleId)) {
               Player player = World.getInstance().getPlayer(objId);
               if (player != null) {
                  if (clear) {
                     player.setSiegeState((byte)0);
                     if (!this._isTWChannelOpen) {
                        player.setSiegeSide(0);
                     }
                  } else {
                     if (this._isTWInProgress) {
                        player.setSiegeState((byte)1);
                     }

                     player.setSiegeSide(80 + castleId);
                  }

                  player.broadcastUserInfo(true);
               }
            }
         }

         for(TerritoryWarManager.Territory terr : this._territoryList.values()) {
            if (terr.getOwnerClan() != null) {
               for(Player player : terr.getOwnerClan().getOnlineMembers(0)) {
                  if (player != null) {
                     if (clear) {
                        player.setSiegeState((byte)0);
                        if (!this._isTWChannelOpen) {
                           player.setSiegeSide(0);
                        }
                     } else {
                        if (player.getLevel() < PLAYERMINLEVEL || player.getClassId().level() < 2) {
                           continue;
                        }

                        if (this._isTWInProgress) {
                           player.setSiegeState((byte)1);
                        }

                        player.setSiegeSide(80 + terr.getCastleId());
                     }

                     player.broadcastUserInfo(true);
                  }
               }
            }
         }

         twQuest.setOnEnterWorld(this._isTWInProgress);
         return true;
      }
   }

   public void announceToParticipants(GameServerPacket sm, int exp, int sp) {
      for(TerritoryWarManager.Territory ter : this._territoryList.values()) {
         if (ter.getOwnerClan() != null) {
            for(Player member : ter.getOwnerClan().getOnlineMembers(0)) {
               member.sendPacket(sm);
               if (exp > 0 || sp > 0) {
                  member.addExpAndSp((long)exp, sp);
               }
            }
         }
      }

      for(List<Clan> list : this._registeredClans.values()) {
         for(Clan c : list) {
            for(Player member : c.getOnlineMembers(0)) {
               member.sendPacket(sm);
               if (exp > 0 || sp > 0) {
                  member.addExpAndSp((long)exp, sp);
               }
            }
         }
      }

      for(List<Integer> list : this._registeredMercenaries.values()) {
         for(int objId : list) {
            Player player = World.getInstance().getPlayer(objId);
            if (player != null && (player.getClan() == null || !this.checkIsRegistered(-1, player.getClan()))) {
               player.sendPacket(sm);
               if (exp > 0 || sp > 0) {
                  player.addExpAndSp((long)exp, sp);
               }
            }
         }
      }
   }

   @Override
   public void startSiege() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void endSiege() {
      throw new UnsupportedOperationException();
   }

   @Override
   public SiegeClan getAttackerClan(int clanId) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SiegeClan getAttackerClan(Clan clan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<SiegeClan> getAttackerClans() {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<Player> getAttackersInZone() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean checkIsAttacker(Clan clan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SiegeClan getDefenderClan(int clanId) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SiegeClan getDefenderClan(Clan clan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<SiegeClan> getDefenderClans() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean checkIsDefender(Clan clan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<Npc> getFlag(Clan clan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Calendar getSiegeDate() {
      throw new UnsupportedOperationException();
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

   public String getSiegeDateTime() {
      return _siegeDateTime;
   }

   public void generateNextWarTime() {
      Calendar cal = Calendar.getInstance();
      long warTime = ServerVariables.getLong("TerritoryWarDate", 0L);
      if (warTime > System.currentTimeMillis()) {
         cal.setTimeInMillis(warTime);
      } else {
         if (cal.before(Calendar.getInstance())) {
            cal.setTimeInMillis(System.currentTimeMillis());
         }

         String siegeDate = this.getSiegeDateTime();
         if (siegeDate != null && !siegeDate.isEmpty()) {
            SchedulingPattern cronTime;
            try {
               cronTime = new SchedulingPattern(siegeDate);
            } catch (SchedulingPattern.InvalidPatternException var8) {
               return;
            }

            long nextTime = cronTime.next(System.currentTimeMillis());
            cal.setTimeInMillis(nextTime);
         } else {
            cal.set(7, 7);
            cal.set(11, 20);
            cal.set(12, 0);
         }

         cal.set(13, 0);
         if (cal.before(Calendar.getInstance())) {
            cal.add(5, 7);
         }

         if (SiegeManager.getInstance().isCheckSevenSignStatus() && !SevenSigns.getInstance().isDateInSealValidPeriod(cal)) {
            cal.add(5, 7);
         }

         ServerVariables.set("TerritoryWarDate", cal.getTimeInMillis());
      }

      this.setTWStartTimeInMillis(cal.getTimeInMillis());
      _log.info(this.getClass().getSimpleName() + ": Next battle " + cal.getTime());
   }

   static {
      TERRITORY_ITEM_IDS.put(81, 13757);
      TERRITORY_ITEM_IDS.put(82, 13758);
      TERRITORY_ITEM_IDS.put(83, 13759);
      TERRITORY_ITEM_IDS.put(84, 13760);
      TERRITORY_ITEM_IDS.put(85, 13761);
      TERRITORY_ITEM_IDS.put(86, 13762);
      TERRITORY_ITEM_IDS.put(87, 13763);
      TERRITORY_ITEM_IDS.put(88, 13764);
      TERRITORY_ITEM_IDS.put(89, 13765);
   }

   private class RewardOnlineParticipants implements Runnable {
      public RewardOnlineParticipants() {
      }

      @Override
      public void run() {
         if (TerritoryWarManager.this.isTWInProgress()) {
            for(Player player : World.getInstance().getAllPlayers()) {
               if (player != null && player.getSiegeSide() > 0) {
                  TerritoryWarManager.this.giveTWPoint(player, 1000, 6);
               }
            }
         } else {
            TerritoryWarManager.this._scheduledRewardOnlineTask.cancel(false);
         }
      }
   }

   private class ScheduleEndTWTask implements Runnable {
      public ScheduleEndTWTask() {
      }

      @Override
      public void run() {
         try {
            if (TerritoryWarManager.this.isTWInProgress()) {
               TerritoryWarManager.this._scheduledEndTWTask.cancel(false);
               long timeRemaining = TerritoryWarManager.this._startTWDate.getTimeInMillis()
                  + TerritoryWarManager.WARLENGTH
                  - Calendar.getInstance().getTimeInMillis();
               if (timeRemaining > 3600000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_TERRITORY_WAR_WILL_END_IN_S1_HOURS);
                  sm.addNumber(2);
                  TerritoryWarManager.this.announceToParticipants(sm, 0, 0);
                  TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                     .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), timeRemaining - 3600000L);
               } else if (timeRemaining <= 3600000L && timeRemaining > 600000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_TERRITORY_WAR_WILL_END_IN_S1_MINUTES);
                  sm.addNumber((int)(timeRemaining / 60000L));
                  TerritoryWarManager.this.announceToParticipants(sm, 0, 0);
                  TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                     .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), timeRemaining - 600000L);
               } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_TERRITORY_WAR_WILL_END_IN_S1_MINUTES);
                  sm.addNumber((int)(timeRemaining / 60000L));
                  TerritoryWarManager.this.announceToParticipants(sm, 0, 0);
                  TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                     .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), timeRemaining - 300000L);
               } else if (timeRemaining <= 300000L && timeRemaining > 10000L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_TERRITORY_WAR_WILL_END_IN_S1_MINUTES);
                  sm.addNumber((int)(timeRemaining / 60000L));
                  TerritoryWarManager.this.announceToParticipants(sm, 0, 0);
                  TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                     .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), timeRemaining - 10000L);
               } else if (timeRemaining <= 10000L && timeRemaining > 0L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR);
                  sm.addNumber((int)(timeRemaining / 1000L));
                  TerritoryWarManager.this.announceToParticipants(sm, 0, 0);
                  TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                     .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), timeRemaining);
               } else {
                  TerritoryWarManager.this.endTerritoryWar();
                  ThreadPoolManager.getInstance().schedule(TerritoryWarManager.this.new closeTerritoryChannelTask(), 600000L);
               }
            } else {
               TerritoryWarManager.this._scheduledEndTWTask.cancel(false);
            }
         } catch (Exception var4) {
            TerritoryWarManager._log.log(Level.SEVERE, "", (Throwable)var4);
         }
      }
   }

   private class ScheduleStartTWTask implements Runnable {
      public ScheduleStartTWTask() {
      }

      @Override
      public void run() {
         TerritoryWarManager.this._scheduledStartTWTask.cancel(false);

         try {
            long timeRemaining = TerritoryWarManager.this._startTWDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
            if (timeRemaining > 7200000L) {
               TerritoryWarManager.this._isRegistrationOver = false;
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining - 7200000L);
            } else if (timeRemaining <= 7200000L && timeRemaining > 1200000L) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_TERRITORY_WAR_REGISTERING_PERIOD_ENDED);
               Announcements.getInstance().announceToAll(sm);
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining - 1200000L);
            } else if (timeRemaining <= 1200000L && timeRemaining > 600000L) {
               if (TerritoryWarManager.this.haveActiveTerritoryList()) {
                  Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_BEGINS_IN_20_MINUTES));
               }

               TerritoryWarManager.this._isTWChannelOpen = true;
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this.updatePlayerTWStateFlags(false);
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining - 600000L);
            } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
               if (TerritoryWarManager.this.haveActiveTerritoryList()) {
                  Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_BEGINS_IN_10_MINUTES));
               }

               TerritoryWarManager.this._isTWChannelOpen = true;
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this.updatePlayerTWStateFlags(false);
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining - 300000L);
            } else if (timeRemaining <= 300000L && timeRemaining > 60000L) {
               if (TerritoryWarManager.this.haveActiveTerritoryList()) {
                  Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_BEGINS_IN_5_MINUTES));
               }

               TerritoryWarManager.this._isTWChannelOpen = true;
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this.updatePlayerTWStateFlags(false);
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining - 60000L);
            } else if (timeRemaining <= 60000L && timeRemaining > 0L) {
               if (TerritoryWarManager.this.haveActiveTerritoryList()) {
                  Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.TERRITORY_WAR_BEGINS_IN_1_MINUTE));
               }

               TerritoryWarManager.this._isTWChannelOpen = true;
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this.updatePlayerTWStateFlags(false);
               TerritoryWarManager.this._scheduledStartTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleStartTWTask(), timeRemaining);
            } else if (timeRemaining + TerritoryWarManager.WARLENGTH > 0L) {
               TerritoryWarManager.this._isTWChannelOpen = true;
               TerritoryWarManager.this._isRegistrationOver = true;
               TerritoryWarManager.this.startTerritoryWar();
               TerritoryWarManager.this._scheduledEndTWTask = ThreadPoolManager.getInstance()
                  .schedule(TerritoryWarManager.this.new ScheduleEndTWTask(), 1000L);
               TerritoryWarManager.this._scheduledRewardOnlineTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(TerritoryWarManager.this.new RewardOnlineParticipants(), 60000L, 60000L);
            }
         } catch (Exception var4) {
            TerritoryWarManager._log.log(Level.SEVERE, "", (Throwable)var4);
         }
      }
   }

   private static class SingletonHolder {
      protected static final TerritoryWarManager _instance = new TerritoryWarManager();
   }

   public class Territory {
      private final int _territoryId;
      private final int _castleId;
      protected int _fortId;
      private Clan _ownerClan;
      private int _lordObjId;
      private final List<TerritoryWarManager.TerritoryNPCSpawn> _spawnList = new LinkedList<>();
      private final TerritoryWarManager.TerritoryNPCSpawn[] _territoryWardSpawnPlaces;
      private boolean _isInProgress = false;
      private final int[] _questDone;

      public Territory(int castleId) {
         this._castleId = castleId;
         this._territoryId = castleId + 80;
         this._territoryWardSpawnPlaces = new TerritoryWarManager.TerritoryNPCSpawn[9];
         this._questDone = new int[2];
      }

      protected void addWardSpawnPlace(Location loc) {
         for(int i = 0; i < this._territoryWardSpawnPlaces.length; ++i) {
            if (this._territoryWardSpawnPlaces[i] == null) {
               this._territoryWardSpawnPlaces[i] = new TerritoryWarManager.TerritoryNPCSpawn(this._castleId, loc, 0, 4, null);
               return;
            }
         }
      }

      protected TerritoryWarManager.TerritoryNPCSpawn getFreeWardSpawnPlace() {
         for(TerritoryWarManager.TerritoryNPCSpawn _territoryWardSpawnPlace : this._territoryWardSpawnPlaces) {
            if (_territoryWardSpawnPlace != null && _territoryWardSpawnPlace.getNpc() == null) {
               return _territoryWardSpawnPlace;
            }
         }

         TerritoryWarManager._log.log(Level.WARNING, "TerritoryWarManager: no free Ward spawn found for territory: " + this._territoryId);

         for(int i = 0; i < this._territoryWardSpawnPlaces.length; ++i) {
            if (this._territoryWardSpawnPlaces[i] == null) {
               TerritoryWarManager._log.log(Level.WARNING, "TerritoryWarManager: territory ward spawn place " + i + " is null!");
            } else if (this._territoryWardSpawnPlaces[i].getNpc() != null) {
               TerritoryWarManager._log
                  .log(
                     Level.WARNING,
                     "TerritoryWarManager: territory ward spawn place " + i + " has npc name: " + this._territoryWardSpawnPlaces[i].getNpc().getName()
                  );
            } else {
               TerritoryWarManager._log.log(Level.WARNING, "TerritoryWarManager: territory ward spawn place " + i + " is empty!");
            }
         }

         return null;
      }

      public void changeOwner(Clan clan) {
         int newLordObjectId;
         if (clan == null) {
            if (this._lordObjId <= 0) {
               return;
            }

            newLordObjectId = 0;
         } else {
            newLordObjectId = clan.getLeaderId();
            SystemMessage message = SystemMessage.getSystemMessage(
               SystemMessageId.CLAN_LORD_C2_WHO_LEADS_CLAN_S1_HAS_BEEN_DECLARED_THE_LORD_OF_THE_S3_TERRITORY
            );
            message.addPcName(clan.getLeader().getPlayerInstance());
            message.addString(clan.getName());
            message.addCastleId(this.getCastleId());

            for(Player player : World.getInstance().getAllPlayers()) {
               player.sendPacket(message);
            }
         }

         this._lordObjId = newLordObjectId;
         TerritoryWarManager.this.updateTerritoryLord(this._territoryId, this._lordObjId);

         for(Npc npc : World.getInstance().getNpcs()) {
            if (npc != null && npc.getTerritory() == this) {
               npc.broadcastInfo();
            }
         }
      }

      public List<TerritoryWarManager.TerritoryNPCSpawn> getSpawnList() {
         return this._spawnList;
      }

      protected void changeNPCsSpawn(int type, boolean isSpawn) {
         if (type >= 0 && type <= 3) {
            for(TerritoryWarManager.TerritoryNPCSpawn twSpawn : this._spawnList) {
               if (twSpawn.getType() == type) {
                  if (isSpawn) {
                     twSpawn.setNPC(TerritoryWarManager.this.spawnNPC(twSpawn.getId(), twSpawn.getLocation()));
                  } else {
                     Npc npc = twSpawn.getNpc();
                     if (npc != null && !npc.isDead()) {
                        npc.deleteMe();
                     }

                     twSpawn.setNPC(null);
                  }
               }
            }
         } else {
            TerritoryWarManager._log.log(Level.WARNING, "TerritoryWarManager: wrong type(" + type + ") for NPCs spawn change!");
         }
      }

      protected void removeWard(int wardId) {
         for(TerritoryWarManager.TerritoryNPCSpawn wardSpawn : this._territoryWardSpawnPlaces) {
            if (wardSpawn.getId() == wardId) {
               wardSpawn.getNpc().deleteMe();
               wardSpawn.setNPC(null);
               wardSpawn._npcId = 0;
               return;
            }
         }

         TerritoryWarManager._log.log(Level.WARNING, "TerritoryWarManager: cant delete wardId: " + wardId + " for territory: " + this._territoryId);
      }

      public int getTerritoryId() {
         return this._territoryId;
      }

      public int getCastleId() {
         return this._castleId;
      }

      public int getFortId() {
         return this._fortId;
      }

      public Clan getOwnerClan() {
         return this._ownerClan;
      }

      public int getLordObjectId() {
         return this._lordObjId;
      }

      public void setOwnerClan(Clan newOwner) {
         this._ownerClan = newOwner;
      }

      public TerritoryWarManager.TerritoryNPCSpawn[] getOwnedWard() {
         return this._territoryWardSpawnPlaces;
      }

      public int[] getQuestDone() {
         return this._questDone;
      }

      public List<Integer> getOwnedWardIds() {
         List<Integer> ret = new LinkedList<>();

         for(TerritoryWarManager.TerritoryNPCSpawn wardSpawn : this._territoryWardSpawnPlaces) {
            if (wardSpawn.getId() > 0) {
               ret.add(wardSpawn.getId());
            }
         }

         return ret;
      }

      public boolean getIsInProgress() {
         return this._isInProgress;
      }

      public void setIsInProgress(boolean val) {
         this._isInProgress = val;
      }
   }

   public static class TerritoryNPCSpawn implements IIdentifiable {
      private final Location _location;
      protected int _npcId;
      private final int _castleId;
      private final int _type;
      private Npc _npc;

      public TerritoryNPCSpawn(int castle_id, Location loc, int npc_id, int type, Npc npc) {
         this._castleId = castle_id;
         this._location = loc;
         this._npcId = npc_id;
         this._type = type;
         this._npc = npc;
      }

      public int getCastleId() {
         return this._castleId;
      }

      @Override
      public int getId() {
         return this._npcId;
      }

      public int getType() {
         return this._type;
      }

      public void setNPC(Npc npc) {
         if (this._npc != null) {
            this._npc.deleteMe();
         }

         this._npc = npc;
      }

      public Npc getNpc() {
         return this._npc;
      }

      public Location getLocation() {
         return this._location;
      }
   }

   private class closeTerritoryChannelTask implements Runnable {
      public closeTerritoryChannelTask() {
      }

      @Override
      public void run() {
         TerritoryWarManager.this._isTWChannelOpen = false;
         TerritoryWarManager.this._disguisedPlayers.clear();
         TerritoryWarManager.this.updatePlayerTWStateFlags(true);
      }
   }
}
