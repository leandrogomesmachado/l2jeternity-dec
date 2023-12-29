package l2e.gameserver.model.actor.templates;

public final class SeedTemplate {
   final int _seedId;
   long _residual;
   final long _price;
   final long _sales;

   public SeedTemplate(int id) {
      this._seedId = id;
      this._residual = 0L;
      this._price = 0L;
      this._sales = 0L;
   }

   public SeedTemplate(int id, long amount, long price, long sales) {
      this._seedId = id;
      this._residual = amount;
      this._price = price;
      this._sales = sales;
   }

   public int getId() {
      return this._seedId;
   }

   public long getCanProduce() {
      return this._residual;
   }

   public long getPrice() {
      return this._price;
   }

   public long getStartProduce() {
      return this._sales;
   }

   public void setCanProduce(long amount) {
      this._residual = amount;
   }
}
