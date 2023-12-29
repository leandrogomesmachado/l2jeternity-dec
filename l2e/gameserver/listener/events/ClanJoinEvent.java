package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;

public class ClanJoinEvent implements EventListener {
   private Player _player;
   private Clan _clan;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public Clan getClan() {
      return this._clan;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
   }
}
