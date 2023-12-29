package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Creature;

public interface IDeathEventListener extends IEventListener {
   boolean onDeath(Creature var1, Creature var2);
}
