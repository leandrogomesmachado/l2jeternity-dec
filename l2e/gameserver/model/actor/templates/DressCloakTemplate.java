package l2e.gameserver.model.actor.templates;

public class DressCloakTemplate {
   private final int _id;
   private final int _cloak;
   private final String _name;
   private final int _priceId;
   private final long _priceCount;

   public DressCloakTemplate(int id, int cloak, String name, int priceId, long priceCount) {
      this._id = id;
      this._cloak = cloak;
      this._name = name;
      this._priceId = priceId;
      this._priceCount = priceCount;
   }

   public int getId() {
      return this._id;
   }

   public int getCloakId() {
      return this._cloak;
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
