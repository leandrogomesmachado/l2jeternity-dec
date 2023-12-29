package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IPKPointsChangeEventListener extends IEventListener {
   boolean onPKPointsChange(Player var1, int var2, int var3);
}
