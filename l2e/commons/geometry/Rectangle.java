package l2e.commons.geometry;

public class Rectangle extends AbstractShape {
   public Rectangle(int x1, int y1, int x2, int y2) {
      this.min._x = Math.min(x1, x2);
      this.min._y = Math.min(y1, y2);
      this.max._x = Math.max(x1, x2);
      this.max._y = Math.max(y1, y2);
   }

   public Rectangle setZmax(int z) {
      this.max._z = z;
      return this;
   }

   public Rectangle setZmin(int z) {
      this.min._z = z;
      return this;
   }

   @Override
   public boolean isInside(int x, int y) {
      return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY();
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      sb.append(this.min).append(", ").append(this.max);
      sb.append("]");
      return sb.toString();
   }
}
