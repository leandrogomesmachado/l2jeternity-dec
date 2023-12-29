package l2e.gameserver.model.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.geometry.Point3D;
import l2e.commons.geometry.Shape;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;

public class SpawnTerritory implements Shape, SpawnRange {
   private static final Logger _log = Logger.getLogger(SpawnTerritory.class.getName());
   protected final Point3D max = new Point3D();
   protected final Point3D min = new Point3D();
   private final List<Shape> include = new ArrayList<>(1);
   private final List<Shape> exclude = new ArrayList<>(1);

   public SpawnTerritory add(Shape shape) {
      if (this.include.isEmpty()) {
         this.max._x = shape.getXmax();
         this.max._y = shape.getYmax();
         this.max._z = shape.getZmax();
         this.min._x = shape.getXmin();
         this.min._y = shape.getYmin();
         this.min._z = shape.getZmin();
      } else {
         this.max._x = Math.max(this.max.getX(), shape.getXmax());
         this.max._y = Math.max(this.max.getY(), shape.getYmax());
         this.max._z = Math.max(this.max.getZ(), shape.getZmax());
         this.min._x = Math.min(this.min.getX(), shape.getXmin());
         this.min._y = Math.min(this.min.getY(), shape.getYmin());
         this.min._z = Math.min(this.min.getZ(), shape.getZmin());
      }

      this.include.add(shape);
      return this;
   }

   public SpawnTerritory addBanned(Shape shape) {
      this.exclude.add(shape);
      return this;
   }

   public List<Shape> getTerritories() {
      return this.include;
   }

   public List<Shape> getBannedTerritories() {
      return this.exclude;
   }

   @Override
   public boolean isInside(int x, int y) {
      for(Shape shape : this.include) {
         if (shape.isInside(x, y)) {
            return !this.isExcluded(x, y);
         }
      }

      return false;
   }

   @Override
   public boolean isInside(int x, int y, int z) {
      if (x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY() && z >= this.min.getZ() && z <= this.max.getZ()) {
         for(Shape shape : this.include) {
            if (shape.isInside(x, y, z)) {
               return !this.isExcluded(x, y, z);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean isExcluded(int x, int y) {
      for(Shape shape : this.exclude) {
         if (shape.isInside(x, y)) {
            return true;
         }
      }

      return false;
   }

   public boolean isExcluded(int x, int y, int z) {
      for(Shape shape : this.exclude) {
         if (shape.isInside(x, y, z)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getXmax() {
      return this.max.getX();
   }

   @Override
   public int getXmin() {
      return this.min.getX();
   }

   @Override
   public int getYmax() {
      return this.max.getY();
   }

   @Override
   public int getYmin() {
      return this.min.getY();
   }

   @Override
   public int getZmax() {
      return this.max.getZ();
   }

   @Override
   public int getZmin() {
      return this.min.getZ();
   }

   public static Location getRandomLoc(SpawnTerritory territory, boolean fly) {
      return getRandomLoc(territory, 0, fly);
   }

   public static Location getRandomLoc(SpawnTerritory territory, int geoIndex, boolean fly) {
      Location pos = new Location(0, 0, 0, 0);
      List<Shape> territories = territory.getTerritories();

      label69:
      for(int i = 1; i <= 100; ++i) {
         Shape shape = territories.get(Rnd.get(territories.size()));
         pos.setX(Rnd.get(shape.getXmin(), shape.getXmax()));
         pos.setY(Rnd.get(shape.getYmin(), shape.getYmax()));
         pos.setZ(shape.getZmin() + (shape.getZmax() - shape.getZmin()) / 2);
         int minZ = Math.min(shape.getZmin(), shape.getZmax());
         int maxZ = Math.max(shape.getZmin(), shape.getZmax());
         if (territory.isInside(pos.getX(), pos.getY())) {
            if (fly) {
               pos.setZ(Rnd.get(minZ, maxZ));
               break;
            }

            if (minZ == maxZ) {
               minZ -= 100;
               maxZ += 100;
            }

            if (!Config.GEODATA) {
               break;
            }

            int tempz = GeoEngine.getHeight(pos, geoIndex);
            if (shape.getZmin() != shape.getZmax()
               ? tempz >= shape.getZmin() && tempz <= shape.getZmax()
               : tempz >= shape.getZmin() - 200 && tempz <= shape.getZmin() + 200) {
               pos.setZ(tempz);
               int geoX = pos.getX() - -294912 >> 4;
               int geoY = pos.getY() - -262144 >> 4;

               for(int x = geoX - 1; x <= geoX + 1; ++x) {
                  for(int y = geoY - 1; y <= geoY + 1; ++y) {
                     if (GeoEngine.NgetNSWE(x, y, tempz, geoIndex) != 15) {
                        continue label69;
                     }
                  }
               }

               pos.setHeading(-1);
               return pos;
            }
         } else if (i == 100) {
            pos.setZ(GeoEngine.getHeight(pos.getX(), pos.getY(), maxZ, geoIndex));
            if (Config.DEBUG_SPAWN) {
               _log.warning("SpawnTerritory: Problem to found correct position for npc. Final location: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            }
            break;
         }
      }

      pos.setHeading(-1);
      return pos;
   }

   @Override
   public Location getRandomLoc(int geoIndex, boolean fly) {
      return getRandomLoc(this, geoIndex, fly);
   }
}
