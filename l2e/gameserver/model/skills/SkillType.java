package l2e.gameserver.model.skills;

import java.lang.reflect.Constructor;
import l2e.gameserver.model.skills.l2skills.SkillChargeDmg;
import l2e.gameserver.model.skills.l2skills.SkillDefault;
import l2e.gameserver.model.skills.l2skills.SkillDrain;
import l2e.gameserver.model.skills.l2skills.SkillSiegeFlag;
import l2e.gameserver.model.skills.l2skills.SkillSignet;
import l2e.gameserver.model.skills.l2skills.SkillSignetCasttime;
import l2e.gameserver.model.skills.l2skills.SkillSummon;
import l2e.gameserver.model.stats.StatsSet;

public enum SkillType {
   PDAM,
   MDAM,
   MANADAM,
   CPDAMPERCENT,
   DOT,
   MDOT,
   DRAIN(SkillDrain.class),
   DEATHLINK,
   FATAL,
   BLOW,
   SIGNET(SkillSignet.class),
   SIGNET_CASTTIME(SkillSignetCasttime.class),
   BLEED,
   POISON,
   STUN,
   ROOT,
   CONFUSION,
   FEAR,
   SLEEP,
   CONFUSE_MOB_ONLY,
   MUTE,
   PARALYZE,
   DISARM,
   AGGDAMAGE,
   AGGREDUCE,
   AGGREMOVE,
   AGGREDUCE_CHAR,
   AGGDEBUFF,
   FISHING,
   PUMPING,
   REELING,
   UNLOCK,
   UNLOCK_SPECIAL,
   ENCHANT_ARMOR,
   ENCHANT_WEAPON,
   ENCHANT_ATTRIBUTE,
   SOULSHOT,
   SPIRITSHOT,
   SIEGEFLAG(SkillSiegeFlag.class),
   TAKEFORT,
   DELUXE_KEY_UNLOCK,
   SOW,
   GET_PLAYER,
   DETECTION,
   DUMMY,
   INSTANT_JUMP,
   SUMMON(SkillSummon.class),
   FEED_PET,
   ERASE,
   BETRAY,
   BUFF,
   DEBUFF,
   CONT,
   FUSION,
   RESURRECT,
   CHARGEDAM(SkillChargeDmg.class),
   DETECT_TRAP,
   REMOVE_TRAP,
   COREDONE,
   NORNILS_POWER,
   NOTDONE,
   BALLISTA,
   BOMB,
   CAPTURE,
   ENERGY_REPLENISH,
   ENERGY_SPEND,
   EXTRACT_STONE,
   CONVERT_ITEM,
   NEGATE_EFFECTS;

   private final Class<? extends Skill> _class;

   public Skill makeSkill(StatsSet set) {
      try {
         Constructor<? extends Skill> c = this._class.getConstructor(StatsSet.class);
         return c.newInstance(set);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   private SkillType() {
      this._class = SkillDefault.class;
   }

   private SkillType(Class<? extends Skill> classType) {
      this._class = classType;
   }
}
