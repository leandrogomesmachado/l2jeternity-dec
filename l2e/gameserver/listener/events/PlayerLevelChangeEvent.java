package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;

public class PlayerLevelChangeEvent implements EventListener {
   private Player _player;
   private int _oldLevel;
   private int _newLevel;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public int getOldLevel() {
      return this._oldLevel;
   }

   public void setOldLevel(int oldLevel) {
      this._oldLevel = oldLevel;
   }

   public int getNewLevel() {
      return this._newLevel;
   }

   public void setNewLevel(int newLevel) {
      this._newLevel = newLevel;
   }
}
