package l2e.gameserver.model.reward;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.templates.items.Item;

public class RewardItem {
   public final int _itemId;
   public long _count;

   public RewardItem(int itemId) {
      this._itemId = itemId;
      this._count = 1L;
   }

   public boolean isHerb() {
      Item item = ItemsParser.getInstance().getTemplate(this._itemId);
      return item == null ? false : item.is_ex_immediate_effect();
   }

   public boolean isAdena() {
      Item item = ItemsParser.getInstance().getTemplate(this._itemId);
      return item == null ? false : item.isAdena();
   }
}
