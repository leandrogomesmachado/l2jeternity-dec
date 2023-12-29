package l2e.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.ChampionManager;
import l2e.gameserver.listener.SpawnListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.ILocational;
import l2e.gameserver.model.interfaces.IPositionable;

public class Spawner implements IPositionable, IIdentifiable {
   protected static final Logger _log = Logger.getLogger(Spawner.class.getName());
   private NpcTemplate _template;
   private int _maximumCount;
   private int _currentCount;
   protected int _scheduledCount;
   private int _locationId;
   private String _territoryName;
   private Location _location = new Location(0, 0, 0, 0);
   private int _respawnMinDelay;
   private int _respawnMaxDelay;
   private SchedulingPattern _respawnPattern;
   private Constructor<?> _constructor;
   private boolean _doRespawn;
   private boolean _customSpawn;
   private SpawnTemplate _spawnTemplate;
   private int _spawnIndex;
   private int _reflectionId = 0;
   private int _geoIndex = 0;
   private final Deque<Npc> _spawnedNpcs = new ConcurrentLinkedDeque<>();
   private static List<SpawnListener> _spawnListeners = new CopyOnWriteArrayList<>();
   private Map<Integer, Location> _lastSpawnPoints;

   public Spawner(NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
      this._template = mobTemplate;
      if (this._template != null) {
         Class<?>[] parameters = new Class[]{Integer.TYPE, Class.forName("l2e.gameserver.model.actor.templates.npc.NpcTemplate")};
         this._constructor = Class.forName("l2e.gameserver.model.actor.instance." + this._template.getType() + "Instance").getConstructor(parameters);
      }
   }

   public int getAmount() {
      return this._maximumCount;
   }

   public int getLocationId() {
      return this._locationId;
   }

   public Location getLocation() {
      return this._location;
   }

   public Location getLocation(GameObject obj) {
      return this._lastSpawnPoints != null && obj != null && this._lastSpawnPoints.containsKey(obj.getObjectId())
         ? this._lastSpawnPoints.get(obj.getObjectId())
         : this._location;
   }

   @Override
   public int getId() {
      return this._template.getId();
   }

   public int getX(GameObject obj) {
      return this.getLocation(obj).getX();
   }

   @Override
   public int getX() {
      return this._location.getX();
   }

   @Override
   public void setX(int x) {
      this._location.setX(x);
   }

   public int getY(GameObject obj) {
      return this.getLocation(obj).getY();
   }

   @Override
   public int getY() {
      return this._location.getY();
   }

   @Override
   public void setY(int y) {
      this._location.setY(y);
   }

   public int getZ(GameObject obj) {
      return this.getLocation(obj).getZ();
   }

   @Override
   public int getZ() {
      return this._location.getZ();
   }

   @Override
   public void setZ(int z) {
      this._location.setZ(z);
   }

   @Override
   public int getHeading() {
      return this._location.getHeading();
   }

   @Override
   public void setHeading(int heading) {
      this._location.setHeading(heading);
   }

   @Override
   public void setLocation(Location loc) {
      this._location = loc;
   }

   public void setLocationId(int id) {
      this._locationId = id;
   }

   public int getRespawnMinDelay() {
      return this._respawnMinDelay;
   }

   public int getRespawnMaxDelay() {
      return this._respawnMaxDelay;
   }

   public SchedulingPattern getRespawnPattern() {
      return this._respawnPattern;
   }

   public void setAmount(int amount) {
      this._maximumCount = amount;
   }

   public void setRespawnMinDelay(int date) {
      this._respawnMinDelay = date;
   }

   public void setRespawnMaxDelay(int date) {
      this._respawnMaxDelay = date;
   }

   public void setRespawnPattern(SchedulingPattern pattern) {
      this._respawnPattern = pattern;
   }

   public void setCustom(boolean custom) {
      this._customSpawn = custom;
   }

   public boolean isCustom() {
      return this._customSpawn;
   }

   public void decreaseCount(Npc oldNpc) {
      if (this._currentCount > 0) {
         --this._currentCount;
         this._spawnedNpcs.remove(oldNpc);
         if (this._lastSpawnPoints != null) {
            this._lastSpawnPoints.remove(oldNpc.getObjectId());
         }

         if (this._doRespawn && this._scheduledCount + this._currentCount < this._maximumCount) {
            ++this._scheduledCount;
            int respawnTime = 0;
            if (this.getRespawnPattern() != null) {
               respawnTime = (int)(this.getRespawnPattern().next(System.currentTimeMillis()) - System.currentTimeMillis());
               _log.info("Spawner: " + oldNpc.getName() + " Dead! Respawn date [" + new Date((long)respawnTime + System.currentTimeMillis()) + "].");
            } else {
               respawnTime = this.hasRespawnRandom() ? Rnd.get(this._respawnMinDelay, this._respawnMaxDelay) : this._respawnMinDelay;
            }

            ThreadPoolManager.getInstance().schedule(new Spawner.SpawnTask(oldNpc), (long)respawnTime);
         }
      }
   }

   public int init() {
      while(this._currentCount < this._maximumCount) {
         this.doSpawn();
      }

      this._doRespawn = this._respawnMinDelay != 0 || this.getRespawnPattern() != null;
      return this._currentCount;
   }

   public Npc spawnOne(boolean val) {
      return this.doSpawn(val);
   }

   public boolean isRespawnEnabled() {
      return this._doRespawn;
   }

   public void stopRespawn() {
      this._doRespawn = false;
   }

   public void startRespawn() {
      this._doRespawn = true;
   }

   public Npc doSpawn() {
      return this.doSpawn(false);
   }

   public Npc doSpawn(boolean isSummonSpawn) {
      Npc mob = null;

      try {
         if (!this._template.isType("Pet") && !this._template.isType("Decoy") && !this._template.isType("Trap") && !this._template.isType("EffectPoint")) {
            Object[] parameters = new Object[]{IdFactory.getInstance().getNextId(), this._template};
            Object tmp = this._constructor.newInstance(parameters);
            ((GameObject)tmp).setReflectionId(this.getReflectionId());
            if (isSummonSpawn && tmp instanceof Creature) {
               ((Creature)tmp).setShowSummonAnimation(isSummonSpawn);
            }

            if (!(tmp instanceof Npc)) {
               return mob;
            } else {
               mob = (Npc)tmp;
               if (this.getReflectionId() > 0) {
                  this.setGeoIndex(mob.getGeoIndex());
               }

               return this.initializeNpcInstance(mob);
            }
         } else {
            ++this._currentCount;
            return mob;
         }
      } catch (Exception var5) {
         _log.log(Level.WARNING, "NPC " + this._template.getId() + " class not found", (Throwable)var5);
         return mob;
      }
   }

   private Npc initializeNpcInstance(Npc mob) {
      if (this.getSpawnTemplate() != null) {
         Location loc = this.calcSpawnRangeLoc(mob.getGeoIndex(), mob.getTemplate());
         if (loc != null) {
            this.setX(loc.getX());
            this.setY(loc.getY());
            if (mob.isAttackable() && !mob.isFlying()) {
               this.setZ(GeoEngine.getHeight(loc.getX(), loc.getY(), loc.getZ(), mob.getGeoIndex()));
            } else {
               this.setZ(loc.getZ());
            }
         }
      } else {
         if (this.getX() == 0 && this.getY() == 0) {
            _log.warning("Problem with spawn location at npc id:" + mob.getId());
            return null;
         }

         if (mob.isAttackable() && !mob.isFlying()) {
            this.setZ(GeoEngine.getHeight(this.getX(), this.getY(), this.getZ(), mob.getGeoIndex()));
         }
      }

      int newlocx = this.getX();
      int newlocy = this.getY();
      int newlocz = this.getZ();
      mob.stopAllEffects();
      mob.setIsDead(false);
      mob.setDecayed(false);
      mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
      mob.setScriptValue(0);
      if (this.getHeading() == -1) {
         mob.setHeading(Rnd.nextInt(61794));
      } else {
         mob.setHeading(this.getHeading());
      }

      if (mob.isMonster() && !this.getTemplate().getCanChampion() && !this.getTemplate().isQuestMonster() && !mob.isRaid() && !mob.isRaidMinion()) {
         if (mob.getChampionTemplate() != null) {
            mob.setChampionTemplate(null);
         }

         if (ChampionManager.getInstance().ENABLE_EXT_CHAMPION_MODE) {
            int rnd = Rnd.get(ChampionManager.getInstance().EXT_CHAMPION_MODE_MAX_ROLL_VALUE);

            for(ChampionTemplate ct : ChampionManager.getInstance().getChampionTemplates()) {
               if ((ct.spawnsInInstances || mob.getReflectionId() == 0)
                  && rnd >= ct.minChance
                  && rnd <= ct.maxChance
                  && mob.getLevel() >= ct.minLevel
                  && mob.getLevel() <= ct.maxLevel) {
                  mob.setChampionTemplate(ct);
                  mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
                  break;
               }
            }
         }
      }

      mob.setSpawn(this);
      mob.spawnMe(newlocx, newlocy, newlocz);
      notifyNpcSpawned(mob);
      this._spawnedNpcs.add(mob);
      if (this._lastSpawnPoints != null) {
         this._lastSpawnPoints.put(mob.getObjectId(), new Location(newlocx, newlocy, newlocz));
      }

      if (Config.DEBUG) {
         _log.finest("Spawned Mob Id: " + this._template.getId() + " , at: X: " + mob.getX() + " Y: " + mob.getY() + " Z: " + mob.getZ());
      }

      ++this._currentCount;
      return mob;
   }

   public static void addSpawnListener(SpawnListener listener) {
      synchronized(_spawnListeners) {
         _spawnListeners.add(listener);
      }
   }

   public static void removeSpawnListener(SpawnListener listener) {
      synchronized(_spawnListeners) {
         _spawnListeners.remove(listener);
      }
   }

   public static void notifyNpcSpawned(Npc npc) {
      synchronized(_spawnListeners) {
         for(SpawnListener listener : _spawnListeners) {
            listener.npcSpawned(npc);
         }
      }
   }

   public void setRespawnDelay(int delay, int randomInterval) {
      if (delay != 0) {
         if (delay < 0) {
            _log.warning("respawn delay is negative for spawn:" + this);
         }

         int minDelay = delay - randomInterval;
         int maxDelay = delay + randomInterval;
         this._respawnMinDelay = Math.max(10, minDelay) * 1000;
         this._respawnMaxDelay = Math.max(10, maxDelay) * 1000;
      } else {
         this._respawnMinDelay = 0;
         this._respawnMaxDelay = 0;
      }
   }

   public void setRespawnDelay(int delay) {
      this.setRespawnDelay(delay, 0);
   }

   public int getRespawnDelay() {
      return (this._respawnMinDelay + this._respawnMaxDelay) / 2;
   }

   public boolean hasRespawnRandom() {
      return this._respawnMinDelay != this._respawnMaxDelay;
   }

   public Npc getLastSpawn() {
      return this._spawnedNpcs.peekLast();
   }

   public final Deque<Npc> getSpawnedNpcs() {
      return this._spawnedNpcs;
   }

   public void respawnNpc(Npc oldNpc) {
      if (this._doRespawn) {
         oldNpc.refreshID();
         this.initializeNpcInstance(oldNpc);
      }
   }

   public NpcTemplate getTemplate() {
      return this._template;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   public void setReflectionId(int instanceId) {
      this._reflectionId = instanceId;
   }

   public SpawnTemplate getSpawnTemplate() {
      return this._spawnTemplate;
   }

   public void setSpawnTemplate(SpawnTemplate spawnRange) {
      this._spawnTemplate = spawnRange;
   }

   public int generateSpawnIndex() {
      return this._spawnIndex = this._spawnTemplate != null && !this._spawnTemplate.getSpawnRangeList().isEmpty()
         ? Rnd.get(this._spawnTemplate.getSpawnRangeList().size())
         : 0;
   }

   public SpawnRange calcSpawnRange() {
      if (this.getSpawnTemplate() != null) {
         SpawnRange spawnRange = this.getSpawnTemplate().getSpawnRangeList().get(this.generateSpawnIndex());
         if (spawnRange == null) {
            _log.warning("Problem with calc SpawnRange at npc id:" + this.getId());
            return null;
         } else {
            return spawnRange;
         }
      } else {
         return null;
      }
   }

   public Location calcSpawnRangeLoc(int geoIndex, NpcTemplate template) {
      SpawnRange spawnRange = this.calcSpawnRange();
      if (spawnRange == null) {
         _log.warning("Problem with calc SpawnRange at npc id:" + this.getId());
         return null;
      } else {
         return spawnRange.getRandomLoc(geoIndex, template.isFlying());
      }
   }

   public int getSpawnIndex() {
      return this._spawnIndex;
   }

   public int getGeoIndex() {
      return this._geoIndex;
   }

   public void setGeoIndex(int geoIndex) {
      this._geoIndex = geoIndex;
   }

   @Override
   public String toString() {
      return "Spawner [_template="
         + this.getId()
         + ", _locX="
         + this.getX()
         + ", _locY="
         + this.getY()
         + ", _locZ="
         + this.getZ()
         + ", _heading="
         + this.getHeading()
         + "]";
   }

   @Override
   public void setXYZ(int x, int y, int z) {
      this.setX(x);
      this.setY(y);
      this.setZ(z);
   }

   @Override
   public void setXYZ(ILocational loc) {
      this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
   }

   public void setTerritoryName(String territoryName) {
      this._territoryName = territoryName;
   }

   public String getTerritoryName() {
      return this._territoryName;
   }

   class SpawnTask implements Runnable {
      private final Npc _oldNpc;

      public SpawnTask(Npc pOldNpc) {
         this._oldNpc = pOldNpc;
      }

      @Override
      public void run() {
         try {
            Spawner.this.respawnNpc(this._oldNpc);
         } catch (Exception var2) {
            Spawner._log.log(Level.WARNING, "", (Throwable)var2);
         }

         --Spawner.this._scheduledCount;
      }
   }
}
