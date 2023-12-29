package l2e.gameserver.model.service.donate;

public class Enchant {
   private final int _id;
   private final long _count;
   private final int _value;

   public Enchant(int id, long count, int value) {
      this._id = id;
      this._count = count;
      this._value = value;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }

   public int getEnchant() {
      return this._value;
   }
}
