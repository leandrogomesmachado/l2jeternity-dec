package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Clan;

public class ClanLeaveEvent implements EventListener {
   private int _playerId;
   private Clan _clan;

   public int getPlayerId() {
      return this._playerId;
   }

   public void setPlayerId(int playerId) {
      this._playerId = playerId;
   }

   public Clan getClan() {
      return this._clan;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
   }
}
