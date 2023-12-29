package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.templates.door.DoorTemplate;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DoorParser extends DocumentParser {
   private final Map<Integer, DoorInstance> _doors = new HashMap<>();
   private static final Map<String, Set<Integer>> _groups = new HashMap<>();
   private final Map<Integer, List<DoorInstance>> _regions = new HashMap<>();

   protected DoorParser() {
      this.load();
   }

   @Override
   public void load() {
      this._doors.clear();
      _groups.clear();
      this._regions.clear();
      this.parseDatapackFile("data/stats/regions/doors.xml");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node a = this.getCurrentDocument().getFirstChild(); a != null; a = a.getNextSibling()) {
         if ("list".equalsIgnoreCase(a.getNodeName())) {
            for(Node b = a.getFirstChild(); b != null; b = b.getNextSibling()) {
               if ("door".equalsIgnoreCase(b.getNodeName())) {
                  NamedNodeMap attrs = b.getAttributes();
                  StatsSet set = new StatsSet();
                  set.set("baseHpMax", 1);

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  this.makeDoor(set);
               }
            }
         }
      }

      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._doors.size() + " door templates for " + this._regions.size() + " regions.");
   }

   public void insertCollisionData(StatsSet set) {
      int height = set.getInteger("height");
      String[] pos = set.getString("node1").split(",");
      int nodeX = Integer.parseInt(pos[0]);
      int nodeY = Integer.parseInt(pos[1]);
      pos = set.getString("node2").split(",");
      int posX = Integer.parseInt(pos[0]);
      int posY = Integer.parseInt(pos[1]);
      int collisionRadius = Math.min(Math.abs(nodeX - posX), Math.abs(nodeY - posY));
      if (collisionRadius < 20) {
         collisionRadius = 20;
      }

      set.set("collision_radius", collisionRadius);
      set.set("collision_height", height / 4);
   }

   private void makeDoor(StatsSet set) {
      this.insertCollisionData(set);
      DoorTemplate template = new DoorTemplate(set);
      DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), template, set);
      door.setCurrentHp(door.getMaxHp());
      int x = template.posX;
      int y = template.posY;
      int z = template.posZ;
      int gz = z + 32;
      door.spawnMe(x, y, gz);
      this.putDoor(door, MapRegionManager.getInstance().getMapRegionLocId(door));
   }

   public DoorTemplate getDoorTemplate(int doorId) {
      return this._doors.get(doorId).getTemplate();
   }

   public DoorInstance getDoor(int doorId) {
      return this._doors.get(doorId);
   }

   public void putDoor(DoorInstance door, int region) {
      this._doors.put(door.getDoorId(), door);
      if (!this._regions.containsKey(region)) {
         this._regions.put(region, new ArrayList<>());
      }

      this._regions.get(region).add(door);
   }

   public static void addDoorGroup(String groupName, int doorId) {
      Set<Integer> set = _groups.get(groupName);
      if (set == null) {
         set = new HashSet<>();
         _groups.put(groupName, set);
      }

      set.add(doorId);
   }

   public static Set<Integer> getDoorsByGroup(String groupName) {
      return _groups.get(groupName);
   }

   public Collection<DoorInstance> getDoors() {
      return this._doors.values();
   }

   public boolean checkIfDoorsBetween(Location start, Location end, int instanceId) {
      return this.checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), instanceId, false);
   }

   public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz, int instanceId) {
      return this.checkIfDoorsBetween(x, y, z, tx, ty, tz, instanceId, false);
   }

   public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean doubleFaceCheck) {
      Collection<DoorInstance> allDoors;
      if (instanceId > 0 && ReflectionManager.getInstance().getReflection(instanceId) != null) {
         allDoors = ReflectionManager.getInstance().getReflection(instanceId).getDoors();
      } else {
         allDoors = this._regions.get(MapRegionManager.getInstance().getMapRegionLocId(x, y));
      }

      if (allDoors == null) {
         return false;
      } else {
         for(DoorInstance doorInst : allDoors) {
            if (!doorInst.isDead()
               && (!doorInst.isOpen() || doorInst.getId() == 20250777 || doorInst.getId() == 20250778)
               && (!doorInst.isClosed() || doorInst.getId() != 20250777 && doorInst.getId() != 20250778)
               && doorInst.getX(0) != 0) {
               boolean intersectFace = false;

               for(int i = 0; i < 4; ++i) {
                  int j = i + 1 < 4 ? i + 1 : 0;
                  int denominator = (ty - y) * (doorInst.getX(i) - doorInst.getX(j)) - (tx - x) * (doorInst.getY(i) - doorInst.getY(j));
                  if (denominator != 0) {
                     float multiplier1 = (float)(
                           (doorInst.getX(j) - doorInst.getX(i)) * (y - doorInst.getY(i)) - (doorInst.getY(j) - doorInst.getY(i)) * (x - doorInst.getX(i))
                        )
                        / (float)denominator;
                     float multiplier2 = (float)((tx - x) * (y - doorInst.getY(i)) - (ty - y) * (x - doorInst.getX(i))) / (float)denominator;
                     if (multiplier1 >= 0.0F && multiplier1 <= 1.0F && multiplier2 >= 0.0F && multiplier2 <= 1.0F) {
                        int intersectZ = Math.round((float)z + multiplier1 * (float)(tz - z));
                        if (intersectZ > doorInst.getZMin() && intersectZ < doorInst.getZMax()) {
                           if (!doubleFaceCheck || intersectFace) {
                              return true;
                           }

                           intersectFace = true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public static DoorParser getInstance() {
      return DoorParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DoorParser _instance = new DoorParser();
   }
}
