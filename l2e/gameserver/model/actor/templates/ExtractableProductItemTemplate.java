package l2e.gameserver.model.actor.templates;

import java.util.List;
import l2e.gameserver.model.holders.ItemHolder;

public class ExtractableProductItemTemplate {
   private final List<ItemHolder> _items;
   private final double _chance;

   public ExtractableProductItemTemplate(List<ItemHolder> items, double chance) {
      this._items = items;
      this._chance = chance;
   }

   public List<ItemHolder> getItems() {
      return this._items;
   }

   public double getChance() {
      return this._chance;
   }
}
