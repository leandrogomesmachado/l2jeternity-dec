package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Player;

public class StandUpTask implements Runnable {
   private final Player _player;

   public StandUpTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.setIsSitting(false);
         this._player.getAI().setIntention(CtrlIntention.IDLE);
      }
   }
}
