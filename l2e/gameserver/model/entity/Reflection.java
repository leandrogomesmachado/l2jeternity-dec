package l2e.gameserver.model.entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Rnd;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.ColosseumFence;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.actor.templates.door.DoorTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.holders.ReflectionReenterTimeHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.spawn.SpawnTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.ReflectionZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Reflection {
   private static final Logger _log = Logger.getLogger(Reflection.class.getName());
   private final int _id;
   private String _name;
   private int _geoIndex = 0;
   private int _mapx = -1;
   private int _mapy = -1;
   private int _ejectTime = Config.EJECT_DEAD_PLAYER_TIME;
   private final List<Integer> _players = new CopyOnWriteArrayList<>();
   private final List<Npc> _npcs = new CopyOnWriteArrayList<>();
   private final IntObjectMap<DoorInstance> _doors = new HashIntObjectMap<>();
   private final Map<Integer, ColosseumFence> _fences = new ConcurrentHashMap<>();
   private final List<ItemInstance> _items = new CopyOnWriteArrayList<>();
   private final Map<String, List<Spawner>> _manualSpawn = new HashMap<>();
   protected Map<String, List<Spawner>> _spawners = Collections.emptyMap();
   private final int[] _spawnsLoc = new int[3];
   private boolean _allowSummon = true;
   private long _emptyDestroyTime = -1L;
   private long _lastLeft = -1L;
   private long _instanceStartTime = -1L;
   private long _instanceEndTime = -1L;
   private boolean _isPvPInstance = false;
   private boolean _showTimer = false;
   private boolean _isTimerIncrease = true;
   private String _timerText = "";
   private Location _returnCoords = null;
   private boolean _disableMessages = false;
   private final List<Integer> _zones = new ArrayList<>();
   private boolean _reuseUponEntry;
   private List<ReflectionReenterTimeHolder> _resetData = new ArrayList<>();
   private StatsSet _params = new StatsSet();
   protected ScheduledFuture<?> _checkTimeUpTask = null;
   protected final Map<Integer, ScheduledFuture<?>> _ejectDeadTasks = new ConcurrentHashMap<>();

   public Reflection(int id) {
      this._id = id;
      this._instanceStartTime = System.currentTimeMillis();
   }

   public Reflection(int id, String name) {
      this._id = id;
      this._name = name;
      this._instanceStartTime = System.currentTimeMillis();
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public void setName(String name) {
      this._name = name;
   }

   public int getEjectTime() {
      return this._ejectTime;
   }

   public void setEjectTime(int ejectTime) {
      this._ejectTime = ejectTime;
   }

   public boolean isSummonAllowed() {
      return this._allowSummon;
   }

   public void setAllowSummon(boolean b) {
      this._allowSummon = b;
   }

   public boolean isPvPInstance() {
      return this._isPvPInstance;
   }

   public void setPvPInstance(boolean b) {
      this._isPvPInstance = b;
   }

   public void setDuration(int duration) {
      if (this._checkTimeUpTask != null) {
         this._checkTimeUpTask.cancel(true);
      }

      this._checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new Reflection.CheckTimeUp(duration), 500L);
      this._instanceEndTime = System.currentTimeMillis() + (long)duration + 500L;
   }

   public void setEmptyDestroyTime(long time) {
      this._emptyDestroyTime = time;
   }

   public boolean containsPlayer(int objectId) {
      return this._players.contains(objectId);
   }

   public void addPlayer(int objectId) {
      this._players.add(objectId);
   }

   public void removePlayer(Integer objectId) {
      this._players.remove(objectId);
      if (this._players.isEmpty() && this._emptyDestroyTime >= 0L) {
         this._lastLeft = System.currentTimeMillis();
         this.setDuration((int)(this._instanceEndTime - System.currentTimeMillis() - 500L));
      }
   }

   public void addNpc(Npc npc) {
      this._npcs.add(npc);
   }

   public void removeNpc(Npc npc) {
      if (npc.getSpawn() != null) {
         npc.getSpawn().stopRespawn();
      }

      this._npcs.remove(npc);
   }

   public void addDoor(int doorId, StatsSet set) {
      if (this._doors.containsKey(doorId)) {
         _log.warning("Door ID " + doorId + " already exists in instance " + this.getId());
      } else {
         DoorTemplate temp = DoorParser.getInstance().getDoorTemplate(doorId);
         DoorInstance newdoor = new DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
         newdoor.setReflectionId(this.getId());
         newdoor.setCurrentHp(newdoor.getMaxHp());
         int gz = temp.posZ + 32;
         newdoor.spawnMe(temp.posX, temp.posY, gz);
         this._doors.put(doorId, newdoor);
      }
   }

   public void addEventDoor(int doorId, StatsSet set) {
      if (this._doors.containsKey(doorId)) {
         _log.warning("Door ID " + doorId + " already exists in instance " + this.getId());
      } else {
         DoorTemplate temp = DoorParser.getInstance().getDoorTemplate(doorId);
         DoorInstance newdoor = new DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
         newdoor.setReflectionId(this.getId());
         newdoor.setCurrentHp(newdoor.getMaxHp());
         newdoor.setGeoIndex(this.getGeoIndex());
         int gz = temp.posZ + 32;
         newdoor.spawnMe(temp.posX, temp.posY, gz);
         newdoor.openMe();
         this._doors.put(doorId, newdoor);
      }
   }

   public List<Integer> getPlayers() {
      return this._players;
   }

   public List<Npc> getNpcs() {
      return this._npcs;
   }

   public Collection<DoorInstance> getDoors() {
      return this._doors.valueCollection();
   }

   public DoorInstance getDoor(int id) {
      return this._doors.get(id);
   }

   public void openDoor(int doorId) {
      DoorInstance door = this._doors.get(doorId);
      if (door != null) {
         door.openMe();
      }
   }

   public void closeDoor(int doorId) {
      DoorInstance door = this._doors.get(doorId);
      if (door != null) {
         door.closeMe();
      }
   }

   public long getInstanceEndTime() {
      return this._instanceEndTime;
   }

   public long getInstanceStartTime() {
      return this._instanceStartTime;
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

   public Location getReturnLoc() {
      return this._returnCoords;
   }

   public void setReturnLoc(Location loc) {
      this._returnCoords = loc;
   }

   public int[] getSpawnsLoc() {
      return this._spawnsLoc;
   }

   public void setSpawnsLoc(int[] loc) {
      if (loc != null && loc.length >= 3) {
         System.arraycopy(loc, 0, this._spawnsLoc, 0, 3);
      }
   }

   public void cleanupPlayers() {
      for(Integer objectId : this._players) {
         Player player = World.getInstance().getPlayer(objectId);
         if (player != null && player.getReflectionId() == this.getId()) {
            player.setReflectionId(0);
            if (player.getParty() != null && player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel().isLeader(player)) {
               player.getParty().getCommandChannel().setReflectionId(0);
            }

            if (this.getReturnLoc() != null) {
               player.teleToLocation(this.getReturnLoc(), true);
            } else {
               ThreadPoolManager.getInstance().execute(new TeleportToTownTask(player));
            }
         }
      }

      this._players.clear();
   }

   public void cleanupNpcs() {
      for(Npc mob : this._npcs) {
         if (mob != null) {
            if (mob.getSpawn() != null) {
               mob.getSpawn().stopRespawn();
            }

            mob.deleteMe();
         }
      }

      for(String group : this._spawners.keySet()) {
         this.despawnByGroup(group);
      }

      this._npcs.clear();
      this._manualSpawn.clear();
      this._spawners.clear();
   }

   public void cleanupDoors() {
      for(DoorInstance door : this._doors.valueCollection()) {
         if (door != null) {
            door.decayMe();
         }
      }

      this._doors.clear();
   }

   public List<Npc> spawnGroup(String groupName) {
      List<Npc> ret = null;
      if (this._manualSpawn.containsKey(groupName)) {
         List<Spawner> manualSpawn = this._manualSpawn.get(groupName);
         ret = new ArrayList<>(manualSpawn.size());

         for(Spawner spawnDat : manualSpawn) {
            ret.add(spawnDat.doSpawn());
         }
      } else {
         _log.warning(this.getName() + " instance: cannot spawn NPC's, wrong group name: " + groupName);
      }

      return ret;
   }

   public void loadReflectionTemplate(ReflectionTemplate template) {
      if (template != null) {
         this._name = template.getName();
         if (template.getTimelimit() != 0) {
            this._checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new Reflection.CheckTimeUp(template.getTimelimit() * 60000), 15000L);
            this._instanceEndTime = System.currentTimeMillis() + (long)(template.getTimelimit() * 60000) + 15000L;
         }

         this._ejectTime = template.getRespawnTime();
         this._allowSummon = template.isSummonAllowed();
         this._emptyDestroyTime = (long)(template.getCollapseIfEmpty() * 60000);
         this._showTimer = template.isShowTimer();
         this._isTimerIncrease = template.isTimerIncrease();
         this._timerText = template.getTimerText();
         this._isPvPInstance = template.isPvPInstance();
         this._returnCoords = template.getReturnCoords();
         this._mapx = template.getMapX();
         this._mapy = template.getMapY();
         this._reuseUponEntry = template.getReuseUponEntry();
         this._resetData = template.getReenterData();
         this._params = template.getParams();
         if (this.getMapX() >= 0) {
            int geoIndex = GeoEngine.NextGeoIndex(this.getMapX(), this.getMapY(), this.getId());
            this.setGeoIndex(geoIndex);
         }

         if (template.getDoorList() != null && !template.getDoorList().isEmpty()) {
            for(int doorId : template.getDoorList().keySet()) {
               this.addDoor(doorId, template.getDoorList().get(doorId));
            }
         }

         if (template.getSpawnsInfo() != null && !template.getSpawnsInfo().isEmpty()) {
            for(ReflectionTemplate.SpawnInfo s : template.getSpawnsInfo()) {
               switch(s.getSpawnType()) {
                  case 0:
                     for(Location loc : s.getCoords()) {
                        try {
                           SpawnTemplate tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
                           tpl.addSpawnRange(loc);
                           Spawner c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
                           c.setAmount(s.getCount());
                           c.setSpawnTemplate(tpl);
                           c.setLocation(c.calcSpawnRangeLoc(this.getGeoIndex(), NpcsParser.getInstance().getTemplate(s.getId())));
                           c.setReflectionId(this.getId());
                           c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                           if (s.getRespawnDelay() == 0) {
                              c.stopRespawn();
                           } else {
                              c.startRespawn();
                           }

                           Npc npc = c.spawnOne(true);
                           this.addNpc(npc);
                        } catch (Exception var11) {
                           _log.log(
                              Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var11.getMessage(), (Throwable)var11
                           );
                        }
                     }
                     break;
                  case 1:
                     Location loc = s.getCoords().get(Rnd.get(s.getCoords().size()));

                     try {
                        SpawnTemplate tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
                        tpl.addSpawnRange(loc);
                        Spawner c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
                        c.setAmount(1);
                        c.setSpawnTemplate(tpl);
                        c.setLocation(c.calcSpawnRangeLoc(this.getGeoIndex(), NpcsParser.getInstance().getTemplate(s.getId())));
                        c.setReflectionId(this.getId());
                        c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                        if (s.getRespawnDelay() == 0) {
                           c.stopRespawn();
                        } else {
                           c.startRespawn();
                        }

                        Npc npc = c.spawnOne(true);
                        this.addNpc(npc);
                     } catch (Exception var10) {
                        _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var10.getMessage(), (Throwable)var10);
                     }
                     break;
                  case 2:
                     int totalCount = 0;

                     while(totalCount < s.getCount()) {
                        try {
                           SpawnTemplate tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
                           tpl.addSpawnRange(s.getLoc());
                           Spawner c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
                           c.setAmount(1);
                           c.setSpawnTemplate(tpl);
                           c.setLocation(c.calcSpawnRangeLoc(this.getGeoIndex(), NpcsParser.getInstance().getTemplate(s.getId())));
                           c.setReflectionId(this.getId());
                           c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                           if (s.getRespawnDelay() == 0) {
                              c.stopRespawn();
                           } else {
                              c.startRespawn();
                           }

                           Npc npc = c.spawnOne(true);
                           this.addNpc(npc);
                           ++totalCount;
                        } catch (Exception var9) {
                           _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var9.getMessage(), (Throwable)var9);
                        }
                     }
               }
            }
         }

         if (template.getSpawns().size() > 0) {
            this._spawners = new HashMap<>(template.getSpawns().size());

            for(Entry<String, ReflectionTemplate.SpawnInfo2> entry : template.getSpawns().entrySet()) {
               List<Spawner> spawnList = new ArrayList<>(entry.getValue().getTemplates().size());
               this._spawners.put(entry.getKey(), spawnList);

               for(Spawner c : entry.getValue().getTemplates()) {
                  c.setReflectionId(this.getId());
                  spawnList.add(c);
               }

               if (entry.getValue().isSpawned()) {
                  this.spawnByGroup(entry.getKey());
               }
            }
         }
      }
   }

   public void loadInstanceTemplate(String filename) {
      Document doc = null;
      File xml = new File(Config.DATAPACK_ROOT, "data/instances/" + filename);

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         doc = factory.newDocumentBuilder().parse(xml);

         for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("instance".equalsIgnoreCase(n.getNodeName())) {
               this.parseInstance(n);
            }
         }
      } catch (IOException var6) {
         _log.log(Level.WARNING, "Instance: can not find " + xml.getAbsolutePath() + " ! " + var6.getMessage(), (Throwable)var6);
      } catch (Exception var7) {
         _log.log(Level.WARNING, "Instance: error while loading " + xml.getAbsolutePath() + " ! " + var7.getMessage(), (Throwable)var7);
      }
   }

   private void parseInstance(Node n) throws Exception {
      this._name = n.getAttributes().getNamedItem("name").getNodeValue();
      Node a = n.getAttributes().getNamedItem("ejectTime");
      if (a != null) {
         this._ejectTime = 1000 * Integer.parseInt(a.getNodeValue());
      }

      Node first = n.getFirstChild();

      for(Node var20 = first; var20 != null; var20 = var20.getNextSibling()) {
         if ("activityTime".equalsIgnoreCase(var20.getNodeName())) {
            a = var20.getAttributes().getNamedItem("val");
            if (a != null) {
               this._checkTimeUpTask = ThreadPoolManager.getInstance()
                  .schedule(new Reflection.CheckTimeUp(Integer.parseInt(a.getNodeValue()) * 60000), 15000L);
               this._instanceEndTime = System.currentTimeMillis() + Long.parseLong(a.getNodeValue()) * 60000L + 15000L;
            }
         } else if ("allowSummon".equalsIgnoreCase(var20.getNodeName())) {
            a = var20.getAttributes().getNamedItem("val");
            if (a != null) {
               this.setAllowSummon(Boolean.parseBoolean(a.getNodeValue()));
            }
         } else if ("emptyDestroyTime".equalsIgnoreCase(var20.getNodeName())) {
            a = var20.getAttributes().getNamedItem("val");
            if (a != null) {
               this._emptyDestroyTime = Long.parseLong(a.getNodeValue()) * 1000L;
            }
         } else if ("showTimer".equalsIgnoreCase(var20.getNodeName())) {
            a = var20.getAttributes().getNamedItem("val");
            if (a != null) {
               this._showTimer = Boolean.parseBoolean(a.getNodeValue());
            }

            a = var20.getAttributes().getNamedItem("increase");
            if (a != null) {
               this._isTimerIncrease = Boolean.parseBoolean(a.getNodeValue());
            }

            a = var20.getAttributes().getNamedItem("text");
            if (a != null) {
               this._timerText = a.getNodeValue();
            }
         } else if ("PvPInstance".equalsIgnoreCase(var20.getNodeName())) {
            a = var20.getAttributes().getNamedItem("val");
            if (a != null) {
               this.setPvPInstance(Boolean.parseBoolean(a.getNodeValue()));
            }
         } else if ("geodata".equalsIgnoreCase(var20.getNodeName())) {
            this.setMapX(Integer.parseInt(var20.getAttributes().getNamedItem("mapX").getNodeValue()));
            this.setMapY(Integer.parseInt(var20.getAttributes().getNamedItem("mapY").getNodeValue()));
            if (this.getMapX() >= 0) {
               int geoIndex = GeoEngine.NextGeoIndex(this.getMapX(), this.getMapY(), this.getId());
               this.setGeoIndex(geoIndex);
            }
         } else if ("doorlist".equalsIgnoreCase(var20.getNodeName())) {
            for(Node d = var20.getFirstChild(); d != null; d = d.getNextSibling()) {
               int doorId = 0;
               if ("door".equalsIgnoreCase(d.getNodeName())) {
                  doorId = Integer.parseInt(d.getAttributes().getNamedItem("doorId").getNodeValue());
                  StatsSet set = new StatsSet();

                  for(Node bean = d.getFirstChild(); bean != null; bean = bean.getNextSibling()) {
                     if ("set".equalsIgnoreCase(bean.getNodeName())) {
                        NamedNodeMap attrs = bean.getAttributes();
                        String setname = attrs.getNamedItem("name").getNodeValue();
                        String value = attrs.getNamedItem("val").getNodeValue();
                        set.set(setname, value);
                     }
                  }

                  this.addDoor(doorId, set);
               }
            }
         } else if ("colloseum_fence_list".equalsIgnoreCase(var20.getNodeName())) {
            for(Node group = var20.getFirstChild(); group != null; group = group.getNextSibling()) {
               if ("colosseum_fence".equalsIgnoreCase(group.getNodeName())) {
                  int x = Integer.parseInt(group.getAttributes().getNamedItem("x").getNodeValue());
                  int y = Integer.parseInt(group.getAttributes().getNamedItem("y").getNodeValue());
                  int z = Integer.parseInt(group.getAttributes().getNamedItem("z").getNodeValue());
                  int minz = Integer.parseInt(group.getAttributes().getNamedItem("min_z").getNodeValue());
                  int maxz = Integer.parseInt(group.getAttributes().getNamedItem("max_z").getNodeValue());
                  int width = Integer.parseInt(group.getAttributes().getNamedItem("width").getNodeValue());
                  int height = Integer.parseInt(group.getAttributes().getNamedItem("height").getNodeValue());
                  this.addFence(x, y, z, minz, maxz, width, height, ColosseumFence.FenceState.CLOSED);
               }
            }
         } else if (!"spawnlist".equalsIgnoreCase(var20.getNodeName())) {
            if ("spawnpoint".equalsIgnoreCase(var20.getNodeName())) {
               try {
                  int x = Integer.parseInt(var20.getAttributes().getNamedItem("spawnX").getNodeValue());
                  int y = Integer.parseInt(var20.getAttributes().getNamedItem("spawnY").getNodeValue());
                  int z = Integer.parseInt(var20.getAttributes().getNamedItem("spawnZ").getNodeValue());
                  this._returnCoords = new Location(x, y, z);
               } catch (Exception var19) {
                  _log.log(Level.WARNING, "Error parsing instance xml: " + var19.getMessage(), (Throwable)var19);
                  this._returnCoords = null;
               }
            }
         } else {
            for(Node group = var20.getFirstChild(); group != null; group = group.getNextSibling()) {
               if ("group".equalsIgnoreCase(group.getNodeName())) {
                  String spawnGroup = group.getAttributes().getNamedItem("name").getNodeValue();
                  List<Spawner> manualSpawn = new ArrayList<>();

                  for(Node d = group.getFirstChild(); d != null; d = d.getNextSibling()) {
                     int npcId = 0;
                     int x = 0;
                     int y = 0;
                     int z = 0;
                     int heading = 0;
                     int respawn = 0;
                     int respawnRandom = 0;
                     int delay = -1;
                     if ("spawn".equalsIgnoreCase(d.getNodeName())) {
                        npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
                        x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
                        y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
                        z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
                        heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
                        respawn = Integer.parseInt(d.getAttributes().getNamedItem("respawn").getNodeValue());
                        if (d.getAttributes().getNamedItem("onKillDelay") != null) {
                           delay = Integer.parseInt(d.getAttributes().getNamedItem("onKillDelay").getNodeValue());
                        }

                        if (d.getAttributes().getNamedItem("respawnRandom") != null) {
                           respawnRandom = Integer.parseInt(d.getAttributes().getNamedItem("respawnRandom").getNodeValue());
                        }

                        NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
                        if (npcTemplate != null) {
                           Spawner spawnDat = new Spawner(npcTemplate);
                           spawnDat.setX(x);
                           spawnDat.setY(y);
                           spawnDat.setZ(z);
                           spawnDat.setAmount(1);
                           spawnDat.setHeading(heading);
                           spawnDat.setRespawnDelay(respawn, respawnRandom);
                           if (respawn == 0) {
                              spawnDat.stopRespawn();
                           } else {
                              spawnDat.startRespawn();
                           }

                           spawnDat.setReflectionId(this.getId());
                           if (spawnGroup.equals("general")) {
                              Npc spawned = spawnDat.doSpawn();
                              if (delay >= 0 && spawned instanceof Attackable) {
                                 ((Attackable)spawned).setOnKillDelay(delay);
                              }
                           } else {
                              manualSpawn.add(spawnDat);
                           }
                        } else {
                           _log.warning("Instance: Data missing in NPC table for ID: " + npcId + " in Instance " + this.getId());
                        }
                     }
                  }

                  if (!manualSpawn.isEmpty()) {
                     this._manualSpawn.put(spawnGroup, manualSpawn);
                  }
               }
            }
         }
      }
   }

   protected void doCheckTimeUp(int remaining) {
      CreatureSay cs = null;
      int interval;
      if (this._players.isEmpty() && this._emptyDestroyTime == 0L) {
         remaining = 0;
         interval = 500;
      } else if (this._players.isEmpty() && this._emptyDestroyTime > 0L) {
         Long emptyTimeLeft = this._lastLeft + this._emptyDestroyTime - System.currentTimeMillis();
         if (emptyTimeLeft <= 0L) {
            interval = 0;
            remaining = 0;
         } else if (remaining > 300000 && emptyTimeLeft > 300000L) {
            interval = 300000;
            remaining -= 300000;
         } else if (remaining > 60000 && emptyTimeLeft > 60000L) {
            interval = 60000;
            remaining -= 60000;
         } else if (remaining > 30000 && emptyTimeLeft > 30000L) {
            interval = 30000;
            remaining -= 30000;
         } else {
            interval = 10000;
            remaining -= 10000;
         }
      } else if (remaining > 300000) {
         int timeLeft = remaining / 60000;
         interval = 300000;
         if (!this._disableMessages) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
            sm.addString(Integer.toString(timeLeft));
            Announcements.getInstance().announceToInstance(sm, this.getId());
         }

         remaining -= 300000;
      } else if (remaining > 60000) {
         int timeLeft = remaining / 60000;
         interval = 60000;
         if (!this._disableMessages) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
            sm.addString(Integer.toString(timeLeft));
            Announcements.getInstance().announceToInstance(sm, this.getId());
         }

         remaining -= 60000;
      } else if (remaining > 30000) {
         int timeLeft = remaining / 1000;
         interval = 30000;
         if (!this._disableMessages) {
            cs = new CreatureSay(0, 9, "Notice", timeLeft + " seconds left.");
         }

         remaining -= 30000;
      } else {
         int timeLeft = remaining / 1000;
         interval = 10000;
         if (!this._disableMessages) {
            cs = new CreatureSay(0, 9, "Notice", timeLeft + " seconds left.");
         }

         remaining -= 10000;
      }

      if (cs != null) {
         for(Integer objectId : this._players) {
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null && player.getReflectionId() == this.getId()) {
               player.sendPacket(cs);
            }
         }
      }

      this.cancelTimer();
      if (remaining >= 10000) {
         this._checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new Reflection.CheckTimeUp(remaining), (long)interval);
      } else {
         this._checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new Reflection.TimeUp(), (long)interval);
      }
   }

   public void cancelTimer() {
      if (this._checkTimeUpTask != null) {
         this._checkTimeUpTask.cancel(true);
      }
   }

   public void cancelEjectDeadPlayer(Player player) {
      if (this._ejectDeadTasks.containsKey(player.getObjectId())) {
         ScheduledFuture<?> task = this._ejectDeadTasks.remove(player.getObjectId());
         if (task != null) {
            task.cancel(true);
         }
      }
   }

   public void addEjectDeadTask(Player player) {
      if (player != null) {
         this._ejectDeadTasks.put(player.getObjectId(), ThreadPoolManager.getInstance().schedule(new Reflection.EjectPlayer(player), (long)this._ejectTime));
      }
   }

   public final void notifyDeath(Creature killer, Creature victim) {
      ReflectionWorld instance = ReflectionManager.getInstance().getPlayerWorld(victim.getActingPlayer());
      if (instance != null) {
         instance.onDeath(killer, victim);
      }
   }

   public void disableMessages() {
      this._disableMessages = true;
   }

   public int getGeoIndex() {
      return this._geoIndex;
   }

   public void setGeoIndex(int geoIndex) {
      this._geoIndex = geoIndex;
   }

   public int getMapX() {
      return this._mapx;
   }

   public int getMapY() {
      return this._mapy;
   }

   public void setMapX(int x) {
      this._mapx = x;
   }

   public void setMapY(int y) {
      this._mapy = y;
   }

   public Npc getNpc(int id) {
      for(Npc mob : this._npcs) {
         if (mob != null && mob.getId() == id) {
            return mob;
         }
      }

      return null;
   }

   public void spawnByGroup(String name) {
      List<Spawner> list = this._spawners.get(name);
      if (list != null) {
         for(Spawner s : list) {
            Npc npc = s.spawnOne(true);
            this.addNpc(npc);
         }
      }
   }

   public void despawnByGroup(String name) {
      List<Spawner> list = this._spawners.get(name);
      if (list != null) {
         for(Spawner s : list) {
            s.stopRespawn();
            if (s.getLastSpawn() != null) {
               s.getLastSpawn().deleteMe();
            }
         }
      }
   }

   public ColosseumFence addFence(int x, int y, int z, int minZ, int maxZ, int width, int height, ColosseumFence.FenceState state) {
      ColosseumFence newFence = new ColosseumFence(this.getId(), x, y, z, minZ, maxZ, width, height, state);
      newFence.spawnMe();
      this._fences.put(newFence.getObjectId(), newFence);
      return newFence;
   }

   public Collection<ColosseumFence> getFences() {
      return this._fences.values();
   }

   public void cleanupFences() {
      for(ColosseumFence fence : this._fences.values()) {
         if (fence != null) {
            fence.decayMe();
         }
      }

      this._fences.clear();
   }

   public void addItem(ItemInstance item) {
      this._items.add(item);
   }

   public void removeItem(ItemInstance item) {
      this._items.remove(item);
   }

   public void cleanupItems() {
      for(ItemInstance item : this._items) {
         if (item != null) {
            item.decayMe();
         }
      }

      this._items.clear();
   }

   public boolean getReuseUponEntry() {
      return this._reuseUponEntry;
   }

   public List<ReflectionReenterTimeHolder> getReenterData() {
      return this._resetData;
   }

   public StatsSet getParams() {
      return this._params;
   }

   public void removeZone(int id) {
      if (this._zones.contains(id)) {
         this._zones.remove(this._zones.indexOf(id));
      }
   }

   public void addZone(int id) {
      if (!this._zones.contains(id)) {
         this._zones.add(id);
      }
   }

   public void cleanupZones() {
      if (!this._zones.isEmpty()) {
         for(int zoneId : this._zones) {
            ReflectionZone zone = ZoneManager.getInstance().getZoneById(zoneId, ReflectionZone.class);
            if (zone != null) {
               zone.removeRef(this.getId());
            }
         }
      }
   }

   public class CheckTimeUp implements Runnable {
      private final int _remaining;

      public CheckTimeUp(int remaining) {
         this._remaining = remaining;
      }

      @Override
      public void run() {
         Reflection.this.doCheckTimeUp(this._remaining);
      }
   }

   protected class EjectPlayer implements Runnable {
      private final Player _player;

      public EjectPlayer(Player player) {
         this._player = player;
      }

      @Override
      public void run() {
         if (this._player != null && this._player.isDead() && this._player.getReflectionId() == Reflection.this.getId()) {
            this._player.setReflectionId(0);
            if (Reflection.this.getReturnLoc() != null) {
               this._player.teleToLocation(Reflection.this.getReturnLoc(), true);
            } else {
               this._player.teleToLocation(TeleportWhereType.TOWN, true);
            }
         }
      }
   }

   public class TimeUp implements Runnable {
      @Override
      public void run() {
         ReflectionManager.getInstance().destroyReflection(Reflection.this.getId());
      }
   }
}
