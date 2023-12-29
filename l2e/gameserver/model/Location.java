package l2e.gameserver.model;

import java.util.logging.Logger;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.interfaces.ILocational;
import l2e.gameserver.model.interfaces.IPositionable;
import l2e.gameserver.model.spawn.SpawnRange;

public class Location implements IPositionable, SpawnRange {
   private static final Logger _log = Logger.getLogger(Location.class.getName());
   private int _x;
   private int _y;
   private int _z;
   private int _heading;

   public Location(int x, int y, int z) {
      this(x, y, z, -1);
   }

   public Location(GameObject obj) {
      this(obj.getX(), obj.getY(), obj.getZ(), obj.getHeading());
   }

   public Location(int x, int y, int z, int heading) {
      this._x = x;
      this._y = y;
      this._z = z;
      this._heading = heading;
   }

   @Override
   public int getX() {
      return this._x;
   }

   @Override
   public void setX(int x) {
      this._x = x;
   }

   @Override
   public int getY() {
      return this._y;
   }

   @Override
   public void setY(int y) {
      this._y = y;
   }

   @Override
   public int getZ() {
      return this._z;
   }

   @Override
   public void setZ(int z) {
      this._z = z;
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

   @Override
   public int getHeading() {
      return this._heading;
   }

   @Override
   public void setHeading(int heading) {
      this._heading = heading;
   }

   public IPositionable getLocation() {
      return this;
   }

   @Override
   public void setLocation(Location loc) {
      this._x = loc.getX();
      this._y = loc.getY();
      this._z = loc.getZ();
      this._heading = loc.getHeading();
   }

   public void set(int x, int y, int z) {
      this._x = x;
      this._y = y;
      this._z = z;
   }

   public Location set(int x, int y, int z, int h) {
      this._x = x;
      this._y = y;
      this._z = z;
      this._heading = h;
      return this;
   }

   public Location set(Location loc) {
      this._x = loc.getX();
      this._y = loc.getY();
      this._z = loc.getZ();
      this._heading = loc.getHeading();
      return this;
   }

   public Location setH(int h) {
      this._heading = h;
      return this;
   }

   public Location geo2world() {
      this._x = (this._x << 4) + -294912 + 8;
      this._y = (this._y << 4) + -262144 + 8;
      return this;
   }

   public Location world2geo() {
      this._x = this._x - -294912 >> 4;
      this._y = this._y - -262144 >> 4;
      return this;
   }

   public Location(String s) throws IllegalArgumentException {
      this._heading = 0;
      String[] xyzh = s.replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
      if (xyzh.length < 3) {
         throw new IllegalArgumentException("Can't parse location from string: " + s);
      } else {
         this._x = Integer.parseInt(xyzh[0]);
         this._y = Integer.parseInt(xyzh[1]);
         this._z = Integer.parseInt(xyzh[2]);
         this._heading = xyzh.length >= 4 ? Integer.parseInt(xyzh[3]) : 0;
      }
   }

   public static Location coordsRandomize(Location loc, int radiusmin, int radiusmax) {
      return coordsRandomize(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), radiusmin, radiusmax);
   }

   public static Location coordsRandomize(int x, int y, int z, int heading, int radiusmin, int radiusmax) {
      if (radiusmax != 0 && radiusmax >= radiusmin) {
         int radius = Rnd.get(radiusmin, radiusmax);
         double angle = Rnd.nextDouble() * 2.0 * Math.PI;
         return new Location((int)((double)x + (double)radius * Math.cos(angle)), (int)((double)y + (double)radius * Math.sin(angle)), z, heading);
      } else {
         return new Location(x, y, z, heading);
      }
   }

   public Location rnd(int min, int max, boolean change) {
      Location loc = coordsRandomize(this, min, max);
      if (Config.GEODATA) {
         loc = GeoEngine.moveCheck(this._x, this._y, this._z, loc.getX(), loc.getY(), 0);
      }

      if (change) {
         this._x = loc.getX();
         this._y = loc.getY();
         this._z = loc.getZ();
         return this;
      } else {
         return loc;
      }
   }

   public Location correctGeoZ(int geoIndex) {
      this._z = GeoEngine.getHeight(this._x, this._y, this._z, geoIndex);
      return this;
   }

   public Location clone() {
      return new Location(this._x, this._y, this._z, this._heading);
   }

   @Override
   public Location getRandomLoc(int ref, boolean fly) {
      return this;
   }

   public static Location parseLoc(String s) throws IllegalArgumentException {
      String[] xyzh = s.split("[\\s,;]+");
      if (xyzh.length < 3) {
         throw new IllegalArgumentException("Can't parse location from string: " + s);
      } else {
         int x = Integer.parseInt(xyzh[0]);
         int y = Integer.parseInt(xyzh[1]);
         int z = Integer.parseInt(xyzh[2]);
         int h = xyzh.length < 4 ? -1 : Integer.parseInt(xyzh[3]);
         return new Location(x, y, z, h);
      }
   }

   public static Location findPointToStay(Location loc, int radius, int geoIndex, boolean applyDefault) {
      return findPointToStay(loc.getX(), loc.getY(), loc.getZ(), 0, radius, geoIndex, applyDefault);
   }

   public static Location findPointToStay(Location loc, int radiusmin, int radiusmax, int geoIndex, boolean applyDefault) {
      return findPointToStay(loc.getX(), loc.getY(), loc.getZ(), radiusmin, radiusmax, geoIndex, applyDefault);
   }

   public static Location findPointToStay(GameObject obj, Location loc, int radiusmin, int radiusmax, boolean applyDefault) {
      return findPointToStay(loc.getX(), loc.getY(), loc.getZ(), radiusmin, radiusmax, obj.getGeoIndex(), applyDefault);
   }

   public static Location findPointToStay(GameObject obj, int radiusmin, int radiusmax, boolean applyDefault) {
      return findPointToStay(obj, obj.getLocation(), radiusmin, radiusmax, applyDefault);
   }

   public static Location findPointToStay(GameObject obj, int radius, boolean applyDefault) {
      return findPointToStay(obj, 0, radius, applyDefault);
   }

   public static Location findPointToStay(int x, int y, int z, int radiusmin, int radiusmax, int geoIndex, boolean applyDefault) {
      for(int i = 0; i < 100; ++i) {
         Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
         int tempz = GeoEngine.getHeight(pos.getX(), pos.getY(), pos.getZ(), geoIndex);
         if (Math.abs(pos.getZ() - tempz) < 200 && GeoEngine.getNSWE(pos.getX(), pos.getY(), tempz, geoIndex) == 15) {
            return new Location(pos.getX(), pos.getY(), tempz);
         }
      }

      if (applyDefault) {
         if (Config.DEBUG_SPAWN) {
            _log.warning("Location: Problem to found correct position for npc. Final location: " + x + " " + y + " " + z);
         }

         return new Location(x, y, z);
      } else {
         return null;
      }
   }

   public static Location findNearest(Creature creature, Location[] locs) {
      Location defloc = null;

      for(Location loc : locs) {
         if (defloc == null) {
            defloc = loc;
         } else if (creature.getDistance(loc) < creature.getDistance(defloc)) {
            defloc = loc;
         }
      }

      return defloc;
   }

   public static Location findAroundPosition(GameObject obj, int radius) {
      return findAroundPosition(obj, 0, radius);
   }

   public static Location findAroundPosition(GameObject obj, int radiusmin, int radiusmax) {
      return findAroundPosition(obj, obj.getLocation(), radiusmin, radiusmax);
   }

   public static Location findAroundPosition(GameObject obj, Location loc, int radiusmin, int radiusmax) {
      return findAroundPosition(loc.getX(), loc.getY(), loc.getZ(), radiusmin, radiusmax, obj.getGeoIndex());
   }

   public static Location findAroundPosition(int x, int y, int z, int radiusmin, int radiusmax, int geoIndex) {
      for(int i = 0; i < 100; ++i) {
         Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
         int tempz = GeoEngine.getHeight(pos.getX(), pos.getY(), pos.getZ(), geoIndex);
         if (GeoEngine.canMoveToCoord(x, y, z, pos.getX(), pos.getY(), tempz, geoIndex)
            && GeoEngine.canMoveToCoord(pos.getX(), pos.getY(), tempz, x, y, z, geoIndex)) {
            pos._z = tempz;
            return pos;
         }
      }

      return new Location(x, y, z);
   }

   public static Location findFrontPosition(GameObject obj, GameObject obj2, int radiusmin, int radiusmax) {
      if (radiusmax != 0 && radiusmax >= radiusmin) {
         double collision = obj.getColRadius() + obj2.getColRadius();
         int minangle = 0;
         int maxangle = 360;
         if (!obj.equals(obj2)) {
            double angle = PositionUtils.calculateAngleFrom(obj, obj2);
            minangle = (int)angle - 45;
            maxangle = (int)angle + 45;
         }

         Location pos = new Location(0, 0, 0, 0);

         for(int i = 0; i < 100; ++i) {
            int randomRadius = Rnd.get(radiusmin, radiusmax);
            int randomAngle = Rnd.get(minangle, maxangle);
            pos._x = obj.getX() + (int)((collision + (double)randomRadius) * Math.cos(Math.toRadians((double)randomAngle)));
            pos._y = obj.getY() + (int)((collision + (double)randomRadius) * Math.sin(Math.toRadians((double)randomAngle)));
            pos._z = obj.getZ();
            int tempz = GeoEngine.getHeight(pos._x, pos._y, pos._z, obj.getGeoIndex());
            if (Math.abs(pos._z - tempz) < 200 && GeoEngine.getNSWE(pos._x, pos._y, tempz, obj.getGeoIndex()) == 15) {
               pos._z = tempz;
               if (!obj.equals(obj2)) {
                  pos._heading = PositionUtils.getHeadingTo(pos, obj2.getLocation());
               } else {
                  pos._heading = obj.getHeading();
               }

               return pos;
            }
         }

         return new Location(obj);
      } else {
         return new Location(obj);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj instanceof Location) {
         Location loc = (Location)obj;
         return this.getX() == loc.getX() && this.getY() == loc.getY() && this.getZ() == loc.getZ() && this.getHeading() == loc.getHeading();
      } else {
         return false;
      }
   }

   public boolean equals(int x, int y, int z) {
      return this.getX() == x && this.getY() == y && this.getZ() == z;
   }

   @Override
   public String toString() {
      return "[" + this.getClass().getSimpleName() + "] X: " + this.getX() + " Y: " + this.getY() + " Z: " + this.getZ() + " Heading: " + this._heading;
   }
}
