package l2e.gameserver.model.zone.form;

import java.awt.Polygon;
import l2e.commons.util.Rnd;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.zone.ZoneForm;

public class ZoneNPoly extends ZoneForm {
   private final Polygon _p;
   private final int _z1;
   private final int _z2;

   public ZoneNPoly(int[] x, int[] y, int z1, int z2) {
      this._p = new Polygon(x, y, x.length);
      this._z1 = Math.min(z1, z2);
      this._z2 = Math.max(z1, z2);
   }

   @Override
   public boolean isInsideZone(int x, int y, int z) {
      return this._p.contains(x, y) && z >= this._z1 && z <= this._z2;
   }

   @Override
   public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
      return this._p.intersects((double)Math.min(ax1, ax2), (double)Math.min(ay1, ay2), (double)Math.abs(ax2 - ax1), (double)Math.abs(ay2 - ay1));
   }

   @Override
   public double getDistanceToZone(int x, int y) {
      int[] _x = this._p.xpoints;
      int[] _y = this._p.ypoints;
      double shortestDist = Math.pow((double)(_x[0] - x), 2.0) + Math.pow((double)(_y[0] - y), 2.0);

      for(int i = 1; i < this._p.npoints; ++i) {
         double test = Math.pow((double)(_x[i] - x), 2.0) + Math.pow((double)(_y[i] - y), 2.0);
         if (test < shortestDist) {
            shortestDist = test;
         }
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
      int[] _x = this._p.xpoints;
      int[] _y = this._p.ypoints;

      for(int i = 0; i < this._p.npoints; ++i) {
         int nextIndex = i + 1;
         if (nextIndex == _x.length) {
            nextIndex = 0;
         }

         int vx = _x[nextIndex] - _x[i];
         int vy = _y[nextIndex] - _y[i];
         float lenght = (float)Math.sqrt((double)(vx * vx + vy * vy));
         lenght /= 10.0F;

         for(int o = 1; (float)o <= lenght; ++o) {
            float k = (float)o / lenght;
            this.dropDebugItem(57, 1, (int)((float)_x[i] + k * (float)vx), (int)((float)_y[i] + k * (float)vy), z);
         }
      }
   }

   @Override
   public int[] getRandomPoint() {
      int _minX = this._p.getBounds().x;
      int _maxX = this._p.getBounds().x + this._p.getBounds().width;
      int _minY = this._p.getBounds().y;
      int _maxY = this._p.getBounds().y + this._p.getBounds().height;
      int x = Rnd.get(_minX, _maxX);
      int y = Rnd.get(_minY, _maxY);

      for(int antiBlocker = 0; !this._p.contains(x, y) && antiBlocker < 1000; ++antiBlocker) {
         x = Rnd.get(_minX, _maxX);
         y = Rnd.get(_minY, _maxY);
      }

      return new int[]{x, y, GeoEngine.getHeight(x, y, this._z1, 0)};
   }
}
