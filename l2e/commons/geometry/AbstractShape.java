package l2e.commons.geometry;

public abstract class AbstractShape implements Shape {
   protected final Point3D max = new Point3D();
   protected final Point3D min = new Point3D();

   @Override
   public boolean isInside(int x, int y, int z) {
      return this.min._z <= z && this.max._z >= z && this.isInside(x, y);
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

   public AbstractShape setZmax(int z) {
      this.max._z = z;
      return this;
   }

   public AbstractShape setZmin(int z) {
      this.min._z = z;
      return this;
   }

   @Override
   public int getZmax() {
      return this.max.getZ();
   }

   @Override
   public int getZmin() {
      return this.min.getZ();
   }
}
