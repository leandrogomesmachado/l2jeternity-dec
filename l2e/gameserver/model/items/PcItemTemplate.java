package l2e.gameserver.model.items;

import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.stats.StatsSet;

public final class PcItemTemplate extends ItemHolder {
   private final boolean _equipped;
   private final int _enchant;
   private final int _augmentId;
   private final String _elementals;
   private final int _durability;

   public PcItemTemplate(StatsSet set) {
      super(set.getInteger("id"), set.getLong("count"));
      this._equipped = set.getBool("equipped", false);
      this._enchant = set.getInteger("enchant", 0);
      this._augmentId = set.getInteger("augmentId", -1);
      this._elementals = set.getString("elementals", null);
      this._durability = set.getInteger("durability", 0);
   }

   public boolean isEquipped() {
      return this._equipped;
   }

   public int getEnchant() {
      return this._enchant;
   }

   public int getAugmentId() {
      return this._augmentId;
   }

   public String getElementals() {
      return this._elementals;
   }

   public int getDurability() {
      return this._durability;
   }
}
