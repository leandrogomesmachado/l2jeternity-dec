package l2e.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class BabyPetInstance extends PetInstance {
   private static final int BUFF_CONTROL = 5771;
   private static final int AWAKENING = 5753;
   protected List<SkillHolder> _buffs = null;
   protected SkillHolder _majorHeal = null;
   protected SkillHolder _minorHeal = null;
   protected SkillHolder _recharge = null;
   private Future<?> _castTask;
   protected boolean _bufferMode = true;

   public BabyPetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control) {
      super(objectId, template, owner, control);
      this.setInstanceType(GameObject.InstanceType.BabyPetInstance);
   }

   public BabyPetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, byte level) {
      super(objectId, template, owner, control, level);
      this.setInstanceType(GameObject.InstanceType.BabyPetInstance);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();

      for(PetData.L2PetSkillLearn psl : PetsParser.getInstance().getPetData(this.getId()).getAvailableSkills()) {
         int id = psl.getId();
         int lvl = PetsParser.getInstance().getPetData(this.getId()).getAvailableLevel(id, this.getLevel());
         if (lvl != 0) {
            Skill skill = SkillsParser.getInstance().getInfo(id, lvl);
            if (skill != null && skill.getId() != 5771 && skill.getId() != 5753) {
               switch(skill.getSkillType()) {
                  case BUFF:
                     if (skill.getId() >= 23167 && skill.getId() <= 23169) {
                        break;
                     }

                     if (this._buffs == null) {
                        this._buffs = new ArrayList<>();
                     }

                     this._buffs.add(new SkillHolder(skill));
                     break;
                  case DUMMY:
                     if (skill.hasEffectType(EffectType.MANAHEAL_BY_LEVEL)) {
                        this._recharge = new SkillHolder(skill);
                     } else if (skill.hasEffectType(EffectType.HEAL)) {
                        if (skill.isPetMajorHeal()) {
                           this._majorHeal = new SkillHolder(skill);
                        } else {
                           this._minorHeal = new SkillHolder(skill);
                        }
                     }
               }
            }
         }
      }

      this.startCastTask();
   }

   @Override
   protected void onDeath(Creature killer) {
      this.stopCastTask();
      this.abortCast();
      super.onDeath(killer);
   }

   @Override
   public synchronized void unSummon(Player owner) {
      this.stopCastTask();
      this.abortCast();
      super.unSummon(owner);
   }

   @Override
   public void doRevive() {
      super.doRevive();
      this.startCastTask();
   }

   @Override
   public void onDecay() {
      super.onDecay();
      if (this._buffs != null) {
         this._buffs.clear();
      }
   }

   @Override
   public void onTeleported() {
      this.stopCastTask();
      if (this._buffs != null) {
         this._buffs.clear();
      }

      super.onTeleported();
   }

   private final void startCastTask() {
      if (this._majorHeal != null || this._buffs != null || this._recharge != null && this._castTask == null && !this.isDead()) {
         this._castTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new BabyPetInstance.CastTask(this), 3000L, 2000L);
      }
   }

   @Override
   public void switchMode() {
      this._bufferMode = !this._bufferMode;
   }

   public boolean isInSupportMode() {
      return this._bufferMode;
   }

   private final void stopCastTask() {
      if (this._castTask != null) {
         this._castTask.cancel(false);
         this._castTask = null;
      }
   }

   protected void castSkill(Skill skill) {
      boolean previousFollowStatus = this.isInFollowStatus();
      if (previousFollowStatus || this.isInsideRadius(this.getOwner(), skill.getCastRange(), true, true)) {
         if (this.checkDoCastConditions(skill, false) && GeoEngine.canSeeTarget(this, this.getOwner(), false)) {
            this.setTarget(this.getOwner());
            this.useMagic(skill, false, false, true);
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
            msg.addSkillName(skill);
            this.sendPacket(msg);
            if (previousFollowStatus != this.isInFollowStatus()) {
               this.setFollowStatus(previousFollowStatus);
            }
         }
      }
   }

   private class CastTask implements Runnable {
      private final BabyPetInstance _baby;
      private final List<Skill> _currentBuffs = new ArrayList<>();

      public CastTask(BabyPetInstance baby) {
         this._baby = baby;
      }

      @Override
      public void run() {
         if (this._baby != null && !this._baby.isDead()) {
            Player owner = this._baby.getOwner();
            if (owner != null
               && !owner.isDead()
               && !owner.isInvul()
               && !this._baby.isCastingNow()
               && !this._baby.isBetrayed()
               && !this._baby.isMuted()
               && !this._baby.isOutOfControl()
               && this._baby.getAI().getIntention() != CtrlIntention.CAST
               && this._baby.getAI().getIntention() != CtrlIntention.ATTACK) {
               Skill skill = null;
               if (BabyPetInstance.this._majorHeal != null || BabyPetInstance.this._minorHeal != null) {
                  double hpPercent = owner.getCurrentHp() / owner.getMaxHp();
                  double percent = 0.0;
                  if (BabyPetInstance.this._majorHeal != null) {
                     percent = PetsParser.getInstance()
                        .getPetData(this._baby.getId())
                        .getHpPercent(BabyPetInstance.this._majorHeal.getId(), BabyPetInstance.this._majorHeal.getLvl());
                     skill = BabyPetInstance.this._majorHeal.getSkill();
                     if (!this._baby.isSkillDisabled(skill) && hpPercent < percent && this._baby.getCurrentMp() >= (double)skill.getMpConsume()) {
                        BabyPetInstance.this.castSkill(skill);
                        return;
                     }
                  }

                  if (BabyPetInstance.this._minorHeal != null) {
                     percent = PetsParser.getInstance()
                        .getPetData(this._baby.getId())
                        .getHpPercent(BabyPetInstance.this._minorHeal.getId(), BabyPetInstance.this._minorHeal.getLvl());
                     skill = BabyPetInstance.this._minorHeal.getSkill();
                     if (!this._baby.isSkillDisabled(skill) && hpPercent < percent && this._baby.getCurrentMp() >= (double)skill.getMpConsume()) {
                        BabyPetInstance.this.castSkill(skill);
                        return;
                     }
                  }
               }

               if (this._baby.getFirstEffect(5771) == null && BabyPetInstance.this.isInSupportMode()) {
                  if (BabyPetInstance.this._buffs != null && !BabyPetInstance.this._buffs.isEmpty()) {
                     for(SkillHolder i : BabyPetInstance.this._buffs) {
                        skill = i.getSkill();
                        if (!this._baby.isSkillDisabled(skill)
                           && skill.getTargetType() != TargetType.SELF
                           && this._baby.getCurrentMp() >= (double)skill.getMpConsume()) {
                           this._currentBuffs.add(skill);
                        }
                     }
                  }

                  if (!this._currentBuffs.isEmpty()) {
                     List<Skill> skillList = new ArrayList<>();
                     Skill rndSkill = null;
                     if (owner.getEffectList().hasBuffs()) {
                        for(Effect e : owner.getEffectList().getBuffs()) {
                           if (e != null) {
                              Skill currentSkill = e.getSkill();
                              if (!currentSkill.isDebuff() && !currentSkill.isPassive() && !currentSkill.isToggle()) {
                                 for(Skill sk : this._currentBuffs) {
                                    if (sk != null) {
                                       if (currentSkill.getId() == sk.getId() && currentSkill.getLevel() >= sk.getLevel()) {
                                          if (!skillList.contains(sk)) {
                                             skillList.add(sk);
                                          }
                                       } else if (owner.getEffectList().getAllBlockedBuffSlots() != null
                                          && owner.getEffectList().getAllBlockedBuffSlots().contains(sk.getEffectTemplates()[0].abnormalType)) {
                                          if (!skillList.contains(sk)) {
                                             skillList.add(sk);
                                          }
                                       } else if (sk.hasEffects()
                                          && !"none".equals(sk.getEffectTemplates()[0].abnormalType)
                                          && e.getAbnormalType().equalsIgnoreCase(sk.getEffectTemplates()[0].abnormalType)
                                          && e.getAbnormalLvl() >= sk.getEffectTemplates()[0].abnormalLvl
                                          && !skillList.contains(sk)) {
                                          skillList.add(sk);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }

                     if (!skillList.isEmpty()) {
                        for(Skill sk : skillList) {
                           this._currentBuffs.remove(sk);
                        }
                     }

                     skillList.clear();
                     if (!this._currentBuffs.isEmpty()) {
                        rndSkill = this._currentBuffs.get(Rnd.get(this._currentBuffs.size()));
                        if (rndSkill != null) {
                           BabyPetInstance.this.castSkill(rndSkill);
                        }

                        this._currentBuffs.clear();
                        return;
                     }
                  }
               }

               if (BabyPetInstance.this._recharge != null && owner.isInCombat() && owner.getCurrentMp() / owner.getMaxMp() < 0.6 && Rnd.get(100) <= 60) {
                  skill = BabyPetInstance.this._recharge.getSkill();
                  if (!this._baby.isSkillDisabled(skill) && this._baby.getCurrentMp() >= (double)skill.getMpConsume()) {
                     BabyPetInstance.this.castSkill(skill);
                     return;
                  }
               }
            }
         }
      }
   }
}
