package l2e.gameserver.model.actor.templates;

public class DressHatTemplate {
   private final int _id;
   private final int _hat;
   private final String _name;
   private final int _slot;
   private final int _priceId;
   private final long _priceCount;

   public DressHatTemplate(int id, int hat, String name, int slot, int priceId, long priceCount) {
      this._id = id;
      this._hat = hat;
      this._name = name;
      this._slot = slot;
      this._priceId = priceId;
      this._priceCount = priceCount;
   }

   public int getId() {
      return this._id;
   }

   public int getHatId() {
      return this._hat;
   }

   public String getName() {
      return this._name;
   }

   public int getSlot() {
      return this._slot;
   }

   public int getPriceId() {
      return this._priceId;
   }

   public long getPriceCount() {
      return this._priceCount;
   }
}
