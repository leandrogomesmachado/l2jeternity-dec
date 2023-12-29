package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public class HennaEvent implements EventListener {
   private Player _player;
   private Henna _henna;
   private boolean _add;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public Henna getHenna() {
      return this._henna;
   }

   public void setHenna(Henna henna) {
      this._henna = henna;
   }

   public boolean isAdd() {
      return this._add;
   }

   public void setAdd(boolean add) {
      this._add = add;
   }
}
