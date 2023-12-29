package l2e.gameserver.model.interfaces;

import java.util.Map;
import l2e.gameserver.model.skills.Skill;

public interface ISkillsHolder {
   Map<Integer, Skill> getSkills();

   Skill addSkill(Skill var1);

   Skill getKnownSkill(int var1);

   int getSkillLevel(int var1);
}
