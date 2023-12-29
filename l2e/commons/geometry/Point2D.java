package l2e.commons.geometry;

public class Point2D implements Cloneable {
   public static final Point2D[] EMPTY_ARRAY = new Point2D[0];
   public int _x;
   public int _y;

   public Point2D() {
   }

   public Point2D(int x, int y) {
      this._x = x;
      this._y = y;
   }

   public Point2D clone() {
      return new Point2D(this.getX(), this.getY());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o.getClass() != this.getClass() ? false : this.equals((Point2D)o);
      }
   }

   public boolean equals(Point2D p) {
      return this.equals(p.getX(), p.getY());
   }

   public boolean equals(int x, int y) {
      return this.getX() == x && this.getY() == y;
   }

   public int getX() {
      return this._x;
   }

   public int getY() {
      return this._y;
   }

   @Override
   public String toString() {
      return "[x: " + this.getX() + " y: " + this.getY() + "]";
   }
}
