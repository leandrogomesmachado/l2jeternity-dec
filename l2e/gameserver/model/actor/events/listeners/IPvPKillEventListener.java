package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IPvPKillEventListener extends IEventListener {
   void onPvPKill(Player var1, Player var2);
}
