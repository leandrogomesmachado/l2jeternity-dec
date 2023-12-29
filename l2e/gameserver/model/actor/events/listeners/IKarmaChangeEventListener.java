package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IKarmaChangeEventListener extends IEventListener {
   boolean onKarmaChange(Player var1, int var2, int var3);
}
