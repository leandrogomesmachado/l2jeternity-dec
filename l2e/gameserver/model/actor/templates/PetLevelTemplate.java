package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.StatsSet;

public class PetLevelTemplate {
   private final int _ownerExpTaken;
   private final int _petFeedBattle;
   private final int _petFeedNormal;
   private final float _petMAtk;
   private final long _petMaxExp;
   private final int _petMaxFeed;
   private final float _petMaxHP;
   private final float _petMaxMP;
   private final float _petMDef;
   private final float _petPAtk;
   private final float _petPDef;
   private final float _petRegenHP;
   private final float _petRegenMP;
   private final short _petSoulShot;
   private final short _petSpiritShot;
   private final double _walkSpeedOnRide;
   private final double _runSpeedOnRide;
   private final double _slowSwimSpeedOnRide;
   private final double _fastSwimSpeedOnRide;
   private final double _slowFlySpeedOnRide;
   private final double _fastFlySpeedOnRide;

   public PetLevelTemplate(StatsSet set) {
      this._ownerExpTaken = set.getInteger("get_exp_type");
      this._petMaxExp = (long)set.getDouble("exp");
      this._petMaxHP = set.getFloat("org_hp");
      this._petMaxMP = set.getFloat("org_mp");
      this._petPAtk = set.getFloat("org_pattack");
      this._petPDef = set.getFloat("org_pdefend");
      this._petMAtk = set.getFloat("org_mattack");
      this._petMDef = set.getFloat("org_mdefend");
      this._petMaxFeed = set.getInteger("max_meal");
      this._petFeedBattle = set.getInteger("consume_meal_in_battle");
      this._petFeedNormal = set.getInteger("consume_meal_in_normal");
      this._petRegenHP = set.getFloat("org_hp_regen");
      this._petRegenMP = set.getFloat("org_mp_regen");
      this._petSoulShot = set.getShort("soulshot_count");
      this._petSpiritShot = set.getShort("spiritshot_count");
      this._walkSpeedOnRide = set.getDouble("walkSpeedOnRide", 0.0);
      this._runSpeedOnRide = set.getDouble("runSpeedOnRide", 0.0);
      this._slowSwimSpeedOnRide = set.getDouble("slowSwimSpeedOnRide", 0.0);
      this._fastSwimSpeedOnRide = set.getDouble("fastSwimSpeedOnRide", 0.0);
      this._slowFlySpeedOnRide = set.getDouble("slowFlySpeedOnRide", 0.0);
      this._fastFlySpeedOnRide = set.getDouble("fastFlySpeedOnRide", 0.0);
   }

   public int getOwnerExpTaken() {
      return this._ownerExpTaken;
   }

   public int getPetFeedBattle() {
      return this._petFeedBattle;
   }

   public int getPetFeedNormal() {
      return this._petFeedNormal;
   }

   public float getPetMAtk() {
      return this._petMAtk;
   }

   public long getPetMaxExp() {
      return this._petMaxExp;
   }

   public int getPetMaxFeed() {
      return this._petMaxFeed;
   }

   public float getPetMaxHP() {
      return this._petMaxHP;
   }

   public float getPetMaxMP() {
      return this._petMaxMP;
   }

   public float getPetMDef() {
      return this._petMDef;
   }

   public float getPetPAtk() {
      return this._petPAtk;
   }

   public float getPetPDef() {
      return this._petPDef;
   }

   public float getPetRegenHP() {
      return this._petRegenHP;
   }

   public float getPetRegenMP() {
      return this._petRegenMP;
   }

   public short getPetSoulShot() {
      return this._petSoulShot;
   }

   public short getPetSpiritShot() {
      return this._petSpiritShot;
   }

   public double getSpeedOnRide(MoveType mt) {
      switch(mt) {
         case WALK:
            return this._walkSpeedOnRide;
         case RUN:
            return this._runSpeedOnRide;
         case SLOW_SWIM:
            return this._slowSwimSpeedOnRide;
         case FAST_SWIM:
            return this._fastSwimSpeedOnRide;
         case SLOW_FLY:
            return this._slowFlySpeedOnRide;
         case FAST_FLY:
            return this._fastFlySpeedOnRide;
         default:
            return 0.0;
      }
   }
}
