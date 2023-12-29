package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IPlayerLogoutEventListener extends IEventListener {
   void onPlayerLogout(Player var1);
}
