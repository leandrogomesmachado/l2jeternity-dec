package l2e.gameserver.model;

public enum PcCondOverride {
   MAX_STATS_VALUE(0, "PcCondOverride.MAX_STATS_VALUE"),
   ITEM_CONDITIONS(1, "PcCondOverride.ITEM_CONDITIONS"),
   SKILL_CONDITIONS(2, "PcCondOverride.SKILL_CONDITIONS"),
   ZONE_CONDITIONS(3, "PcCondOverride.ZONE_CONDITIONS"),
   CASTLE_CONDITIONS(4, "PcCondOverride.CASTLE_CONDITIONS"),
   FORTRESS_CONDITIONS(5, "PcCondOverride.FORTRESS_CONDITIONS"),
   CLANHALL_CONDITIONS(6, "PcCondOverride.CLANHALL_CONDITIONS"),
   FLOOD_CONDITIONS(7, "PcCondOverride.FLOOD_CONDITIONS"),
   CHAT_CONDITIONS(8, "PcCondOverride.CHAT_CONDITIONS"),
   INSTANCE_CONDITIONS(9, "PcCondOverride.INSTANCE_CONDITIONS"),
   QUEST_CONDITIONS(10, "PcCondOverride.QUEST_CONDITIONS"),
   DEATH_PENALTY(11, "PcCondOverride.DEATH_PENALTY"),
   DESTROY_ALL_ITEMS(12, "PcCondOverride.DESTROY_ALL_ITEMS"),
   SEE_ALL_PLAYERS(13, "PcCondOverride.SEE_ALL_PLAYERS"),
   TARGET_ALL(14, "PcCondOverride.TARGET_ALL"),
   DROP_ALL_ITEMS(15, "PcCondOverride.DROP_ALL_ITEMS");

   private final int _mask;
   private final String _descr;

   private PcCondOverride(int id, String descr) {
      this._mask = 1 << id;
      this._descr = descr;
   }

   public int getMask() {
      return this._mask;
   }

   public String getDescription() {
      return this._descr;
   }

   public static PcCondOverride getCondOverride(int ordinal) {
      try {
         return values()[ordinal];
      } catch (Exception var2) {
         return null;
      }
   }

   public static long getAllExceptionsMask() {
      long result = 0L;

      for(PcCondOverride ex : values()) {
         result |= (long)ex.getMask();
      }

      return result;
   }
}
