package l2e.gameserver.model.service.premium;

public class PremiumPrice {
   private final int _id;
   private final long _count;

   public PremiumPrice(int id, long count) {
      this._id = id;
      this._count = count;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }
}
