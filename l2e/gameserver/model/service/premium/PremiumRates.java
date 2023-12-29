package l2e.gameserver.model.service.premium;

public enum PremiumRates {
   EXP,
   SP,
   SIEGE,
   ADENA,
   DROP,
   ELEMENT_STONES,
   SPOIL,
   CRAFT,
   MASTERWORK_CRAFT,
   WEIGHT_LIMIT,
   QUEST_REWARD,
   QUEST_DROP,
   FISHING,
   DROP_RAID,
   ENCHANT,
   FAME,
   REFLECTION_REDUCE,
   SEAL_STONES,
   DROP_EPIC,
   MODIFIER_SEAL_STONES,
   MODIFIER_LIFE_STONES,
   MODIFIER_ENCHANT_SCROLLS,
   MODIFIER_FORGOTTEN_SCROLLS,
   MODIFIER_MATERIALS,
   MODIFIER_RECIPES,
   MODIFIER_BELTS,
   MODIFIER_BRACELETS,
   MODIFIER_CLOAKS,
   MODIFIER_CODEX,
   MODIFIER_ATT_STONES,
   MODIFIER_ATT_CRYSTALS,
   MODIFIER_ATT_JEWELS,
   MODIFIER_ATT_ENERGY,
   MODIFIER_ARMORS,
   MODIFIER_WEAPONS,
   MODIFIER_ACCESSORYES,
   MAX_SPOIL_PER_ONE_GROUP,
   MAX_DROP_PER_ONE_GROUP,
   MAX_DROP_RAID_PER_ONE_GROUP,
   MODIFIER_NOBLE_STONES;

   private static final PremiumRates[] VALUES = values();

   public static PremiumRates find(String name) {
      for(PremiumRates key : VALUES) {
         if (key.name().equalsIgnoreCase(name)) {
            return key;
         }
      }

      return null;
   }
}
