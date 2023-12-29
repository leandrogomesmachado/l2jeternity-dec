package l2e.gameserver.model.holders;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.skills.Skill;

public class PlayerSkillHolder implements ISkillsHolder {
   private final Map<Integer, Skill> _skills = new HashMap<>();

   public PlayerSkillHolder(Player player) {
      for(Skill skill : player.getSkills().values()) {
         if (SkillTreesParser.getInstance().isSkillAllowed(player, skill)) {
            this.addSkill(skill);
         }
      }
   }

   @Override
   public Map<Integer, Skill> getSkills() {
      return this._skills;
   }

   @Override
   public Skill addSkill(Skill skill) {
      return this._skills.put(skill.getId(), skill);
   }

   @Override
   public int getSkillLevel(int skillId) {
      Skill skill = this.getKnownSkill(skillId);
      return skill == null ? -1 : skill.getLevel();
   }

   @Override
   public Skill getKnownSkill(int skillId) {
      return this._skills.get(skillId);
   }
}
