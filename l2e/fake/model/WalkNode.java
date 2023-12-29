package l2e.fake.model;

public class WalkNode {
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _stayIterations;

   public WalkNode(int x, int y, int z, int stayIterations) {
      this._x = x;
      this._y = y;
      this._z = z;
      this._stayIterations = stayIterations;
   }

   public int getX() {
      return this._x;
   }

   public int getY() {
      return this._y;
   }

   public int getZ() {
      return this._z;
   }

   public int getStayIterations() {
      return this._stayIterations;
   }
}
