package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Creature;

public class AttackEvent implements EventListener {
   private Creature _attacker;
   private Creature _target;

   public Creature getAttacker() {
      return this._attacker;
   }

   public void setAttacker(Creature attacker) {
      this._attacker = attacker;
   }

   public Creature getTarget() {
      return this._target;
   }

   public void setTarget(Creature target) {
      this._target = target;
   }
}
