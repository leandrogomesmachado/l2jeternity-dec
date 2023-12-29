package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.geometry.Polygon;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.BloodAltarManager;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.SpawnNpcInfo;
import l2e.gameserver.model.spawn.SpawnTemplate;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class SpawnParser extends DocumentParser {
   private final Set<Spawner> _spawnParser = ConcurrentHashMap.newKeySet();
   private final Map<String, List<Spawner>> _spawns = new ConcurrentHashMap<>();
   private final Map<Integer, Integer> _spawnCountByNpcId = new HashMap<>();
   private final Map<Integer, List<Location>> _spawnLocationsByNpcId = new HashMap<>();

   protected SpawnParser() {
      if (!Config.ALT_DEV_NO_SPAWNS) {
         this.load();
      }
   }

   @Override
   public final void load() {
      this._spawnParser.clear();
      this._spawns.clear();
      this._spawnCountByNpcId.clear();
      this._spawnLocationsByNpcId.clear();
      this.parseDirectory("data/stats/npcs/spawns", false);
      if (Config.CUSTOM_SPAWNLIST) {
         this.parseDirectory("data/stats/npcs/spawns/custom", false);
      }

      if (this.size() > 0) {
         this._log.info(this.getClass().getSimpleName() + ": Loaded " + this.size() + " npc spawn templates.");
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Map<String, SpawnTerritory> territories = new HashMap<>();

      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node n = c.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("territory".equalsIgnoreCase(n.getNodeName())) {
                  NamedNodeMap list = n.getAttributes();
                  String terName = list.getNamedItem("name").getNodeValue();
                  SpawnTerritory territory = this.parseTerritory(terName, n, list);
                  territories.put(terName, territory);
               } else if ("spawn".equalsIgnoreCase(n.getNodeName())) {
                  NamedNodeMap list = n.getAttributes();
                  int count = list.getNamedItem("count") == null ? 1 : Integer.parseInt(list.getNamedItem("count").getNodeValue());
                  int respawn = list.getNamedItem("respawn") == null ? 60 : Integer.parseInt(list.getNamedItem("respawn").getNodeValue());
                  int respawnRandom = list.getNamedItem("respawn_random") == null ? 0 : Integer.parseInt(list.getNamedItem("respawn_random").getNodeValue());
                  String respawnPattern = list.getNamedItem("respawn_pattern") == null ? null : list.getNamedItem("respawn_pattern").getNodeValue();
                  String periodOfDay = list.getNamedItem("period_of_day") == null ? "none" : list.getNamedItem("period_of_day").getNodeValue();
                  String territoryName = "";
                  boolean spawned;
                  String group;
                  if (list.getNamedItem("group") == null) {
                     group = periodOfDay;
                     spawned = true;
                  } else {
                     group = list.getNamedItem("group").getNodeValue();
                     spawned = false;
                  }

                  SpawnTemplate template = new SpawnTemplate(periodOfDay, count, respawn, respawnRandom);
                  int npcId = 0;
                  int x = 0;
                  int y = 0;
                  int z = 0;
                  int h = -1;

                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("debug".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap debug = d.getAttributes();
                        if (debug.getNamedItem("name") != null) {
                           territoryName = debug.getNamedItem("val") == null ? "No Name" : debug.getNamedItem("val").getNodeValue();
                        }
                     } else if ("point".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap point = d.getAttributes();
                        x = Integer.parseInt(point.getNamedItem("x").getNodeValue());
                        y = Integer.parseInt(point.getNamedItem("y").getNodeValue());
                        z = Integer.parseInt(point.getNamedItem("z").getNodeValue());
                        h = point.getNamedItem("h") == null ? -1 : Integer.parseInt(point.getNamedItem("h").getNodeValue());
                        template.addSpawnRange(new Location(x, y, z, h));
                     } else if ("territory".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap territory = d.getAttributes();
                        String terName = territory.getNamedItem("name") == null ? null : territory.getNamedItem("name").getNodeValue();
                        if (terName != null) {
                           SpawnTerritory g = territories.get(terName);
                           if (g == null) {
                              this._log.warning("Invalid territory name: " + terName + "; " + this.getClass().getSimpleName());
                           } else {
                              template.addSpawnRange(g);
                           }
                        } else {
                           SpawnTerritory temp = this.parseTerritory(null, d, territory);
                           template.addSpawnRange(temp);
                        }
                     } else if ("npc".equalsIgnoreCase(d.getNodeName())) {
                        npcId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                        int amount = d.getAttributes().getNamedItem("count") == null
                           ? count
                           : Integer.parseInt(d.getAttributes().getNamedItem("count").getNodeValue());
                        int perRespawn = d.getAttributes().getNamedItem("respawn") == null
                           ? respawn
                           : Integer.parseInt(d.getAttributes().getNamedItem("respawn").getNodeValue());
                        int perRespawnRandom = d.getAttributes().getNamedItem("respawn_random") == null
                           ? respawnRandom
                           : Integer.parseInt(d.getAttributes().getNamedItem("respawn_random").getNodeValue());
                        String perRespawnPattern = d.getAttributes().getNamedItem("respawn_pattern") == null
                           ? respawnPattern
                           : d.getAttributes().getNamedItem("respawn_pattern").getNodeValue();
                        int max = d.getAttributes().getNamedItem("max") == null ? 0 : Integer.parseInt(d.getAttributes().getNamedItem("max").getNodeValue());
                        String name = d.getAttributes().getNamedItem("name") == null ? null : d.getAttributes().getNamedItem("name").getNodeValue();
                        String value = d.getAttributes().getNamedItem("value") == null ? null : d.getAttributes().getNamedItem("value").getNodeValue();
                        MultiValueSet<String> parameters = StatsSet.EMPTY;

                        for(Node e = d.getFirstChild(); e != null; e = e.getNextSibling()) {
                           if (parameters.isEmpty()) {
                              parameters = new MultiValueSet<>();
                           }

                           parameters.set(name, value);
                        }

                        template.addNpc(new SpawnNpcInfo(npcId, max, parameters));
                        this.parseNpcSpawn(
                           npcId, amount, group, template, territoryName, perRespawn, perRespawnRandom, perRespawnPattern, periodOfDay, spawned
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private void parseNpcSpawn(
      int npcId,
      int count,
      String group,
      SpawnTemplate temp,
      String territoryName,
      int respawn,
      int respawnRandom,
      String respawnPattern,
      String periodOfDay,
      boolean spawned
   ) {
      int totalCount = 0;

      while(totalCount < count) {
         NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
         if (npcTemplate != null && !npcTemplate.isType("SiegeGuard")) {
            if (!ClassMasterParser.getInstance().isAllowClassMaster() && npcTemplate.isType("ClassMaster")
               || Config.ALT_CHEST_NO_SPAWNS && npcTemplate.isType("TreasureChest")) {
               break;
            }

            try {
               Spawner spawnDat = new Spawner(npcTemplate);
               spawnDat.setAmount(1);
               spawnDat.setTerritoryName(territoryName);
               spawnDat.setSpawnTemplate(temp);
               spawnDat.setLocation(spawnDat.calcSpawnRangeLoc(0, npcTemplate));
               int currentCount = this._spawnCountByNpcId.containsKey(npcTemplate.getId()) ? this._spawnCountByNpcId.get(npcTemplate.getId()) : 0;
               this._spawnCountByNpcId.put(npcTemplate.getId(), currentCount + 1);
               spawnDat.setRespawnPattern(respawnPattern != null && !respawnPattern.isEmpty() ? new SchedulingPattern(respawnPattern) : null);
               spawnDat.setRespawnDelay(respawn, respawnRandom);
               if (npcTemplate.isType("RaidBoss") || npcTemplate.isType("FlyRaidBoss")) {
                  RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, false);
                  spawned = false;
                  if (Config.DEBUG_SPAWN) {
                     Npc npc = spawnDat.getLastSpawn();
                     if (npc != null && !npc.isInRangeZ(npc.getSpawn().getLocation(), (long)Config.MAX_DRIFT_RANGE)) {
                        this._log
                           .warning(
                              this.getClass().getSimpleName()
                                 + ": npcId["
                                 + npc.getId()
                                 + "] z coords bug! ["
                                 + npc.getZ()
                                 + "] != ["
                                 + spawnDat.getZ()
                                 + "] - ["
                                 + spawnDat.getX()
                                 + " "
                                 + spawnDat.getY()
                                 + " "
                                 + spawnDat.getZ()
                                 + "]"
                           );
                     }
                  }
               } else if (respawn == 0) {
                  spawnDat.stopRespawn();
               } else {
                  spawnDat.startRespawn();
               }

               Location spawnLoc = spawnDat.calcSpawnRangeLoc(spawnDat.getGeoIndex(), npcTemplate);
               if (!this._spawnLocationsByNpcId.containsKey(npcTemplate.getId())) {
                  this._spawnLocationsByNpcId.put(npcTemplate.getId(), new ArrayList<>());
               }

               this._spawnLocationsByNpcId.get(npcTemplate.getId()).add(spawnLoc);
               ++totalCount;
               switch(periodOfDay) {
                  case "none":
                     if (spawned) {
                        spawnDat.doSpawn();
                        if (Config.DEBUG_SPAWN) {
                           Npc npc = spawnDat.getLastSpawn();
                           if (npc != null && npc.isMonster() && !npc.isInRangeZ(npc.getSpawn().getLocation(), (long)Config.MAX_DRIFT_RANGE)) {
                              this._log
                                 .warning(
                                    this.getClass().getSimpleName()
                                       + ": npcId["
                                       + npc.getId()
                                       + "] z coords bug! ["
                                       + npc.getZ()
                                       + "] != ["
                                       + spawnDat.getZ()
                                       + "] - ["
                                       + spawnDat.getX()
                                       + " "
                                       + spawnDat.getY()
                                       + " "
                                       + spawnDat.getZ()
                                       + "]"
                                 );
                           }
                        }
                     }
                     break;
                  case "day":
                     if (spawned) {
                        DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                     }
                     break;
                  case "night":
                     if (spawned) {
                        DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                     }
               }

               this.addSpawn(group, spawnDat);
               this.addNewSpawn(spawnDat);
            } catch (Exception var19) {
               this._log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var19.getMessage(), (Throwable)var19);
            }
         }
      }
   }

   private SpawnTerritory parseTerritory(String name, Node n, NamedNodeMap attrs) {
      SpawnTerritory t = new SpawnTerritory();
      t.add(this.parsePolygon0(name, n, attrs));

      for(Node b = n.getFirstChild(); b != null; b = b.getNextSibling()) {
         if ("banned_territory".equalsIgnoreCase(b.getNodeName())) {
            t.addBanned(this.parsePolygon0(name, b, b.getAttributes()));
         }
      }

      return t;
   }

   private Polygon parsePolygon0(String name, Node n, NamedNodeMap attrs) {
      Polygon temp = new Polygon();

      for(Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("add".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
            int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
            int zmin = Integer.parseInt(attrs.getNamedItem("zmin").getNodeValue());
            int zmax = Integer.parseInt(attrs.getNamedItem("zmax").getNodeValue());
            temp.add(x, y).setZmin(zmin).setZmax(zmax);
         }
      }

      if (!temp.validate()) {
         this._log.warning("Invalid polygon: " + name + "{" + temp + "}. File: " + this.getClass().getSimpleName());
      }

      return temp;
   }

   public void reloadAll() {
      this.load();
   }

   public void addSpawn(String group, Spawner spawn) {
      List<Spawner> spawns = this._spawns.get(group);
      if (spawns == null) {
         this._spawns.put(group, spawns = new ArrayList<>());
      }

      spawns.add(spawn);
   }

   public List<Spawner> getSpawn(String name) {
      List<Spawner> template = this._spawns.get(name);
      return template == null ? Collections.emptyList() : template;
   }

   public int size() {
      int i = 0;

      for(List<?> l : this._spawns.values()) {
         i += l.size();
      }

      return i;
   }

   public Map<String, List<Spawner>> getSpawns() {
      return this._spawns;
   }

   public void addNewSpawn(Spawner spawn) {
      if (!this._spawnParser.contains(spawn)) {
         this._spawnParser.add(spawn);
      }
   }

   public void deleteSpawn(Spawner spawn) {
      if (this._spawnParser.remove(spawn)) {
         ;
      }
   }

   public Collection<Spawner> getAllSpawns() {
      return this._spawnParser;
   }

   public Set<Spawner> getSpawnData() {
      return this._spawnParser;
   }

   public void spawnGroup(String group) {
      List<Spawner> spawnerList = this._spawns.get(group);
      if (spawnerList != null) {
         int npcSpawnCount = 0;

         for(Spawner spawner : spawnerList) {
            if (spawner.getTemplate().getParameter("isDestructionBoss", false)) {
               BloodAltarManager.getInstance().addBossSpawn(spawner);
            } else {
               npcSpawnCount += spawner.init();
            }

            if (npcSpawnCount % 1000 == 0 && npcSpawnCount != 0 && Config.DEBUG) {
               this._log.info(this.getClass().getSimpleName() + ": Spawned " + npcSpawnCount + " npcs for group: " + group);
            }
         }

         if (Config.DEBUG) {
            this._log.info(this.getClass().getSimpleName() + ": Spawned " + npcSpawnCount + " npcs for group: " + group);
         }
      }
   }

   public void spawnCkeckGroup(String group, List<Integer> npcId) {
      List<Spawner> spawnerList = this._spawns.get(group);
      if (spawnerList != null) {
         int npcSpawnCount = 0;

         for(Spawner spawner : spawnerList) {
            if (npcId == null || !npcId.contains(spawner.getId())) {
               if (spawner.getTemplate().getParameter("isDestructionBoss", false)) {
                  BloodAltarManager.getInstance().addBossSpawn(spawner);
               } else {
                  npcSpawnCount += spawner.init();
               }

               if (npcSpawnCount % 1000 == 0 && npcSpawnCount != 0 && Config.DEBUG) {
                  this._log.info(this.getClass().getSimpleName() + ": Spawned " + npcSpawnCount + " npcs for group: " + group);
               }
            }
         }

         if (Config.DEBUG) {
            this._log.info(this.getClass().getSimpleName() + ": Spawned " + npcSpawnCount + " npcs for group: " + group);
         }
      }
   }

   public void despawnGroup(String group) {
      List<Spawner> spawnerList = this._spawns.get(group);
      if (spawnerList != null) {
         int npcDespawnSpawn = 0;

         for(Spawner spawner : spawnerList) {
            if (spawner.getTemplate().getParameter("isDestructionBoss", false)) {
               BloodAltarManager.getInstance().removeBossSpawn(spawner);
            }

            spawner.stopRespawn();
            Npc last = spawner.getLastSpawn();
            if (last != null) {
               ++npcDespawnSpawn;
               last.deleteMe();
            }
         }

         if (npcDespawnSpawn != 0 && Config.DEBUG) {
            this._log.info(this.getClass().getSimpleName() + ": Despawned " + npcDespawnSpawn + " npcs for group: " + group);
         }
      }
   }

   public int getSpawnedCountByNpc(int npcId) {
      return !this._spawnCountByNpcId.containsKey(npcId) ? 0 : this._spawnCountByNpcId.get(npcId);
   }

   public List<Location> getRandomSpawnsByNpc(int npcId) {
      return this._spawnLocationsByNpcId.get(npcId);
   }

   public void addRandomSpawnByNpc(Spawner spawnDat, NpcTemplate npcTemplate) {
      int currentCount = this._spawnCountByNpcId.containsKey(npcTemplate.getId()) ? this._spawnCountByNpcId.get(npcTemplate.getId()) : 0;
      this._spawnCountByNpcId.put(npcTemplate.getId(), currentCount + 1);
      Location spawnLoc = spawnDat.calcSpawnRangeLoc(spawnDat.getGeoIndex(), npcTemplate);
      if (!this._spawnLocationsByNpcId.containsKey(npcTemplate.getId())) {
         this._spawnLocationsByNpcId.put(npcTemplate.getId(), new ArrayList<>());
      }

      this._spawnLocationsByNpcId.get(npcTemplate.getId()).add(spawnLoc);
   }

   public void removeRandomSpawnByNpc(Npc npc) {
      int currentCount = this._spawnCountByNpcId.containsKey(npc.getId()) ? this._spawnCountByNpcId.get(npc.getId()) : 0;
      if (currentCount > 0) {
         this._spawnCountByNpcId.put(npc.getId(), currentCount - 1);
      } else {
         this._spawnCountByNpcId.remove(npc.getId());
      }

      Location spawnLoc = npc.getSpawn().calcSpawnRangeLoc(npc.getSpawn().getGeoIndex(), npc.getTemplate());
      if (this._spawnLocationsByNpcId.containsKey(npc.getId())) {
         this._spawnLocationsByNpcId.get(npc.getId()).remove(spawnLoc);
      }
   }

   public static SpawnParser getInstance() {
      return SpawnParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SpawnParser _instance = new SpawnParser();
   }
}
