package l2e.gameserver.instancemanager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.form.ZoneCuboid;
import l2e.gameserver.model.zone.form.ZoneCylinder;
import l2e.gameserver.model.zone.form.ZoneNPoly;
import l2e.gameserver.model.zone.type.ArenaZone;
import l2e.gameserver.model.zone.type.OlympiadStadiumZone;
import l2e.gameserver.model.zone.type.RespawnZone;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ZoneManager extends DocumentParser {
   private static final Map<String, AbstractZoneSettings> _settings = new HashMap<>();
   private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> _classZones = new HashMap<>();
   private final List<ZoneType> _reflectionZones = new ArrayList<>();
   private int _lastDynamicId = 300000;
   private List<ItemInstance> _debugItems;
   private static ZoneType[][][] _zones;

   protected ZoneManager() {
      this.load();
   }

   public void reload() {
      _zones = (ZoneType[][][])null;

      for(Map<Integer, ? extends ZoneType> map : this._classZones.values()) {
         for(ZoneType zone : map.values()) {
            if (zone.getSettings() != null) {
               _settings.put(zone.getName(), zone.getSettings());
            }
         }
      }

      EpicBossManager.getInstance().getZones().clear();
      this.load();

      for(GameObject obj : World.getInstance().getAllVisibleObjects()) {
         if (obj instanceof Creature) {
            ((Creature)obj).revalidateZone(true);
         }
      }

      _settings.clear();
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      List<int[]> rs = new ArrayList<>();

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            NamedNodeMap attrs = n.getAttributes();
            Node attribute = attrs.getNamedItem("enabled");
            if (attribute == null || Boolean.parseBoolean(attribute.getNodeValue())) {
               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if ("zone".equalsIgnoreCase(d.getNodeName())) {
                     attrs = d.getAttributes();
                     attribute = attrs.getNamedItem("id");
                     int zoneId;
                     if (attribute != null) {
                        zoneId = Integer.parseInt(attribute.getNodeValue());
                     } else {
                        zoneId = this._lastDynamicId++;
                     }

                     attribute = attrs.getNamedItem("name");
                     String zoneName;
                     if (attribute != null) {
                        zoneName = attribute.getNodeValue();
                     } else {
                        zoneName = null;
                     }

                     int minZ = parseInt(attrs, "minZ");
                     int maxZ = parseInt(attrs, "maxZ");
                     String zoneType = attrs.getNamedItem("type").getNodeValue();
                     String zoneShape = attrs.getNamedItem("shape").getNodeValue();
                     Class<?> newZone = null;
                     Constructor<?> zoneConstructor = null;

                     ZoneType temp;
                     try {
                        newZone = Class.forName("l2e.gameserver.model.zone.type." + zoneType);
                        zoneConstructor = newZone.getConstructor(Integer.TYPE);
                        temp = (ZoneType)zoneConstructor.newInstance(zoneId);
                     } catch (Exception var23) {
                        this._log.warning("ZoneData: No such zone type: " + zoneType + " in file: " + this.getCurrentFile().getName());
                        continue;
                     }

                     try {
                        int[][] coords = (int[][])null;
                        rs.clear();

                        for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                           if ("node".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int[] point = new int[]{parseInt(attrs, "X"), parseInt(attrs, "Y")};
                              rs.add(point);
                           }
                        }

                        coords = (int[][])rs.toArray(new int[rs.size()][2]);
                        if (coords == null || coords.length == 0) {
                           this._log.warning("ZoneData: missing data for zone: " + zoneId + " XML file: " + this.getCurrentFile().getName());
                           continue;
                        }

                        if (zoneShape.equalsIgnoreCase("Cuboid")) {
                           if (coords.length != 2) {
                              this._log
                                 .warning("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + this.getCurrentFile().getName());
                              continue;
                           }

                           temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
                        } else if (zoneShape.equalsIgnoreCase("NPoly")) {
                           if (coords.length <= 2) {
                              this._log.warning("ZoneData: Bad data for zone: " + zoneId + " in file: " + this.getCurrentFile().getName());
                              continue;
                           }

                           int[] aX = new int[coords.length];
                           int[] aY = new int[coords.length];

                           for(int i = 0; i < coords.length; ++i) {
                              aX[i] = coords[i][0];
                              aY[i] = coords[i][1];
                           }

                           temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
                        } else if (zoneShape.equalsIgnoreCase("Cylinder")) {
                           attrs = d.getAttributes();
                           int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
                           if (coords.length != 1 || zoneRad <= 0) {
                              this._log.warning("ZoneData: Bad data for zone: " + zoneId + " in file: " + this.getCurrentFile().getName());
                              continue;
                           }

                           temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
                        }
                     } catch (Exception var24) {
                        this._log.log(Level.WARNING, "ZoneData: Failed to load zone " + zoneId + " coordinates: " + var24.getMessage(), (Throwable)var24);
                     }

                     for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                        if ("stat".equalsIgnoreCase(cd.getNodeName())) {
                           attrs = cd.getAttributes();
                           String name = attrs.getNamedItem("name").getNodeValue();
                           String val = attrs.getNamedItem("val").getNodeValue();
                           temp.setParameter(name, val);
                        } else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && temp instanceof ZoneRespawn) {
                           attrs = cd.getAttributes();
                           int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
                           int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
                           int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
                           Node val = attrs.getNamedItem("type");
                           ((ZoneRespawn)temp).parseLoc(spawnX, spawnY, spawnZ, val == null ? null : val.getNodeValue());
                        } else if ("race".equalsIgnoreCase(cd.getNodeName()) && temp instanceof RespawnZone) {
                           attrs = cd.getAttributes();
                           String race = attrs.getNamedItem("name").getNodeValue();
                           String point = attrs.getNamedItem("point").getNodeValue();
                           ((RespawnZone)temp).addRaceRespawnPoint(race, point);
                        }
                     }

                     if (this.checkId(zoneId)) {
                        this._log.config("Caution: Zone (" + zoneId + ") from file: " + this.getCurrentFile().getName() + " overrides previos definition.");
                     }

                     if (zoneName != null && !zoneName.isEmpty()) {
                        temp.setName(zoneName);
                     }

                     temp.setType(zoneType);
                     this.addZone(zoneId, temp);
                     if (temp.getReflectionTemplateId() > 0) {
                        this._reflectionZones.add(temp);
                     }

                     for(int x = 0; x < _zones.length; ++x) {
                        for(int y = 0; y < _zones[x].length; ++y) {
                           int ax = 11 + x - 20 << 15;
                           int ay = 10 + y - 18 << 15;
                           int bx = ax + 32767;
                           int by = ay + 32767;
                           if (temp.getZone().intersectsRectangle(ax, bx, ay, by)) {
                              if (_zones[x][y] == null) {
                                 _zones[x][y] = new ZoneType[]{temp};
                              } else {
                                 ZoneType[] za = new ZoneType[_zones[x][y].length + 1];
                                 System.arraycopy(_zones[x][y], 0, za, 0, _zones[x][y].length);
                                 za[za.length - 1] = temp;
                                 _zones[x][y] = za;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public final void load() {
      this._classZones.clear();
      this._reflectionZones.clear();
      long started = System.currentTimeMillis();
      _zones = new ZoneType[16][17][];
      this.parseDirectory("data/stats/regions/zones", false);
      started = System.currentTimeMillis() - started;
      this._log
         .info(
            this.getClass().getSimpleName()
               + ": Loaded "
               + this._classZones.size()
               + " zone classes and "
               + this.getSize()
               + " zones in "
               + started / 1000L
               + " seconds."
         );
   }

   public int getSize() {
      int i = 0;

      for(Map<Integer, ? extends ZoneType> map : this._classZones.values()) {
         i += map.size();
      }

      return i;
   }

   public boolean checkId(int id) {
      for(Map<Integer, ? extends ZoneType> map : this._classZones.values()) {
         if (map.containsKey(id)) {
            return true;
         }
      }

      return false;
   }

   public <T extends ZoneType> void addZone(Integer id, T zone) {
      Map<Integer, T> map = this._classZones.get(zone.getClass());
      if (map == null) {
         Map<Integer, T> var4 = new HashMap();
         var4.put(id, zone);
         this._classZones.put(zone.getClass(), var4);
      } else {
         map.put(id, zone);
      }
   }

   public Collection<ZoneType> getAllZones() {
      List<ZoneType> zones = new ArrayList<>();

      for(Map<Integer, ? extends ZoneType> map : this._classZones.values()) {
         zones.addAll(map.values());
      }

      return zones;
   }

   public <T extends ZoneType> Collection<T> getAllZones(Class<T> zoneType) {
      return this._classZones.get(zoneType).values();
   }

   public ZoneType getZoneById(int id) {
      for(Map<Integer, ? extends ZoneType> map : this._classZones.values()) {
         if (map.containsKey(id)) {
            return map.get(id);
         }
      }

      return null;
   }

   public ZoneType getZoneByZoneId(Player player, ZoneId id) {
      ZoneType[] za = this.getAllZones(player.getX(), player.getY());
      if (za == null) {
         return null;
      } else {
         for(ZoneType zone : za) {
            if (zone != null && zone.getZoneId().contains(id) && zone.isInsideZone(player.getX(), player.getY(), player.getZ())) {
               return zone;
            }
         }

         return null;
      }
   }

   public boolean isInsideZone(int id, GameObject object) {
      ZoneType zone = this.getZoneById(id);
      return zone != null && zone.isInsideZone(object.getX(), object.getY(), object.getZ());
   }

   public <T extends ZoneType> T getZoneById(int id, Class<T> zoneType) {
      return (T)this._classZones.get(zoneType).get(id);
   }

   public List<ZoneType> getZones(GameObject object) {
      return this.getZones(object.getX(), object.getY(), object.getZ());
   }

   public <T extends ZoneType> T getZone(GameObject object, Class<T> type) {
      return object == null ? null : this.getZone(object.getX(), object.getY(), object.getZ(), type);
   }

   public ZoneType[] getAllZones(int x, int y) {
      int gx = x - -294912 >> 15;
      int gy = y - -262144 >> 15;
      if (gx >= 0 && gx < _zones.length && gy >= 0 && gy < _zones[gx].length) {
         return _zones[gx][gy];
      } else {
         this._log.warning("Wrong world region: " + gx + " " + gy + " (" + x + "," + y + ")");
         return null;
      }
   }

   public List<ZoneType> getZones(int x, int y, int z) {
      ZoneType[] za = this.getAllZones(x, y);
      if (za == null) {
         return null;
      } else {
         List<ZoneType> temp = new ArrayList<>();

         for(ZoneType zone : za) {
            if (zone != null && zone.isInsideZone(x, y, z)) {
               temp.add(zone);
            }
         }

         return temp;
      }
   }

   public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type) {
      ZoneType[] za = this.getAllZones(x, y);
      if (za == null) {
         return null;
      } else {
         for(ZoneType zone : za) {
            if (zone != null && zone.isInsideZone(x, y, z) && type.isInstance(zone)) {
               return (T)zone;
            }
         }

         return null;
      }
   }

   public final ArenaZone getArena(Creature creature) {
      if (creature == null) {
         return null;
      } else {
         List<ZoneType> zones = this.getZones(creature.getX(), creature.getY(), creature.getZ());
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType temp : zones) {
               if (temp != null && temp instanceof ArenaZone && temp.isCharacterInZone(creature)) {
                  return (ArenaZone)temp;
               }
            }
         }

         return null;
      }
   }

   public final OlympiadStadiumZone getOlympiadStadium(Creature creature) {
      if (creature == null) {
         return null;
      } else {
         List<ZoneType> zones = this.getZones(creature.getX(), creature.getY(), creature.getZ());
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType temp : zones) {
               if (temp != null && temp instanceof OlympiadStadiumZone && temp.isCharacterInZone(creature)) {
                  return (OlympiadStadiumZone)temp;
               }
            }
         }

         return null;
      }
   }

   public <T extends ZoneType> T getClosestZone(GameObject obj, Class<T> type) {
      T zone = this.getZone(obj, type);
      if (zone == null) {
         double closestdis = Double.MAX_VALUE;

         for(T temp : this._classZones.get(type).values()) {
            double distance = temp.getDistanceToZone(obj);
            if (distance < closestdis) {
               closestdis = distance;
               zone = temp;
            }
         }
      }

      return zone;
   }

   public final List<ZoneType> isInsideZone(int x, int y) {
      List<ZoneType> zones = new ArrayList<>();

      for(ZoneType temp : this.getAllZones()) {
         if (temp != null && temp.isInsideZone(x, y)) {
            zones.add(temp);
         }
      }

      return zones;
   }

   public List<ItemInstance> getDebugItems() {
      if (this._debugItems == null) {
         this._debugItems = new ArrayList<>();
      }

      return this._debugItems;
   }

   public void clearDebugItems() {
      if (this._debugItems != null) {
         for(Iterator<ItemInstance> it = this._debugItems.iterator(); it.hasNext(); it.remove()) {
            ItemInstance item = it.next();
            if (item != null) {
               item.decayMe();
            }
         }
      }
   }

   public static AbstractZoneSettings getSettings(String name) {
      return _settings.get(name);
   }

   public void createZoneReflections() {
      int i = 0;
      if (this._reflectionZones != null && !this._reflectionZones.isEmpty()) {
         for(ZoneType z : this._reflectionZones) {
            if (z != null) {
               z.generateReflection();
               ++i;
            }
         }
      }

      this._log.info(this.getClass().getSimpleName() + ": Generate " + i + " reflections for zones.");
   }

   public static final ZoneManager getInstance() {
      return ZoneManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ZoneManager _instance = new ZoneManager();
   }
}
