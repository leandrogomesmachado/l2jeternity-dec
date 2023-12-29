package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.AbnormalEffect;

public class CheckBotTask implements Runnable {
   private final Player _player;

   public CheckBotTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (this._player.isParalyzed()) {
            this._player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
            this._player.setIsParalyzed(false);
         }

         this._player.setIsInvul(false);
         this._player.kick();
      }
   }
}
