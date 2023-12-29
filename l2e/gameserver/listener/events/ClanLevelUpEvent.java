package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Clan;

public class ClanLevelUpEvent implements EventListener {
   private Clan _clan;
   private int _oldLevel;

   public Clan getClan() {
      return this._clan;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
   }

   public int getOldLevel() {
      return this._oldLevel;
   }

   public void setOldLevel(int oldLevel) {
      this._oldLevel = oldLevel;
   }
}
