package l2e.gameserver.model.actor.templates.player.online;

import java.util.List;
import l2e.gameserver.model.holders.ItemHolder;

public class OnlineRewardTemplate {
   private final int _id;
   private final int _minutes;
   private final List<ItemHolder> _items;
   private final boolean _printItem;

   public OnlineRewardTemplate(int id, int minutes, List<ItemHolder> items, boolean printItem) {
      this._id = id;
      this._minutes = minutes;
      this._items = items;
      this._printItem = printItem;
   }

   public int getId() {
      return this._id;
   }

   public int getMinutes() {
      return this._minutes;
   }

   public boolean haveRewards() {
      return this._items != null && !this._items.isEmpty();
   }

   public List<ItemHolder> getRewards() {
      return this._items;
   }

   public boolean isPrintItem() {
      return this._printItem;
   }
}
