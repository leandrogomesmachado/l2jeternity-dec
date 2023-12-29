package l2e.gameserver.model.service.buffer;

public class SingleBuff {
   private final String _buffType;
   private final int _buffId;
   private final int _buffLevel;
   private final int _premiumBuffLevel;
   private final int _buffTime;
   private final int _premiumBuffTime;
   private final boolean _isDanceSlot;
   private final int[][] _requestItems;
   private final boolean _needAllItems;
   private final boolean _isBuffForItems;
   private final boolean _removeItems;

   public SingleBuff(
      String buffType,
      int buffId,
      int buffLevel,
      int premiumBuffLevel,
      int buffTime,
      int premiumBuffTime,
      boolean isDanceSlot,
      int[][] requestItems,
      boolean needAllItems,
      boolean removeItems
   ) {
      this._buffType = buffType;
      this._buffId = buffId;
      this._buffLevel = buffLevel;
      this._premiumBuffLevel = premiumBuffLevel;
      this._buffTime = buffTime;
      this._premiumBuffTime = premiumBuffTime;
      this._isDanceSlot = isDanceSlot;
      this._requestItems = requestItems;
      this._needAllItems = needAllItems;
      this._removeItems = removeItems;
      this._isBuffForItems = this._requestItems != null;
   }

   public String getBuffType() {
      return this._buffType;
   }

   public int getSkillId() {
      return this._buffId;
   }

   public int getLevel() {
      return this._buffLevel;
   }

   public int getPremiumLevel() {
      return this._premiumBuffLevel;
   }

   public boolean isDanceSlot() {
      return this._isDanceSlot;
   }

   public int getBuffTime() {
      return this._buffTime;
   }

   public int getPremiumBuffTime() {
      return this._premiumBuffTime;
   }

   public int[][] getRequestItems() {
      return this._requestItems;
   }

   public boolean needAllItems() {
      return this._needAllItems;
   }

   public boolean isBuffForItems() {
      return this._isBuffForItems;
   }

   public boolean isRemoveItems() {
      return this._removeItems;
   }
}
