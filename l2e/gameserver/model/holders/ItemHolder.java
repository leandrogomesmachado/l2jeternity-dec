package l2e.gameserver.model.holders;

import l2e.gameserver.model.interfaces.IIdentifiable;

public class ItemHolder implements IIdentifiable {
   private final int _id;
   private final int _objectId;
   private final long _count;
   private final double _chance;

   public ItemHolder(int id, long count) {
      this._id = id;
      this._objectId = -1;
      this._count = count;
      this._chance = 100.0;
   }

   public ItemHolder(int id, int objectId, long count) {
      this._id = id;
      this._objectId = objectId;
      this._count = count;
      this._chance = 100.0;
   }

   public ItemHolder(int id, long count, double chance) {
      this._id = id;
      this._objectId = -1;
      this._count = count;
      this._chance = chance;
   }

   @Override
   public int getId() {
      return this._id;
   }

   public int getObjectId() {
      return this._objectId;
   }

   public long getCount() {
      return this._count;
   }

   public double getChance() {
      return this._chance;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + ": Id: " + this._id + " Count: " + this._count;
   }
}
