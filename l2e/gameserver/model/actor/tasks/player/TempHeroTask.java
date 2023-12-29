package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public class TempHeroTask implements Runnable {
   private final Player _player;

   public TempHeroTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.unsetVar("tempHero");
         this._player.setHero(false, false);
         if (this._player.getClan() != null) {
            this._player.setPledgeClass(ClanMember.calculatePledgeClass(this._player));
         } else {
            this._player.setPledgeClass(this._player.isNoble() ? 5 : 1);
         }
      }
   }
}
