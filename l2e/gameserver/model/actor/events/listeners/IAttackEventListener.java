package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Creature;

public interface IAttackEventListener extends IEventListener {
   boolean onAttack(Creature var1, Creature var2);
}
