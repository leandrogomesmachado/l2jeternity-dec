package l2e.commons.geometry;

public class Circle extends AbstractShape {
   protected final Point2D c;
   protected final int r;

   public Circle(Point2D center, int radius) {
      this.c = center;
      this.r = radius;
      this.min._x = this.c.getX() - this.r;
      this.max._x = this.c.getX() + this.r;
      this.min._y = this.c.getY() - this.r;
      this.max._y = this.c.getY() + this.r;
   }

   public Circle(int x, int y, int radius) {
      this(new Point2D(x, y), radius);
   }

   public Circle setZmax(int z) {
      this.max._z = z;
      return this;
   }

   public Circle setZmin(int z) {
      this.min._z = z;
      return this;
   }

   @Override
   public boolean isInside(int x, int y) {
      return (x - this.c.getX()) * (this.c.getX() - x) + (y - this.c.getY()) * (this.c.getY() - y) <= this.r * this.r;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      sb.append(this.c).append("{ radius: ").append(this.r).append("}");
      sb.append("]");
      return sb.toString();
   }
}
