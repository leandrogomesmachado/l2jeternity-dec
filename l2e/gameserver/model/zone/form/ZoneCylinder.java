package l2e.gameserver.model.zone.form;

import l2e.commons.util.Rnd;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.zone.ZoneForm;

public class ZoneCylinder extends ZoneForm {
   private final int _x;
   private final int _y;
   private final int _z1;
   private final int _z2;
   private final int _rad;
   private final int _radS;

   public ZoneCylinder(int x, int y, int z1, int z2, int rad) {
      this._x = x;
      this._y = y;
      this._z1 = z1;
      this._z2 = z2;
      this._rad = rad;
      this._radS = rad * rad;
   }

   @Override
   public boolean isInsideZone(int x, int y, int z) {
      return !(Math.pow((double)(this._x - x), 2.0) + Math.pow((double)(this._y - y), 2.0) > (double)this._radS) && z >= this._z1 && z <= this._z2;
   }

   @Override
   public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
      if (this._x > ax1 && this._x < ax2 && this._y > ay1 && this._y < ay2) {
         return true;
      } else if (Math.pow((double)(ax1 - this._x), 2.0) + Math.pow((double)(ay1 - this._y), 2.0) < (double)this._radS) {
         return true;
      } else if (Math.pow((double)(ax1 - this._x), 2.0) + Math.pow((double)(ay2 - this._y), 2.0) < (double)this._radS) {
         return true;
      } else if (Math.pow((double)(ax2 - this._x), 2.0) + Math.pow((double)(ay1 - this._y), 2.0) < (double)this._radS) {
         return true;
      } else if (Math.pow((double)(ax2 - this._x), 2.0) + Math.pow((double)(ay2 - this._y), 2.0) < (double)this._radS) {
         return true;
      } else {
         if (this._x > ax1 && this._x < ax2) {
            if (Math.abs(this._y - ay2) < this._rad) {
               return true;
            }

            if (Math.abs(this._y - ay1) < this._rad) {
               return true;
            }
         }

         if (this._y > ay1 && this._y < ay2) {
            if (Math.abs(this._x - ax2) < this._rad) {
               return true;
            }

            if (Math.abs(this._x - ax1) < this._rad) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public double getDistanceToZone(int x, int y) {
      return Math.sqrt(Math.pow((double)(this._x - x), 2.0) + Math.pow((double)(this._y - y), 2.0)) - (double)this._rad;
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
      int count = (int)((Math.PI * 2) * (double)this._rad / 10.0);
      double angle = (Math.PI * 2) / (double)count;

      for(int i = 0; i < count; ++i) {
         int x = (int)(Math.cos(angle * (double)i) * (double)this._rad);
         int y = (int)(Math.sin(angle * (double)i) * (double)this._rad);
         this.dropDebugItem(57, 1, this._x + x, this._y + y, z);
      }
   }

   @Override
   public int[] getRandomPoint() {
      double q = Rnd.get() * 2.0 * Math.PI;
      double r = Math.sqrt(Rnd.get());
      double x = (double)this._rad * r * Math.cos(q) + (double)this._x;
      double y = (double)this._rad * r * Math.sin(q) + (double)this._y;
      return new int[]{(int)x, (int)y, GeoEngine.getHeight((int)x, (int)y, this._z1, 0)};
   }
}
