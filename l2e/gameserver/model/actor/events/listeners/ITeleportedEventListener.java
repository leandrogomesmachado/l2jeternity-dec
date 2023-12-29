package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Creature;

public interface ITeleportedEventListener extends IEventListener {
   void onTeleported(Creature var1);
}
