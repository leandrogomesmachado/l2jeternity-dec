package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2e.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2e.gameserver.network.serverpackets.PartySpelled;
import l2e.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CharEffectList {
   protected static final Logger _log = Logger.getLogger(CharEffectList.class.getName());
   private static final Effect[] EMPTY_EFFECTS = new Effect[0];
   private List<Effect> _buffs;
   private List<Effect> _debuffs;
   private List<Effect> _passives;
   private Map<String, List<Effect>> _stackedEffects;
   private volatile boolean _hasBuffsRemovedOnAnyAction = false;
   private volatile boolean _hasBuffsRemovedOnDamage = false;
   private volatile boolean _hasDebuffsRemovedOnDamage = false;
   private boolean _queuesInitialized = false;
   private LinkedBlockingQueue<Effect> _addQueue;
   private LinkedBlockingQueue<Effect> _removeQueue;
   private final AtomicBoolean queueLock = new AtomicBoolean();
   private int _effectFlags;
   private boolean _partyOnly = false;
   private final Creature _owner;
   private Effect[] _effectCache;
   private volatile boolean _rebuildCache = true;
   private final Object _buildEffectLock = new Object();
   private ScheduledFuture<?> _effectIconsUpdate;
   private volatile Set<String> _blockedBuffSlots = null;
   private Effect _shortBuff = null;

   public CharEffectList(Creature owner) {
      this._owner = owner;
   }

   public final Effect[] getAllEffects() {
      if (this.isEmpty()) {
         return EMPTY_EFFECTS;
      } else {
         synchronized(this._buildEffectLock) {
            if (!this._rebuildCache) {
               return this._effectCache;
            } else {
               this._rebuildCache = false;
               List<Effect> temp = new ArrayList<>();
               if (this.hasBuffs()) {
                  temp.addAll(this.getBuffs());
               }

               if (this.hasDebuffs()) {
                  temp.addAll(this.getDebuffs());
               }

               Effect[] tempArray = new Effect[temp.size()];
               temp.toArray(tempArray);
               return this._effectCache = tempArray;
            }
         }
      }
   }

   public final Effect getFirstEffect(EffectType tp) {
      Effect effectNotInUse = null;
      if (this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null && e.getEffectType() == tp) {
               if (e.isInUse()) {
                  return e;
               }

               effectNotInUse = e;
            }
         }
      }

      if (effectNotInUse == null && this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null && e.getEffectType() == tp) {
               if (e.isInUse()) {
                  return e;
               }

               effectNotInUse = e;
            }
         }
      }

      return effectNotInUse;
   }

   public final Effect getFirstEffect(Skill skill) {
      Effect effectNotInUse = null;
      if (skill.isDebuff()) {
         if (this.hasDebuffs()) {
            for(Effect e : this.getDebuffs()) {
               if (e != null && e.getSkill() == skill) {
                  if (e.isInUse()) {
                     return e;
                  }

                  effectNotInUse = e;
               }
            }
         }
      } else if (this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null && e.getSkill() == skill) {
               if (e.isInUse()) {
                  return e;
               }

               effectNotInUse = e;
            }
         }
      }

      return effectNotInUse;
   }

   public final Effect getFirstEffect(int skillId) {
      Effect effectNotInUse = null;
      if (this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null && e.getSkill().getId() == skillId) {
               if (e.isInUse()) {
                  return e;
               }

               effectNotInUse = e;
            }
         }
      }

      if (effectNotInUse == null && this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null && e.getSkill().getId() == skillId) {
               if (e.isInUse()) {
                  return e;
               }

               effectNotInUse = e;
            }
         }
      }

      return effectNotInUse;
   }

   public final Effect getFirstPassiveEffect(EffectType type) {
      if (this.hasPassives()) {
         for(Effect e : this.getPassives()) {
            if (e != null && e.getEffectType() == type && e.isInUse()) {
               return e;
            }
         }
      }

      return null;
   }

   private boolean doesStack(Skill checkSkill) {
      if (this.hasBuffs()
         && checkSkill._effectTemplates != null
         && checkSkill._effectTemplates.length >= 1
         && checkSkill._effectTemplates[0].abnormalType != null
         && !"none".equals(checkSkill._effectTemplates[0].abnormalType)) {
         String stackType = checkSkill._effectTemplates[0].abnormalType;

         for(Effect e : this.getBuffs()) {
            if (e.getAbnormalType() != null && e.getAbnormalType().equalsIgnoreCase(stackType)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public int getBuffCount() {
      if (!this.hasBuffs()) {
         return 0;
      } else {
         List<Integer> buffIds = new LinkedList<>();

         for(Effect e : this.getBuffs()) {
            if (e != null
               && e.isIconDisplay()
               && !e.getSkill().isDance()
               && !e.getSkill().isTriggeredSkill()
               && !e.getSkill().is7Signs()
               && !e.getSkill().isPassive()
               && !e.getSkill().isToggle()
               && !e.getSkill().isDebuff()
               && !e.getSkill().isHealingPotionSkill()) {
               int skillId = e.getSkill().getId();
               if (!buffIds.contains(skillId)) {
                  buffIds.add(skillId);
               }
            }
         }

         return buffIds.size();
      }
   }

   public int getDanceCount() {
      if (!this.hasBuffs()) {
         return 0;
      } else {
         List<Integer> buffIds = new LinkedList<>();

         for(Effect e : this.getBuffs()) {
            if (e != null && e.getSkill().isDance() && e.isInUse() && !e.isInstant()) {
               switch(e.getEffectType()) {
                  case CANCEL:
                  case CANCEL_ALL:
                  case CANCEL_BY_SLOT:
                     break;
                  default:
                     int skillId = e.getSkill().getId();
                     if (!buffIds.contains(skillId)) {
                        buffIds.add(skillId);
                     }
               }
            }
         }

         return buffIds.size();
      }
   }

   public List<Effect> getEffects() {
      if (this.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<Effect> buffs = new ArrayList<>();
         if (this.hasBuffs()) {
            buffs.addAll(this.getBuffs());
         }

         if (this.hasDebuffs()) {
            buffs.addAll(this.getDebuffs());
         }

         return buffs;
      }
   }

   public int getTriggeredBuffCount() {
      if (!this.hasBuffs()) {
         return 0;
      } else {
         List<Integer> buffIds = new LinkedList<>();

         for(Effect e : this.getBuffs()) {
            if (e != null && e.getSkill().isTriggeredSkill() && e.isInUse()) {
               int skillId = e.getSkill().getId();
               if (!buffIds.contains(skillId)) {
                  buffIds.add(skillId);
               }
            }
         }

         return buffIds.size();
      }
   }

   public final void stopAllEffects() {
      for(Effect e : this.getAllEffects()) {
         if (e != null) {
            e.exit(true, true);
         }
      }
   }

   public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
      for(Effect e : this.getAllEffects()) {
         if (e != null && !e.getSkill().isStayAfterDeath()) {
            e.exit(true, Config.DISPLAY_MESSAGE);
         }
      }

      this.getEffects().stream().filter(ex -> ex != null && !ex.getSkill().isStayAfterDeath()).forEach(ex -> ex.exit(true, Config.DISPLAY_MESSAGE));
   }

   public void stopAllToggles() {
      this.getBuffs().stream().filter(e -> e != null && e.getSkill().isToggle()).forEach(e -> e.exit());
   }

   public final void stopEffects(EffectType type) {
      this.getBuffs().stream().filter(e -> e != null && e.getEffectType() == type).forEach(e -> this.stopSkillEffects(e.getSkill().getId()));
      this.getDebuffs().stream().filter(e -> e != null && e.getEffectType() == type).forEach(e -> this.stopSkillEffects(e.getSkill().getId()));
   }

   public final void stopSkillEffects(int skillId) {
      this.getBuffs().stream().filter(e -> e != null && e.getSkill().getId() == skillId).forEach(e -> e.exit());
      this.getDebuffs().stream().filter(e -> e != null && e.getSkill().getId() == skillId).forEach(e -> e.exit());
   }

   public final void stopSkillEffect(Effect newEffect) {
      if (this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null
               && e.getClass().getSimpleName().equalsIgnoreCase(newEffect.getClass().getSimpleName())
               && e.getSkill().getId() == newEffect.getSkill().getId()
               && e.getEffectType() == newEffect.getEffectType()
               && e.getAbnormalLvl() <= newEffect.getAbnormalLvl()
               && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType())) {
               e.exit();
            }
         }
      }
   }

   public void stopEffectsOnAction() {
      if (this._hasBuffsRemovedOnAnyAction) {
         this.getBuffs().stream().filter(e -> e != null && e.getSkill().isRemovedOnAnyActionExceptMove()).forEach(e -> e.exit(true, true));
      }
   }

   public void stopEffectsOnDamage(boolean awake) {
      if (this._hasBuffsRemovedOnDamage && this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null && e.getSkill().isRemovedOnDamage() && (awake || e.getSkill().getSkillType() != SkillType.SLEEP)) {
               e.exit(true, true);
            }
         }
      }

      if (this._hasDebuffsRemovedOnDamage && this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null && e.getSkill().isRemovedOnDamage() && (awake || e.getSkill().getSkillType() != SkillType.SLEEP)) {
               e.exit(true, true);
            }
         }
      }
   }

   public void updateEffectIcons(boolean partyOnly, boolean printMessage) {
      if (this.hasBuffs() || this.hasDebuffs()) {
         if (partyOnly) {
            this._partyOnly = true;
         }

         this.queueRunner(printMessage);
      }
   }

   public void queueEffect(Effect effect, boolean remove, boolean printMessage) {
      if (effect != null) {
         if (!this._queuesInitialized) {
            this.init();
         }

         if (remove) {
            this._removeQueue.offer(effect);
         } else {
            this._addQueue.offer(effect);
         }

         this.queueRunner(printMessage);
      }
   }

   private synchronized void init() {
      if (!this._queuesInitialized) {
         this._addQueue = new LinkedBlockingQueue<>();
         this._removeQueue = new LinkedBlockingQueue<>();
         this._queuesInitialized = true;
      }
   }

   private void queueRunner(boolean printMessage) {
      if (this.queueLock.compareAndSet(false, true)) {
         try {
            do {
               Effect effect;
               while((effect = this._removeQueue.poll()) != null) {
                  this.removeEffectFromQueue(effect, printMessage);
                  this._partyOnly = false;
               }

               if ((effect = this._addQueue.poll()) != null) {
                  this.addEffectFromQueue(effect);
                  this._partyOnly = false;
               }
            } while(!this._addQueue.isEmpty() || !this._removeQueue.isEmpty());

            this.computeEffectFlags();
            this.updateEffectIcons();
         } finally {
            this.queueLock.set(false);
         }
      }
   }

   protected void removeEffectFromQueue(Effect effect, boolean printMessage) {
      if (effect != null) {
         if (effect.getSkill().isPassive() && effect.setInUse(false)) {
            this._owner.removeStatsOwner(effect.getStatFuncs());
            if (this._passives != null) {
               this._passives.remove(effect);
            }
         }

         this._rebuildCache = true;
         List<Effect> effectList;
         if (effect.getSkill().isDebuff()) {
            if (!this.hasDebuffs()) {
               return;
            }

            effectList = this.getDebuffs();
         } else {
            if (!this.hasBuffs()) {
               return;
            }

            effectList = this.getBuffs();
         }

         if ("none".equals(effect.getAbnormalType())) {
            this._owner.removeStatsOwner(effect);
         } else {
            if (this._stackedEffects == null) {
               return;
            }

            List<Effect> stackQueue = this._stackedEffects.get(effect.getAbnormalType());
            if (stackQueue == null || stackQueue.isEmpty()) {
               return;
            }

            int index = stackQueue.indexOf(effect);
            if (index >= 0) {
               stackQueue.remove(effect);
               if (index == 0) {
                  this._owner.removeStatsOwner(effect);
                  if (!stackQueue.isEmpty()) {
                     Effect newStackedEffect = this.listsContains(stackQueue.get(0));
                     if (newStackedEffect != null && newStackedEffect.setInUse(true)) {
                        this._owner.addStatFuncs(newStackedEffect.getStatFuncs());
                     }
                  }
               }

               if (stackQueue.isEmpty()) {
                  this._stackedEffects.remove(effect.getAbnormalType());
               } else {
                  this._stackedEffects.put(effect.getAbnormalType(), stackQueue);
               }
            }
         }

         if (effectList.remove(effect) && this._owner.isPlayer() && effect.isIconDisplay() && !effect.isInstant() && printMessage) {
            SystemMessage sm;
            if (effect.getTickCount() >= effect.getEffectTemplate().getTotalTickCount() - 1 && effect.isIconDisplay() && !effect.isInstant()) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
            } else {
               sm = effect.getSkill().isToggle()
                  ? SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED)
                  : SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
            }

            sm.addSkillName(effect);
            this._owner.sendPacket(sm);
         }

         if (effect == this._owner.getEffectList().getShortBuff()) {
            this._owner.getEffectList().shortBuffStatusUpdate(null);
         }
      }
   }

   protected void addEffectFromQueue(Effect newEffect) {
      if (newEffect != null) {
         Skill newSkill = newEffect.getSkill();
         if (this._blockedBuffSlots == null || !this._blockedBuffSlots.contains(newEffect.getAbnormalType())) {
            if (newEffect.getSkill().isPassive()) {
               if ("none".equals(newEffect.getAbnormalType()) && newEffect.setInUse(true)) {
                  for(Effect eff : this.getPassives()) {
                     if (eff != null && eff.getEffectTemplate().equals(newEffect.getEffectTemplate())) {
                        eff.exit();
                     }
                  }

                  this._owner.addStatFuncs(newEffect.getStatFuncs());
                  this.getPassives().add(newEffect);
               }
            } else {
               this._rebuildCache = true;
               if (newSkill.isDebuff()) {
                  for(Effect e : this.getDebuffs()) {
                     if (e != null
                        && !e.getAbnormalType().equals("none")
                        && e.getSkill().getId() == newEffect.getSkill().getId()
                        && e.getEffectType() == newEffect.getEffectType()
                        && e.getAbnormalLvl() == newEffect.getAbnormalLvl()
                        && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType())) {
                        newEffect.stopEffectTask(true);
                        return;
                     }
                  }

                  this.getDebuffs().add(newEffect);
               } else {
                  for(Effect e : this.getBuffs()) {
                     if (e != null
                        && !e.getAbnormalType().equals("none")
                        && e.getSkill().getId() == newEffect.getSkill().getId()
                        && e.getEffectType() == newEffect.getEffectType()
                        && e.getAbnormalLvl() == newEffect.getAbnormalLvl()
                        && e.getAbnormalType().equalsIgnoreCase(newEffect.getAbnormalType())) {
                        e.exit();
                     }
                  }

                  if (!this.doesStack(newSkill) && !newSkill.is7Signs()) {
                     if (newSkill.isDance()) {
                        int effectsToRemove = this.getDanceCount() - Config.DANCES_MAX_AMOUNT;
                        if (effectsToRemove >= 0) {
                           for(Effect e : this.getBuffs()) {
                              if (e != null && e.getSkill().isDance()) {
                                 e.exit();
                                 if (--effectsToRemove < 0) {
                                    break;
                                 }
                              }
                           }
                        }
                     } else if (newSkill.isTriggeredSkill()) {
                        int effectsToRemove = this.getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
                        if (effectsToRemove >= 0) {
                           for(Effect e : this.getBuffs()) {
                              if (e != null && e.getSkill().isTriggeredSkill()) {
                                 e.exit();
                                 if (--effectsToRemove < 0) {
                                    break;
                                 }
                              }
                           }
                        }
                     } else if (!newSkill.isHealingPotionSkill() && !newEffect.isInstant()) {
                        int effectsToRemove = this.getBuffCount() - this._owner.getMaxBuffCount();
                        if (effectsToRemove >= 0 && newSkill.getSkillType() == SkillType.BUFF) {
                           for(Effect e : this.getBuffs()) {
                              if (e != null
                                 && !e.getSkill().isDance()
                                 && !e.getSkill().isTriggeredSkill()
                                 && e.getEffectType() != EffectType.TRANSFORMATION
                                 && e.getSkill().getSkillType() == SkillType.BUFF) {
                                 e.exit();
                                 if (--effectsToRemove < 0) {
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (newSkill.isTriggeredSkill()) {
                     this.getBuffs().add(newEffect);
                  } else {
                     int pos = 0;
                     if (newSkill.isToggle()) {
                        for(Effect e : this.getBuffs()) {
                           if (e != null) {
                              if (e.getSkill().isDance()) {
                                 break;
                              }

                              ++pos;
                           }
                        }
                     } else if (newSkill.isDance()) {
                        for(Effect e : this.getBuffs()) {
                           if (e != null) {
                              if (e.getSkill().isTriggeredSkill()) {
                                 break;
                              }

                              ++pos;
                           }
                        }
                     } else {
                        for(Effect e : this.getBuffs()) {
                           if (e != null) {
                              if (e.getSkill().isToggle() || e.getSkill().is7Signs() || e.getSkill().isDance() || e.getSkill().isTriggeredSkill()) {
                                 break;
                              }

                              ++pos;
                           }
                        }
                     }

                     this.getBuffs().add(pos, newEffect);
                  }
               }

               if ("none".equals(newEffect.getAbnormalType())) {
                  if (newEffect.setInUse(true)) {
                     this._owner.addStatFuncs(newEffect.getStatFuncs());
                  }
               } else {
                  Effect effectToAdd = null;
                  Effect effectToRemove = null;
                  if (this._stackedEffects == null) {
                     this._stackedEffects = new ConcurrentHashMap<>();
                  }

                  List<Effect> stackQueue = this._stackedEffects.get(newEffect.getAbnormalType());
                  if (stackQueue == null) {
                     stackQueue = new ArrayList<>();
                  }

                  if (!stackQueue.isEmpty()) {
                     int pos = 0;
                     if (!stackQueue.isEmpty()) {
                        effectToRemove = this.listsContains(stackQueue.get(0));
                        Iterator<Effect> queueIterator = stackQueue.iterator();

                        while(queueIterator.hasNext() && newEffect.getAbnormalLvl() < queueIterator.next().getAbnormalLvl()) {
                           ++pos;
                        }

                        stackQueue.add(pos, newEffect);
                        if (Config.EFFECT_CANCELING && !newEffect.getSkill().isStatic() && stackQueue.size() > 1) {
                           if (newSkill.isDebuff()) {
                              this.getDebuffs().remove(stackQueue.remove(1));
                           } else {
                              this.getBuffs().remove(stackQueue.remove(1));
                           }
                        }
                     } else {
                        stackQueue.add(0, newEffect);
                     }
                  } else {
                     stackQueue.add(0, newEffect);
                  }

                  this._stackedEffects.put(newEffect.getAbnormalType(), stackQueue);
                  if (!stackQueue.isEmpty()) {
                     effectToAdd = this.listsContains(stackQueue.get(0));
                  }

                  if (effectToRemove != effectToAdd) {
                     if (effectToRemove != null) {
                        this._owner.removeStatsOwner(effectToRemove);
                        effectToRemove.setInUse(false);
                     }

                     if (effectToAdd != null && effectToAdd.setInUse(true)) {
                        this._owner.addStatFuncs(effectToAdd.getStatFuncs());
                     }
                  }
               }
            }
         }
      }
   }

   public void removePassiveEffects(int skillId) {
      if (this.hasPassives()) {
         for(Effect eff : this.getPassives()) {
            if (eff != null && eff.getSkill().getId() == skillId) {
               eff.exit();
               this.getPassives().remove(eff);
            }
         }
      }
   }

   protected void updateEffectIcons() {
      if (this._owner != null) {
         if (this._effectIconsUpdate == null || this._effectIconsUpdate.isDone()) {
            this._effectIconsUpdate = ThreadPoolManager.getInstance()
               .schedule(new CharEffectList.UpdateEffectIconsTask(), Config.USER_ABNORMAL_EFFECTS_INTERVAL);
         }
      }
   }

   protected void updateEffectFlags() {
      boolean foundRemovedOnAction = false;
      boolean foundRemovedOnDamage = false;
      if (this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null) {
               if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                  foundRemovedOnAction = true;
               }

               if (e.getSkill().isRemovedOnDamage()) {
                  foundRemovedOnDamage = true;
               }
            }
         }
      }

      this._hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
      this._hasBuffsRemovedOnDamage = foundRemovedOnDamage;
      foundRemovedOnDamage = false;
      if (this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null && e.getSkill().isRemovedOnDamage()) {
               foundRemovedOnDamage = true;
            }
         }
      }

      this._hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
   }

   private Effect listsContains(Effect effect) {
      if (this.hasBuffs() && this.getBuffs().contains(effect)) {
         return effect;
      } else {
         return this.hasDebuffs() && this.getDebuffs().contains(effect) ? effect : null;
      }
   }

   private final void computeEffectFlags() {
      int flags = 0;
      if (this.hasBuffs()) {
         for(Effect e : this.getBuffs()) {
            if (e != null) {
               flags |= e.getEffectFlags();
            }
         }
      }

      if (this.hasDebuffs()) {
         for(Effect e : this.getDebuffs()) {
            if (e != null) {
               flags |= e.getEffectFlags();
            }
         }
      }

      if (this.hasPassives()) {
         for(Effect e : this.getPassives()) {
            if (e != null) {
               flags |= e.getEffectFlags();
            }
         }
      }

      this._effectFlags = flags;
   }

   public boolean isEmpty() {
      return (this._buffs == null || this._buffs.isEmpty()) && (this._debuffs == null || this._debuffs.isEmpty());
   }

   public boolean hasBuffs() {
      return this._buffs != null && !this._buffs.isEmpty();
   }

   public boolean hasDebuffs() {
      return this._debuffs != null && !this._debuffs.isEmpty();
   }

   public boolean hasPassives() {
      return this._passives != null && !this._passives.isEmpty();
   }

   public List<Effect> getBuffs() {
      if (this._buffs == null) {
         this._buffs = new CopyOnWriteArrayList<>();
      }

      return this._buffs;
   }

   public List<Effect> getDebuffs() {
      if (this._debuffs == null) {
         this._debuffs = new CopyOnWriteArrayList<>();
      }

      return this._debuffs;
   }

   public int getDebuffCount() {
      if (this._debuffs == null) {
         this._debuffs = new CopyOnWriteArrayList<>();
      }

      return this._debuffs.size();
   }

   public List<Effect> getPassives() {
      if (this._passives == null) {
         this._passives = new CopyOnWriteArrayList<>();
      }

      return this._passives;
   }

   public boolean isAffected(EffectFlag flag) {
      return (this._effectFlags & flag.getMask()) != 0;
   }

   public void clear() {
      try {
         this._addQueue = null;
         this._removeQueue = null;
         this._buffs = null;
         this._debuffs = null;
         this._stackedEffects = null;
         this._queuesInitialized = false;
         this._effectCache = null;
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }
   }

   public void addBlockedBuffSlots(Set<String> blockedBuffSlots) {
      if (this._blockedBuffSlots == null) {
         synchronized(this) {
            if (this._blockedBuffSlots == null) {
               this._blockedBuffSlots = ConcurrentHashMap.newKeySet(blockedBuffSlots.size());
            }
         }
      }

      this._blockedBuffSlots.addAll(blockedBuffSlots);
   }

   public boolean removeBlockedBuffSlots(Set<String> blockedBuffSlots) {
      return this._blockedBuffSlots != null ? this._blockedBuffSlots.removeAll(blockedBuffSlots) : false;
   }

   public Set<String> getAllBlockedBuffSlots() {
      return this._blockedBuffSlots;
   }

   public Effect getShortBuff() {
      return this._shortBuff;
   }

   private void addIcon(Effect info, AbnormalStatusUpdate asu, PartySpelled ps, PartySpelled psSummon, ExOlympiadSpelledInfo os, boolean isSummon) {
      if (info != null && info.isInUse()) {
         Skill skill = info.getSkill();
         if (asu != null) {
            asu.addSkill(info);
         }

         if (ps != null && (isSummon || !skill.isToggle())) {
            ps.addSkill(info);
         }

         if (psSummon != null && !skill.isToggle()) {
            psSummon.addSkill(info);
         }

         if (os != null) {
            os.addSkill(info);
         }
      }
   }

   public void shortBuffStatusUpdate(Effect info) {
      if (this._owner.isPlayer()) {
         this._shortBuff = info;
         if (info == null) {
            this._owner.sendPacket(ShortBuffStatusUpdate.RESET_SHORT_BUFF);
         } else {
            this._owner.sendPacket(new ShortBuffStatusUpdate(info.getSkill().getId(), info.getSkill().getLevel(), info.getTimeLeft()));
         }
      }
   }

   private class UpdateEffectIconsTask implements Runnable {
      private UpdateEffectIconsTask() {
      }

      @Override
      public void run() {
         if (CharEffectList.this._owner != null) {
            if (!CharEffectList.this._owner.isPlayer() || !CharEffectList.this._owner.getActingPlayer()._entering) {
               if (!CharEffectList.this._owner.isPlayable()) {
                  CharEffectList.this.updateEffectFlags();
               } else {
                  AbnormalStatusUpdate asu = null;
                  PartySpelled ps = null;
                  PartySpelled psSummon = null;
                  ExOlympiadSpelledInfo os = null;
                  boolean isSummon = false;
                  if (CharEffectList.this._owner.isPlayer()) {
                     if (CharEffectList.this._partyOnly) {
                        CharEffectList.this._partyOnly = false;
                     } else {
                        asu = new AbnormalStatusUpdate();
                     }

                     if (CharEffectList.this._owner.isInParty()) {
                        ps = new PartySpelled(CharEffectList.this._owner);
                     }

                     if (CharEffectList.this._owner.getActingPlayer().isInOlympiadMode() && CharEffectList.this._owner.getActingPlayer().isOlympiadStart()) {
                        os = new ExOlympiadSpelledInfo(CharEffectList.this._owner.getActingPlayer());
                     }
                  } else if (CharEffectList.this._owner.isSummon()) {
                     isSummon = true;
                     ps = new PartySpelled(CharEffectList.this._owner);
                     psSummon = new PartySpelled(CharEffectList.this._owner);
                  }

                  boolean foundRemovedOnAction = false;
                  boolean foundRemovedOnDamage = false;
                  if (CharEffectList.this.hasBuffs()) {
                     for(Effect e : CharEffectList.this.getBuffs()) {
                        if (e != null) {
                           if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                              foundRemovedOnAction = true;
                           }

                           if (e.getSkill().isRemovedOnDamage()) {
                              foundRemovedOnDamage = true;
                           }

                           if (e.isIconDisplay() && !e.isInstant() && e.getEffectType() != EffectType.SIGNET_GROUND && e.isInUse()) {
                              if (e.getSkill().isHealingPotionSkill()) {
                                 CharEffectList.this.shortBuffStatusUpdate(e);
                              } else {
                                 CharEffectList.this.addIcon(e, asu, ps, psSummon, os, isSummon);
                              }
                           }
                        }
                     }
                  }

                  CharEffectList.this._hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
                  CharEffectList.this._hasBuffsRemovedOnDamage = foundRemovedOnDamage;
                  foundRemovedOnDamage = false;
                  if (CharEffectList.this.hasDebuffs()) {
                     for(Effect e : CharEffectList.this.getDebuffs()) {
                        if (e != null) {
                           if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                              foundRemovedOnAction = true;
                           }

                           if (e.getSkill().isRemovedOnDamage()) {
                              foundRemovedOnDamage = true;
                           }

                           if (e.isIconDisplay() && !e.isInstant() && e.getEffectType() != EffectType.SIGNET_GROUND && e.isInUse()) {
                              CharEffectList.this.addIcon(e, asu, ps, psSummon, os, isSummon);
                           }
                        }
                     }
                  }

                  CharEffectList.this._hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
                  if (asu != null) {
                     CharEffectList.this._owner.sendPacket(asu);
                  }

                  if (ps != null) {
                     if (CharEffectList.this._owner.isSummon()) {
                        Player summonOwner = ((Summon)CharEffectList.this._owner).getOwner();
                        if (summonOwner != null) {
                           if (summonOwner.isInParty()) {
                              summonOwner.getParty().broadcastToPartyMembers(summonOwner, psSummon);
                           }

                           summonOwner.sendPacket(ps);
                        }
                     } else if (CharEffectList.this._owner.isPlayer() && CharEffectList.this._owner.isInParty()) {
                        CharEffectList.this._owner.getParty().broadCast(ps);
                     }
                  }

                  if (os != null) {
                     OlympiadGameTask game = OlympiadGameManager.getInstance()
                        .getOlympiadTask(CharEffectList.this._owner.getActingPlayer().getOlympiadGameId());
                     if (game != null && game.isBattleStarted()) {
                        game.getZone().broadcastPacketToObservers(os);
                     }
                  }

                  CharEffectList.this._effectIconsUpdate = null;
               }
            }
         }
      }
   }
}
