package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Playable;

public interface ILevelChangeEventListener extends IEventListener {
   boolean onLevelChange(Playable var1, byte var2);
}
