package l2e.gameserver.model.actor.templates;

public class DressShieldTemplate {
   private final int _id;
   private final int _shield;
   private final String _name;
   private final int _priceId;
   private final long _priceCount;

   public DressShieldTemplate(int id, int shield, String name, int priceId, long priceCount) {
      this._id = id;
      this._shield = shield;
      this._name = name;
      this._priceId = priceId;
      this._priceCount = priceCount;
   }

   public int getId() {
      return this._id;
   }

   public int getShieldId() {
      return this._shield;
   }

   public String getName() {
      return this._name;
   }

   public int getPriceId() {
      return this._priceId;
   }

   public long getPriceCount() {
      return this._priceCount;
   }
}
