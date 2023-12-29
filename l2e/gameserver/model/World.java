package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.collections.LazyArrayList;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.PetInstance;

public final class World {
   private static Logger _log = Logger.getLogger(World.class.getName());
   public static final int GRACIA_MAX_X = -166168;
   public static final int GRACIA_MAX_Z = 6105;
   public static final int GRACIA_MIN_Z = -895;
   public static final int TILE_X_MIN = 11;
   public static final int TILE_Y_MIN = 10;
   public static final int TILE_X_MAX = 26;
   public static final int TILE_Y_MAX = 26;
   public static final int WORLD_SIZE_X = 16;
   public static final int WORLD_SIZE_Y = 17;
   public static final int MAP_MIN_X = -294912;
   public static final int MAP_MAX_X = 229375;
   public static final int MAP_MIN_Y = -262144;
   public static final int MAP_MAX_Y = 294911;
   public static final int MAP_MIN_Z = -16384;
   public static final int MAP_MAX_Z = 16383;
   public static final int SHIFT_BY = 12;
   public static final int SHIFT_BY_FOR_Z = 10;
   public static final int OFFSET_X = Math.abs(-72);
   public static final int OFFSET_Y = Math.abs(-64);
   public static final int OFFSET_Z = Math.abs(-16);
   private static final int REGIONS_X = 55 + OFFSET_X;
   private static final int REGIONS_Y = 71 + OFFSET_Y;
   private static final int REGIONS_Z = 15 + OFFSET_Z;
   private final Map<Integer, Player> _allPlayers = new ConcurrentHashMap<>();
   private final Map<Integer, Npc> _allNpcs = new ConcurrentHashMap<>();
   private final Map<Integer, GameObject> _allObjects = new ConcurrentHashMap<>();
   private final Map<Integer, PetInstance> _petsInstance = new ConcurrentHashMap<>();
   private final WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][];

   protected World() {
      List<String> split_regions = Arrays.asList(Config.VERTICAL_SPLIT_REGIONS.split(";"));

      for(int x = 0; x <= REGIONS_X; ++x) {
         for(int y = 0; y <= REGIONS_Y; ++y) {
            int wx = ((x - OFFSET_X << 12) - -294912 >> 15) + 11;
            int wy = ((y - OFFSET_Y << 12) - -262144 >> 15) + 10;
            if (split_regions.contains(wx + "_" + wy)) {
               this._worldRegions[x][y] = new WorldRegion[REGIONS_Z + 1];
            } else {
               this._worldRegions[x][y] = new WorldRegion[1];
            }
         }
      }

      _log.info("World: (" + REGIONS_X + " by " + REGIONS_Y + ") world region grid set up.");
   }

   public static World getInstance() {
      return World.SingletonHolder._instance;
   }

   public GameObject findObject(int objectId) {
      return this._allObjects.get(objectId);
   }

   public final Collection<GameObject> getAllVisibleObjects() {
      return this._allObjects.values();
   }

   public List<Player> getAllGMs() {
      return AdminParser.getInstance().getAllGms(true);
   }

   public Collection<Player> getAllPlayers() {
      return this._allPlayers.values();
   }

   public Player getPlayer(String name) {
      return this.getPlayer(CharNameHolder.getInstance().getIdByName(name));
   }

   public Player getPlayer(int objectId) {
      return this._allPlayers.get(objectId);
   }

   public PetInstance getPet(int ownerId) {
      return this._petsInstance.get(ownerId);
   }

   public PetInstance addPet(int ownerId, PetInstance pet) {
      return this._petsInstance.put(ownerId, pet);
   }

   public void removePet(int ownerId) {
      this._petsInstance.remove(ownerId);
   }

   public void removePet(PetInstance pet) {
      this._petsInstance.remove(pet.getOwner().getObjectId());
   }

   public Collection<Npc> getNpcs() {
      return this._allNpcs.values();
   }

   public Npc getNpc(int objectId) {
      return this._allNpcs.get(objectId);
   }

   public void addToAllNpcs(Npc npc) {
      this._allNpcs.put(npc.getObjectId(), npc);
   }

   public void removeFromAllNpcs(Npc npc) {
      this._allNpcs.remove(npc.getObjectId());
   }

   public void addObject(GameObject object) {
      if (object.isNpc()) {
         Npc npc = object.getActingNpc();
         if (npc != null) {
            Npc tmp = this.getNpc(npc.getObjectId());
            if (tmp == null) {
               this.addToAllNpcs(npc);
            }
         }
      }

      this._allObjects.putIfAbsent(object.getObjectId(), object);
   }

   public void addToAllPlayers(Player cha) {
      this._allPlayers.put(cha.getObjectId(), cha);
   }

   public void removeFromAllPlayers(Player cha) {
      cha.clearZones();
      this._allPlayers.remove(cha.getObjectId());
   }

   public void removeObject(GameObject object) {
      if (object.isNpc()) {
         Npc npc = object.getActingNpc();
         if (npc != null) {
            this.removeFromAllNpcs(npc);
         }
      }

      if (object.isCreature()) {
         ((Creature)object).clearZones();
      }

      this._allObjects.remove(object.getObjectId());
   }

   public List<WorldRegion> getNeighbors(int regX, int regY, int regZ, int deepH, int deepV) {
      List<WorldRegion> neighbors = new ArrayList<>();
      deepH *= 2;
      deepV *= 2;

      for(int x = 0; x <= deepH; ++x) {
         for(int y = 0; y <= deepH; ++y) {
            for(int z = 0; z <= deepV; ++z) {
               int rx = regX + (x % 2 == 0 ? -x / 2 : x - x / 2);
               int ry = regY + (y % 2 == 0 ? -y / 2 : y - y / 2);
               int rz = 0;
               if (this.validRegion(rx, ry, rz)) {
                  if (this._worldRegions[rx][ry].length > 1) {
                     rz = regZ + (z % 2 == 0 ? -z / 2 : z - z / 2);
                     if (!this.validRegion(rx, ry, rz)) {
                        continue;
                     }
                  } else {
                     z = deepV + 1;
                  }

                  if (this._worldRegions[rx][ry][rz] != null) {
                     neighbors.add(this._worldRegions[rx][ry][rz]);
                  }
               }
            }
         }
      }

      return neighbors;
   }

   public List<Player> getAroundPlayers(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<Player> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisiblePlayable().values()) {
               if (obj != null && obj.isPlayer() && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add((Player)obj);
               }
            }
         }

         return result;
      }
   }

   public List<Player> getAroundTraders(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<Player> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisiblePlayable().values()) {
               if (obj != null
                  && obj.isPlayer()
                  && obj.getObjectId() != oid
                  && obj.getReflectionId() == rid
                  && obj.getActingPlayer().getPrivateStoreType() != 0) {
                  result.add((Player)obj);
               }
            }
         }

         return result;
      }
   }

   public List<Player> getAroundPlayers(GameObject object, int radius, int height) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<Player> result = new LazyArrayList<>(64);

         for(WorldRegion regi : object.getWorldRegion().getNeighbors()) {
            for(GameObject obj : regi.getVisiblePlayable().values()) {
               if (obj != null && obj.isPlayer() && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add((Player)obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public List<GameObject> getAroundObjects(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<GameObject> result = new LazyArrayList<>(128);

         for(WorldRegion regi : object.getWorldRegion().getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add(obj);
               }
            }
         }

         return result;
      }
   }

   public List<GameObject> getAroundObjects(GameObject object, int radius, int height) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<GameObject> result = new LazyArrayList<>(128);

         for(WorldRegion regi : object.getWorldRegion().getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add(obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public List<Playable> getAroundPlayables(GameObject object) {
      WorldRegion reg = object.getWorldRegion();
      if (reg == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<Playable> result = new LazyArrayList<>(64);

         for(WorldRegion regi : reg.getNeighbors()) {
            for(Playable obj : regi.getVisiblePlayable().values()) {
               if (obj != null && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add(obj);
               }
            }
         }

         return result;
      }
   }

   public List<Playable> getAroundPlayables(GameObject object, int radius, int height) {
      WorldRegion reg = object.getWorldRegion();
      if (reg == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<Playable> result = new LazyArrayList<>(64);

         for(WorldRegion regi : reg.getNeighbors()) {
            for(Playable obj : regi.getVisiblePlayable().values()) {
               if (obj != null && obj.isPlayable() && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add(obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public List<Creature> getAroundCharacters(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<Creature> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isCreature() && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add((Creature)obj);
               }
            }
         }

         return result;
      }
   }

   public List<Creature> getAroundCharacters(GameObject object, int radius, int height) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<Creature> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isCreature() && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add((Creature)obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public List<Npc> getAroundNpc(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<Npc> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isNpc() && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add((Npc)obj);
               }
            }
         }

         return result;
      }
   }

   public List<Npc> getAroundNpc(GameObject object, int radius, int height) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<Npc> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isNpc() && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add((Npc)obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public List<DoorInstance> getAroundDoors(GameObject object) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         List<DoorInstance> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isDoor() && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                  result.add((DoorInstance)obj);
               }
            }
         }

         return result;
      }
   }

   public List<DoorInstance> getAroundDoors(GameObject object, int radius, int height) {
      WorldRegion currentRegion = object.getWorldRegion();
      if (currentRegion == null) {
         return Collections.emptyList();
      } else {
         int oid = object.getObjectId();
         int rid = object.getReflectionId();
         int ox = object.getX();
         int oy = object.getY();
         int oz = object.getZ();
         int sqrad = radius * radius;
         List<DoorInstance> result = new LazyArrayList<>(64);

         for(WorldRegion regi : currentRegion.getNeighbors()) {
            for(GameObject obj : regi.getVisibleObjects().values()) {
               if (obj != null && obj.isDoor() && obj.getObjectId() != oid && obj.getReflectionId() == rid && Math.abs(obj.getZ() - oz) <= height) {
                  int dx = Math.abs(obj.getX() - ox);
                  if (dx <= radius) {
                     int dy = Math.abs(obj.getY() - oy);
                     if (dy <= radius && dx * dx + dy * dy <= sqrad) {
                        result.add((DoorInstance)obj);
                     }
                  }
               }
            }
         }

         return result;
      }
   }

   public Npc getNpcById(int npcId) {
      Npc result = null;

      for(GameObject temp : this.getAllVisibleObjects()) {
         if (temp != null && temp.isNpc() && npcId == temp.getId()) {
            Npc npc = (Npc)temp;
            if (!npc.isDead() && npc.isVisible()) {
               return npc;
            }
         }
      }

      return result;
   }

   public WorldRegion getRegion(Location loc) {
      return this.getRegion(loc.getX(), loc.getY(), loc.getZ());
   }

   public WorldRegion getRegion(int x, int y, int z) {
      int _x = (x >> 12) + OFFSET_X;
      int _y = (y >> 12) + OFFSET_Y;
      int _z = 0;
      if (this.validRegion(_x, _y, _z)) {
         if (this._worldRegions[_x][_y].length > 1) {
            _z = (z >> 10) + OFFSET_Z;
         }

         if (this._worldRegions[_x][_y][_z] == null) {
            this._worldRegions[_x][_y][_z] = new WorldRegion(_x, _y, _z);
         }

         return this._worldRegions[_x][_y][_z];
      } else {
         return null;
      }
   }

   public WorldRegion[][][] getAllWorldRegions() {
      return this._worldRegions;
   }

   public boolean validRegion(int x, int y, int z) {
      return x >= 0 && x < REGIONS_X && y >= 0 && y < REGIONS_Y && z >= 0 && z < REGIONS_Z;
   }

   public void deleteVisibleNpcSpawns() {
      _log.info("Deleting all visible NPC's.");

      for(int i = 0; i < REGIONS_X; ++i) {
         for(int j = 0; j < REGIONS_Y; ++j) {
            for(int k = 0; k < this._worldRegions[i][j].length; ++k) {
               if (this._worldRegions[i][j][k] != null) {
                  this._worldRegions[i][j][k].deleteVisibleNpcSpawns();
               }
            }
         }
      }

      _log.info("All visible NPC's deleted.");
   }

   private static class SingletonHolder {
      protected static final World _instance = new World();
   }
}
