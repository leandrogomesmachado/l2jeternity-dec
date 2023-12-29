package l2e.gameserver.model.actor.events;

import l2e.gameserver.model.actor.Attackable;

public class AttackableEvents extends CharEvents {
   public AttackableEvents(Attackable activeChar) {
      super(activeChar);
   }

   public Attackable getActingPlayer() {
      return (Attackable)super.getActingPlayer();
   }
}
