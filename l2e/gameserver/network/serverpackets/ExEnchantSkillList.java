package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillList extends GameServerPacket {
   private final ExEnchantSkillList.EnchantSkillType _type;
   private final List<ExEnchantSkillList.Skill> _skills;

   public void addSkill(int id, int level) {
      this._skills.add(new ExEnchantSkillList.Skill(id, level));
   }

   public ExEnchantSkillList(ExEnchantSkillList.EnchantSkillType type) {
      this._type = type;
      this._skills = new ArrayList<>();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type.ordinal());
      this.writeD(this._skills.size());

      for(ExEnchantSkillList.Skill sk : this._skills) {
         this.writeD(sk.id);
         this.writeD(sk.nextLevel);
      }
   }

   public static enum EnchantSkillType {
      NORMAL,
      SAFE,
      UNTRAIN,
      CHANGE_ROUTE;
   }

   static class Skill {
      public int id;
      public int nextLevel;

      Skill(int pId, int pNextLevel) {
         this.id = pId;
         this.nextLevel = pNextLevel;
      }
   }
}
