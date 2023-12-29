package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Creature;

public class DeathEvent implements EventListener {
   private Creature _victim;
   private Creature _killer;

   public Creature getVictim() {
      return this._victim;
   }

   public void setVictim(Creature victim) {
      this._victim = victim;
   }

   public Creature getKiller() {
      return this._killer;
   }

   public void setKiller(Creature killer) {
      this._killer = killer;
   }
}
