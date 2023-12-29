package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IFamePointsChangeEventListener extends IEventListener {
   boolean onFamePointsChange(Player var1, int var2, int var3);
}
