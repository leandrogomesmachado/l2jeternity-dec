package l2e.gameserver.model.actor.templates.items;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.skills.conditions.ConditionGameChance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.StatsSet;

public final class Weapon extends Item {
   private final WeaponType _type;
   private final boolean _isMagicWeapon;
   private final int _rndDam;
   private final int _soulShotCount;
   private final int _spiritShotCount;
   private final int _mpConsume;
   private SkillHolder _enchant4Skill = null;
   private final int _changeWeaponId;
   private final SkillHolder _unequipSkill = null;
   private SkillHolder _skillsOnMagic;
   private Condition _skillsOnMagicCondition = null;
   private SkillHolder _skillsOnCrit;
   private Condition _skillsOnCritCondition = null;
   private final int _reducedSoulshot;
   private final int _reducedSoulshotChance;
   private final int _reducedMpConsume;
   private final int _reducedMpConsumeChance;
   private final boolean _isForceEquip;
   private final boolean _isAttackWeapon;
   private final boolean _useWeaponSkillsOnly;
   private final int _baseAttackRange;
   private int[] _damageRange;

   public Weapon(StatsSet set) {
      super(set);
      this._type = WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
      this._type1 = 0;
      this._type2 = 0;
      this._isMagicWeapon = set.getBool("is_magic_weapon", false);
      this._soulShotCount = set.getInteger("soulshots", 0);
      this._spiritShotCount = set.getInteger("spiritshots", 0);
      this._rndDam = set.getInteger("random_damage", 0);
      this._mpConsume = set.getInteger("mp_consume", 0);
      this._baseAttackRange = set.getInteger("attack_range", 40);
      this._damageRange = null;
      String[] damgeRange = set.getString("damage_range", "").split(";");
      if (damgeRange.length > 1 && Util.isDigit(damgeRange[3])) {
         this._damageRange = new int[4];
         this._damageRange[0] = Integer.parseInt(damgeRange[0]);
         this._damageRange[1] = Integer.parseInt(damgeRange[1]);
         this._damageRange[2] = Integer.parseInt(damgeRange[2]);
         this._damageRange[3] = Integer.parseInt(damgeRange[3]);
      }

      String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
      this._reducedSoulshotChance = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[0]) : 0;
      this._reducedSoulshot = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[1]) : 0;
      String[] reduced_mpconsume = set.getString("reduced_mp_consume", "").split(",");
      this._reducedMpConsumeChance = reduced_mpconsume.length == 2 ? Integer.parseInt(reduced_mpconsume[0]) : 0;
      this._reducedMpConsume = reduced_mpconsume.length == 2 ? Integer.parseInt(reduced_mpconsume[1]) : 0;
      String skill = set.getString("enchant4_skill", null);
      if (skill != null) {
         String[] info = skill.split("-");
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var14) {
               _log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon enchant skills! item ", this.toString()));
            }

            if (id > 0 && level > 0) {
               this._enchant4Skill = new SkillHolder(id, level);
            }
         }
      }

      skill = set.getString("onmagic_skill", null);
      if (skill != null) {
         String[] info = skill.split("-");
         int chance = set.getInteger("onmagic_chance", 100);
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var13) {
               _log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon onmagic skills! item ", this.toString()));
            }

            if (id > 0 && level > 0 && chance > 0) {
               this._skillsOnMagic = new SkillHolder(id, level);
               this._skillsOnMagicCondition = new ConditionGameChance(chance);
            }
         }
      }

      skill = set.getString("oncrit_skill", null);
      if (skill != null) {
         String[] info = skill.split("-");
         int chance = set.getInteger("oncrit_chance", 100);
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var12) {
               _log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon oncrit skills! item ", this.toString()));
            }

            if (id > 0 && level > 0 && chance > 0) {
               this._skillsOnCrit = new SkillHolder(id, level);
               this._skillsOnCritCondition = new ConditionGameChance(chance);
            }
         }
      }

      skill = set.getString("unequip_skill", null);
      if (skill != null) {
         String[] info = skill.split("-");
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var11) {
               _log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon unequip skills! item ", this.toString()));
            }

            if (id > 0 && level > 0) {
               this.setUnequipSkills(new SkillHolder(id, level));
            }
         }
      }

      this._changeWeaponId = set.getInteger("change_weaponId", 0);
      this._isForceEquip = set.getBool("isForceEquip", false);
      this._isAttackWeapon = set.getBool("isAttackWeapon", true);
      this._useWeaponSkillsOnly = set.getBool("useWeaponSkillsOnly", false);
   }

   public WeaponType getItemType() {
      return this._type;
   }

   @Override
   public int getItemMask() {
      return this.getItemType().mask();
   }

   @Override
   public boolean isMagicWeapon() {
      return this._isMagicWeapon;
   }

   public int getSoulShotCount() {
      return this._soulShotCount;
   }

   public int getSpiritShotCount() {
      return this._spiritShotCount;
   }

   public int getReducedSoulShot() {
      return this._reducedSoulshot;
   }

   public int getReducedSoulShotChance() {
      return this._reducedSoulshotChance;
   }

   public int getRandomDamage() {
      return this._rndDam;
   }

   public int getMpConsume() {
      return this._mpConsume;
   }

   public int getReducedMpConsume() {
      return this._reducedMpConsume;
   }

   public int getReducedMpConsumeChance() {
      return this._reducedMpConsumeChance;
   }

   @Override
   public Skill getEnchant4Skill() {
      return this._enchant4Skill == null ? null : this._enchant4Skill.getSkill();
   }

   public int getChangeWeaponId() {
      return this._changeWeaponId;
   }

   public boolean isForceEquip() {
      return this._isForceEquip;
   }

   public boolean isAttackWeapon() {
      return this._isAttackWeapon;
   }

   public boolean useWeaponSkillsOnly() {
      return this._useWeaponSkillsOnly;
   }

   public Effect[] getSkillEffects(Creature caster, Creature target, boolean crit) {
      if (this._skillsOnCrit != null && crit) {
         List<Effect> effects = new ArrayList<>();
         Skill onCritSkill = this._skillsOnCrit.getSkill();
         if (this._skillsOnCritCondition != null) {
            Env env = new Env();
            env.setCharacter(caster);
            env.setTarget(target);
            env.setSkill(onCritSkill);
            if (!this._skillsOnCritCondition.test(env)) {
               return _emptyEffectSet;
            }
         }

         if (!onCritSkill.checkCondition(caster, target, false, true)) {
            return _emptyEffectSet;
         } else {
            byte shld = Formulas.calcShldUse(caster, target, onCritSkill);
            if (!Formulas.calcSkillSuccess(caster, target, onCritSkill, shld, false, false, false)) {
               return _emptyEffectSet;
            } else {
               if (target != null && target.getFirstEffect(onCritSkill.getId()) != null) {
                  target.getFirstEffect(onCritSkill.getId()).exit();
               }

               for(Effect e : onCritSkill.getEffects(caster, target, new Env(shld, false, false, false), true)) {
                  effects.add(e);
               }

               return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
            }
         }
      } else {
         return _emptyEffectSet;
      }
   }

   public Effect[] getSkillEffects(Creature caster, Creature target, Skill trigger) {
      if (this._skillsOnMagic == null) {
         return _emptyEffectSet;
      } else {
         Skill onMagicSkill = this._skillsOnMagic.getSkill();
         if (trigger.isOffensive() != onMagicSkill.isOffensive()) {
            return _emptyEffectSet;
         } else if (trigger.isMagic() != onMagicSkill.isMagic()) {
            return _emptyEffectSet;
         } else if (trigger.isToggle()) {
            return _emptyEffectSet;
         } else if (onMagicSkill.isDebuff() && caster == target) {
            return _emptyEffectSet;
         } else {
            if (this._skillsOnMagicCondition != null) {
               Env env = new Env();
               env.setCharacter(caster);
               env.setTarget(target);
               env.setSkill(onMagicSkill);
               if (!this._skillsOnMagicCondition.test(env)) {
                  return _emptyEffectSet;
               }
            }

            if (!onMagicSkill.checkCondition(caster, target, false, true)) {
               return _emptyEffectSet;
            } else {
               byte shld = Formulas.calcShldUse(caster, target, onMagicSkill);
               if (onMagicSkill.isOffensive() && !Formulas.calcSkillSuccess(caster, target, onMagicSkill, shld, false, false, false)) {
                  return _emptyEffectSet;
               } else {
                  Creature[] targets = new Creature[]{target};
                  ISkillHandler handler = SkillHandler.getInstance().getHandler(onMagicSkill.getSkillType());
                  if (handler != null) {
                     handler.useSkill(caster, onMagicSkill, targets);
                  } else {
                     onMagicSkill.useSkill(caster, targets);
                  }

                  if (caster.isPlayer()) {
                     for(Npc npcMob : World.getInstance().getAroundNpc(caster)) {
                        if (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null) {
                           for(Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE)) {
                              quest.notifySkillSee(npcMob, caster.getActingPlayer(), onMagicSkill, targets, false);
                           }
                        }
                     }
                  }

                  return _emptyEffectSet;
               }
            }
         }
      }
   }

   public SkillHolder getUnequipSkills() {
      return this._unequipSkill;
   }

   public void setUnequipSkills(SkillHolder unequipSkill) {
      unequipSkill = this._unequipSkill;
   }

   public int getBaseAttackRange() {
      return this._baseAttackRange;
   }

   public int[] getDamageRange() {
      return this._damageRange;
   }
}
