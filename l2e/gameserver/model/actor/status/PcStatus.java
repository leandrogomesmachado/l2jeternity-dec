package l2e.gameserver.model.actor.status;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PcStatus extends PlayableStatus {
   private double _currentCp = 0.0;

   public PcStatus(Player activeChar) {
      super(activeChar);
   }

   @Override
   public final void reduceCp(int value) {
      if (this.getCurrentCp() > (double)value) {
         this.setCurrentCp(this.getCurrentCp() - (double)value);
      } else {
         this.setCurrentCp(0.0);
      }
   }

   @Override
   public final void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false, false);
   }

   @Override
   public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
      this.reduceHp(value, attacker, awake, isDOT, isHPConsumption, false);
   }

   public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP) {
      if (!this.getActiveChar().isDead()) {
         if (!Config.OFFLINE_MODE_NO_DAMAGE
            || this.getActiveChar().getClient() == null
            || !this.getActiveChar().getClient().isDetached()
            || (!Config.OFFLINE_TRADE_ENABLE || this.getActiveChar().getPrivateStoreType() != 1 && this.getActiveChar().getPrivateStoreType() != 3)
               && (!Config.OFFLINE_CRAFT_ENABLE || !this.getActiveChar().isInCraftMode() && this.getActiveChar().getPrivateStoreType() != 5)) {
            if (!this.getActiveChar().isInvul() || isDOT || isHPConsumption) {
               if (!isHPConsumption) {
                  this.getActiveChar().stopEffectsOnDamage(awake);
                  if (this.getActiveChar().isInCraftMode() || this.getActiveChar().isInStoreMode()) {
                     this.getActiveChar().setPrivateStoreType(0);
                     this.getActiveChar().standUp();
                     this.getActiveChar().broadcastUserInfo(true);
                  } else if (this.getActiveChar().isSitting()) {
                     this.getActiveChar().standUp();
                  }
               }

               int fullValue = (int)value;
               int tDmg = 0;
               int mpDam = 0;
               if (attacker != null && attacker != this.getActiveChar()) {
                  Player attackerPlayer = attacker.getActingPlayer();
                  if (attackerPlayer != null) {
                     if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage()) {
                        return;
                     }

                     if (this.getActiveChar().isInDuel()) {
                        if (this.getActiveChar().getDuelState() == 2) {
                           return;
                        }

                        if (this.getActiveChar().getDuelState() == 3) {
                           return;
                        }

                        if (attackerPlayer.getDuelId() != this.getActiveChar().getDuelId()) {
                           this.getActiveChar().setDuelState(4);
                        }
                     }
                  }

                  Summon summon = this.getActiveChar().getSummon();
                  if (this.getActiveChar().hasServitor() && Util.checkIfInRange(1000, this.getActiveChar(), summon, true)) {
                     tDmg = (int)value * (int)this.getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0, null, null) / 100;
                     tDmg = Math.min((int)summon.getCurrentHp() - 1, tDmg);
                     if (tDmg > 0) {
                        summon.reduceCurrentHp((double)tDmg, attacker, null);
                        value -= (double)tDmg;
                        fullValue = (int)value;
                     }
                  }

                  mpDam = (int)value * (int)this.getActiveChar().getStat().calcStat(Stats.MANA_SHIELD_PERCENT, 0.0, null, null) / 100;
                  if (mpDam > 0) {
                     mpDam = (int)(value - (double)mpDam);
                     if (!((double)mpDam > this.getActiveChar().getCurrentMp())) {
                        this.getActiveChar().reduceCurrentMp((double)mpDam);
                        SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP);
                        smsg.addNumber(mpDam);
                        this.getActiveChar().sendPacket(smsg);
                        return;
                     }

                     this.getActiveChar().sendPacket(SystemMessageId.MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING);
                     this.getActiveChar().stopSkillEffects(1556);
                     value = (double)mpDam - this.getActiveChar().getCurrentMp();
                     this.getActiveChar().setCurrentMp(0.0);
                  }

                  Player caster = this.getActiveChar().getTransferingDamageTo();
                  if (caster != null
                     && this.getActiveChar().getParty() != null
                     && Util.checkIfInRange(1000, this.getActiveChar(), caster, true)
                     && !caster.isDead()
                     && this.getActiveChar() != caster
                     && this.getActiveChar().getParty().getMembers().contains(caster)) {
                     int transferDmg = 0;
                     transferDmg = (int)value * (int)this.getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0.0, null, null) / 100;
                     transferDmg = Math.min((int)caster.getCurrentHp() - 1, transferDmg);
                     if (transferDmg > 0) {
                        int membersInRange = 0;
                        if (caster.getParty() != null) {
                           for(Player member : caster.getParty().getMembers()) {
                              if (Util.checkIfInRange(1000, member, caster, false) && member != caster) {
                                 ++membersInRange;
                              }
                           }
                        }

                        if (attacker instanceof Playable && caster.getCurrentCp() > 0.0) {
                           if (caster.getCurrentCp() > (double)transferDmg) {
                              caster.getStatus().reduceCp(transferDmg);
                           } else {
                              transferDmg = (int)((double)transferDmg - caster.getCurrentCp());
                              caster.getStatus().reduceCp((int)caster.getCurrentCp());
                           }
                        }

                        if (membersInRange > 0) {
                           caster.reduceCurrentHp((double)(transferDmg / membersInRange), attacker, null);
                           value -= (double)transferDmg;
                           fullValue = (int)value;
                        }
                     }
                  }

                  if (!ignoreCP && attacker instanceof Playable) {
                     if (this.getCurrentCp() >= value) {
                        this.setCurrentCp(this.getCurrentCp() - value);
                        value = 0.0;
                     } else {
                        value -= this.getCurrentCp();
                        this.setCurrentCp(0.0, false);
                     }
                  }

                  if (fullValue > 0 && !isDOT) {
                     SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
                     smsg.addString(this.getActiveChar().getName());
                     smsg.addCharName(attacker);
                     smsg.addNumber(fullValue);
                     this.getActiveChar().sendPacket(smsg);
                     if (tDmg > 0) {
                        smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
                        smsg.addString(this.getActiveChar().getSummon().getName());
                        smsg.addCharName(attacker);
                        smsg.addNumber(tDmg);
                        this.getActiveChar().sendPacket(smsg);
                        if (attackerPlayer != null) {
                           smsg = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR);
                           smsg.addNumber(fullValue);
                           smsg.addNumber(tDmg);
                           attackerPlayer.sendPacket(smsg);
                        }
                     }
                  }
               }

               if (value > 0.0) {
                  value = this.getCurrentHp() - value;
                  if (value <= 0.0) {
                     if (this.getActiveChar().isInDuel()) {
                        this.getActiveChar().disableAllSkills();
                        this.stopHpMpRegeneration();
                        if (attacker != null) {
                           attacker.getAI().setIntention(CtrlIntention.ACTIVE);
                           attacker.sendActionFailed();
                        }

                        DuelManager.getInstance().onPlayerDefeat(this.getActiveChar());
                        value = 1.0;
                     } else {
                        value = 0.0;
                     }
                  }

                  this.setCurrentHp(value);
               }

               if (this.getActiveChar().getCurrentHp() < 0.5) {
                  this.getActiveChar().abortAttack();
                  this.getActiveChar().abortCast();
                  if (this.getActiveChar().isInOlympiadMode()) {
                     this.stopHpMpRegeneration();
                     this.getActiveChar().setIsDead(true);
                     this.getActiveChar().setIsPendingRevive(true);
                     this.getActiveChar().getOlympiadGame().checkWinner();
                     if (this.getActiveChar().hasSummon()) {
                        this.getActiveChar().getSummon().cancelAction();
                     }

                     return;
                  }

                  this.getActiveChar().doDie(attacker);
                  if (!Config.DISABLE_TUTORIAL) {
                     QuestState qs = this.getActiveChar().getQuestState("_255_Tutorial");
                     if (qs != null) {
                        qs.getQuest().notifyEvent("CE30", null, this.getActiveChar());
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public final boolean setCurrentHp(double newHp, boolean broadcastPacket) {
      boolean result = super.setCurrentHp(newHp, broadcastPacket);
      if (!Config.DISABLE_TUTORIAL && this.getCurrentHp() <= this.getActiveChar().getStat().getMaxHp() * 0.3) {
         QuestState qs = this.getActiveChar().getQuestState("_255_Tutorial");
         if (qs != null) {
            qs.getQuest().notifyEvent("CE45", null, this.getActiveChar());
         }
      }

      return result;
   }

   @Override
   public final double getCurrentCp() {
      return this._currentCp;
   }

   @Override
   public final void setCurrentCp(double newCp) {
      this.setCurrentCp(newCp, true);
   }

   public final void setCurrentCp(double newCp, boolean broadcastPacket) {
      int currentCp = (int)this.getCurrentCp();
      double maxCp = this.getActiveChar().getStat().getMaxCp();
      synchronized(this) {
         if (this.getActiveChar().isDead()) {
            return;
         }

         if (newCp < 0.0) {
            newCp = 0.0;
         }

         if (newCp >= maxCp) {
            this._currentCp = maxCp;
            this._flagsRegenActive &= -5;
            if (this._flagsRegenActive == 0) {
               this.stopHpMpRegeneration();
            }
         } else {
            this._currentCp = newCp;
            this._flagsRegenActive = (byte)(this._flagsRegenActive | 4);
            this.startHpMpRegeneration();
         }
      }

      if ((double)currentCp != this._currentCp && broadcastPacket) {
         this.getActiveChar().broadcastStatusUpdate();
      }
   }

   @Override
   protected void doRegeneration() {
      PcStat charstat = this.getActiveChar().getStat();
      if (this.getCurrentCp() < (double)charstat.getMaxRecoverableCp()) {
         this.setCurrentCp(this.getCurrentCp() + Formulas.calcCpRegen(this.getActiveChar()), false);
      }

      if (this.getCurrentHp() < (double)charstat.getMaxRecoverableHp()) {
         this.setCurrentHp(this.getCurrentHp() + Formulas.calcHpRegen(this.getActiveChar()), false);
      }

      if (this.getCurrentMp() < (double)charstat.getMaxRecoverableMp()) {
         this.setCurrentMp(this.getCurrentMp() + Formulas.calcMpRegen(this.getActiveChar()), false);
      }

      this.getActiveChar().broadcastStatusUpdate();
   }

   public Player getActiveChar() {
      return (Player)super.getActiveChar();
   }
}
