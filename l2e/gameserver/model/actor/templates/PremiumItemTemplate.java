package l2e.gameserver.model.actor.templates;

public class PremiumItemTemplate {
   private final int _itemId;
   private long _count;
   private final String _sender;

   public PremiumItemTemplate(int itemid, long count, String sender) {
      this._itemId = itemid;
      this._count = count;
      this._sender = sender;
   }

   public void updateCount(long newcount) {
      this._count = newcount;
   }

   public int getId() {
      return this._itemId;
   }

   public long getCount() {
      return this._count;
   }

   public String getSender() {
      return this._sender;
   }
}
