package l2e.gameserver.model.actor.stat;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ClassMasterInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.PetLevelTemplate;
import l2e.gameserver.model.actor.transform.TransformTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExVitalityPointInfo;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PcStat extends PlayableStat {
   private double _oldMaxHp;
   private double _oldMaxMp;
   private double _oldMaxCp;
   private double _vitalityPoints = 1.0;
   private byte _vitalityLevel = 0;
   private long _startingXp;
   public static final int[] VITALITY_LEVELS = new int[]{240, 2000, 13000, 17000, 20000};
   public static final int MAX_VITALITY_POINTS = VITALITY_LEVELS[4];
   public static final int MIN_VITALITY_POINTS = 1;

   public PcStat(Player activeChar) {
      super(activeChar);
   }

   @Override
   public boolean addExp(long value) {
      Player activeChar = this.getActiveChar();
      if (!this.getActiveChar().getAccessLevel().canGainExp()) {
         return false;
      } else if (!super.addExp(value)) {
         return false;
      } else {
         if (!activeChar.isCursedWeaponEquipped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(ZoneId.PVP))) {
            int karmaLost = activeChar.calculateKarmaLost(value);
            if (karmaLost > 0) {
               activeChar.setKarma(activeChar.getKarma() - karmaLost);
            }
         }

         activeChar.sendUserInfo();
         return true;
      }
   }

   @Override
   public boolean addExpAndSp(long addToExp, int addToSp) {
      return this.addExpAndSp(addToExp, addToSp, false);
   }

   public boolean addExpAndSp(long addToExp, int addToSp, boolean useBonuses) {
      Player activeChar = this.getActiveChar();
      if (!activeChar.getAccessLevel().canGainExp()) {
         return false;
      } else {
         double bonusExp = 1.0;
         double bonusSp = 1.0;
         if (useBonuses) {
            bonusExp = this.getExpBonusMultiplier();
            bonusSp = this.getSpBonusMultiplier();
            if (addToExp > 0L && !activeChar.isInsideZone(ZoneId.PEACE)) {
               activeChar.getRecommendation().startRecBonus();
               activeChar.getNevitSystem().startAdventTask();
            }
         }

         addToExp = (long)((double)addToExp * bonusExp);
         addToSp = (int)((double)addToSp * bonusSp);
         float ratioTakenByPlayer = 0.0F;
         if (activeChar.hasPet() && Util.checkIfInShortRadius(Config.ALT_PARTY_RANGE, activeChar, activeChar.getSummon(), false)) {
            PetInstance pet = (PetInstance)activeChar.getSummon();
            ratioTakenByPlayer = (float)pet.getPetLevelData().getOwnerExpTaken() / 100.0F;
            if (ratioTakenByPlayer > 1.0F) {
               ratioTakenByPlayer = 1.0F;
            }

            if (!pet.isDead()) {
               pet.addExpAndSp((long)((float)addToExp * (1.0F - ratioTakenByPlayer)), (int)((float)addToSp * (1.0F - ratioTakenByPlayer)));
            }

            addToExp = (long)((float)addToExp * ratioTakenByPlayer);
            addToSp = (int)((float)addToSp * ratioTakenByPlayer);
         }

         addToExp = Math.max(0L, addToExp);
         addToSp = Math.max(0, addToSp);
         if (!super.addExpAndSp(addToExp, addToSp)) {
            return false;
         } else {
            SystemMessage sm = null;
            if (addToExp == 0L && addToSp != 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
               sm.addInt(addToSp);
            } else if (addToSp == 0 && addToExp != 0L) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
               sm.addLong(addToExp);
            } else if (addToExp - addToExp > 0L && addToSp - addToSp > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
               sm.addLong(addToExp);
               sm.addLong(addToExp - addToExp);
               sm.addInt(addToSp);
               sm.addInt(addToSp - addToSp);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
               sm.addLong(addToExp);
               sm.addInt(addToSp);
            }

            activeChar.sendPacket(sm);
            return true;
         }
      }
   }

   @Override
   public boolean removeExpAndSp(long addToExp, int addToSp) {
      return this.removeExpAndSp(addToExp, addToSp, true);
   }

   public boolean removeExpAndSp(long addToExp, int addToSp, boolean sendMessage) {
      int level = this.getLevel();
      if (!super.removeExpAndSp(addToExp, addToSp)) {
         return false;
      } else {
         if (sendMessage) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
            sm.addLong(addToExp);
            this.getActiveChar().sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
            sm.addInt(addToSp);
            this.getActiveChar().sendPacket(sm);
            if (this.getLevel() < level) {
               this.getActiveChar().broadcastStatusUpdate();
            }
         }

         return true;
      }
   }

   @Override
   public final boolean addLevel(byte value, boolean canLower) {
      if (this.getLevel() + value <= ExperienceParser.getInstance().getMaxLevel() - 1 && (canLower || value >= 0)) {
         if (!this.getActiveChar().getEvents().onLevelChange(value)) {
            return false;
         } else {
            boolean levelIncreased = super.addLevel(value, canLower);
            if (levelIncreased) {
               if (!Config.DISABLE_TUTORIAL) {
                  QuestState qs = this.getActiveChar().getQuestState("_255_Tutorial");
                  if (qs != null) {
                     qs.getQuest().notifyEvent("CE40", null, this.getActiveChar());
                  }
               }

               this.getActiveChar().setCurrentCp(this.getMaxCp());
               this.getActiveChar().broadcastPacket(new SocialAction(this.getActiveChar().getObjectId(), 2122));
               this.getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
               ClassMasterInstance.showQuestionMark(this.getActiveChar());
            }

            this.getActiveChar().rewardSkills();
            if (this.getActiveChar().getClan() != null) {
               this.getActiveChar().getClan().updateClanMember(this.getActiveChar());
               this.getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this.getActiveChar()));
            }

            if (this.getActiveChar().isInParty()) {
               this.getActiveChar().getParty().recalculatePartyLevel();
            }

            if (this.getActiveChar().getMatchingRoom() != null) {
               this.getActiveChar().getMatchingRoom().broadcastPlayerUpdate(this.getActiveChar());
            }

            if (this.getActiveChar().isTransformed() || this.getActiveChar().isInStance()) {
               this.getActiveChar().getTransformation().onLevelUp(this.getActiveChar());
            }

            if (this.getActiveChar().hasPet()) {
               PetInstance pet = (PetInstance)this.getActiveChar().getSummon();
               if (pet.getPetData().isSynchLevel() && pet.getLevel() != this.getLevel()) {
                  pet.getStat().setLevel(this.getLevel());
                  pet.getStat().getExpForLevel(this.getActiveChar().getLevel());
                  pet.setCurrentHp(pet.getMaxHp());
                  pet.setCurrentMp(pet.getMaxMp());
                  pet.broadcastPacket(new SocialAction(this.getActiveChar().getObjectId(), 2122));
                  pet.updateAndBroadcastStatus(1);
               }
            }

            StatusUpdate su = new StatusUpdate(this.getActiveChar());
            su.addAttribute(1, this.getLevel());
            su.addAttribute(34, this.getMaxCp());
            su.addAttribute(10, this.getMaxHp());
            su.addAttribute(12, this.getMaxMp());
            this.getActiveChar().sendPacket(su);
            this.getActiveChar().refreshOverloaded();
            this.getActiveChar().refreshExpertisePenalty();
            this.getActiveChar().sendUserInfo();
            this.getActiveChar().sendVoteSystemInfo();
            this.getActiveChar().getNevitSystem().addPoints(levelIncreased ? 1950 : -1950);
            return levelIncreased;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean addSp(int value) {
      if (!super.addSp(value)) {
         return false;
      } else {
         StatusUpdate su = new StatusUpdate(this.getActiveChar());
         su.addAttribute(13, this.getSp());
         this.getActiveChar().sendPacket(su);
         return true;
      }
   }

   @Override
   public final long getExpForLevel(int level) {
      return ExperienceParser.getInstance().getExpForLevel(level);
   }

   public final Player getActiveChar() {
      return (Player)super.getActiveChar();
   }

   @Override
   public final long getExp() {
      return this.getActiveChar().isSubClassActive()
         ? this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).getExp()
         : super.getExp();
   }

   public final long getBaseExp() {
      return super.getExp();
   }

   @Override
   public final void setExp(long value) {
      if (this.getActiveChar().isSubClassActive()) {
         this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).setExp(value);
      } else {
         super.setExp(value);
      }
   }

   public void setStartingExp(long value) {
      if (Config.BOTREPORT_ENABLE) {
         this._startingXp = value;
      }
   }

   public long getStartingExp() {
      return this._startingXp;
   }

   @Override
   public final byte getLevel() {
      return this.getActiveChar().isSubClassActive()
         ? this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).getLevel()
         : super.getLevel();
   }

   public final byte getBaseLevel() {
      return super.getLevel();
   }

   @Override
   public final void setLevel(byte value) {
      if (value > ExperienceParser.getInstance().getMaxLevel() - 1) {
         value = (byte)(ExperienceParser.getInstance().getMaxLevel() - 1);
      }

      if (this.getActiveChar().isSubClassActive()) {
         this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).setLevel(value);
      } else {
         super.setLevel(value);
      }
   }

   @Override
   public final double getMaxCp() {
      double val = this.getActiveChar() == null
         ? 1.0
         : this.calcStat(Stats.MAX_CP, (double)this.getActiveChar().getTemplate().getBaseCpMax(this.getActiveChar().getLevel()));
      if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.CP_LIMIT)) {
         ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.CP_LIMIT);
         if (zone != null && zone.getCpLimit() > 0.0) {
            val = zone.getCpLimit();
         }
      }

      if (val != this._oldMaxCp) {
         this._oldMaxCp = val;
         if (this.getActiveChar().getStatus().getCurrentCp() != val) {
            this.getActiveChar().getStatus().setCurrentCp(this.getActiveChar().getStatus().getCurrentCp());
         }
      }

      return val;
   }

   @Override
   public final double getMaxHp() {
      double val = this.getActiveChar() == null
         ? 1.0
         : (double)((int)this.calcStat(Stats.MAX_HP, (double)this.getActiveChar().getTemplate().getBaseHpMax(this.getActiveChar().getLevel())));
      if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.HP_LIMIT)) {
         ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.HP_LIMIT);
         if (zone != null && zone.getHpLimit() > 0.0) {
            val = zone.getHpLimit();
         }
      }

      if (val != this._oldMaxHp) {
         this._oldMaxHp = val;
         if (this.getActiveChar().getStatus().getCurrentHp() != val) {
            this.getActiveChar().getStatus().setCurrentHp(this.getActiveChar().getStatus().getCurrentHp());
         }
      }

      return val;
   }

   @Override
   public final double getMaxMp() {
      double val = this.getActiveChar() == null
         ? 1.0
         : (double)((int)this.calcStat(Stats.MAX_MP, (double)this.getActiveChar().getTemplate().getBaseMpMax(this.getActiveChar().getLevel())));
      if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.MP_LIMIT)) {
         ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.MP_LIMIT);
         if (zone != null && zone.getMpLimit() > 0.0) {
            val = zone.getMpLimit();
         }
      }

      if (val != this._oldMaxMp) {
         this._oldMaxMp = val;
         if (this.getActiveChar().getStatus().getCurrentMp() != val) {
            this.getActiveChar().getStatus().setCurrentMp(this.getActiveChar().getStatus().getCurrentMp());
         }
      }

      return val;
   }

   @Override
   public final int getSp() {
      return this.getActiveChar().isSubClassActive() ? this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).getSp() : super.getSp();
   }

   public final int getBaseSp() {
      return super.getSp();
   }

   @Override
   public final void setSp(int value) {
      if (this.getActiveChar().isSubClassActive()) {
         this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex()).setSp(value);
      } else {
         super.setSp(value);
      }
   }

   @Override
   public double getBaseMoveSpeed(MoveType type) {
      Player player = this.getActiveChar();
      if (player.isTransformed()) {
         TransformTemplate template = player.getTransformation().getTemplate(player);
         if (template != null) {
            return (double)template.getBaseMoveSpeed(type);
         }
      } else if (player.isMounted()) {
         PetLevelTemplate data = PetsParser.getInstance().getPetLevelData(player.getMountNpcId(), player.getMountLevel());
         if (data != null) {
            return data.getSpeedOnRide(type);
         }
      }

      return super.getBaseMoveSpeed(type);
   }

   @Override
   public double getRunSpeed() {
      double val = super.getRunSpeed() + (double)Config.RUN_SPD_BOOST;
      if (val > (double)Config.MAX_RUN_SPEED && !this.getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
         return (double)Config.MAX_RUN_SPEED;
      } else {
         if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.RUN_SPEED_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.RUN_SPEED_LIMIT);
            if (zone != null && zone.getRunSpeedLimit() > 0.0) {
               val = zone.getRunSpeedLimit();
            }
         }

         if (this.getActiveChar().isMounted()) {
            if (this.getActiveChar().getMountLevel() - this.getActiveChar().getLevel() >= 10) {
               val /= 2.0;
            }

            if (this.getActiveChar().isHungry()) {
               val /= 2.0;
            }
         }

         if (Config.SPEED_UP_RUN && this.getActiveChar().isInsideZone(ZoneId.PEACE)) {
            val *= 2.0;
         }

         return val;
      }
   }

   @Override
   public double getWalkSpeed() {
      double val = super.getWalkSpeed() + (double)Config.RUN_SPD_BOOST;
      if (val > (double)Config.MAX_RUN_SPEED && !this.getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
         return (double)Config.MAX_RUN_SPEED;
      } else {
         if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.WALK_SPEED_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.WALK_SPEED_LIMIT);
            if (zone != null && zone.getWalkSpeedLimit() > 0.0) {
               val = zone.getWalkSpeedLimit();
            }
         }

         if (this.getActiveChar().isMounted()) {
            if (this.getActiveChar().getMountLevel() - this.getActiveChar().getLevel() >= 10) {
               val /= 2.0;
            }

            if (this.getActiveChar().isHungry()) {
               val /= 2.0;
            }
         }

         if (Config.SPEED_UP_RUN && this.getActiveChar().isInsideZone(ZoneId.PEACE)) {
            val *= 2.0;
         }

         return val;
      }
   }

   @Override
   public double getPAtk(Creature target) {
      double val = super.getPAtk(target);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.P_ATK_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.P_ATK_LIMIT);
            if (zone != null && zone.getPAtkLimit() > 0.0) {
               val = zone.getPAtkLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPAtkSpd() {
      double val = super.getPAtkSpd();
      if (Config.ALLOW_ZONES_LIMITS && this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.ATK_SPEED_LIMIT)) {
         ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.ATK_SPEED_LIMIT);
         if (zone != null && zone.getAtkSpeedLimit() > 0.0) {
            val = zone.getAtkSpeedLimit();
         }
      }

      return val > (double)Config.MAX_PATK_SPEED && !this.getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)
         ? (double)Config.MAX_PATK_SPEED
         : val;
   }

   @Override
   public double getPDef(Creature target) {
      double val = super.getPDef(target);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.P_DEF_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.P_DEF_LIMIT);
            if (zone != null && zone.getPDefLimit() > 0.0) {
               val = zone.getPDefLimit();
            }
         }

         return val;
      }
   }

   @Override
   public final double getCriticalDmg(Creature target, double init, Skill skill) {
      double val = super.getCriticalDmg(target, init, skill);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.CRIT_DMG_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.CRIT_DMG_LIMIT);
            if (zone != null && zone.getCritDmgLimit() > 0.0) {
               val = zone.getCritDmgLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getMAtk(Creature target, Skill skill) {
      double val = super.getMAtk(target, skill);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.M_ATK_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.M_ATK_LIMIT);
            if (zone != null && zone.getMAtkLimit() > 0.0) {
               val = zone.getMAtkLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getMAtkSpd() {
      double val = super.getMAtkSpd();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.M_ATK_SPEED_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.M_ATK_SPEED_LIMIT);
            if (zone != null && zone.getMAtkSpeedLimit() > 0.0) {
               val = zone.getMAtkSpeedLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getMDef(Creature target, Skill skill) {
      double val = super.getMDef(target, skill);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.M_DEF_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.M_DEF_LIMIT);
            if (zone != null && zone.getMDefLimit() > 0.0) {
               val = zone.getMDefLimit();
            }
         }

         return val;
      }
   }

   @Override
   public int getAccuracy() {
      int val = super.getAccuracy();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.ACCURACY_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.ACCURACY_LIMIT);
            if (zone != null && zone.getAccuracyLimit() > 0) {
               val = zone.getAccuracyLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getCriticalHit(Creature target, Skill skill) {
      double val = super.getCriticalHit(target, skill);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.CRIT_HIT_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.CRIT_HIT_LIMIT);
            if (zone != null && zone.getCritHitLimit() > 0.0) {
               val = zone.getCritHitLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getMCriticalHit(Creature target, Skill skill) {
      double val = super.getMCriticalHit(target, skill);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.MCRIT_HIT_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.MCRIT_HIT_LIMIT);
            if (zone != null && zone.getMCritHitLimit() > 0.0) {
               val = zone.getMCritHitLimit();
            }
         }

         return val;
      }
   }

   @Override
   public int getEvasionRate(Creature target) {
      int val = super.getEvasionRate(target);
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.EVASION_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.EVASION_LIMIT);
            if (zone != null && zone.getEvasionLimit() > 0) {
               val = zone.getEvasionLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpPhysSkillDmg() {
      double val = super.getPvpPhysSkillDmg();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_PHYS_SKILL_DMG_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_PHYS_SKILL_DMG_LIMIT);
            if (zone != null && zone.getPvpPhysSkillDmgLimit() > 0.0) {
               val = zone.getPvpPhysSkillDmgLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpPhysSkillDef() {
      double val = super.getPvpPhysSkillDef();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_PHYS_SKILL_DEF_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_PHYS_SKILL_DEF_LIMIT);
            if (zone != null && zone.getPvpPhysSkillDefLimit() > 0.0) {
               val = zone.getPvpPhysSkillDefLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpPhysDef() {
      double val = super.getPvpPhysDef();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_PHYS_DEF_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_PHYS_DEF_LIMIT);
            if (zone != null && zone.getPvpPhysDefLimit() > 0.0) {
               val = zone.getPvpPhysDefLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpPhysDmg() {
      double val = super.getPvpPhysDmg();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_PHYS_DMG_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_PHYS_DMG_LIMIT);
            if (zone != null && zone.getPvpPhysDmgLimit() > 0.0) {
               val = zone.getPvpPhysDmgLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpMagicDmg() {
      double val = super.getPvpMagicDmg();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_MAGIC_DMG_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_MAGIC_DMG_LIMIT);
            if (zone != null && zone.getPvpMagicDmgLimit() > 0.0) {
               val = zone.getPvpMagicDmgLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getPvpMagicDef() {
      double val = super.getPvpMagicDef();
      if (!Config.ALLOW_ZONES_LIMITS) {
         return val;
      } else {
         if (this.getActiveChar() != null && this.getActiveChar().isInsideZone(ZoneId.PVP_MAGIC_DEF_LIMIT)) {
            ZoneType zone = ZoneManager.getInstance().getZoneByZoneId(this.getActiveChar(), ZoneId.PVP_MAGIC_DEF_LIMIT);
            if (zone != null && zone.getPvpMagicDefLimit() > 0.0) {
               val = zone.getPvpMagicDefLimit();
            }
         }

         return val;
      }
   }

   @Override
   public double getMovementSpeedMultiplier() {
      if (this.getActiveChar().isMounted()) {
         PetLevelTemplate data = PetsParser.getInstance().getPetLevelData(this.getActiveChar().getMountNpcId(), this.getActiveChar().getMountLevel());
         double baseSpeed = data != null
            ? data.getSpeedOnRide(MoveType.RUN)
            : NpcsParser.getInstance().getTemplate(this.getActiveChar().getMountNpcId()).getBaseMoveSpeed(MoveType.RUN);
         return this.getRunSpeed() / baseSpeed;
      } else {
         return super.getMovementSpeedMultiplier();
      }
   }

   private void updateVitalityLevel(boolean quiet) {
      byte level;
      if (this._vitalityPoints <= (double)VITALITY_LEVELS[0]) {
         level = 0;
      } else if (this._vitalityPoints <= (double)VITALITY_LEVELS[1]) {
         level = 1;
      } else if (this._vitalityPoints <= (double)VITALITY_LEVELS[2]) {
         level = 2;
      } else if (this._vitalityPoints <= (double)VITALITY_LEVELS[3]) {
         level = 3;
      } else {
         level = 4;
      }

      if (this._vitalityLevel > level) {
         this.getActiveChar().getNevitSystem().addPoints(1500);
      }

      if (!quiet && level != this._vitalityLevel) {
         if (level < this._vitalityLevel) {
            this.getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_DECREASED);
         } else {
            this.getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_INCREASED);
         }

         if (level == 0) {
            this.getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_EXHAUSTED);
         } else if (level == 4) {
            this.getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_AT_MAXIMUM);
         }
      }

      this._vitalityLevel = level;
   }

   public int getVitalityPoints() {
      return (int)this._vitalityPoints;
   }

   public void setVitalityPoints(int points, boolean quiet) {
      if (Config.ENABLE_VITALITY) {
         points = Math.min(Math.max(points, 1), MAX_VITALITY_POINTS);
         if ((double)points != this._vitalityPoints) {
            this._vitalityPoints = (double)points;
            this.updateVitalityLevel(quiet);
            this.getActiveChar().sendPacket(new ExVitalityPointInfo(this.getVitalityPoints()));
         }
      }
   }

   public synchronized void updateVitalityPoints(double vitalityPoints, boolean useRates, boolean quiet) {
      if (vitalityPoints != 0.0 && Config.ENABLE_VITALITY) {
         if (useRates) {
            if (this.getActiveChar().isLucky()) {
               return;
            }

            if (vitalityPoints < 0.0) {
               int stat = (int)this.calcStat(Stats.VITALITY_CONSUME_RATE, 1.0, this.getActiveChar(), null);
               if (this.getActiveChar().getNevitSystem().isBlessingActive()) {
                  stat = (int)((double)stat - Config.VITALITY_NEVIT_POINT);
               }

               if (stat == 0) {
                  return;
               }

               if (stat < 0) {
                  vitalityPoints = -vitalityPoints;
               }
            }

            if (vitalityPoints > 0.0) {
               vitalityPoints *= (double)Config.RATE_VITALITY_GAIN;
            } else {
               vitalityPoints *= (double)Config.RATE_VITALITY_LOST;
            }
         }

         if (vitalityPoints > 0.0) {
            vitalityPoints = Math.min(this._vitalityPoints + vitalityPoints, (double)MAX_VITALITY_POINTS);
         } else {
            vitalityPoints = Math.max(this._vitalityPoints + vitalityPoints, 1.0);
         }

         if (!(Math.abs(vitalityPoints - this._vitalityPoints) <= 1.0E-6)) {
            this._vitalityPoints = vitalityPoints;
            this.updateVitalityLevel(quiet);
         }
      }
   }

   public double getVitalityMultiplier() {
      double vitality = 1.0;
      if (Config.ENABLE_VITALITY) {
         switch(this.getVitalityLevel()) {
            case 1:
               vitality = (double)Config.RATE_VITALITY_LEVEL_1;
               break;
            case 2:
               vitality = (double)Config.RATE_VITALITY_LEVEL_2;
               break;
            case 3:
               vitality = (double)Config.RATE_VITALITY_LEVEL_3;
               break;
            case 4:
               vitality = (double)Config.RATE_VITALITY_LEVEL_4;
         }
      }

      return vitality;
   }

   public byte getVitalityLevel() {
      return this.getActiveChar().getNevitSystem().isBlessingActive() ? 4 : this._vitalityLevel;
   }

   public int getVitalityLevel(boolean blessActive) {
      return Config.ENABLE_VITALITY ? (blessActive ? 4 : this._vitalityLevel) : 0;
   }

   public double getExpBonusMultiplier() {
      double vitality = this.getVitalityMultiplier();
      double nevits = this.getActiveChar().getRecommendation().getRecoMultiplier();
      double bonusExp = this.calcStat(Stats.BONUS_EXP, 0.0, null, null) / 100.0;
      double bonus = 0.0;
      bonus += Math.max(vitality, 0.0);
      bonus += Math.max(nevits, 0.0);
      bonus += Math.max(bonusExp, 0.0);
      bonus = Math.max(bonus, 1.0);
      return Math.min(bonus, Config.MAX_BONUS_EXP);
   }

   public double getSpBonusMultiplier() {
      double vitality = this.getVitalityMultiplier();
      double nevits = this.getActiveChar().getRecommendation().getRecoMultiplier();
      double bonusSp = this.calcStat(Stats.BONUS_SP, 0.0, null, null) / 100.0;
      double bonus = 0.0;
      bonus += Math.max(vitality, 0.0);
      bonus += Math.max(nevits, 0.0);
      bonus += Math.max(bonusSp, 0.0);
      bonus = Math.max(bonus, 1.0);
      return Math.min(bonus, Config.MAX_BONUS_SP);
   }
}
