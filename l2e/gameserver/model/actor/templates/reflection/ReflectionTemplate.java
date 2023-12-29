package l2e.gameserver.model.actor.templates.reflection;

import java.util.List;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.holders.ReflectionReenterTimeHolder;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;

public class ReflectionTemplate {
   private final int _id;
   private final String _name;
   private final int _timelimit;
   private final boolean _dispelBuffs;
   private final int _respawnTime;
   private final int _minLevel;
   private final int _maxLevel;
   private final int _minParty;
   private final int _maxParty;
   private final List<Location> _teleportCoords;
   private final Location _returnCoords;
   private final int _collapseIfEmpty;
   private final int _maxChannels;
   private final int _removedItemId;
   private final int _removedItemCount;
   private final boolean _removedItemNecessity;
   private final ReflectionTemplate.ReflectionRemoveType _removeType;
   private final int _giveItemId;
   private final int _givedItemCount;
   private final boolean _allowSummon;
   private final boolean _isPvPInstance;
   private final boolean _showTimer;
   private final boolean _isTimerIncrease;
   private final String _timerText;
   private ReflectionTemplate.ReflectionEntryType _entryType = null;
   private final Map<Integer, StatsSet> _doors;
   private final Map<String, ReflectionTemplate.SpawnInfo2> _spawns;
   private final List<ReflectionTemplate.SpawnInfo> _spawnsInfo;
   private final boolean _reuseUponEntry;
   private final int _sharedReuseGroup;
   final List<ReflectionReenterTimeHolder> _resetData;
   private final String _requiredQuest;
   private final ReflectionTemplate.ReflectionQuestType _questType;
   private final StatsSet _params;
   private int _mapx = -1;
   private int _mapy = -1;

   public ReflectionTemplate(
      int id,
      String name,
      int timelimit,
      boolean dispelBuffs,
      int respawnTime,
      int minLevel,
      int maxLevel,
      int minParty,
      int maxParty,
      List<Location> tele,
      Location ret,
      int collapseIfEmpty,
      int maxChannels,
      int removedItemId,
      int removedItemCount,
      boolean removedItemNecessity,
      ReflectionTemplate.ReflectionRemoveType removeType,
      int giveItemId,
      int givedItemCount,
      boolean allowSummon,
      boolean isPvPInstance,
      boolean showTimer,
      boolean isTimerIncrease,
      String timerText,
      Map<Integer, StatsSet> doors,
      Map<String, ReflectionTemplate.SpawnInfo2> spawns,
      List<ReflectionTemplate.SpawnInfo> spawnsInfo,
      int mapx,
      int mapy,
      boolean reuseUponEntry,
      int sharedReuseGroup,
      List<ReflectionReenterTimeHolder> resetData,
      String requiredQuest,
      ReflectionTemplate.ReflectionQuestType questType,
      StatsSet params
   ) {
      this._id = id;
      this._name = name;
      this._timelimit = timelimit;
      this._dispelBuffs = dispelBuffs;
      this._respawnTime = respawnTime;
      this._minLevel = minLevel;
      this._maxLevel = maxLevel;
      this._teleportCoords = tele;
      this._returnCoords = ret;
      this._minParty = minParty;
      this._maxParty = maxParty;
      this._collapseIfEmpty = collapseIfEmpty;
      this._maxChannels = maxChannels;
      this._removedItemId = removedItemId;
      this._removedItemCount = removedItemCount;
      this._removedItemNecessity = removedItemNecessity;
      this._removeType = removeType;
      this._giveItemId = giveItemId;
      this._givedItemCount = givedItemCount;
      this._allowSummon = allowSummon;
      this._isPvPInstance = isPvPInstance;
      this._showTimer = showTimer;
      this._isTimerIncrease = isTimerIncrease;
      this._timerText = timerText;
      this._doors = doors;
      this._spawnsInfo = spawnsInfo;
      this._spawns = spawns;
      this._mapx = mapx;
      this._mapy = mapy;
      this._reuseUponEntry = reuseUponEntry;
      this._sharedReuseGroup = sharedReuseGroup;
      this._resetData = resetData;
      this._requiredQuest = requiredQuest;
      this._questType = questType;
      this._params = params;
      if (this.getMinParty() == 1 && this.getMaxParty() == 1) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.SOLO;
      } else if (this.getMinParty() == 1 && this.getMaxParty() <= 9) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.SOLO_PARTY;
      } else if (this.getMinParty() > 1 && this.getMaxParty() <= 9) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.PARTY;
      } else if (this.getMinParty() < 9 && this.getMaxParty() > 9) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.PARTY_COMMAND_CHANNEL;
      } else if (this.getMinParty() >= 9 && this.getMaxParty() > 9) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.COMMAND_CHANNEL;
      } else if (this.getMaxParty() == 0) {
         this._entryType = ReflectionTemplate.ReflectionEntryType.EVENT;
      }

      if (this._entryType == null) {
         throw new IllegalArgumentException("Invalid type for reflection: " + this._name);
      }
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public boolean isDispelBuffs() {
      return this._dispelBuffs;
   }

   public int getTimelimit() {
      return this._timelimit;
   }

   public int getRespawnTime() {
      return this._respawnTime;
   }

   public int getMinLevel() {
      return this._minLevel;
   }

   public int getMaxLevel() {
      return this._maxLevel;
   }

   public int getMinParty() {
      return this._minParty;
   }

   public int getMaxParty() {
      return this._maxParty;
   }

   public Location getTeleportCoord() {
      if (this._teleportCoords != null && !this._teleportCoords.isEmpty()) {
         return this._teleportCoords.size() == 1 ? this._teleportCoords.get(0) : this._teleportCoords.get(Rnd.get(this._teleportCoords.size()));
      } else {
         return null;
      }
   }

   public void setNewTeleportCoords(Location loc) {
      this._teleportCoords.clear();
      this._teleportCoords.add(loc);
   }

   public Location getReturnCoords() {
      return this._returnCoords;
   }

   public int getCollapseIfEmpty() {
      return this._collapseIfEmpty;
   }

   public int getRemovedItemId() {
      return this._removedItemId;
   }

   public int getRemovedItemCount() {
      return this._removedItemCount;
   }

   public boolean getRemovedItemNecessity() {
      return this._removedItemNecessity;
   }

   public ReflectionTemplate.ReflectionRemoveType getRemoveType() {
      return this._removeType;
   }

   public int getGiveItemId() {
      return this._giveItemId;
   }

   public int getGiveItemCount() {
      return this._givedItemCount;
   }

   public int getMaxChannels() {
      return this._maxChannels;
   }

   public ReflectionTemplate.ReflectionEntryType getEntryType() {
      return this._entryType;
   }

   public List<Location> getTeleportCoords() {
      return this._teleportCoords;
   }

   public boolean isSummonAllowed() {
      return this._allowSummon;
   }

   public boolean isPvPInstance() {
      return this._isPvPInstance;
   }

   public boolean isShowTimer() {
      return this._showTimer;
   }

   public boolean isTimerIncrease() {
      return this._isTimerIncrease;
   }

   public String getTimerText() {
      return this._timerText;
   }

   public Map<Integer, StatsSet> getDoorList() {
      return this._doors;
   }

   public List<ReflectionTemplate.SpawnInfo> getSpawnsInfo() {
      return this._spawnsInfo;
   }

   public Map<String, ReflectionTemplate.SpawnInfo2> getSpawns() {
      return this._spawns;
   }

   public int getMapX() {
      return this._mapx;
   }

   public int getMapY() {
      return this._mapy;
   }

   public boolean getReuseUponEntry() {
      return this._reuseUponEntry;
   }

   public int getSharedReuseGroup() {
      return this._sharedReuseGroup;
   }

   public List<ReflectionReenterTimeHolder> getReenterData() {
      return this._resetData;
   }

   public String getRequiredQuest() {
      return this._requiredQuest;
   }

   public ReflectionTemplate.ReflectionQuestType getQuestType() {
      return this._questType;
   }

   public StatsSet getParams() {
      return this._params;
   }

   public static enum ReflectionEntryType {
      SOLO,
      SOLO_PARTY,
      PARTY,
      EVENT,
      PARTY_COMMAND_CHANNEL,
      COMMAND_CHANNEL;
   }

   public static enum ReflectionQuestType {
      STARTED,
      COMPLETED;
   }

   public static enum ReflectionRemoveType {
      NONE,
      LEADER,
      ALL;
   }

   public static class SpawnInfo {
      private final int _spawnType;
      private final int _npcId;
      private final int _count;
      private final int _respawn;
      private final int _respawnRnd;
      private final List<Location> _coords;
      private final SpawnTerritory _territory;

      public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, SpawnTerritory territory) {
         this(spawnType, npcId, count, respawn, respawnRnd, null, territory);
      }

      public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, List<Location> coords) {
         this(spawnType, npcId, count, respawn, respawnRnd, coords, null);
      }

      public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, List<Location> coords, SpawnTerritory territory) {
         this._spawnType = spawnType;
         this._npcId = npcId;
         this._count = count;
         this._respawn = respawn;
         this._respawnRnd = respawnRnd;
         this._coords = coords;
         this._territory = territory;
      }

      public int getSpawnType() {
         return this._spawnType;
      }

      public int getId() {
         return this._npcId;
      }

      public int getCount() {
         return this._count;
      }

      public int getRespawnDelay() {
         return this._respawn;
      }

      public int getRespawnRnd() {
         return this._respawnRnd;
      }

      public List<Location> getCoords() {
         return this._coords;
      }

      public SpawnTerritory getLoc() {
         return this._territory;
      }
   }

   public static class SpawnInfo2 {
      private final List<Spawner> _template;
      private final boolean _spawned;

      public SpawnInfo2(List<Spawner> template, boolean spawned) {
         this._template = template;
         this._spawned = spawned;
      }

      public List<Spawner> getTemplates() {
         return this._template;
      }

      public boolean isSpawned() {
         return this._spawned;
      }
   }
}
