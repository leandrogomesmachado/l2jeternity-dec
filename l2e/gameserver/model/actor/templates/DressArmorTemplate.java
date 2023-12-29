package l2e.gameserver.model.actor.templates;

public class DressArmorTemplate {
   private final int _id;
   private final String _name;
   private final boolean _checkEquip;
   private final boolean _isForKamael;
   private final int _chest;
   private final int _legs;
   private final int _gloves;
   private final int _feet;
   private final int _shield;
   private final int _cloak;
   private final int _hat;
   private final int _slot;
   private final int _priceId;
   private final long _priceCount;

   public DressArmorTemplate(
      int id,
      String name,
      boolean checkEquip,
      boolean isForKamael,
      int chest,
      int legs,
      int gloves,
      int feet,
      int shield,
      int cloak,
      int hat,
      int slot,
      int priceId,
      long priceCount
   ) {
      this._id = id;
      this._name = name;
      this._checkEquip = checkEquip;
      this._isForKamael = isForKamael;
      this._chest = chest;
      this._legs = legs;
      this._gloves = gloves;
      this._feet = feet;
      this._shield = shield;
      this._cloak = cloak;
      this._hat = hat;
      this._slot = slot;
      this._priceId = priceId;
      this._priceCount = priceCount;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public int getChest() {
      return this._chest;
   }

   public int getLegs() {
      return this._legs;
   }

   public int getGloves() {
      return this._gloves;
   }

   public int getFeet() {
      return this._feet;
   }

   public int getShieldId() {
      return this._shield;
   }

   public int getCloakId() {
      return this._cloak;
   }

   public int getHatId() {
      return this._hat;
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

   public boolean isForKamael() {
      return this._isForKamael;
   }

   public boolean isCheckEquip() {
      return this._checkEquip;
   }
}
