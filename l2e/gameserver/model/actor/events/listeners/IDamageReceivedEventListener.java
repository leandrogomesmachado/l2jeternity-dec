package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public interface IDamageReceivedEventListener extends IEventListener {
   void onDamageReceivedEvent(Creature var1, Creature var2, double var3, Skill var5, boolean var6, boolean var7);
}
