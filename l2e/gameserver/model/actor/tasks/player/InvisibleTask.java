package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;

public class InvisibleTask implements Runnable {
   private final Player _player;

   public InvisibleTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         Effect eInvis = this._player.getFirstEffect(EffectType.INVINCIBLE);
         if (eInvis != null) {
            eInvis.exit();
         }

         this._player.startHealBlocked(false);
         this._player.setIsInvul(false);
      }
   }
}
