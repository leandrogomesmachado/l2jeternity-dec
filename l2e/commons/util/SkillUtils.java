package l2e.commons.util;

import l2e.gameserver.data.parser.SkillsParser;

public final class SkillUtils {
   public static int generateSkillHashCode(int id, int level) {
      return id * 1000 + level;
   }

   public static int getSkillLevel(int enchantType, int enchantLevel) {
      return 100 * enchantType + enchantLevel;
   }

   public static boolean isEnchantedSkill(int level) {
      return getSkillEnchantLevel(level) > 0;
   }

   public static int getSkillEnchantType(int level) {
      return level / 100;
   }

   public static int getSkillEnchantLevel(int level) {
      return level > 100 ? level % 100 : 0;
   }

   public static int getSkillLevelMask(int skillLevel, int subSkillLevel) {
      return skillLevel | subSkillLevel << 16;
   }

   public static int getSkillLevelFromMask(int skillLevelMask) {
      int mask = 65535;
      return 65535 & skillLevelMask;
   }

   public static int getSubSkillLevelFromMask(int skillLevelMask) {
      int mask = 65535;
      return 65535 & skillLevelMask >>> 16;
   }

   public static int convertHFSkillLevelToGODMask(int id, int level) {
      int enchantLevel = getSkillEnchantLevel(level);
      if (enchantLevel != 0) {
         int baseLevel = SkillsParser.getInstance().getMaxLevel(id);
         int subLevel = getSkillEnchantType(level) * 1000 + enchantLevel;
         return getSkillLevelMask(baseLevel, subLevel);
      } else {
         return level;
      }
   }

   public static int convertGODSkillLevelToHF(int id, int levelMask) {
      return convertGODSkillLevelToHF(id, getSkillLevelFromMask(levelMask), getSubSkillLevelFromMask(levelMask));
   }

   public static int convertGODSkillLevelToHF(int id, int level, int subLevel) {
      return subLevel != 0 && SkillsParser.getInstance().getMaxLevel(id) == level ? subLevel / 1000 * 100 + subLevel % 1000 : level;
   }

   public static int convertHFSkillLevelToGOD(int id, int level) {
      return level > 100 ? SkillsParser.getInstance().getMaxLevel(id) : level;
   }

   public static int convertHFSkillLevelToGODSubLevel(int id, int level) {
      int enchantLevel = getSkillEnchantLevel(level);
      return enchantLevel != 0 ? getSkillEnchantType(level) * 1000 + enchantLevel : 0;
   }
}
