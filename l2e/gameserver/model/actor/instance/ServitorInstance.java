package l2e.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharSummonHolder;
import l2e.gameserver.data.holder.SummonEffectsHolder;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.l2skills.SkillSummon;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.serverpackets.SetSummonRemainTime;

public class ServitorInstance extends Summon {
   protected static final Logger log = Logger.getLogger(ServitorInstance.class.getName());
   private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index) VALUES (?,?,?,?,?,?,?,?,?)";
   private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
   private static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
   private float _expPenalty = 0.0F;
   private int _itemConsumeId;
   private int _itemConsumeCount;
   private int _itemConsumeSteps;
   private final int _totalLifeTime;
   private final int _timeLostIdle;
   private final int _timeLostActive;
   private int _timeRemaining;
   private int _nextItemConsumeTime;
   public int lastShowntimeRemaining;
   protected Future<?> _summonLifeTask;
   private int _referenceSkill;
   private boolean _shareElementals = false;
   private double _sharedElementalsPercent = 1.0;

   public ServitorInstance(int objectId, NpcTemplate template, Player owner, Skill skill) {
      super(objectId, template, owner);
      this.setInstanceType(GameObject.InstanceType.ServitorInstance);
      this.setShowSummonAnimation(true);
      if (skill != null) {
         SkillSummon summonSkill = (SkillSummon)skill;
         this._itemConsumeId = summonSkill.getItemConsumeIdOT();
         this._itemConsumeCount = summonSkill.getItemConsumeOT();
         this._itemConsumeSteps = summonSkill.getItemConsumeSteps();
         this._totalLifeTime = summonSkill.getTotalLifeTime();
         this._timeLostIdle = summonSkill.getTimeLostIdle();
         this._timeLostActive = summonSkill.getTimeLostActive();
         this._referenceSkill = summonSkill.getId();
      } else {
         this._itemConsumeId = 0;
         this._itemConsumeCount = 0;
         this._itemConsumeSteps = 0;
         this._totalLifeTime = 1200000;
         this._timeLostIdle = 1000;
         this._timeLostActive = 1000;
      }

      this._timeRemaining = this._totalLifeTime;
      this.lastShowntimeRemaining = this._totalLifeTime;
      if (this._itemConsumeId == 0) {
         this._nextItemConsumeTime = -1;
      } else if (this._itemConsumeSteps == 0) {
         this._nextItemConsumeTime = -1;
      } else {
         this._nextItemConsumeTime = this._totalLifeTime - this._totalLifeTime / (this._itemConsumeSteps + 1);
      }

      int delay = 1000;
      if (Config.DEBUG && this._itemConsumeCount != 0) {
         _log.warning(
            this.getClass().getSimpleName()
               + ": Item Consume ID: "
               + this._itemConsumeId
               + ", Count: "
               + this._itemConsumeCount
               + ", Rate: "
               + this._itemConsumeSteps
               + " times."
         );
      }

      if (Config.DEBUG) {
         _log.warning(this.getClass().getSimpleName() + ": Task Delay " + 1 + " seconds.");
      }

      this._summonLifeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ServitorInstance.SummonLifetime(this.getOwner(), this), 1000L, 1000L);
   }

   @Override
   public final int getLevel() {
      return this.getTemplate() != null ? this.getTemplate().getLevel() : 0;
   }

   @Override
   public int getSummonType() {
      return 1;
   }

   public void setExpPenalty(float expPenalty) {
      this._expPenalty = expPenalty;
   }

   public float getExpPenalty() {
      return this._expPenalty;
   }

   public void setSharedElementals(boolean val) {
      this._shareElementals = val;
   }

   public boolean isSharingElementals() {
      return this._shareElementals;
   }

   public void setSharedElementalsValue(double val) {
      this._sharedElementalsPercent = val;
   }

   public double sharedElementalsPercent() {
      return this._sharedElementalsPercent;
   }

   public int getItemConsumeCount() {
      return this._itemConsumeCount;
   }

   public int getItemConsumeId() {
      return this._itemConsumeId;
   }

   public int getItemConsumeSteps() {
      return this._itemConsumeSteps;
   }

   public int getNextItemConsumeTime() {
      return this._nextItemConsumeTime;
   }

   public int getTotalLifeTime() {
      return this._totalLifeTime;
   }

   public int getTimeLostIdle() {
      return this._timeLostIdle;
   }

   public int getTimeLostActive() {
      return this._timeLostActive;
   }

   public int getTimeRemaining() {
      return this._timeRemaining;
   }

   public void setNextItemConsumeTime(int value) {
      this._nextItemConsumeTime = value;
   }

   public void decNextItemConsumeTime(int value) {
      this._nextItemConsumeTime -= value;
   }

   public void decTimeRemaining(int value) {
      this._timeRemaining -= value;
   }

   public void addExpAndSp(int addToExp, int addToSp) {
      this.getOwner().addExpAndSp((long)addToExp, addToSp);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (Config.DEBUG) {
         _log.warning(this.getClass().getSimpleName() + ": " + this.getTemplate().getName() + " (" + this.getOwner().getName() + ") has been killed.");
      }

      if (this._summonLifeTask != null) {
         this._summonLifeTask.cancel(false);
         this._summonLifeTask = null;
      }

      CharSummonHolder.getInstance().removeServitor(this.getOwner());
      super.onDeath(killer);
   }

   @Override
   public void setRestoreSummon(boolean val) {
      this._restoreSummon = val;
   }

   @Override
   public final void stopSkillEffects(int skillId) {
      super.stopSkillEffects(skillId);
      SummonEffectsHolder.getInstance().removeServitorEffects(this.getOwner(), this.getReferenceSkill(), skillId);
   }

   @Override
   public void store() {
      if (this._referenceSkill != 0 && !this.isDead()) {
         if (Config.RESTORE_SERVITOR_ON_RECONNECT) {
            CharSummonHolder.getInstance().saveSummon(this);
         }
      }
   }

   @Override
   public void storeEffect(boolean storeEffects) {
      if (Config.SUMMON_STORE_SKILL_COOLTIME) {
         if (this.getOwner() != null && !this.getOwner().isInOlympiadMode()) {
            SummonEffectsHolder.getInstance().clearServitorEffects(this.getOwner(), this.getReferenceSkill());

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement(
                  "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?"
               );
            ) {
               con.setAutoCommit(false);
               statement.setInt(1, this.getOwner().getObjectId());
               statement.setInt(2, this.getOwner().getClassIndex());
               statement.setInt(3, this.getReferenceSkill());
               statement.execute();
               int buff_index = 0;
               List<Integer> storedSkills = new LinkedList<>();
               if (storeEffects) {
                  try (PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index) VALUES (?,?,?,?,?,?,?,?,?)"
                     )) {
                     for(Effect effect : this.getAllEffects()) {
                        if (effect != null) {
                           switch(effect.getEffectType()) {
                              case HEAL_OVER_TIME:
                              case CPHEAL_OVER_TIME:
                              case HIDE:
                                 break;
                              default:
                                 if (!effect.getAbnormalType().equalsIgnoreCase("LIFE_FORCE_OTHERS")) {
                                    Skill skill = effect.getSkill();
                                    if (!skill.isToggle() && (!skill.isDance() || Config.ALT_STORE_DANCES) && !storedSkills.contains(skill.getReuseHashCode())
                                       )
                                     {
                                       storedSkills.add(skill.getReuseHashCode());
                                       ps2.setInt(1, this.getOwner().getObjectId());
                                       ps2.setInt(2, this.getOwner().getClassIndex());
                                       ps2.setInt(3, this.getReferenceSkill());
                                       ps2.setInt(4, skill.getId());
                                       ps2.setInt(5, skill.getLevel());
                                       ps2.setInt(6, effect.getTickCount());
                                       ps2.setInt(7, effect.getTime());
                                       ps2.setInt(8, effect.getAbnormalTime());
                                       ps2.setInt(9, ++buff_index);
                                       ps2.addBatch();
                                       SummonEffectsHolder.getInstance()
                                          .addServitorEffect(
                                             this.getOwner(),
                                             this.getReferenceSkill(),
                                             skill,
                                             effect.getTickCount(),
                                             effect.getTime(),
                                             effect.getAbnormalTime()
                                          );
                                    }
                                 }
                           }
                        }
                     }
                  }
               }

               con.commit();
            } catch (Exception var66) {
               _log.log(Level.WARNING, "Could not store summon effect data: ", (Throwable)var66);
            }
         }
      }
   }

   @Override
   public void restoreEffects() {
      if (!this.getOwner().isInOlympiadMode()) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            if (!SummonEffectsHolder.getInstance().containsSkill(this.getOwner(), this.getReferenceSkill())) {
               try (PreparedStatement statement = con.prepareStatement(
                     "SELECT skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC"
                  )) {
                  statement.setInt(1, this.getOwner().getObjectId());
                  statement.setInt(2, this.getOwner().getClassIndex());
                  statement.setInt(3, this.getReferenceSkill());

                  try (ResultSet rset = statement.executeQuery()) {
                     while(rset.next()) {
                        int effectCount = rset.getInt("effect_count");
                        int effectCurTime = rset.getInt("effect_cur_time");
                        int effectTotalTime = rset.getInt("effect_total_time");
                        Skill skill = SkillsParser.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
                        if (skill != null && skill.hasEffects()) {
                           SummonEffectsHolder.getInstance()
                              .addServitorEffect(this.getOwner(), this.getReferenceSkill(), skill, effectCount, effectCurTime, effectTotalTime);
                        }
                     }
                  }
               }
            }

            try (PreparedStatement statement = con.prepareStatement(
                  "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?"
               )) {
               statement.setInt(1, this.getOwner().getObjectId());
               statement.setInt(2, this.getOwner().getClassIndex());
               statement.setInt(3, this.getReferenceSkill());
               statement.executeUpdate();
            }
         } catch (Exception var116) {
            _log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var116.getMessage(), (Throwable)var116);
         } finally {
            SummonEffectsHolder.getInstance().applyServitorEffects(this, this.getOwner(), this.getReferenceSkill());
         }
      }
   }

   @Override
   public void unSummon(Player owner) {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": " + this.getTemplate().getName() + " (" + owner.getName() + ") unsummoned.");
      }

      if (this._summonLifeTask != null) {
         this._summonLifeTask.cancel(false);
         this._summonLifeTask = null;
      }

      super.unSummon(owner);
      if (!this._restoreSummon) {
         CharSummonHolder.getInstance().removeServitor(owner);
      }
   }

   @Override
   public boolean destroyItem(String process, int objectId, long count, GameObject reference, boolean sendMessage) {
      return this.getOwner().destroyItem(process, objectId, count, reference, sendMessage);
   }

   @Override
   public boolean destroyItemByItemId(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      if (Config.DEBUG) {
         _log.warning(this.getClass().getSimpleName() + ": " + this.getTemplate().getName() + " (" + this.getOwner().getName() + ") consume.");
      }

      return this.getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
   }

   public void setTimeRemaining(int time) {
      this._timeRemaining = time;
   }

   public int getReferenceSkill() {
      return this._referenceSkill;
   }

   @Override
   public byte getAttackElement() {
      return this.isSharingElementals() && this.getOwner() != null ? this.getOwner().getAttackElement() : super.getAttackElement();
   }

   @Override
   public int getAttackElementValue(byte attackAttribute) {
      return this.isSharingElementals() && this.getOwner() != null
         ? (int)((double)this.getOwner().getAttackElementValue(attackAttribute) * this.sharedElementalsPercent())
         : super.getAttackElementValue(attackAttribute);
   }

   @Override
   public int getDefenseElementValue(byte defenseAttribute) {
      return this.isSharingElementals() && this.getOwner() != null
         ? (int)((double)this.getOwner().getDefenseElementValue(defenseAttribute) * this.sharedElementalsPercent())
         : super.getDefenseElementValue(defenseAttribute);
   }

   @Override
   public boolean isServitor() {
      return true;
   }

   @Override
   public double getMAtk(Creature target, Skill skill) {
      return super.getMAtk(target, skill) + this.getOwner().getMAtk(target, skill) * (this.getOwner().getServitorShareBonus(Stats.MAGIC_ATTACK) - 1.0);
   }

   @Override
   public double getMDef(Creature target, Skill skill) {
      return super.getMDef(target, skill) + this.getOwner().getMDef(target, skill) * (this.getOwner().getServitorShareBonus(Stats.MAGIC_DEFENCE) - 1.0);
   }

   @Override
   public double getPAtk(Creature target) {
      return super.getPAtk(target) + this.getOwner().getPAtk(target) * (this.getOwner().getServitorShareBonus(Stats.POWER_ATTACK) - 1.0);
   }

   @Override
   public double getPDef(Creature target) {
      return super.getPDef(target) + this.getOwner().getPDef(target) * (this.getOwner().getServitorShareBonus(Stats.POWER_DEFENCE) - 1.0);
   }

   @Override
   public double getMAtkSpd() {
      return super.getMAtkSpd() + this.getOwner().getMAtkSpd() * (this.getOwner().getServitorShareBonus(Stats.MAGIC_ATTACK_SPEED) - 1.0);
   }

   @Override
   public double getCriticalHit(Creature target, Skill skill) {
      return super.getCriticalHit(target, skill)
         + this.getOwner().getCriticalHit(target, skill) * (this.getOwner().getServitorShareBonus(Stats.CRITICAL_RATE) - 1.0);
   }

   @Override
   public double getPAtkSpd() {
      return super.getPAtkSpd() + this.getOwner().getPAtkSpd() * (this.getOwner().getServitorShareBonus(Stats.POWER_ATTACK_SPEED) - 1.0);
   }

   @Override
   public double getLevelMod() {
      return Config.ALLOW_SUMMON_LVL_MOD ? (double)(this.getLevel() + 89) / 100.0 : 1.0;
   }

   static class SummonLifetime implements Runnable {
      private final Player _activeChar;
      private final ServitorInstance _summon;

      SummonLifetime(Player activeChar, ServitorInstance newpet) {
         this._activeChar = activeChar;
         this._summon = newpet;
      }

      @Override
      public void run() {
         if (Config.DEBUG) {
            ServitorInstance.log
               .warning(this.getClass().getSimpleName() + ": " + this._summon.getTemplate().getName() + " (" + this._activeChar.getName() + ") run task.");
         }

         try {
            double oldTimeRemaining = (double)this._summon.getTimeRemaining();
            int maxTime = this._summon.getTotalLifeTime();
            if (this._summon.isAttackingNow()) {
               this._summon.decTimeRemaining(this._summon.getTimeLostActive());
            } else {
               this._summon.decTimeRemaining(this._summon.getTimeLostIdle());
            }

            double newTimeRemaining = (double)this._summon.getTimeRemaining();
            if (newTimeRemaining < 0.0) {
               this._summon.unSummon(this._activeChar);
            } else if (newTimeRemaining <= (double)this._summon.getNextItemConsumeTime() && oldTimeRemaining > (double)this._summon.getNextItemConsumeTime()) {
               this._summon.decNextItemConsumeTime(maxTime / (this._summon.getItemConsumeSteps() + 1));
               if (this._summon.getItemConsumeCount() > 0
                  && this._summon.getItemConsumeId() != 0
                  && !this._summon.isDead()
                  && !this._summon
                     .destroyItemByItemId("Consume", this._summon.getItemConsumeId(), (long)this._summon.getItemConsumeCount(), this._activeChar, true)) {
                  this._summon.unSummon(this._activeChar);
               }
            }

            if ((double)this._summon.lastShowntimeRemaining - newTimeRemaining > (double)(maxTime / 352)) {
               this._summon.sendPacket(new SetSummonRemainTime(maxTime, (int)newTimeRemaining));
               this._summon.lastShowntimeRemaining = (int)newTimeRemaining;
               this._summon.updateEffectIcons();
            }
         } catch (Exception var6) {
            ServitorInstance.log.log(Level.SEVERE, "Error on player [" + this._activeChar.getName() + "] summon item consume task.", (Throwable)var6);
         }
      }
   }
}
