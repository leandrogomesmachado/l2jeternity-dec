package l2e.gameserver.model.actor.events.listeners;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public interface ISkillUseEventListener extends IEventListener {
   boolean onSkillUse(Creature var1, Skill var2, boolean var3, Creature var4, GameObject[] var5);
}
