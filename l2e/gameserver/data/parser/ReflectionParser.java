package l2e.gameserver.data.parser;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.geometry.Polygon;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.holders.ReflectionReenterTimeHolder;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ReflectionParser extends DocumentParser {
   private final IntObjectMap<ReflectionTemplate> _reflections = new HashIntObjectMap<>();

   protected ReflectionParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this._reflections.clear();
      this.parseDirectory("data/stats/instances", false);
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._reflections.size() + " reflection templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("instance".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap ref = d.getAttributes();
                  boolean allowSummon = true;
                  boolean showTimer = false;
                  boolean isTimerIncrease = true;
                  boolean isPvPInstance = false;
                  String text = "";
                  String requiredQuest = null;
                  Map<Integer, StatsSet> doors = new ConcurrentHashMap<>();
                  int mapX = -1;
                  int mapY = -1;
                  boolean removedItemNecessity = false;
                  boolean setReuseUponEntry = false;
                  int removedItemId = 0;
                  int removedItemCount = 0;
                  int giveItemId = 0;
                  int givedItemCount = 0;
                  int sharedReuseGroup = 0;
                  int minLevel = 0;
                  int maxLevel = 0;
                  int minParty = 1;
                  int maxParty = 9;
                  List<Location> teleportLocs = Collections.emptyList();
                  Location ret = null;
                  int spawnType = 0;
                  int count = 0;
                  ReflectionTemplate.SpawnInfo spawnDat = null;
                  List<ReflectionTemplate.SpawnInfo> spawns = new ArrayList<>();
                  Map<String, ReflectionTemplate.SpawnInfo2> spawns2 = Collections.emptyMap();
                  List<ReflectionReenterTimeHolder> resetData = new ArrayList<>();
                  ReflectionTemplate.ReflectionRemoveType removeType = null;
                  ReflectionTemplate.ReflectionQuestType questType = null;
                  int id = Integer.parseInt(ref.getNamedItem("id").getNodeValue());
                  String name = ref.getNamedItem("name").getNodeValue();
                  int maxChannels = Integer.parseInt(ref.getNamedItem("maxChannels").getNodeValue());
                  int collapseIfEmpty = Integer.parseInt(ref.getNamedItem("collapseIfEmpty").getNodeValue());
                  int timelimit = Integer.parseInt(ref.getNamedItem("timelimit").getNodeValue());
                  boolean dispelBuffs = ref.getNamedItem("dispelBuffs") != null ? Boolean.parseBoolean(ref.getNamedItem("dispelBuffs").getNodeValue()) : false;
                  int respawnTime = ref.getNamedItem("respawn") != null
                     ? Integer.parseInt(ref.getNamedItem("respawn").getNodeValue()) * 1000
                     : Config.EJECT_DEAD_PLAYER_TIME;
                  StatsSet params = new StatsSet();

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     ref = cd.getAttributes();
                     if ("level".equalsIgnoreCase(cd.getNodeName())) {
                        minLevel = ref.getNamedItem("min") == null ? 1 : Integer.parseInt(ref.getNamedItem("min").getNodeValue());
                        maxLevel = ref.getNamedItem("max") == null ? Integer.MAX_VALUE : Integer.parseInt(ref.getNamedItem("max").getNodeValue());
                     } else if ("party".equalsIgnoreCase(cd.getNodeName())) {
                        minParty = Integer.parseInt(ref.getNamedItem("min").getNodeValue());
                        maxParty = Integer.parseInt(ref.getNamedItem("max").getNodeValue());
                     } else if ("return".equalsIgnoreCase(cd.getNodeName())) {
                        int x = Integer.parseInt(ref.getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(ref.getNamedItem("y").getNodeValue());
                        int z = Integer.parseInt(ref.getNamedItem("z").getNodeValue());
                        ret = new Location(x, y, z);
                     } else if ("teleport".equalsIgnoreCase(cd.getNodeName())) {
                        if (teleportLocs.isEmpty()) {
                           teleportLocs = new ArrayList<>(1);
                        }

                        int x = Integer.parseInt(ref.getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(ref.getNamedItem("y").getNodeValue());
                        int z = Integer.parseInt(ref.getNamedItem("z").getNodeValue());
                        teleportLocs.add(new Location(x, y, z));
                     } else if ("remove".equalsIgnoreCase(cd.getNodeName())) {
                        removedItemId = Integer.parseInt(ref.getNamedItem("itemId").getNodeValue());
                        removedItemCount = Integer.parseInt(ref.getNamedItem("count").getNodeValue());
                        removedItemNecessity = Boolean.parseBoolean(ref.getNamedItem("necessary").getNodeValue());
                        removeType = ref.getNamedItem("type") != null
                           ? ReflectionTemplate.ReflectionRemoveType.valueOf(ref.getNamedItem("type").getNodeValue())
                           : ReflectionTemplate.ReflectionRemoveType.NONE;
                     } else if ("give".equalsIgnoreCase(cd.getNodeName())) {
                        giveItemId = Integer.parseInt(ref.getNamedItem("itemId").getNodeValue());
                        givedItemCount = Integer.parseInt(ref.getNamedItem("count").getNodeValue());
                     } else if ("quest".equalsIgnoreCase(cd.getNodeName())) {
                        requiredQuest = ref.getNamedItem("name") != null ? ref.getNamedItem("name").getNodeValue() : null;
                        questType = ref.getNamedItem("type") != null
                           ? ReflectionTemplate.ReflectionQuestType.valueOf(ref.getNamedItem("type").getNodeValue())
                           : ReflectionTemplate.ReflectionQuestType.STARTED;
                     } else if ("allowSummon".equalsIgnoreCase(cd.getNodeName())) {
                        allowSummon = ref.getNamedItem("val") != null ? Boolean.parseBoolean(ref.getNamedItem("val").getNodeValue()) : false;
                     } else if ("showTimer".equalsIgnoreCase(cd.getNodeName())) {
                        showTimer = ref.getNamedItem("val") != null ? Boolean.parseBoolean(ref.getNamedItem("val").getNodeValue()) : false;
                        isTimerIncrease = ref.getNamedItem("increase") != null ? Boolean.parseBoolean(ref.getNamedItem("increase").getNodeValue()) : false;
                        text = ref.getNamedItem("text") != null ? ref.getNamedItem("text").getNodeValue() : "";
                     } else if ("isPvP".equalsIgnoreCase(cd.getNodeName())) {
                        isPvPInstance = ref.getNamedItem("val") != null ? Boolean.parseBoolean(ref.getNamedItem("val").getNodeValue()) : false;
                     } else if ("doorlist".equalsIgnoreCase(cd.getNodeName())) {
                        for(Node door = cd.getFirstChild(); door != null; door = door.getNextSibling()) {
                           int doorId = 0;
                           if ("door".equalsIgnoreCase(door.getNodeName())) {
                              doorId = Integer.parseInt(door.getAttributes().getNamedItem("doorId").getNodeValue());
                              StatsSet set = new StatsSet();

                              for(Node bean = door.getFirstChild(); bean != null; bean = bean.getNextSibling()) {
                                 if ("set".equalsIgnoreCase(bean.getNodeName())) {
                                    NamedNodeMap attrs = bean.getAttributes();
                                    String setname = attrs.getNamedItem("name").getNodeValue();
                                    String value = attrs.getNamedItem("val").getNodeValue();
                                    set.set(setname, value);
                                 }
                              }

                              doors.put(doorId, set);
                           }
                        }
                     } else if ("geodata".equalsIgnoreCase(cd.getNodeName())) {
                        mapX = Integer.parseInt(ref.getNamedItem("mapX").getNodeValue());
                        mapY = Integer.parseInt(ref.getNamedItem("mapY").getNodeValue());
                     } else if ("reenter".equalsIgnoreCase(cd.getNodeName())) {
                        setReuseUponEntry = ref.getNamedItem("setUponEntry") != null
                           ? Boolean.parseBoolean(ref.getNamedItem("setUponEntry").getNodeValue())
                           : false;
                        sharedReuseGroup = ref.getNamedItem("sharedReuseGroup") != null
                           ? Integer.parseInt(ref.getNamedItem("sharedReuseGroup").getNodeValue())
                           : 0;

                        for(Node rt = cd.getFirstChild(); rt != null; rt = rt.getNextSibling()) {
                           if ("reset".equalsIgnoreCase(rt.getNodeName())) {
                              long time = rt.getAttributes().getNamedItem("time") != null
                                 ? Long.parseLong(rt.getAttributes().getNamedItem("time").getNodeValue())
                                 : -1L;
                              if (time > 0L) {
                                 resetData.add(new ReflectionReenterTimeHolder(time));
                              } else if (time == -1L) {
                                 DayOfWeek day = rt.getAttributes().getNamedItem("day") != null
                                    ? DayOfWeek.valueOf(rt.getAttributes().getNamedItem("day").getNodeValue().toUpperCase())
                                    : null;
                                 int hour = rt.getAttributes().getNamedItem("hour") != null
                                    ? Integer.parseInt(rt.getAttributes().getNamedItem("hour").getNodeValue())
                                    : -1;
                                 int minute = rt.getAttributes().getNamedItem("minute") != null
                                    ? Integer.parseInt(rt.getAttributes().getNamedItem("minute").getNodeValue())
                                    : -1;
                                 resetData.add(new ReflectionReenterTimeHolder(day, hour, minute));
                              }
                           }
                        }
                     } else if ("add_parameters".equalsIgnoreCase(cd.getNodeName())) {
                        for(Node sp = cd.getFirstChild(); sp != null; sp = sp.getNextSibling()) {
                           if ("set".equalsIgnoreCase(sp.getNodeName())) {
                              params.set(sp.getAttributes().getNamedItem("name").getNodeValue(), sp.getAttributes().getNamedItem("value").getNodeValue());
                           }
                        }
                     } else if ("spawns".equalsIgnoreCase(cd.getNodeName())) {
                        for(Node sp = cd.getFirstChild(); sp != null; sp = sp.getNextSibling()) {
                           if ("group".equalsIgnoreCase(sp.getNodeName())) {
                              String group = sp.getAttributes().getNamedItem("name").getNodeValue();
                              boolean spawned = sp.getAttributes().getNamedItem("spawned") != null
                                 && Boolean.parseBoolean(sp.getAttributes().getNamedItem("spawned").getNodeValue());
                              List<Spawner> templates = SpawnParser.getInstance().getSpawn(group);
                              if (templates != null) {
                                 if (spawns2.isEmpty()) {
                                    spawns2 = new Hashtable<>();
                                 }

                                 spawns2.put(group, new ReflectionTemplate.SpawnInfo2(templates, spawned));
                              }
                           } else if ("spawn".equalsIgnoreCase(sp.getNodeName())) {
                              String[] mobs = sp.getAttributes().getNamedItem("mobId").getNodeValue().split(" ");
                              int respawn = sp.getAttributes().getNamedItem("respawn") != null
                                 ? Integer.parseInt(sp.getAttributes().getNamedItem("respawn").getNodeValue())
                                 : 0;
                              int respawnRnd = sp.getAttributes().getNamedItem("respawnRnd") != null
                                 ? Integer.parseInt(sp.getAttributes().getNamedItem("respawnRnd").getNodeValue())
                                 : 0;
                              count = sp.getAttributes().getNamedItem("count") != null
                                 ? Integer.parseInt(sp.getAttributes().getNamedItem("count").getNodeValue())
                                 : 1;
                              List<Location> coords = new ArrayList<>();
                              int var56 = 0;
                              String spawnTypeNode = sp.getAttributes().getNamedItem("type").getNodeValue();
                              if (spawnTypeNode == null || spawnTypeNode.equalsIgnoreCase("point")) {
                                 var56 = 0;
                              } else if (spawnTypeNode.equalsIgnoreCase("rnd")) {
                                 var56 = 1;
                              } else if (spawnTypeNode.equalsIgnoreCase("loc")) {
                                 var56 = 2;
                              }

                              for(Node cs = sp.getFirstChild(); cs != null; cs = cs.getNextSibling()) {
                                 if ("coords".equalsIgnoreCase(cs.getNodeName())) {
                                    coords.add(Location.parseLoc(cs.getAttributes().getNamedItem("loc").getNodeValue()));
                                 }
                              }

                              SpawnTerritory territory = null;
                              if (var56 == 2) {
                                 Polygon poly = new Polygon();

                                 for(Location loc : coords) {
                                    poly.add(loc.getX(), loc.getY()).setZmin(loc.getZ()).setZmax(loc.getZ());
                                 }

                                 if (!poly.validate()) {
                                    this._log
                                       .warning(this.getClass().getSimpleName() + ": Invalid spawn territory for instance id : " + id + " - " + poly + "!");
                                 }

                                 territory = new SpawnTerritory().add(poly);
                              }

                              for(String mob : mobs) {
                                 int mobId = Integer.parseInt(mob);
                                 spawnDat = new ReflectionTemplate.SpawnInfo(var56, mobId, count, respawn, respawnRnd, coords, territory);
                                 spawns.add(spawnDat);
                              }
                           }
                        }
                     }
                  }

                  this.addReflection(
                     new ReflectionTemplate(
                        id,
                        name,
                        timelimit,
                        dispelBuffs,
                        respawnTime,
                        minLevel,
                        maxLevel,
                        minParty,
                        maxParty,
                        teleportLocs,
                        ret,
                        collapseIfEmpty,
                        maxChannels,
                        removedItemId,
                        removedItemCount,
                        removedItemNecessity,
                        removeType,
                        giveItemId,
                        givedItemCount,
                        allowSummon,
                        isPvPInstance,
                        showTimer,
                        isTimerIncrease,
                        text,
                        doors,
                        spawns2,
                        spawns,
                        mapX,
                        mapY,
                        setReuseUponEntry,
                        sharedReuseGroup,
                        resetData,
                        requiredQuest,
                        questType,
                        params
                     )
                  );
               }
            }
         }
      }
   }

   public void addReflection(ReflectionTemplate zone) {
      this._reflections.put(zone.getId(), zone);
   }

   public ReflectionTemplate getReflectionId(int id) {
      return this._reflections.get(id);
   }

   public long getMinutesToNextEntrance(int id, Player player) {
      ReflectionTemplate zone = this.getReflectionId(id);
      if (zone == null) {
         return 0L;
      } else {
         Long time = null;
         if (this.getSharedReuseInstanceIds(id) != null && !this.getSharedReuseInstanceIds(id).isEmpty()) {
            List<Long> reuses = new ArrayList<>();

            for(int i : this.getSharedReuseInstanceIds(id)) {
               long reuse = ReflectionManager.getInstance().getReflectionTime(player.getObjectId(), i);
               if (reuse > 0L) {
                  reuses.add(reuse);
               }
            }

            if (!reuses.isEmpty()) {
               Collections.sort(reuses);
               time = reuses.get(reuses.size() - 1);
            }
         } else {
            time = ReflectionManager.getInstance().getReflectionTime(player.getObjectId(), id);
         }

         return time == null ? 0L : time;
      }
   }

   public List<Integer> getSharedReuseInstanceIds(int id) {
      if (this.getReflectionId(id).getSharedReuseGroup() < 1) {
         return null;
      } else {
         List<Integer> sharedInstanceIds = new ArrayList<>();

         for(ReflectionTemplate iz : this._reflections.valueCollection()) {
            if (iz.getSharedReuseGroup() > 0
               && this.getReflectionId(id).getSharedReuseGroup() > 0
               && iz.getSharedReuseGroup() == this.getReflectionId(id).getSharedReuseGroup()) {
               sharedInstanceIds.add(iz.getId());
            }
         }

         return sharedInstanceIds;
      }
   }

   public List<Integer> getSharedReuseInstanceIdsByGroup(int groupId) {
      if (groupId < 1) {
         return null;
      } else {
         List<Integer> sharedInstanceIds = new ArrayList<>();

         for(ReflectionTemplate iz : this._reflections.valueCollection()) {
            if (iz.getSharedReuseGroup() > 0 && iz.getSharedReuseGroup() == groupId) {
               sharedInstanceIds.add(iz.getId());
            }
         }

         return sharedInstanceIds;
      }
   }

   public static ReflectionParser getInstance() {
      return ReflectionParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ReflectionParser _instance = new ReflectionParser();
   }
}
