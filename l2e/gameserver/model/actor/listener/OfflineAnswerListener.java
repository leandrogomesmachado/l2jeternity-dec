package l2e.gameserver.model.actor.listener;

import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;

public class OfflineAnswerListener implements OnAnswerListener {
   private final Player _player;

   public OfflineAnswerListener(Player player) {
      this._player = player;
      this._player.stopOnlineRewardTask();
   }

   @Override
   public void sayYes() {
      if (this._player != null && this._player.isOnline()) {
         this._player.logout(true);
      }
   }

   @Override
   public void sayNo() {
      if (this._player != null && this._player.isOnline()) {
         this._player.logout(false);
      }
   }
}
