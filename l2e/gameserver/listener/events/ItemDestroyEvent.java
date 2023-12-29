package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemDestroyEvent implements EventListener {
   private ItemInstance _item;
   private Player _player;

   public ItemInstance getItem() {
      return this._item;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }
}
