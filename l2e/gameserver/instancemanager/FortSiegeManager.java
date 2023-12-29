package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.CombatFlag;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.spawn.SpawnFortSiege;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class FortSiegeManager {
   private static final Logger _log = Logger.getLogger(FortSiegeManager.class.getName());
   private int _attackerMaxClans = 500;
   private Map<Integer, List<SpawnFortSiege>> _commanderSpawnList;
   private Map<Integer, List<CombatFlag>> _flagList;
   private Map<Integer, List<SpawnFortSiege>> _powerUnitList;
   private Map<Integer, List<SpawnFortSiege>> _controlUnitList;
   private Map<Integer, List<SpawnFortSiege>> _mainMachineList;
   private int _flagMaxCount = 1;
   private int _siegeClanMinLevel = 4;
   private int _siegeLength = 60;
   private int _countDownLength = 10;
   private int _suspiciousMerchantRespawnDelay = 180;
   private int _fortHwidLimit = 0;
   private final List<FortSiege> _sieges = new ArrayList<>();

   protected FortSiegeManager() {
      this.load();
   }

   public final void addSiegeSkills(Player character) {
      character.addSkill(SkillsParser.FrequentSkill.SEAL_OF_RULER.getSkill(), false);
      character.addSkill(SkillsParser.FrequentSkill.BUILD_HEADQUARTERS.getSkill(), false);
   }

   public final boolean checkIsRegistered(Clan clan, int fortid) {
      if (clan == null) {
         return false;
      } else {
         boolean register = false;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");
            statement.setInt(1, clan.getId());
            statement.setInt(2, fortid);
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
      character.removeSkill(SkillsParser.FrequentSkill.SEAL_OF_RULER.getSkill());
      character.removeSkill(SkillsParser.FrequentSkill.BUILD_HEADQUARTERS.getSkill());
   }

   private final void load() {
      Properties siegeSettings = new Properties();
      File file = new File("./config/main/fortsiege.ini");

      try (InputStream is = new FileInputStream(file)) {
         siegeSettings.load(is);
      } catch (Exception var36) {
         _log.log(Level.WARNING, "Error while loading Fort Siege Manager settings!", (Throwable)var36);
      }

      this._attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
      this._flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
      this._siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
      this._siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "60"));
      this._countDownLength = Integer.decode(siegeSettings.getProperty("CountDownLength", "10"));
      this._suspiciousMerchantRespawnDelay = Integer.decode(siegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));
      this._fortHwidLimit = Integer.decode(siegeSettings.getProperty("FortSiegeLimitPlayers", "0"));
      this._commanderSpawnList = new ConcurrentHashMap<>();
      this._flagList = new ConcurrentHashMap<>();
      this._powerUnitList = new ConcurrentHashMap<>();
      this._controlUnitList = new ConcurrentHashMap<>();
      this._mainMachineList = new ConcurrentHashMap<>();

      for(Fort fort : FortManager.getInstance().getForts()) {
         List<SpawnFortSiege> _commanderSpawns = new ArrayList<>();
         List<CombatFlag> _flagSpawns = new ArrayList<>();
         List<SpawnFortSiege> _powerUnitSpawns = new ArrayList<>();
         List<SpawnFortSiege> _controlUnitSpawns = new ArrayList<>();
         List<SpawnFortSiege> _mainMachineSpawns = new ArrayList<>();

         for(int i = 1; i < 5; ++i) {
            String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
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
               _commanderSpawns.add(new SpawnFortSiege(fort.getId(), x, y, z, heading, npc_id, i));
            } catch (Exception var28) {
               _log.warning("Error while loading commander(s) for " + fort.getName() + " fort.");
            }
         }

         this._commanderSpawnList.put(fort.getId(), _commanderSpawns);

         for(int i = 1; i < 4; ++i) {
            String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Flag" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int flag_id = Integer.parseInt(st.nextToken());
               _flagSpawns.add(new CombatFlag(fort.getId(), x, y, z, 0, flag_id));
            } catch (Exception var34) {
               _log.warning("Error while loading flag(s) for " + fort.getName() + " fort.");
            }
         }

         this._flagList.put(fort.getId(), _flagSpawns);

         for(int i = 1; i < 5; ++i) {
            String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "PowerUnit" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int heading = Integer.parseInt(st.nextToken());
               int powerUnitId = Integer.parseInt(st.nextToken());
               _powerUnitSpawns.add(new SpawnFortSiege(fort.getId(), x, y, z, heading, powerUnitId, i));
            } catch (Exception var33) {
               _log.warning("Error while loading power unit(s) for " + fort.getName() + " fort.");
            }
         }

         this._powerUnitList.put(fort.getId(), _powerUnitSpawns);

         for(int i = 1; i < 5; ++i) {
            String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "ControlUnit" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int heading = Integer.parseInt(st.nextToken());
               int controlUnitId = Integer.parseInt(st.nextToken());
               _controlUnitSpawns.add(new SpawnFortSiege(fort.getId(), x, y, z, heading, controlUnitId, i));
            } catch (Exception var32) {
               _log.warning("Error while loading control unit(s) for " + fort.getName() + " fort.");
            }
         }

         this._controlUnitList.put(fort.getId(), _controlUnitSpawns);

         for(int i = 1; i < 2; ++i) {
            String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "MainMachine" + i, "");
            if (_spawnParams.isEmpty()) {
               break;
            }

            StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

            try {
               int x = Integer.parseInt(st.nextToken());
               int y = Integer.parseInt(st.nextToken());
               int z = Integer.parseInt(st.nextToken());
               int heading = Integer.parseInt(st.nextToken());
               int mainMachineId = Integer.parseInt(st.nextToken());
               _mainMachineSpawns.add(new SpawnFortSiege(fort.getId(), x, y, z, heading, mainMachineId, i));
            } catch (Exception var31) {
               _log.warning("Error while loading main machine for " + fort.getName() + " fort.");
            }
         }

         this._mainMachineList.put(fort.getId(), _mainMachineSpawns);
      }
   }

   public final List<SpawnFortSiege> getCommanderSpawnList(int fortId) {
      return this._commanderSpawnList.containsKey(fortId) ? this._commanderSpawnList.get(fortId) : null;
   }

   public final List<SpawnFortSiege> getPowerUnitSpawnList(int fortId) {
      return this._powerUnitList.containsKey(fortId) ? this._powerUnitList.get(fortId) : null;
   }

   public final List<SpawnFortSiege> getControlUnitSpawnList(int fortId) {
      return this._controlUnitList.containsKey(fortId) ? this._controlUnitList.get(fortId) : null;
   }

   public final List<SpawnFortSiege> getMainMachineSpawnList(int fortId) {
      return this._mainMachineList.containsKey(fortId) ? this._mainMachineList.get(fortId) : null;
   }

   public final List<CombatFlag> getFlagList(int fortId) {
      return this._flagList.containsKey(fortId) ? this._flagList.get(fortId) : null;
   }

   public final int getAttackerMaxClans() {
      return this._attackerMaxClans;
   }

   public final int getFlagMaxCount() {
      return this._flagMaxCount;
   }

   public final int getSuspiciousMerchantRespawnDelay() {
      return this._suspiciousMerchantRespawnDelay;
   }

   public final FortSiege getSiege(GameObject activeObject) {
      return this.getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final FortSiege getSiege(int x, int y, int z) {
      for(Fort fort : FortManager.getInstance().getForts()) {
         if (fort.getSiege().checkIfInZone(x, y, z)) {
            return fort.getSiege();
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

   public final int getCountDownLength() {
      return this._countDownLength;
   }

   public final List<FortSiege> getSieges() {
      return this._sieges;
   }

   public final void addSiege(FortSiege fortSiege) {
      this._sieges.add(fortSiege);
   }

   public boolean isCombat(int itemId) {
      return itemId == 9819;
   }

   public boolean activateCombatFlag(Player player, ItemInstance item) {
      if (!this.checkIfCanPickup(player)) {
         return false;
      } else {
         Fort fort = FortManager.getInstance().getFort(player);

         for(CombatFlag cf : this._flagList.get(fort.getId())) {
            if (cf.getCombatFlagInstance() == item) {
               cf.activate(player, item);
            }
         }

         return true;
      }
   }

   public boolean checkIfCanPickup(Player player) {
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
      sm.addItemName(9819);
      if (player.isCombatFlagEquipped()) {
         player.sendPacket(sm);
         return false;
      } else {
         Fort fort = FortManager.getInstance().getFort(player);
         if (fort == null || fort.getId() <= 0) {
            player.sendPacket(sm);
            return false;
         } else if (!fort.getSiege().getIsInProgress()) {
            player.sendPacket(sm);
            return false;
         } else if (fort.getSiege().getAttackerClan(player.getClan()) == null) {
            player.sendPacket(sm);
            return false;
         } else {
            return true;
         }
      }
   }

   public void dropCombatFlag(Player player, int fortId) {
      Fort fort = FortManager.getInstance().getFortById(fortId);

      for(CombatFlag cf : this._flagList.get(fort.getId())) {
         if (cf.getPlayerObjectId() == player.getObjectId()) {
            cf.dropIt();
            if (fort.getSiege().getIsInProgress()) {
               cf.spawnMe();
            }
         }
      }
   }

   public int getFortHwidLimit() {
      return this._fortHwidLimit;
   }

   public static final FortSiegeManager getInstance() {
      return FortSiegeManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FortSiegeManager _instance = new FortSiegeManager();
   }
}
