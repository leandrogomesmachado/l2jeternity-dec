package l2e.gameserver.model;

import l2e.gameserver.data.parser.CategoryParser;

public enum MountType {
   NONE,
   STRIDER,
   WYVERN,
   WOLF;

   public static MountType findByNpcId(int npcId) {
      if (CategoryParser.getInstance().isInCategory(CategoryType.STRIDER, npcId)) {
         return STRIDER;
      } else if (CategoryParser.getInstance().isInCategory(CategoryType.WYVERN_GROUP, npcId)) {
         return WYVERN;
      } else {
         return CategoryParser.getInstance().isInCategory(CategoryType.WOLF_GROUP, npcId) ? WOLF : NONE;
      }
   }
}
