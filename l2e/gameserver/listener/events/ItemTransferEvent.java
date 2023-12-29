package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;

public class ItemTransferEvent implements EventListener {
   private ItemInstance _item;
   private Player _player;
   private ItemContainer _target;

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

   public ItemContainer getTarget() {
      return this._target;
   }

   public void setTarget(ItemContainer target) {
      this._target = target;
   }
}
