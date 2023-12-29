package l2e.gameserver.model.actor.stat;

import l2e.gameserver.model.actor.Vehicle;

public class VehicleStat extends CharStat {
   private float _moveSpeed = 0.0F;
   private int _rotationSpeed = 0;

   public VehicleStat(Vehicle activeChar) {
      super(activeChar);
   }

   @Override
   public double getMoveSpeed() {
      return (double)this._moveSpeed;
   }

   public final void setMoveSpeed(float speed) {
      this._moveSpeed = speed;
   }

   public final int getRotationSpeed() {
      return this._rotationSpeed;
   }

   public final void setRotationSpeed(int speed) {
      this._rotationSpeed = speed;
   }
}
