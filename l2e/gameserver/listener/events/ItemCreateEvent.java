package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;

public class ItemCreateEvent implements EventListener {
   private Player _player;
   private int _itemId;
   private String _process;
   private Object _reference;
   private long _count;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public int getId() {
      return this._itemId;
   }

   public void setId(int itemId) {
      this._itemId = itemId;
   }

   public String getProcess() {
      return this._process;
   }

   public void setProcess(String process) {
      this._process = process;
   }

   public Object getReference() {
      return this._reference;
   }

   public void setReference(Object reference) {
      this._reference = reference;
   }

   public long getCount() {
      return this._count;
   }

   public void setCount(long count) {
      this._count = count;
   }
}
