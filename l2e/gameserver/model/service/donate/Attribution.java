package l2e.gameserver.model.service.donate;

public class Attribution {
   private final int _id;
   private final long _count;
   private final int _value;
   private final int _size;

   public Attribution(int id, long count, int value, int size) {
      this._id = id;
      this._count = count;
      this._value = value;
      this._size = size;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }

   public int getValue() {
      return this._value;
   }

   public int getSize() {
      return this._size;
   }
}
