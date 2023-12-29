package l2e.gameserver.model.service.premium;

public class PremiumGift {
   private final int _id;
   private final long _count;
   private final boolean _removable;

   public PremiumGift(int id, long count, boolean removable) {
      this._id = id;
      this._count = count;
      this._removable = removable;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }

   public boolean isRemovable() {
      return this._removable;
   }
}
