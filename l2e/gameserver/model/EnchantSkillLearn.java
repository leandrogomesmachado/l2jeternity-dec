package l2e.gameserver.model;

import java.util.Set;
import java.util.TreeMap;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;

public final class EnchantSkillLearn {
   private final int _id;
   private final int _baseLvl;
   private final TreeMap<Integer, Integer> _enchantRoutes = new TreeMap<>();

   public EnchantSkillLearn(int id, int baseLvl) {
      this._id = id;
      this._baseLvl = baseLvl;
   }

   public void addNewEnchantRoute(int route, int group) {
      this._enchantRoutes.put(route, group);
   }

   public int getId() {
      return this._id;
   }

   public int getBaseLevel() {
      return this._baseLvl;
   }

   public static int getEnchantRoute(int level) {
      return (int)Math.floor((double)(level / 100));
   }

   public static int getEnchantIndex(int level) {
      return level % 100 - 1;
   }

   public static int getEnchantType(int level) {
      return (level - 1) / 100 - 1;
   }

   public EnchantSkillGroup getFirstRouteGroup() {
      return EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(this._enchantRoutes.firstEntry().getValue());
   }

   public Set<Integer> getAllRoutes() {
      return this._enchantRoutes.keySet();
   }

   public int getMinSkillLevel(int level) {
      return level % 100 == 1 ? this._baseLvl : level - 1;
   }

   public boolean isMaxEnchant(int level) {
      int enchantType = getEnchantRoute(level);
      if (enchantType >= 1 && this._enchantRoutes.containsKey(enchantType)) {
         int index = getEnchantIndex(level);
         return index + 1
            >= EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(this._enchantRoutes.get(enchantType)).getEnchantGroupDetails().size();
      } else {
         return false;
      }
   }

   public EnchantSkillGroup.EnchantSkillsHolder getEnchantSkillsHolder(int level) {
      int enchantType = getEnchantRoute(level);
      if (enchantType >= 1 && this._enchantRoutes.containsKey(enchantType)) {
         int index = getEnchantIndex(level);
         EnchantSkillGroup group = EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(this._enchantRoutes.get(enchantType));
         if (index < 0) {
            return group.getEnchantGroupDetails().get(0);
         } else {
            return index >= group.getEnchantGroupDetails().size()
               ? group.getEnchantGroupDetails()
                  .get(
                     EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(this._enchantRoutes.get(enchantType)).getEnchantGroupDetails().size() - 1
                  )
               : group.getEnchantGroupDetails().get(index);
         }
      } else {
         return null;
      }
   }
}
