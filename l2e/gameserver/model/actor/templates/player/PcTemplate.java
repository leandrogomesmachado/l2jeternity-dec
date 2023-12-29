package l2e.gameserver.model.actor.templates.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.InitialEquipmentParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.stats.StatsSet;

public class PcTemplate extends CharTemplate {
   private final ClassId _classId;
   private final float[] _baseHp;
   private final float[] _baseMp;
   private final float[] _baseCp;
   private final double[] _baseHpReg;
   private final double[] _baseMpReg;
   private final double[] _baseCpReg;
   private final double _fCollisionHeightFemale;
   private final double _fCollisionRadiusFemale;
   private final int _baseSafeFallHeight;
   private final List<PcItemTemplate> _initialEquipment;
   private final List<Location> _creationPoints;
   private final Map<Integer, Integer> _baseSlotDef;

   public PcTemplate(StatsSet set, List<Location> creationPoints) {
      super(set);
      this._classId = ClassId.getClassId(set.getInteger("classId"));
      this._baseHp = new float[ExperienceParser.getInstance().getMaxLevel()];
      this._baseMp = new float[ExperienceParser.getInstance().getMaxLevel()];
      this._baseCp = new float[ExperienceParser.getInstance().getMaxLevel()];
      this._baseHpReg = new double[ExperienceParser.getInstance().getMaxLevel()];
      this._baseMpReg = new double[ExperienceParser.getInstance().getMaxLevel()];
      this._baseCpReg = new double[ExperienceParser.getInstance().getMaxLevel()];
      this._baseSlotDef = new HashMap<>(12);
      this._baseSlotDef.put(6, set.getInteger("basePDefchest", 0));
      this._baseSlotDef.put(11, set.getInteger("basePDeflegs", 0));
      this._baseSlotDef.put(1, set.getInteger("basePDefhead", 0));
      this._baseSlotDef.put(12, set.getInteger("basePDeffeet", 0));
      this._baseSlotDef.put(10, set.getInteger("basePDefgloves", 0));
      this._baseSlotDef.put(0, set.getInteger("basePDefunderwear", 0));
      this._baseSlotDef.put(23, set.getInteger("basePDefcloak", 0));
      this._baseSlotDef.put(8, set.getInteger("baseMDefrear", 0));
      this._baseSlotDef.put(9, set.getInteger("baseMDeflear", 0));
      this._baseSlotDef.put(13, set.getInteger("baseMDefrfinger", 0));
      this._baseSlotDef.put(14, set.getInteger("baseMDefrfinger", 0));
      this._baseSlotDef.put(4, set.getInteger("baseMDefneck", 0));
      this._fCollisionRadiusFemale = set.getDouble("collisionFemaleradius");
      this._fCollisionHeightFemale = set.getDouble("collisionFemaleheight");
      this._baseSafeFallHeight = set.getInteger("baseSafeFall", 333);
      this._initialEquipment = InitialEquipmentParser.getInstance().getEquipmentList(this._classId);
      this._creationPoints = creationPoints;
   }

   public ClassId getClassId() {
      return this._classId;
   }

   public Race getRace() {
      return this._classId.getRace();
   }

   public Location getCreationPoint() {
      return this._creationPoints.get(Rnd.get(this._creationPoints.size()));
   }

   public void setUpgainValue(String paramName, int level, double val) {
      switch(paramName) {
         case "hp":
            this._baseHp[level] = (float)val;
            break;
         case "mp":
            this._baseMp[level] = (float)val;
            break;
         case "cp":
            this._baseCp[level] = (float)val;
            break;
         case "hpRegen":
            this._baseHpReg[level] = val;
            break;
         case "mpRegen":
            this._baseMpReg[level] = val;
            break;
         case "cpRegen":
            this._baseCpReg[level] = val;
      }
   }

   public float getBaseHpMax(int level) {
      return this._baseHp[level];
   }

   public float getBaseMpMax(int level) {
      return this._baseMp[level];
   }

   public float getBaseCpMax(int level) {
      return this._baseCp[level];
   }

   public double getBaseHpRegen(int level) {
      return this._baseHpReg[level];
   }

   public double getBaseMpRegen(int level) {
      return this._baseMpReg[level];
   }

   public double getBaseCpRegen(int level) {
      return this._baseCpReg[level];
   }

   public int getBaseDefBySlot(int slotId) {
      return this._baseSlotDef.containsKey(slotId) ? this._baseSlotDef.get(slotId) : 0;
   }

   public double getFCollisionHeightFemale() {
      return this._fCollisionHeightFemale;
   }

   public double getFCollisionRadiusFemale() {
      return this._fCollisionRadiusFemale;
   }

   public int getSafeFallHeight() {
      return this._baseSafeFallHeight;
   }

   public List<PcItemTemplate> getInitialEquipment() {
      return this._initialEquipment;
   }

   public boolean hasInitialEquipment() {
      return this._initialEquipment != null;
   }
}
