package l2e.gameserver.listener.player;

import java.util.List;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ItemCreateEvent;

public abstract class NewItemListener extends AbstractListener {
   private final List<Integer> _itemIds;

   public NewItemListener(List<Integer> itemIds) {
      this._itemIds = itemIds;
      this.register();
   }

   public abstract boolean onCreate(ItemCreateEvent var1);

   @Override
   public void register() {
      ItemsParser.addNewItemListener(this);
   }

   @Override
   public void unregister() {
      ItemsParser.removeNewItemListener(this);
   }

   public boolean containsItemId(int itemId) {
      return this._itemIds.contains(itemId);
   }
}
