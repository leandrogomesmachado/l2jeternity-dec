package l2e.gameserver.model;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public final class ChanceCondition {
   private static final Logger _log = Logger.getLogger(ChanceCondition.class.getName());
   public static final int EVT_HIT = 1;
   public static final int EVT_CRIT = 2;
   public static final int EVT_CAST = 4;
   public static final int EVT_PHYSICAL = 8;
   public static final int EVT_MAGIC = 16;
   public static final int EVT_MAGIC_GOOD = 32;
   public static final int EVT_MAGIC_OFFENSIVE = 64;
   public static final int EVT_ATTACKED = 128;
   public static final int EVT_ATTACKED_HIT = 256;
   public static final int EVT_ATTACKED_CRIT = 512;
   public static final int EVT_HIT_BY_SKILL = 1024;
   public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
   public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
   public static final int EVT_EVADED_HIT = 8192;
   public static final int EVT_ON_START = 16384;
   public static final int EVT_ON_ACTION_TIME = 32768;
   public static final int EVT_ON_EXIT = 65536;
   private final ChanceCondition.TriggerType _triggerType;
   private final int _chance;
   private final int _mindmg;
   private final byte[] _elements;
   private final int[] _activationSkills;
   private final boolean _pvpOnly;

   private ChanceCondition(ChanceCondition.TriggerType trigger, int chance, int mindmg, byte[] elements, int[] activationSkills, boolean pvpOnly) {
      this._triggerType = trigger;
      this._chance = chance;
      this._mindmg = mindmg;
      this._elements = elements;
      this._pvpOnly = pvpOnly;
      this._activationSkills = activationSkills;
   }

   public static ChanceCondition parse(StatsSet set) {
      try {
         ChanceCondition.TriggerType trigger = set.getEnum("chanceType", ChanceCondition.TriggerType.class, null);
         int chance = set.getInteger("activationChance", -1);
         int mindmg = set.getInteger("activationMinDamage", -1);
         String elements = set.getString("activationElements", null);
         String activationSkills = set.getString("activationSkills", null);
         boolean pvpOnly = set.getBool("pvpChanceOnly", false);
         if (trigger != null) {
            return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly);
         }
      } catch (Exception var7) {
         _log.log(Level.WARNING, "", (Throwable)var7);
      }

      return null;
   }

   public static ChanceCondition parse(String chanceType, int chance, int mindmg, String elements, String activationSkills, boolean pvpOnly) {
      try {
         if (chanceType == null) {
            return null;
         }

         ChanceCondition.TriggerType trigger = Enum.valueOf(ChanceCondition.TriggerType.class, chanceType);
         if (trigger != null) {
            return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly);
         }
      } catch (Exception var7) {
         _log.log(Level.WARNING, "", (Throwable)var7);
      }

      return null;
   }

   public static final byte[] parseElements(String list) {
      if (list == null) {
         return null;
      } else {
         String[] valuesSplit = list.split(",");
         byte[] elements = new byte[valuesSplit.length];

         for(int i = 0; i < valuesSplit.length; ++i) {
            elements[i] = Byte.parseByte(valuesSplit[i]);
         }

         Arrays.sort(elements);
         return elements;
      }
   }

   public static final int[] parseActivationSkills(String list) {
      if (list == null) {
         return null;
      } else {
         String[] valuesSplit = list.split(",");
         int[] skillIds = new int[valuesSplit.length];

         for(int i = 0; i < valuesSplit.length; ++i) {
            skillIds[i] = Integer.parseInt(valuesSplit[i]);
         }

         return skillIds;
      }
   }

   public boolean trigger(int event, int damage, byte element, boolean playable, Skill skill) {
      if (this._pvpOnly && !playable) {
         return false;
      } else if (this._elements != null && Arrays.binarySearch(this._elements, element) < 0) {
         return false;
      } else if (this._activationSkills != null && skill != null && Arrays.binarySearch(this._activationSkills, skill.getId()) < 0) {
         return false;
      } else if (this._mindmg > -1 && this._mindmg > damage) {
         return false;
      } else {
         return this._triggerType.check(event) && (this._chance < 0 || Rnd.get(100) < this._chance);
      }
   }

   @Override
   public String toString() {
      return "Trigger[" + this._chance + ";" + this._triggerType.toString() + "]";
   }

   public static enum TriggerType {
      ON_HIT(1),
      ON_CRIT(2),
      ON_CAST(4),
      ON_PHYSICAL(8),
      ON_MAGIC(16),
      ON_MAGIC_GOOD(32),
      ON_MAGIC_OFFENSIVE(64),
      ON_ATTACKED(128),
      ON_ATTACKED_HIT(256),
      ON_ATTACKED_CRIT(512),
      ON_HIT_BY_SKILL(1024),
      ON_HIT_BY_OFFENSIVE_SKILL(2048),
      ON_HIT_BY_GOOD_MAGIC(4096),
      ON_EVADED_HIT(8192),
      ON_START(16384),
      ON_ACTION_TIME(32768),
      ON_EXIT(65536);

      private final int _mask;

      private TriggerType(int mask) {
         this._mask = mask;
      }

      public final boolean check(int event) {
         return (this._mask & event) != 0;
      }
   }
}
