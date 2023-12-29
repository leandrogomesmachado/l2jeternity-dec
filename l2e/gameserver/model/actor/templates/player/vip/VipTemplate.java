package l2e.gameserver.model.actor.templates.player.vip;

import java.util.List;
import l2e.gameserver.model.holders.ItemHolder;

public class VipTemplate {
   private final int _id;
   private final long _points;
   private final double _expRate;
   private final double _spRate;
   private final double _adenaRate;
   private final double _dropRate;
   private final double _dropRaidRate;
   private final double _spoilRate;
   private final double _epRate;
   private final int _enchantChance;
   private final List<ItemHolder> _items;
   private final List<ItemHolder> _requestItems;

   public VipTemplate(
      int id,
      long points,
      double expRate,
      double spRate,
      double adenaRate,
      double dropRate,
      double dropRaidRate,
      double spoilRate,
      double epRate,
      int enchantChance,
      List<ItemHolder> items,
      List<ItemHolder> requestItems
   ) {
      this._id = id;
      this._points = points;
      this._expRate = expRate;
      this._spRate = spRate;
      this._adenaRate = adenaRate;
      this._dropRate = dropRate;
      this._dropRaidRate = dropRaidRate;
      this._spoilRate = spoilRate;
      this._epRate = epRate;
      this._enchantChance = enchantChance;
      this._items = items;
      this._requestItems = requestItems;
   }

   public int getId() {
      return this._id;
   }

   public long getPoints() {
      return this._points;
   }

   public double getExpRate() {
      return this._expRate;
   }

   public double getSpRate() {
      return this._spRate;
   }

   public double getAdenaRate() {
      return this._adenaRate;
   }

   public double getDropRate() {
      return this._dropRate;
   }

   public double getDropRaidRate() {
      return this._dropRaidRate;
   }

   public double getSpoilRate() {
      return this._spoilRate;
   }

   public double getEpRate() {
      return this._epRate;
   }

   public int getEnchantChance() {
      return this._enchantChance;
   }

   public boolean haveRewards() {
      return this._items != null && !this._items.isEmpty();
   }

   public List<ItemHolder> getDailyItems() {
      return this._items;
   }

   public List<ItemHolder> getRequestItems() {
      return this._requestItems;
   }
}
