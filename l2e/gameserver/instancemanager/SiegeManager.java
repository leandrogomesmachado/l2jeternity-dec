package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.skills.Skill;

public class SiegeManager {
   private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());
   private int _attackerMaxClans = 500;
   private int _attackerRespawnDelay = 0;
   private int _defenderMaxClans = 500;
   private int _castleHwidLimit = 0;
   private final Map<Integer, List<SiegeManager.SiegeSpawn>> _artefactSpawnList = new HashMap<>();
   private final Map<Integer, List<SiegeManager.SiegeSpawn>> _controlTowerSpawnList = new HashMap<>();
   private final Map<Integer, List<SiegeManager.SiegeSpawn>> _flameTowerSpawnList = new HashMap<>();
   private int _flagMaxCount = 1;
   private int _siegeClanMinLevel = 5;
   private int _siegeLength = 120;
   private int _bloodAllianceReward = 0;
   private String _gludioSiegeDate;
   private String _dionSiegeDate;
   private String _giranSiegeDate;
   private String _orenSiegeDate;
   private String _adenSiegeDate;
   private String _innadrilSiegeDate;
   private String _goddardSiegeDate;
   private String _runeSiegeDate;
   private String _schuttgartSiegeDate;
   private static boolean _allowAttackSameSiegeSide;
   private static boolean _allowCheckSevenSignStatus;

   public static final SiegeManager getInstance() {
      return SiegeManager.SingletonHolder._instance;
   }

   protected SiegeManager() {
      this.load();
   }

   public final void addSiegeSkills(Player character) {
      for(Skill sk : SkillsParser.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0)) {
         character.addSkill(sk, false);
      }
   }

   public final boolean checkIsRegistered(Clan clan, int castleid) {
      if (clan == null) {
         return false;
      } else if (clan.getCastleId() > 0) {
         return true;
      } else {
         boolean register = false;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
            statement.setInt(1, clan.getId());
            statement.setInt(2, castleid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
               register = true;
            }

            rs.close();
            statement.close();
         } catch (Exception var18) {
            _log.log(Level.WARNING, "Exception: checkIsRegistered(): " + var18.getMessage(), (Throwable)var18);
         }

         return register;
      }
   }

   public final void removeSiegeSkills(Player character) {
      for(Skill sk : SkillsParser.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0)) {
         character.removeSkill(sk);
      }
   }

   private final void load() {
      Properties siegeSettings = new Properties();
      File file = new File("./config/main/siege.ini");

      try (InputStream is = new FileInputStream(file)) {
         siegeSettings.load(is);
      } catch (Exception var30) {
         _log.log(Level.WARNING, "Error while loading Territory War Manager settings!", (Throwable)var30);
      }

      this._attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
      this._attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "0"));
      this._defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
      this._flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
      this._siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "5"));
      this._siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
      this._bloodAllianceReward = Integer.decode(siegeSettings.getProperty("BloodAllianceReward", "0"));
      this._castleHwidLimit = Integer.decode(siegeSettings.getProperty("CastleSiegeLimitPlayers", "0"));
      this._gludioSiegeDate = siegeSettings.getProperty("GludioSiegePattern", "");
      this._dionSiegeDate = siegeSettings.getProperty("DionSiegePattern", "");
      this._giranSiegeDate = siegeSettings.getProperty("GiranSiegePattern", "");
      this._orenSiegeDate = siegeSettings.getProperty("OrenSiegePattern", "");
      this._adenSiegeDate = siegeSettings.getProperty("AdenSiegePattern", "");
      this._innadrilSiegeDate = siegeSettings.getProperty("InnadrilSiegePattern", "");
      this._goddardSiegeDate = siegeSettings.getProperty("GoddardSiegePattern", "");
      this._runeSiegeDate = siegeSettings.getProperty("RuneSiegePattern", "");
      this._schuttgartSiegeDate = siegeSettings.getProperty("SchuttgartSiegePattern", "");
      _allowAttackSameSiegeSide = Boolean.parseBoolean(siegeSettings.getProperty("AllowAttackSameSiegeSide", "False"));
      _allowCheckSevenSignStatus = Boolean.parseBoolean(siegeSettings.getProperty("AllowCheckSevenSignStatus", "True"));

      for(Castle castle : CastleManager.getInstance().getCastles()) {
         List<SiegeManager.SiegeSpawn> _controlTowersSpawns = new ArrayList<>();

         for(int i = 1; i < 255; ++i) {
            String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int npc_id = Integer.parseInt(st.nextToken());
               int hp = Integer.parseInt(st.nextToken());
               _controlTowersSpawns.add(new SiegeManager.SiegeSpawn(castle.getId(), x, y, z, 0, npc_id, hp));
            } catch (Exception var24) {
               _log.warning("Error while loading control tower(s) for " + castle.getName() + " castle.");
            }
         }

         List<SiegeManager.SiegeSpawn> _flameTowersSpawns = new ArrayList<>();

         for(int i = 1; i < 255; ++i) {
            String _spawnParams = siegeSettings.getProperty(castle.getName() + "FlameTower" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int npc_id = Integer.parseInt(st.nextToken());
               int hp = Integer.parseInt(st.nextToken());
               _flameTowersSpawns.add(new SiegeManager.SiegeSpawn(castle.getId(), x, y, z, 0, npc_id, hp));
            } catch (Exception var28) {
               _log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
            }
         }

         List<SiegeManager.SiegeSpawn> _artefactSpawns = new ArrayList<>();

         for(int i = 1; i < 255; ++i) {
            String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int heading = Integer.parseInt(st.nextToken());
               int npc_id = Integer.parseInt(st.nextToken());
               _artefactSpawns.add(new SiegeManager.SiegeSpawn(castle.getId(), x, y, z, heading, npc_id));
            } catch (Exception var27) {
               _log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
            }
         }

         MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getId() - 1] = Integer.parseInt(
            siegeSettings.getProperty(castle.getName() + "MaxMercenaries", Integer.toString(MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getId() - 1]))
               .trim()
         );
         this._controlTowerSpawnList.put(castle.getId(), _controlTowersSpawns);
         this._artefactSpawnList.put(castle.getId(), _artefactSpawns);
         this._flameTowerSpawnList.put(castle.getId(), _flameTowersSpawns);
      }
   }

   public final List<SiegeManager.SiegeSpawn> getArtefactSpawnList(int _castleId) {
      return this._artefactSpawnList.get(_castleId);
   }

   public final List<SiegeManager.SiegeSpawn> getControlTowerSpawnList(int _castleId) {
      return this._controlTowerSpawnList.get(_castleId);
   }

   public final List<SiegeManager.SiegeSpawn> getFlameTowerSpawnList(int _castleId) {
      return this._flameTowerSpawnList.get(_castleId);
   }

   public final int getAttackerMaxClans() {
      return this._attackerMaxClans;
   }

   public final int getAttackerRespawnDelay() {
      return this._attackerRespawnDelay;
   }

   public final int getDefenderMaxClans() {
      return this._defenderMaxClans;
   }

   public final int getFlagMaxCount() {
      return this._flagMaxCount;
   }

   public final Siege getSiege(GameObject activeObject) {
      return this.getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final Siege getSiege(int x, int y, int z) {
      for(Castle castle : CastleManager.getInstance().getCastles()) {
         if (castle.getSiege().checkIfInZone(x, y, z)) {
            return castle.getSiege();
         }
      }

      return null;
   }

   public final int getSiegeClanMinLevel() {
      return this._siegeClanMinLevel;
   }

   public final int getSiegeLength() {
      return this._siegeLength;
   }

   public final int getBloodAllianceReward() {
      return this._bloodAllianceReward;
   }

   public final List<Siege> getSieges() {
      List<Siege> sieges = new ArrayList<>();

      for(Castle castle : CastleManager.getInstance().getCastles()) {
         sieges.add(castle.getSiege());
      }

      return sieges;
   }

   public String getCastleSiegeDate(int castleId) {
      switch(castleId) {
         case 1:
            return this._gludioSiegeDate;
         case 2:
            return this._dionSiegeDate;
         case 3:
            return this._giranSiegeDate;
         case 4:
            return this._orenSiegeDate;
         case 5:
            return this._adenSiegeDate;
         case 6:
            return this._innadrilSiegeDate;
         case 7:
            return this._goddardSiegeDate;
         case 8:
            return this._runeSiegeDate;
         case 9:
            return this._schuttgartSiegeDate;
         default:
            return null;
      }
   }

   public boolean canAttackSameSiegeSide() {
      return _allowAttackSameSiegeSide;
   }

   public boolean isCheckSevenSignStatus() {
      return _allowCheckSevenSignStatus;
   }

   public int getCastleHwidLimit() {
      return this._castleHwidLimit;
   }

   public static class SiegeSpawn {
      Location _location;
      private final int _npcId;
      private final int _heading;
      private final int _castleId;
      private int _hp;

      public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id) {
         this._castleId = castle_id;
         this._location = new Location(x, y, z, heading);
         this._heading = heading;
         this._npcId = npc_id;
      }

      public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp) {
         this._castleId = castle_id;
         this._location = new Location(x, y, z, heading);
         this._heading = heading;
         this._npcId = npc_id;
         this._hp = hp;
      }

      public int getCastleId() {
         return this._castleId;
      }

      public int getNpcId() {
         return this._npcId;
      }

      public int getHeading() {
         return this._heading;
      }

      public int getHp() {
         return this._hp;
      }

      public Location getLocation() {
         return this._location;
      }
   }

   private static class SingletonHolder {
      protected static final SiegeManager _instance = new SiegeManager();
   }
}
