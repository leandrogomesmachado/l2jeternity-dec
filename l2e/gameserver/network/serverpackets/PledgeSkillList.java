package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.skills.Skill;

public class PledgeSkillList extends GameServerPacket {
   private final Skill[] _skills;
   private final PledgeSkillList.SubPledgeSkill[] _subSkills;

   public PledgeSkillList(Clan clan) {
      this._skills = clan.getAllSkills();
      this._subSkills = clan.getAllSubSkills();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._skills.length);
      this.writeD(this._subSkills.length);

      for(Skill sk : this._skills) {
         this.writeD(sk.getDisplayId());
         this.writeD(sk.getDisplayLevel());
      }

      for(PledgeSkillList.SubPledgeSkill sk : this._subSkills) {
         this.writeD(sk._subType);
         this.writeD(sk._skillId);
         this.writeD(sk._skillLvl);
      }
   }

   public static class SubPledgeSkill {
      int _subType;
      int _skillId;
      int _skillLvl;

      public SubPledgeSkill(int subType, int skillId, int skillLvl) {
         this._subType = subType;
         this._skillId = skillId;
         this._skillLvl = skillLvl;
      }
   }
}
