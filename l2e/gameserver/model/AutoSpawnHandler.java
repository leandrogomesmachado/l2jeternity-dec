package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.spawn.Spawner;

public class AutoSpawnHandler {
   protected static final Logger _log = Logger.getLogger(AutoSpawnHandler.class.getName());
   private static final int DEFAULT_INITIAL_SPAWN = 30000;
   private static final int DEFAULT_RESPAWN = 3600000;
   private static final int DEFAULT_DESPAWN = 3600000;
   protected Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _registeredSpawns = new ConcurrentHashMap<>();
   protected Map<Integer, ScheduledFuture<?>> _runningSpawns = new ConcurrentHashMap<>();
   protected boolean _activeState = true;

   protected AutoSpawnHandler() {
      this.restoreSpawnData();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._registeredSpawns.size() + " AutoSpawnHandlers.");
   }

   public static AutoSpawnHandler getInstance() {
      return AutoSpawnHandler.SingletonHolder._instance;
   }

   public void reload() {
      for(ScheduledFuture<?> sf : this._runningSpawns.values()) {
         if (sf != null) {
            sf.cancel(true);
         }
      }

      for(AutoSpawnHandler.AutoSpawnInstance asi : this._registeredSpawns.values()) {
         if (asi != null) {
            this.removeSpawn(asi);
         }
      }

      this._registeredSpawns.clear();
      this._runningSpawns.clear();
      this.restoreSpawnData();
   }

   private void restoreSpawnData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery("SELECT * FROM random_spawn ORDER BY groupId ASC");
         PreparedStatement ps = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
      ) {
         while(rs.next()) {
            AutoSpawnHandler.AutoSpawnInstance spawnInst = this.registerSpawn(
               rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay")
            );
            spawnInst.setSpawnCount(rs.getInt("count"));
            spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
            spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
            ps.setInt(1, rs.getInt("groupId"));

            try (ResultSet rs2 = ps.executeQuery()) {
               ps.clearParameters();

               while(rs2.next()) {
                  spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
               }
            }
         }
      } catch (Exception var133) {
         _log.log(Level.WARNING, "AutoSpawnHandler: Could not restore spawn data: " + var133.getMessage(), (Throwable)var133);
      }
   }

   public AutoSpawnHandler.AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay) {
      if (initialDelay < 0) {
         initialDelay = 30000;
      }

      if (respawnDelay < 0) {
         respawnDelay = 3600000;
      }

      if (despawnDelay < 0) {
         despawnDelay = 3600000;
      }

      AutoSpawnHandler.AutoSpawnInstance newSpawn = new AutoSpawnHandler.AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
      if (spawnPoints != null) {
         for(int[] spawnPoint : spawnPoints) {
            newSpawn.addSpawnLocation(spawnPoint);
         }
      }

      int newId = IdFactory.getInstance().getNextId();
      newSpawn._objectId = newId;
      this._registeredSpawns.put(newId, newSpawn);
      this.setSpawnActive(newSpawn, true);
      return newSpawn;
   }

   public AutoSpawnHandler.AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay) {
      return this.registerSpawn(npcId, (int[][])null, initialDelay, respawnDelay, despawnDelay);
   }

   public boolean removeSpawn(AutoSpawnHandler.AutoSpawnInstance spawnInst) {
      if (!this.isSpawnRegistered(spawnInst)) {
         return false;
      } else {
         try {
            this._registeredSpawns.remove(spawnInst.getId());
            ScheduledFuture<?> respawnTask = this._runningSpawns.remove(spawnInst._objectId);
            respawnTask.cancel(false);
            return true;
         } catch (Exception var3) {
            if (Config.DEBUG) {
               _log.log(
                  Level.WARNING,
                  "AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + var3.getMessage(),
                  (Throwable)var3
               );
            }

            return false;
         }
      }
   }

   public void removeSpawn(int objectId) {
      this.removeSpawn(this._registeredSpawns.get(objectId));
   }

   public void setSpawnActive(AutoSpawnHandler.AutoSpawnInstance spawnInst, boolean isActive) {
      if (spawnInst != null) {
         int objectId = spawnInst._objectId;
         if (this.isSpawnRegistered(objectId)) {
            ScheduledFuture<?> spawnTask = null;
            if (isActive) {
               AutoSpawnHandler.AutoSpawner rs = new AutoSpawnHandler.AutoSpawner(objectId);
               if (spawnInst._desDelay > 0) {
                  spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(rs, (long)spawnInst._initDelay, (long)spawnInst._resDelay);
               } else {
                  spawnTask = ThreadPoolManager.getInstance().schedule(rs, (long)spawnInst._initDelay);
               }

               this._runningSpawns.put(objectId, spawnTask);
            } else {
               AutoSpawnHandler.AutoDespawner rd = new AutoSpawnHandler.AutoDespawner(objectId);
               spawnTask = this._runningSpawns.remove(objectId);
               if (spawnTask != null) {
                  spawnTask.cancel(false);
               }

               ThreadPoolManager.getInstance().schedule(rd, 0L);
            }

            spawnInst.setSpawnActive(isActive);
         }
      }
   }

   public void setAllActive(boolean isActive) {
      if (this._activeState != isActive) {
         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : this._registeredSpawns.values()) {
            this.setSpawnActive(spawnInst, isActive);
         }

         this._activeState = isActive;
      }
   }

   public final long getTimeToNextSpawn(AutoSpawnHandler.AutoSpawnInstance spawnInst) {
      int objectId = spawnInst.getObjectId();
      return !this.isSpawnRegistered(objectId) ? -1L : this._runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
   }

   public final AutoSpawnHandler.AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId) {
      if (isObjectId) {
         if (this.isSpawnRegistered(id)) {
            return this._registeredSpawns.get(id);
         }
      } else {
         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : this._registeredSpawns.values()) {
            if (spawnInst.getId() == id) {
               return spawnInst;
            }
         }
      }

      return null;
   }

   public List<AutoSpawnHandler.AutoSpawnInstance> getAutoSpawnInstances(int npcId) {
      List<AutoSpawnHandler.AutoSpawnInstance> result = new LinkedList<>();

      for(AutoSpawnHandler.AutoSpawnInstance spawnInst : this._registeredSpawns.values()) {
         if (spawnInst.getId() == npcId) {
            result.add(spawnInst);
         }
      }

      return result;
   }

   public final boolean isSpawnRegistered(int objectId) {
      return this._registeredSpawns.containsKey(objectId);
   }

   public final boolean isSpawnRegistered(AutoSpawnHandler.AutoSpawnInstance spawnInst) {
      return this._registeredSpawns.containsValue(spawnInst);
   }

   private class AutoDespawner implements Runnable {
      private final int _objectId;

      protected AutoDespawner(int objectId) {
         this._objectId = objectId;
      }

      @Override
      public void run() {
         try {
            AutoSpawnHandler.AutoSpawnInstance spawnInst = AutoSpawnHandler.this._registeredSpawns.get(this._objectId);
            if (spawnInst == null) {
               AutoSpawnHandler._log.info("AutoSpawnHandler: No spawn registered for object ID = " + this._objectId + ".");
               return;
            }

            for(Npc npcInst : spawnInst.getNPCInstanceList()) {
               if (npcInst != null) {
                  npcInst.deleteMe();
                  SpawnParser.getInstance().deleteSpawn(npcInst.getSpawn());
                  spawnInst.removeNpcInstance(npcInst);
               }
            }
         } catch (Exception var4) {
            AutoSpawnHandler._log
               .log(
                  Level.WARNING,
                  "AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + this._objectId + "): " + var4.getMessage(),
                  (Throwable)var4
               );
         }
      }
   }

   public static class AutoSpawnInstance implements IIdentifiable {
      protected int _objectId;
      protected int _spawnIndex;
      protected int _npcId;
      protected int _initDelay;
      protected int _resDelay;
      protected int _desDelay;
      protected int _spawnCount = 1;
      protected int _lastLocIndex = -1;
      private final Queue<Npc> _npcList = new ConcurrentLinkedQueue<>();
      private final List<Location> _locList = new CopyOnWriteArrayList<>();
      private boolean _spawnActive;
      private boolean _randomSpawn = false;
      private boolean _broadcastAnnouncement = false;

      protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay) {
         this._npcId = npcId;
         this._initDelay = initDelay;
         this._resDelay = respawnDelay;
         this._desDelay = despawnDelay;
      }

      protected void setSpawnActive(boolean activeValue) {
         this._spawnActive = activeValue;
      }

      protected boolean addNpcInstance(Npc npcInst) {
         return this._npcList.add(npcInst);
      }

      protected boolean removeNpcInstance(Npc npcInst) {
         return this._npcList.remove(npcInst);
      }

      public int getObjectId() {
         return this._objectId;
      }

      public int getInitialDelay() {
         return this._initDelay;
      }

      public int getRespawnDelay() {
         return this._resDelay;
      }

      public int getDespawnDelay() {
         return this._desDelay;
      }

      @Override
      public int getId() {
         return this._npcId;
      }

      public int getSpawnCount() {
         return this._spawnCount;
      }

      public Location[] getLocationList() {
         return this._locList.toArray(new Location[this._locList.size()]);
      }

      public Queue<Npc> getNPCInstanceList() {
         return this._npcList;
      }

      public List<Spawner> getSpawns() {
         List<Spawner> npcSpawns = new ArrayList<>();

         for(Npc npcInst : this._npcList) {
            npcSpawns.add(npcInst.getSpawn());
         }

         return npcSpawns;
      }

      public void setSpawnCount(int spawnCount) {
         this._spawnCount = spawnCount;
      }

      public void setRandomSpawn(boolean randValue) {
         this._randomSpawn = randValue;
      }

      public void setBroadcast(boolean broadcastValue) {
         this._broadcastAnnouncement = broadcastValue;
      }

      public boolean isSpawnActive() {
         return this._spawnActive;
      }

      public boolean isRandomSpawn() {
         return this._randomSpawn;
      }

      public boolean isBroadcasting() {
         return this._broadcastAnnouncement;
      }

      public boolean addSpawnLocation(int x, int y, int z, int heading) {
         return this._locList.add(new Location(x, y, z, heading));
      }

      public boolean addSpawnLocation(int[] spawnLoc) {
         return spawnLoc.length != 3 ? false : this.addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
      }

      public Location removeSpawnLocation(int locIndex) {
         try {
            return this._locList.remove(locIndex);
         } catch (IndexOutOfBoundsException var3) {
            return null;
         }
      }
   }

   private class AutoSpawner implements Runnable {
      private final int _objectId;

      protected AutoSpawner(int objectId) {
         this._objectId = objectId;
      }

      @Override
      public void run() {
         try {
            AutoSpawnHandler.AutoSpawnInstance spawnInst = AutoSpawnHandler.this._registeredSpawns.get(this._objectId);
            if (!spawnInst.isSpawnActive()) {
               return;
            }

            Location[] locationList = spawnInst.getLocationList();
            if (locationList.length == 0) {
               AutoSpawnHandler._log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + this._objectId + ").");
               return;
            }

            int locationCount = locationList.length;
            int locationIndex = Rnd.nextInt(locationCount);
            if (!spawnInst.isRandomSpawn()) {
               locationIndex = spawnInst._lastLocIndex + 1;
               if (locationIndex == locationCount) {
                  locationIndex = 0;
               }

               spawnInst._lastLocIndex = locationIndex;
            }

            int x = locationList[locationIndex].getX();
            int y = locationList[locationIndex].getY();
            int z = locationList[locationIndex].getZ();
            int heading = locationList[locationIndex].getHeading();
            NpcTemplate npcTemp = NpcsParser.getInstance().getTemplate(spawnInst.getId());
            if (npcTemp == null) {
               AutoSpawnHandler._log.warning("Couldnt find NPC id" + spawnInst.getId() + " Try to update your DP");
               return;
            }

            Spawner newSpawn = new Spawner(npcTemp);
            newSpawn.setX(x);
            newSpawn.setY(y);
            newSpawn.setZ(z);
            if (heading != -1) {
               newSpawn.setHeading(heading);
            }

            newSpawn.setAmount(spawnInst.getSpawnCount());
            if (spawnInst._desDelay == 0) {
               newSpawn.setRespawnDelay(spawnInst._resDelay);
            }

            SpawnParser.getInstance().addNewSpawn(newSpawn);
            Npc npcInst = null;
            if (spawnInst._spawnCount == 1) {
               npcInst = newSpawn.doSpawn();
               npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
               spawnInst.addNpcInstance(npcInst);
            } else {
               for(int i = 0; i < spawnInst._spawnCount; ++i) {
                  npcInst = newSpawn.doSpawn();
                  npcInst.setXYZ(npcInst.getX() + Rnd.nextInt(50), npcInst.getY() + Rnd.nextInt(50), npcInst.getZ());
                  spawnInst.addNpcInstance(npcInst);
               }
            }

            String nearestTown = MapRegionManager.getInstance().getClosestTownName(npcInst);
            if (spawnInst.isBroadcasting() && npcInst != null) {
               Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
            }

            if (spawnInst.getDespawnDelay() > 0) {
               AutoSpawnHandler.AutoDespawner rd = AutoSpawnHandler.this.new AutoDespawner(this._objectId);
               ThreadPoolManager.getInstance().schedule(rd, (long)(spawnInst.getDespawnDelay() - 1000));
            }
         } catch (Exception var14) {
            AutoSpawnHandler._log
               .log(
                  Level.WARNING,
                  "AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + this._objectId + "): " + var14.getMessage(),
                  (Throwable)var14
               );
         }
      }
   }

   private static class SingletonHolder {
      protected static final AutoSpawnHandler _instance = new AutoSpawnHandler();
   }
}
