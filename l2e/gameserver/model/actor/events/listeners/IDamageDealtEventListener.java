package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public interface IDamageDealtEventListener extends IEventListener {
   void onDamageDealtEvent(Creature var1, Creature var2, double var3, Skill var5, boolean var6, boolean var7);
}
