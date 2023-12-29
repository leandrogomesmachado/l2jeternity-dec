package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Playable;

public interface IExperienceReceivedEventListener extends IEventListener {
   boolean onExperienceReceived(Playable var1, long var2);
}
