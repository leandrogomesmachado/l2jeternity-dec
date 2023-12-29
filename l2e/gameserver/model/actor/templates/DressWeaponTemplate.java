package l2e.gameserver.model.actor.templates;

public class DressWeaponTemplate {
   private final int _id;
   private final String _name;
   private final String _type;
   private final int _priceId;
   private final long _priceCount;
   private final boolean _allowEnchant;
   private final boolean _allowAugment;

   public DressWeaponTemplate(int id, String name, String type, int priceId, long priceCount, boolean allowEnchant, boolean allowAugment) {
      this._id = id;
      this._name = name;
      this._type = type;
      this._priceId = priceId;
      this._priceCount = priceCount;
      this._allowEnchant = allowEnchant;
      this._allowAugment = allowAugment;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public String getType() {
      return this._type;
   }

   public int getPriceId() {
      return this._priceId;
   }

   public long getPriceCount() {
      return this._priceCount;
   }

   public boolean isAllowEnchant() {
      return this._allowEnchant;
   }

   public boolean isAllowAugment() {
      return this._allowAugment;
   }
}
