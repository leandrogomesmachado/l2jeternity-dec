package l2e.scripts.ai.isle_of_prayer;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class Shade extends Fighter {
   private long _wait_timeout = 0L;
   private boolean _wait = false;
   private static final int DESPAWN_TIME = 300000;

   public Shade(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return false;
      } else {
         if (!this._wait) {
            this._wait = true;
            this._wait_timeout = System.currentTimeMillis() + 300000L;
         }

         if (this._wait_timeout != 0L && this._wait && this._wait_timeout < System.currentTimeMillis()) {
            actor.deleteMe();
            return true;
         } else {
            return super.thinkActive();
         }
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (killer != null) {
         Player player = killer.getActingPlayer();
         if (player != null) {
            Attackable actor = this.getActiveChar();
            if (Rnd.chance(10)) {
               actor.dropItem(player, 9595, 1L);
            }
         }
      }

      super.onEvtDead(killer);
   }
}
