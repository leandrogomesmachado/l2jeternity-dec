package l2e.gameserver.model.actor.templates;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.templates.items.Item;

public class ProductItemTemplate {
   private final int _itemId;
   private final int _count;
   private final int _weight;
   private final boolean _dropable;

   public ProductItemTemplate(int item_id, int count) {
      this._itemId = item_id;
      this._count = count;
      Item item = ItemsParser.getInstance().getTemplate(item_id);
      if (item != null) {
         this._weight = item.getWeight();
         this._dropable = item.isDropable();
      } else {
         this._weight = 0;
         this._dropable = true;
      }
   }

   public int getId() {
      return this._itemId;
   }

   public int getCount() {
      return this._count;
   }

   public int getWeight() {
      return this._weight;
   }

   public boolean isDropable() {
      return this._dropable;
   }
}
