package l2e.gameserver.model.actor.templates.character;

import java.util.Arrays;
import java.util.Map;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.StatsSet;

public class CharTemplate {
   private final int _baseSTR;
   private final int _baseCON;
   private final int _baseDEX;
   private final int _baseINT;
   private final int _baseWIT;
   private final int _baseMEN;
   private final double _baseHpMax;
   private final double _baseCpMax;
   private final double _baseMpMax;
   private final double _baseHpReg;
   private final double _baseMpReg;
   private final double _basePAtk;
   private final double _baseMAtk;
   private final double _basePDef;
   private final double _baseMDef;
   private final double _basePAtkSpd;
   private final double _baseMAtkSpd;
   private final float _baseMReuseRate;
   private int _baseAttackRange;
   private WeaponType _baseAttackType;
   private final int _baseShldDef;
   private final int _baseShldRate;
   private final double _baseCritRate;
   private final double _baseMCritRate;
   private final int _baseBreath;
   private final int _baseAggression;
   private final int _baseBleed;
   private final int _basePoison;
   private final int _baseStun;
   private final int _baseRoot;
   private final int _baseMovement;
   private final int _baseConfusion;
   private final int _baseSleep;
   private final double _baseAggressionVuln;
   private final double _baseBleedVuln;
   private final double _basePoisonVuln;
   private final double _baseStunVuln;
   private final double _baseRootVuln;
   private final double _baseMovementVuln;
   private final double _baseSleepVuln;
   private final double _baseCritVuln;
   private int _baseFire;
   private int _baseWind;
   private int _baseWater;
   private int _baseEarth;
   private int _baseHoly;
   private int _baseDark;
   private double _baseFireRes;
   private double _baseWindRes;
   private double _baseWaterRes;
   private double _baseEarthRes;
   private double _baseHolyRes;
   private double _baseDarkRes;
   private double _baseElementRes;
   private final int _baseMpConsumeRate;
   private final int _baseHpConsumeRate;
   private final int _collisionRadius;
   private final int _collisionHeight;
   private final double _fCollisionRadius;
   private final double _fCollisionHeight;
   private final double[] _moveType = new double[MoveType.values().length];

   public CharTemplate(StatsSet set) {
      this._baseSTR = set.getInteger("baseSTR", 0);
      this._baseCON = set.getInteger("baseCON", 0);
      this._baseDEX = set.getInteger("baseDEX", 0);
      this._baseINT = set.getInteger("baseINT", 0);
      this._baseWIT = set.getInteger("baseWIT", 0);
      this._baseMEN = set.getInteger("baseMEN", 0);
      this._baseHpMax = set.getDouble("baseHpMax", 0.0);
      this._baseCpMax = set.getDouble("baseCpMax", 0.0);
      this._baseMpMax = set.getDouble("baseMpMax", 0.0);
      this._baseHpReg = set.getDouble("baseHpReg", 0.0);
      this._baseMpReg = set.getDouble("baseMpReg", 0.0);
      this._basePAtk = set.getDouble("basePAtk", 0.0);
      this._baseMAtk = set.getDouble("baseMAtk", 0.0);
      this._basePDef = set.getDouble("basePDef", 0.0);
      this._baseMDef = set.getDouble("baseMDef", 0.0);
      this._basePAtkSpd = set.getDouble("basePAtkSpd", 300.0);
      this._baseMAtkSpd = set.getDouble("baseMAtkSpd", 333.0);
      this._baseMReuseRate = set.getFloat("baseMReuseDelay", 1.0F);
      this._baseShldDef = set.getInteger("baseShldDef", 0);
      this._baseAttackRange = set.getInteger("baseAtkRange", 40);
      this._baseAttackType = WeaponType.findByName(set.getString("baseAtkType", "Fist"));
      this._baseShldRate = set.getInteger("baseShldRate", 0);
      this._baseCritRate = set.getDouble("baseCritRate", 4.0);
      this._baseMCritRate = set.getDouble("baseMCritRate", 0.0);
      this._baseBreath = set.getInteger("baseBreath", 100);
      this._baseAggression = set.getInteger("baseAggression", 0);
      this._baseBleed = set.getInteger("baseBleed", 0);
      this._basePoison = set.getInteger("basePoison", 0);
      this._baseStun = set.getInteger("baseStun", 0);
      this._baseRoot = set.getInteger("baseRoot", 0);
      this._baseMovement = set.getInteger("baseMovement", 0);
      this._baseConfusion = set.getInteger("baseConfusion", 0);
      this._baseSleep = set.getInteger("baseSleep", 0);
      this._baseFire = set.getInteger("baseFire", 0);
      this._baseWind = set.getInteger("baseWind", 0);
      this._baseWater = set.getInteger("baseWater", 0);
      this._baseEarth = set.getInteger("baseEarth", 0);
      this._baseHoly = set.getInteger("baseHoly", 0);
      this._baseDark = set.getInteger("baseDark", 0);
      this._baseAggressionVuln = (double)set.getInteger("baseAggressionVuln", 0);
      this._baseBleedVuln = (double)set.getInteger("baseBleedVuln", 0);
      this._basePoisonVuln = (double)set.getInteger("basePoisonVuln", 0);
      this._baseStunVuln = (double)set.getInteger("baseStunVuln", 0);
      this._baseRootVuln = (double)set.getInteger("baseRootVuln", 0);
      this._baseMovementVuln = (double)set.getInteger("baseMovementVuln", 0);
      this._baseSleepVuln = (double)set.getInteger("baseSleepVuln", 0);
      this._baseCritVuln = (double)set.getInteger("baseCritVuln", 1);
      this._baseFireRes = (double)set.getInteger("baseFireRes", 0);
      this._baseWindRes = (double)set.getInteger("baseWindRes", 0);
      this._baseWaterRes = (double)set.getInteger("baseWaterRes", 0);
      this._baseEarthRes = (double)set.getInteger("baseEarthRes", 0);
      this._baseHolyRes = (double)set.getInteger("baseHolyRes", 0);
      this._baseDarkRes = (double)set.getInteger("baseDarkRes", 0);
      this._baseElementRes = (double)set.getInteger("baseElementRes", 0);
      this._baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
      this._baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);
      this._fCollisionHeight = set.getDouble("collision_height", 0.0);
      this._fCollisionRadius = set.getDouble("collision_radius", 0.0);
      this._collisionRadius = (int)this._fCollisionRadius;
      this._collisionHeight = (int)this._fCollisionHeight;
      Arrays.fill(this._moveType, 1.0);
      this.setBaseMoveSpeed(MoveType.RUN, set.getDouble("baseRunSpd", 120.0));
      this.setBaseMoveSpeed(MoveType.WALK, set.getDouble("baseWalkSpd", 50.0));
      this.setBaseMoveSpeed(MoveType.FAST_SWIM, set.getDouble("baseSwimRunSpd", this.getBaseMoveSpeed(MoveType.RUN)));
      this.setBaseMoveSpeed(MoveType.SLOW_SWIM, set.getDouble("baseSwimWalkSpd", this.getBaseMoveSpeed(MoveType.WALK)));
      this.setBaseMoveSpeed(MoveType.FAST_FLY, set.getDouble("baseFlyRunSpd", this.getBaseMoveSpeed(MoveType.RUN)));
      this.setBaseMoveSpeed(MoveType.SLOW_FLY, set.getDouble("baseFlyWalkSpd", this.getBaseMoveSpeed(MoveType.WALK)));
   }

   public double getBaseHpMax() {
      return this._baseHpMax;
   }

   public int getBaseFire() {
      return this._baseFire;
   }

   public int getBaseWind() {
      return this._baseWind;
   }

   public int getBaseWater() {
      return this._baseWater;
   }

   public int getBaseEarth() {
      return this._baseEarth;
   }

   public int getBaseHoly() {
      return this._baseHoly;
   }

   public int getBaseDark() {
      return this._baseDark;
   }

   public double getBaseFireRes() {
      return this._baseFireRes;
   }

   public double getBaseWindRes() {
      return this._baseWindRes;
   }

   public double getBaseWaterRes() {
      return this._baseWaterRes;
   }

   public double getBaseEarthRes() {
      return this._baseEarthRes;
   }

   public double getBaseHolyRes() {
      return this._baseHolyRes;
   }

   public double getBaseDarkRes() {
      return this._baseDarkRes;
   }

   public double getBaseElementRes() {
      return this._baseElementRes;
   }

   public int getBaseSTR() {
      return this._baseSTR;
   }

   public int getBaseCON() {
      return this._baseCON;
   }

   public int getBaseDEX() {
      return this._baseDEX;
   }

   public int getBaseINT() {
      return this._baseINT;
   }

   public int getBaseWIT() {
      return this._baseWIT;
   }

   public int getBaseMEN() {
      return this._baseMEN;
   }

   public double getBaseCpMax() {
      return this._baseCpMax;
   }

   public double getBaseMpMax() {
      return this._baseMpMax;
   }

   public double getBaseHpReg() {
      return this._baseHpReg;
   }

   public double getBaseMpReg() {
      return this._baseMpReg;
   }

   public double getBasePAtk() {
      return this._basePAtk;
   }

   public double getBaseMAtk() {
      return this._baseMAtk;
   }

   public double getBasePDef() {
      return this._basePDef;
   }

   public double getBaseMDef() {
      return this._baseMDef;
   }

   public double getBasePAtkSpd() {
      return this._basePAtkSpd;
   }

   public double getBaseMAtkSpd() {
      return this._baseMAtkSpd;
   }

   public float getBaseMReuseRate() {
      return this._baseMReuseRate;
   }

   public int getBaseShldDef() {
      return this._baseShldDef;
   }

   public int getBaseShldRate() {
      return this._baseShldRate;
   }

   public double getBaseCritRate() {
      return this._baseCritRate;
   }

   public double getBaseMCritRate() {
      return this._baseMCritRate;
   }

   public void setBaseMoveSpeed(MoveType type, double val) {
      this._moveType[type.ordinal()] = val;
   }

   public double getBaseMoveSpeed(MoveType mt) {
      return this._moveType[mt.ordinal()];
   }

   public int getBaseBreath() {
      return this._baseBreath;
   }

   public int getBaseAggression() {
      return this._baseAggression;
   }

   public int getBaseBleed() {
      return this._baseBleed;
   }

   public int getBasePoison() {
      return this._basePoison;
   }

   public int getBaseStun() {
      return this._baseStun;
   }

   public int getBaseRoot() {
      return this._baseRoot;
   }

   public int getBaseMovement() {
      return this._baseMovement;
   }

   public int getBaseConfusion() {
      return this._baseConfusion;
   }

   public int getBaseSleep() {
      return this._baseSleep;
   }

   public double getBaseAggressionVuln() {
      return this._baseAggressionVuln;
   }

   public double getBaseBleedVuln() {
      return this._baseBleedVuln;
   }

   public double getBasePoisonVuln() {
      return this._basePoisonVuln;
   }

   public double getBaseStunVuln() {
      return this._baseStunVuln;
   }

   public double getBaseRootVuln() {
      return this._baseRootVuln;
   }

   public double getBaseMovementVuln() {
      return this._baseMovementVuln;
   }

   public double getBaseSleepVuln() {
      return this._baseSleepVuln;
   }

   public double getBaseCritVuln() {
      return this._baseCritVuln;
   }

   public int getBaseMpConsumeRate() {
      return this._baseMpConsumeRate;
   }

   public int getBaseHpConsumeRate() {
      return this._baseHpConsumeRate;
   }

   public int getCollisionRadius() {
      return this._collisionRadius;
   }

   public int getCollisionHeight() {
      return this._collisionHeight;
   }

   public double getfCollisionRadius() {
      return this._fCollisionRadius;
   }

   public double getfCollisionHeight() {
      return this._fCollisionHeight;
   }

   public void setBaseFire(int baseFire) {
      this._baseFire = baseFire;
   }

   public void setBaseWater(int baseWater) {
      this._baseWater = baseWater;
   }

   public void setBaseEarth(int baseEarth) {
      this._baseEarth = baseEarth;
   }

   public void setBaseWind(int baseWind) {
      this._baseWind = baseWind;
   }

   public void setBaseHoly(int baseHoly) {
      this._baseHoly = baseHoly;
   }

   public void setBaseDark(int baseDark) {
      this._baseDark = baseDark;
   }

   public void setBaseFireRes(double baseFireRes) {
      this._baseFireRes = baseFireRes;
   }

   public void setBaseWaterRes(double baseWaterRes) {
      this._baseWaterRes = baseWaterRes;
   }

   public void setBaseEarthRes(double baseEarthRes) {
      this._baseEarthRes = baseEarthRes;
   }

   public void setBaseWindRes(double baseWindRes) {
      this._baseWindRes = baseWindRes;
   }

   public void setBaseHolyRes(double baseHolyRes) {
      this._baseHolyRes = baseHolyRes;
   }

   public void setBaseDarkRes(double baseDarkRes) {
      this._baseDarkRes = baseDarkRes;
   }

   public void setBaseElementRes(double baseElementRes) {
      this._baseElementRes = baseElementRes;
   }

   public WeaponType getBaseAttackType() {
      return this._baseAttackType;
   }

   public void setBaseAttackType(WeaponType type) {
      this._baseAttackType = type;
   }

   public int getBaseAttackRange() {
      return this._baseAttackRange;
   }

   public void setBaseAttackRange(int val) {
      this._baseAttackRange = val;
   }

   public Map<Integer, Skill> getSkills() {
      return null;
   }
}
