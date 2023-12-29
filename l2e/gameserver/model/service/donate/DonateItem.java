package l2e.gameserver.model.service.donate;

public class DonateItem {
   private final int _id;
   private final long _count;
   private final int _enchant;

   public DonateItem(int id, long count, int enchant) {
      this._id = id;
      this._count = count;
      this._enchant = enchant;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }

   public int getEnchant() {
      return this._enchant;
   }
}
