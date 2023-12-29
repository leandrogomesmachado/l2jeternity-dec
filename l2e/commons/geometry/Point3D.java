package l2e.commons.geometry;

import java.io.Serializable;

public class Point3D extends Point2D implements Serializable {
   private static final long serialVersionUID = 4638345252031872576L;
   public int _z;

   public Point3D() {
   }

   public Point3D(int x, int y, int z) {
      super(x, y);
      this._z = z;
   }

   public int getZ() {
      return this._z;
   }

   public Point3D clone() {
      return new Point3D(this.getX(), this.getY(), this._z);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o.getClass() != this.getClass() ? false : this.equals((Point3D)o);
      }
   }

   public boolean equals(Point3D p) {
      return this.equals(p.getX(), p.getY(), p.getZ());
   }

   public boolean equals(int x, int y, int z) {
      return this.getX() == x && this.getY() == y && this.getZ() == z;
   }

   @Override
   public String toString() {
      return "[x: " + this.getX() + " y: " + this.getY() + " z: " + this.getZ() + "]";
   }
}
