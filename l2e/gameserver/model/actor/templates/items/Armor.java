package l2e.gameserver.model.actor.templates.items;

import l2e.commons.util.StringUtil;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public final class Armor extends Item {
   private SkillHolder _enchant4Skill = null;
   private ArmorType _type;

   public Armor(StatsSet set) {
      super(set);
      this._type = ArmorType.valueOf(set.getString("armor_type", "none").toUpperCase());
      int _bodyPart = this.getBodyPart();
      if (_bodyPart != 8 && (_bodyPart & 4) == 0 && (_bodyPart & 32) == 0 && (_bodyPart & 1048576) == 0 && (_bodyPart & 2097152) == 0) {
         if (this._type == ArmorType.NONE && this.getBodyPart() == 256) {
            this._type = ArmorType.SHIELD;
         }

         this._type1 = 1;
         this._type2 = 1;
      } else {
         this._type1 = 0;
         this._type2 = 2;
      }

      String skill = set.getString("enchant4_skill", null);
      if (skill != null) {
         String[] info = skill.split("-");
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var8) {
               _log.info(StringUtil.concat("> Couldnt parse ", skill, " in armor enchant skills! item ", this.toString()));
            }

            if (id > 0 && level > 0) {
               this._enchant4Skill = new SkillHolder(id, level);
            }
         }
      }
   }

   public ArmorType getItemType() {
      return this._type;
   }

   @Override
   public final int getItemMask() {
      return this.getItemType().mask();
   }

   @Override
   public Skill getEnchant4Skill() {
      return this._enchant4Skill == null ? null : this._enchant4Skill.getSkill();
   }
}
