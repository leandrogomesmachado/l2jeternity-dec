package l2e.gameserver.model.zone.form;

import java.awt.Rectangle;
import l2e.commons.util.Rnd;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.zone.ZoneForm;

public class ZoneCuboid extends ZoneForm {
   private final int _z1;
   private final int _z2;
   Rectangle _r;

   public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2) {
      int _x1 = Math.min(x1, x2);
      int _x2 = Math.max(x1, x2);
      int _y1 = Math.min(y1, y2);
      int _y2 = Math.max(y1, y2);
      this._r = new Rectangle(_x1, _y1, _x2 - _x1, _y2 - _y1);
      this._z1 = Math.min(z1, z2);
      this._z2 = Math.max(z1, z2);
   }

   @Override
   public boolean isInsideZone(int x, int y, int z) {
      return this._r.contains(x, y) && z >= this._z1 && z <= this._z2;
   }

   @Override
   public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
      return this._r.intersects((double)Math.min(ax1, ax2), (double)Math.min(ay1, ay2), (double)Math.abs(ax2 - ax1), (double)Math.abs(ay2 - ay1));
   }

   @Override
   public double getDistanceToZone(int x, int y) {
      int _x1 = this._r.x;
      int _x2 = this._r.x + this._r.width;
      int _y1 = this._r.y;
      int _y2 = this._r.y + this._r.height;
      double shortestDist = Math.pow((double)(_x1 - x), 2.0) + Math.pow((double)(_y1 - y), 2.0);
      double test = Math.pow((double)(_x1 - x), 2.0) + Math.pow((double)(_y2 - y), 2.0);
      if (test < shortestDist) {
         shortestDist = test;
      }

      test = Math.pow((double)(_x2 - x), 2.0) + Math.pow((double)(_y1 - y), 2.0);
      if (test < shortestDist) {
         shortestDist = test;
      }

      test = Math.pow((double)(_x2 - x), 2.0) + Math.pow((double)(_y2 - y), 2.0);
      if (test < shortestDist) {
         shortestDist = test;
      }

      return Math.sqrt(shortestDist);
   }

   @Override
   public int getLowZ() {
      return this._z1;
   }

   @Override
   public int getHighZ() {
      return this._z2;
   }

   @Override
   public void visualizeZone(int z) {
      int _x1 = this._r.x;
      int _x2 = this._r.x + this._r.width;
      int _y1 = this._r.y;
      int _y2 = this._r.y + this._r.height;

      for(int x = _x1; x < _x2; x += 10) {
         this.dropDebugItem(57, 1, x, _y1, z);
         this.dropDebugItem(57, 1, x, _y2, z);
      }

      for(int y = _y1; y < _y2; y += 10) {
         this.dropDebugItem(57, 1, _x1, y, z);
         this.dropDebugItem(57, 1, _x2, y, z);
      }
   }

   @Override
   public int[] getRandomPoint() {
      int x = Rnd.get(this._r.x, this._r.x + this._r.width);
      int y = Rnd.get(this._r.y, this._r.y + this._r.height);
      return new int[]{x, y, GeoEngine.getHeight(x, y, this._z1, 0)};
   }
}
