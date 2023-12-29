package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IPlayerLoginEventListener extends IEventListener {
   void onPlayerLogin(Player var1);
}
