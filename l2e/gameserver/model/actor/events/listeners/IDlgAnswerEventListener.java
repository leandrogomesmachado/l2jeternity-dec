package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Player;

public interface IDlgAnswerEventListener extends IEventListener {
   boolean onDlgAnswer(Player var1, int var2, int var3, int var4);
}
