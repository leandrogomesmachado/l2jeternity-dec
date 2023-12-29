package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import l2e.gameserver.model.base.AcquireSkillType;

public final class AcquireSkillList extends GameServerPacket {
   private final List<AcquireSkillList.Skill> _skills;
   private final AcquireSkillType _skillType;

   public AcquireSkillList(AcquireSkillType type) {
      this._skillType = type;
      this._skills = new ArrayList<>();
   }

   public void addSkill(int id, int gtLvl, int nextLevel, int maxLevel, int spCost, int requirements) {
      this._skills.add(new AcquireSkillList.Skill(id, gtLvl, nextLevel, maxLevel, spCost, requirements));
   }

   @Override
   protected void writeImpl() {
      if (!this._skills.isEmpty()) {
         Collections.sort(this._skills, new AcquireSkillList.SortSkillInfo());
         this.writeD(this._skillType.ordinal());
         this.writeD(this._skills.size());

         for(AcquireSkillList.Skill temp : this._skills) {
            this.writeD(temp.id);
            this.writeD(temp.nextLevel);
            this.writeD(temp.maxLevel);
            this.writeD(temp.spCost);
            this.writeD(temp.requirements);
            if (this._skillType == AcquireSkillType.SUBPLEDGE) {
               this.writeD(2002);
            }
         }
      }
   }

   private static class Skill {
      public int id;
      public int getLvl;
      public int nextLevel;
      public int maxLevel;
      public int spCost;
      public int requirements;

      public Skill(int pId, int gtLvl, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements) {
         this.id = pId;
         this.getLvl = gtLvl;
         this.nextLevel = pNextLevel;
         this.maxLevel = pMaxLevel;
         this.spCost = pSpCost;
         this.requirements = pRequirements;
      }
   }

   private static class SortSkillInfo implements Comparator<AcquireSkillList.Skill> {
      private SortSkillInfo() {
      }

      public int compare(AcquireSkillList.Skill o1, AcquireSkillList.Skill o2) {
         return o1.getLvl - o2.getLvl;
      }
   }
}
