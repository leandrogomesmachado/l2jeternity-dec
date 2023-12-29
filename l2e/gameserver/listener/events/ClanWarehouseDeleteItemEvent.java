package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ClanWarehouseDeleteItemEvent implements EventListener {
   private String _process;
   private ItemInstance _item;
   private Player _actor;
   private long _count;

   public String getProcess() {
      return this._process;
   }

   public void setProcess(String process) {
      this._process = process;
   }

   public ItemInstance getItem() {
      return this._item;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public Player getActor() {
      return this._actor;
   }

   public void setActor(Player actor) {
      this._actor = actor;
   }

   public long getCount() {
      return this._count;
   }

   public void setCount(long count) {
      this._count = count;
   }
}
