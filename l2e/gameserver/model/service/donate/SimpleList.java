package l2e.gameserver.model.service.donate;

import java.util.List;

public class SimpleList {
   private final int _id;
   private final long _count;
   private final List<DonateItem> _items;

   public SimpleList(int id, long count, List<DonateItem> items) {
      this._id = id;
      this._count = count;
      this._items = items;
   }

   public int getId() {
      return this._id;
   }

   public long getCount() {
      return this._count;
   }

   public List<DonateItem> getList() {
      return this._items;
   }
}
