package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public final class SkillList extends GameServerPacket {
   private final List<SkillList.Skill> _skills = new ArrayList<>();

   public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted) {
      this._skills.add(new SkillList.Skill(id, level, passive, disabled, enchanted));
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._skills.size());

      for(SkillList.Skill temp : this._skills) {
         this.writeD(temp.passive ? 1 : 0);
         this.writeD(temp.level);
         this.writeD(temp.id);
         this.writeC(temp.disabled ? 1 : 0);
         this.writeC(temp.enchanted ? 1 : 0);
      }
   }

   static class Skill {
      public int id;
      public int level;
      public boolean passive;
      public boolean disabled;
      public boolean enchanted;

      Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted) {
         this.id = pId;
         this.level = pLevel;
         this.passive = pPassive;
         this.disabled = pDisabled;
         this.enchanted = pEnchanted;
      }
   }
}
