package l2e.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.holders.AdditionalItemHolder;
import l2e.gameserver.model.holders.AdditionalSkillHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.serverpackets.ExBasicActionList;

public final class TransformTemplate {
   private final double _collisionRadius;
   private final double _collisionHeight;
   private final WeaponType _baseAttackType;
   private final int _baseAttackRange;
   private final double _baseRandomDamage;
   private List<SkillHolder> _skills;
   private List<AdditionalSkillHolder> _additionalSkills;
   private List<AdditionalItemHolder> _additionalItems;
   private Map<Integer, Integer> _baseDefense;
   private Map<Integer, Double> _baseStats;
   private Map<Integer, Float> _baseSpeed;
   private ExBasicActionList _list;
   private final Map<Integer, TransformLevelData> _data = new LinkedHashMap<>(100);

   public TransformTemplate(StatsSet set) {
      this._collisionRadius = set.getDouble("radius", 0.0);
      this._collisionHeight = set.getDouble("height", 0.0);
      this._baseAttackType = WeaponType.findByName(set.getString("attackType", "FIST"));
      this._baseAttackRange = set.getInteger("range", 40);
      this._baseRandomDamage = set.getDouble("randomDamage", 0.0);
      this.addSpeed(MoveType.WALK, set.getFloat("walk", 0.0F));
      this.addSpeed(MoveType.RUN, set.getFloat("run", 0.0F));
      this.addSpeed(MoveType.SLOW_SWIM, set.getFloat("waterWalk", 0.0F));
      this.addSpeed(MoveType.FAST_SWIM, set.getFloat("waterRun", 0.0F));
      this.addSpeed(MoveType.SLOW_FLY, set.getFloat("flyWalk", 0.0F));
      this.addSpeed(MoveType.FAST_FLY, set.getFloat("flyRun", 0.0F));
      this.addStats(Stats.POWER_ATTACK, set.getDouble("pAtk", 0.0));
      this.addStats(Stats.MAGIC_ATTACK, set.getDouble("mAtk", 0.0));
      this.addStats(Stats.POWER_ATTACK_RANGE, (double)set.getInteger("range", 0));
      this.addStats(Stats.POWER_ATTACK_SPEED, (double)set.getInteger("attackSpeed", 0));
      this.addStats(Stats.CRITICAL_RATE, (double)set.getInteger("critRate", 0));
      this.addStats(Stats.STAT_STR, (double)set.getInteger("str", 0));
      this.addStats(Stats.STAT_INT, (double)set.getInteger("int", 0));
      this.addStats(Stats.STAT_CON, (double)set.getInteger("con", 0));
      this.addStats(Stats.STAT_DEX, (double)set.getInteger("dex", 0));
      this.addStats(Stats.STAT_WIT, (double)set.getInteger("wit", 0));
      this.addStats(Stats.STAT_MEN, (double)set.getInteger("men", 0));
      this.addDefense(6, set.getInteger("chest", 0));
      this.addDefense(11, set.getInteger("legs", 0));
      this.addDefense(1, set.getInteger("head", 0));
      this.addDefense(12, set.getInteger("feet", 0));
      this.addDefense(10, set.getInteger("gloves", 0));
      this.addDefense(0, set.getInteger("underwear", 0));
      this.addDefense(23, set.getInteger("cloak", 0));
      this.addDefense(8, set.getInteger("rear", 0));
      this.addDefense(9, set.getInteger("lear", 0));
      this.addDefense(13, set.getInteger("rfinger", 0));
      this.addDefense(14, set.getInteger("lfinger", 0));
      this.addDefense(4, set.getInteger("neck", 0));
   }

   private void addSpeed(MoveType type, float val) {
      if (this._baseSpeed == null) {
         this._baseSpeed = new HashMap<>();
      }

      this._baseSpeed.put(type.ordinal(), val);
   }

   public float getBaseMoveSpeed(MoveType type) {
      return this._baseSpeed != null && this._baseSpeed.containsKey(type.ordinal()) ? this._baseSpeed.get(type.ordinal()) : 0.0F;
   }

   private void addDefense(int type, int val) {
      if (this._baseDefense == null) {
         this._baseDefense = new HashMap<>();
      }

      this._baseDefense.put(type, val);
   }

   public int getDefense(int type) {
      return this._baseDefense != null && this._baseDefense.containsKey(type) ? this._baseDefense.get(type) : 0;
   }

   private void addStats(Stats stats, double val) {
      if (this._baseStats == null) {
         this._baseStats = new HashMap<>();
      }

      this._baseStats.put(stats.ordinal(), val);
   }

   public double getStats(Stats stats) {
      return this._baseStats != null && this._baseStats.containsKey(stats.ordinal()) ? this._baseStats.get(stats.ordinal()) : 0.0;
   }

   public double getCollisionRadius() {
      return this._collisionRadius;
   }

   public double getCollisionHeight() {
      return this._collisionHeight;
   }

   public WeaponType getBaseAttackType() {
      return this._baseAttackType;
   }

   public int getBaseAttackRange() {
      return this._baseAttackRange;
   }

   public double getBaseRandomDamage() {
      return this._baseRandomDamage;
   }

   public void addSkill(SkillHolder holder) {
      if (this._skills == null) {
         this._skills = new ArrayList<>();
      }

      this._skills.add(holder);
   }

   public List<SkillHolder> getSkills() {
      return this._skills != null ? this._skills : Collections.emptyList();
   }

   public void addAdditionalSkill(AdditionalSkillHolder holder) {
      if (this._additionalSkills == null) {
         this._additionalSkills = new ArrayList<>();
      }

      this._additionalSkills.add(holder);
   }

   public List<AdditionalSkillHolder> getAdditionalSkills() {
      return this._additionalSkills != null ? this._additionalSkills : Collections.emptyList();
   }

   public void addAdditionalItem(AdditionalItemHolder holder) {
      if (this._additionalItems == null) {
         this._additionalItems = new ArrayList<>();
      }

      this._additionalItems.add(holder);
   }

   public List<AdditionalItemHolder> getAdditionalItems() {
      return this._additionalItems != null ? this._additionalItems : Collections.emptyList();
   }

   public void setBasicActionList(ExBasicActionList list) {
      this._list = list;
   }

   public ExBasicActionList getBasicActionList() {
      return this._list;
   }

   public boolean hasBasicActionList() {
      return this._list != null;
   }

   public void addLevelData(TransformLevelData data) {
      this._data.put(data.getLevel(), data);
   }

   public TransformLevelData getData(int level) {
      return this._data.get(level);
   }
}
