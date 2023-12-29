package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Clan;

public class ClanCreationEvent implements EventListener {
   private Clan _clan;

   public Clan getClan() {
      return this._clan;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
   }
}
