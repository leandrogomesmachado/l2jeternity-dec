package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.Location;

public final class VehicleTemplate extends Location {
   private final int _moveSpeed;
   private final int _rotationSpeed;

   public VehicleTemplate(int x, int y, int z) {
      super(x, y, z);
      this._moveSpeed = 350;
      this._rotationSpeed = 4000;
   }

   public VehicleTemplate(int x, int y, int z, int moveSpeed, int rotationSpeed) {
      super(x, y, z);
      this._moveSpeed = moveSpeed;
      this._rotationSpeed = rotationSpeed;
   }

   public int getMoveSpeed() {
      return this._moveSpeed;
   }

   public int getRotationSpeed() {
      return this._rotationSpeed;
   }
}
