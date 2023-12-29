package l2e.gameserver.model.actor;

import gnu.trove.set.hash.TIntHashSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.guard.FortGuardAI;
import l2e.gameserver.ai.guard.GuardAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.DamageLimitParser;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.VipManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.events.AttackableEvents;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.ServitorInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.status.AttackableStatus;
import l2e.gameserver.model.actor.tasks.character.NotifyAITask;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.npc.DamageLimit;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionRewardItem;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.actor.templates.player.vip.VipNpcTemplate;
import l2e.gameserver.model.entity.events.EventsDropManager;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.reward.RewardItem;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class Attackable extends Npc {
   private Class<DefaultAI> _classAI = DefaultAI.class;
   private Constructor<DefaultAI> _constructorAI = DefaultAI.class.getConstructors()[0];
   private boolean _isRaid = false;
   private boolean _isEpicRaid = false;
   private boolean _isSiegeGuard = false;
   private boolean _isRaidMinion = false;
   private boolean _isLethalImmune = false;
   private boolean _isGlobalAI = false;
   private final Map<Creature, Attackable.AggroInfo> _aggroList = new ConcurrentHashMap<>();
   private boolean _isReturningToSpawnPoint = false;
   private boolean _canReturnToSpawnPoint = true;
   private boolean _seeThroughSilentMove = false;
   private List<RewardItem> _sweepItems;
   private final Lock sweepLock = new ReentrantLock();
   private ItemHolder[] _harvestItems;
   private boolean _seeded;
   private int _seedType = 0;
   private int _seederObjId = 0;
   private boolean _overhit;
   private double _overhitDamage;
   private Creature _overhitAttacker;
   private volatile CommandChannel _firstCommandChannelAttacked = null;
   private Attackable.CommandChannelTimer _commandChannelTimer = null;
   private long _commandChannelLastAttack = 0L;
   private boolean _mustGiveExpSp;
   private boolean _isSpoil = false;
   private int _isSpoiledBy = 0;
   private TIntHashSet _absorbersIds;
   protected int _onKillDelay = 5000;
   protected long _findTargetDelay = 0L;

   public Attackable(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Attackable);
      this.setIsInvul(false);
      this._mustGiveExpSp = true;
      if (template.getCanSeeInSilentMove()) {
         this.setSeeThroughSilentMove(true);
      }

      if (template.isLethalImmune()) {
         this.setIsLethalImmune(true);
      }
   }

   public AttackableStatus getStatus() {
      return (AttackableStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new AttackableStatus(this));
   }

   @Override
   public void initCharEvents() {
      this.setCharEvents(new AttackableEvents(this));
   }

   public AttackableEvents getEvents() {
      return (AttackableEvents)super.getEvents();
   }

   @Override
   protected CharacterAI initAI() {
      Class<DefaultAI> classAI = null;
      DefaultAI constructorAI = null;

      try {
         classAI = Class.forName("l2e.gameserver.ai.npc." + this.getAiType());
      } catch (ClassNotFoundException var7) {
         try {
            classAI = Class.forName("l2e.scripts.ai." + this.getAiType());
         } catch (ClassNotFoundException var6) {
            var6.printStackTrace();
         }
      }

      if (classAI == null) {
         _log.warning("Not found type class for type: " + this.getAiType() + ". NpcId: " + this.getId());
      } else {
         this._classAI = classAI;
         this._constructorAI = this._classAI.getConstructors()[0];
      }

      try {
         constructorAI = this._constructorAI.newInstance(this);
      } catch (InvocationTargetException | InstantiationException | IllegalArgumentException | IllegalAccessException var5) {
         _log.warning("Unable to create ai of NpcId: " + this.getId());
      }

      return constructorAI;
   }

   public final Map<Creature, Attackable.AggroInfo> getAggroList() {
      return this._aggroList;
   }

   public final boolean isReturningToSpawnPoint() {
      return this._isReturningToSpawnPoint;
   }

   public final void setisReturningToSpawnPoint(boolean value) {
      this._isReturningToSpawnPoint = value;
   }

   public final boolean canReturnToSpawnPoint() {
      return this._canReturnToSpawnPoint;
   }

   public final void setCanReturnToSpawnPoint(boolean value) {
      this._canReturnToSpawnPoint = value;
   }

   public boolean canSeeThroughSilentMove() {
      return this._seeThroughSilentMove;
   }

   public void setSeeThroughSilentMove(boolean val) {
      this._seeThroughSilentMove = val;
   }

   public void useMagic(Skill skill) {
      if (skill != null && !this.isAlikeDead() && !skill.isPassive() && !this.isCastingNow() && !this.isSkillDisabled(skill) && !this.isSkillBlocked(skill)) {
         if (!(this.getCurrentMp() < (double)(this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill)))
            && !(this.getCurrentHp() <= (double)skill.getHpConsume())) {
            if (!skill.isStatic()) {
               if (skill.isMagic()) {
                  if (this.isMuted()) {
                     return;
                  }
               } else if (this.isPhysicalMuted()) {
                  return;
               }
            }

            GameObject target = skill.getFirstOfTargetList(this);
            if (target != null) {
               this.getAI().setIntention(CtrlIntention.CAST, skill, target);
            }
         }
      }
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
      if (Config.ALLOW_DAMAGE_LIMIT) {
         DamageLimit limit = DamageLimitParser.getInstance().getDamageLimit(this.getId());
         if (limit != null) {
            int damageLimit = skill != null ? (skill.isMagic() ? limit.getMagicDamage() : limit.getPhysicDamage()) : limit.getDamage();
            if (damageLimit > 0 && damage > (double)damageLimit) {
               damage = (double)damageLimit;
            }
         }
      }

      this.reduceCurrentHp(damage, attacker, true, false, skill);
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (this.isRaid()
         && !this.isMinion()
         && attacker != null
         && attacker.getParty() != null
         && attacker.getParty().isInCommandChannel()
         && attacker.getParty().getCommandChannel().meetRaidWarCondition(this)) {
         if (this._firstCommandChannelAttacked == null) {
            synchronized(this) {
               if (this._firstCommandChannelAttacked == null) {
                  this._firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
                  if (this._firstCommandChannelAttacked != null) {
                     this._commandChannelTimer = new Attackable.CommandChannelTimer(this);
                     this._commandChannelLastAttack = System.currentTimeMillis();
                     ThreadPoolManager.getInstance().schedule(this._commandChannelTimer, 10000L);
                     this._firstCommandChannelAttacked.broadCast(new CreatureSay(0, 16, "", "You have looting rights!"));
                  }
               }
            }
         } else if (attacker.getParty().getCommandChannel().equals(this._firstCommandChannelAttacked)) {
            this._commandChannelLastAttack = System.currentTimeMillis();
         }
      }

      if (!this.isEventMob()) {
         if (attacker != null && skill != null) {
            this.addDamage(attacker, (int)damage, skill);
            if (!Config.ALLOW_UNLIM_ENTER_CATACOMBS && this.isSevenSignsMonster()) {
               Player player = attacker.getActingPlayer();
               if (player != null && (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())) {
                  int pcabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                  int wcabal = SevenSigns.getInstance().getCabalHighestScore();
                  if (pcabal != wcabal && wcabal != 0) {
                     player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
                     player.teleToClosestTown();
                     return;
                  }
               }
            }

            if ((this.isMonster() || this.isMinion()) && this.hasAI()) {
               ((DefaultAI)this.getAI()).checkUD(attacker);
               if (attacker.isPlayer() && !this.getAI().getTargetList().contains(attacker)) {
                  this.getAI().addToTargetList(attacker.getActingPlayer());
               }
            }
         }

         super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
      }
   }

   public synchronized void setMustRewardExpSp(boolean value) {
      this._mustGiveExpSp = value;
   }

   public synchronized boolean getMustRewardExpSP() {
      return this._mustGiveExpSp;
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.hasAI()) {
         this.getAI().getTargetList().clear();
      }

      if (this.getChampionTemplate() != null) {
         for(AbnormalEffect effect : this.getChampionTemplate().abnormalEffect) {
            if (effect != null) {
               this.stopAbnormalEffect(effect);
            }
         }
      }

      if (killer != null) {
         if (killer.isServitor()) {
            killer = ((ServitorInstance)killer).getOwner();
         } else if (killer.isPet()) {
            killer = ((PetInstance)killer).getOwner();
         } else if (killer.isSummon()) {
            killer = ((Summon)killer).getOwner();
         }
      }

      Player player = null;
      if (killer != null) {
         player = killer.getActingPlayer();
      }

      if (player != null) {
         if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null) {
            for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL)) {
               ThreadPoolManager.getInstance()
                  .schedule(new Attackable.OnKillNotifyTask(this, quest, player, killer != null && killer.isSummon()), (long)Config.NPC_DEAD_TIME_TASK * 1000L);
            }
         }

         if (Config.ALLOW_DAILY_TASKS && player.getActiveDailyTasks() != null && (this.isMonster() || this.isRaid())) {
            for(PlayerTaskTemplate taskTemplate : player.getActiveDailyTasks()) {
               if (taskTemplate.getType().equalsIgnoreCase("Farm") && !taskTemplate.isComplete()) {
                  DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                  if (task.getNpcId() == this.getId() && taskTemplate.getCurrentNpcCount() < task.getNpcCount()) {
                     taskTemplate.setCurrentNpcCount(taskTemplate.getCurrentNpcCount() + 1);
                  }

                  if (taskTemplate.isComplete()) {
                     IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                     if (vch != null) {
                        player.updateDailyStatus(taskTemplate);
                        vch.useVoicedCommand("missions", player, null);
                     }
                  }
               }
            }
         }
      } else if (killer != null && killer.isNpc() && this.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null) {
         for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL)) {
            ThreadPoolManager.getInstance().schedule(new Attackable.OnKillByMobNotifyTask(this, quest, (Npc)killer), (long)Config.NPC_DEAD_TIME_TASK * 1000L);
         }
      }

      super.onDeath(killer);
   }

   @Override
   protected void calculateRewards(Creature lastAttacker) {
      if (!this.getAggroList().isEmpty()) {
         Map<Player, Attackable.RewardInfo> rewards = new ConcurrentHashMap<>();
         Player maxDealer = null;
         int maxDamage = 0;
         long totalDamage = 0L;

         for(Attackable.AggroInfo info : this.getAggroList().values()) {
            if (info != null) {
               Player attacker = info.getAttacker().getActingPlayer();
               if (attacker != null) {
                  int damage = info.getDamage();
                  if (damage > 1 && Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, attacker, true)) {
                     totalDamage += (long)damage;
                     Attackable.RewardInfo reward = rewards.get(attacker);
                     if (reward == null) {
                        reward = new Attackable.RewardInfo(attacker, damage);
                        rewards.put(attacker, reward);
                     } else {
                        reward.addDamage(damage);
                     }

                     if (reward.getDamage() > maxDamage) {
                        maxDealer = attacker;
                        maxDamage = reward.getDamage();
                     }
                  }
               }
            }
         }

         this.doItemDrop(lastAttacker, (Creature)(maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker));
         if (this.getMustRewardExpSP()) {
            if (!rewards.isEmpty()) {
               for(Attackable.RewardInfo reward : rewards.values()) {
                  if (reward != null) {
                     Player attacker = reward.getAttacker();
                     int damage = reward.getDamage();
                     Party attackerParty = attacker.getParty();
                     float penalty = attacker.hasServitor() ? ((ServitorInstance)attacker.getSummon()).getExpPenalty() : 0.0F;
                     if (attackerParty != null) {
                        int partyDmg = 0;
                        float partyMul = 1.0F;
                        int partyLvl = 0;
                        List<Player> rewardedMembers = new ArrayList<>();

                        for(Player partyPlayer : attackerParty.getMembers()) {
                           if (partyPlayer != null && !partyPlayer.isDead()) {
                              Attackable.RewardInfo reward2 = rewards.get(partyPlayer);
                              if (reward2 != null) {
                                 if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true)) {
                                    partyDmg += reward2.getDamage();
                                    rewardedMembers.add(partyPlayer);
                                    if (partyPlayer.getLevel() > partyLvl) {
                                       partyLvl = partyPlayer.getLevel();
                                    }
                                 }

                                 rewards.remove(partyPlayer);
                              } else if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true)) {
                                 rewardedMembers.add(partyPlayer);
                                 if (partyPlayer.getLevel() > partyLvl) {
                                    partyLvl = partyPlayer.getLevel();
                                 }
                              }
                           }
                        }

                        if ((long)partyDmg < totalDamage) {
                           partyMul = (float)partyDmg / (float)totalDamage;
                        }

                        int levelDiff = partyLvl - this.getLevel();
                        double[] expSp = this.calculateExpAndSp(levelDiff, partyDmg, totalDamage, attacker);
                        double exp_premium = expSp[0];
                        double sp_premium = expSp[1];
                        expSp = this.calculateExpAndSp(levelDiff, partyDmg, totalDamage, attacker);
                        double exp = expSp[0];
                        double sp = expSp[1];
                        if (this.getChampionTemplate() != null) {
                           exp *= this.getChampionTemplate().expMultiplier;
                           sp *= this.getChampionTemplate().spMultiplier;
                           exp_premium *= this.getChampionTemplate().expMultiplier;
                           sp_premium *= this.getChampionTemplate().spMultiplier;
                        }

                        exp *= (double)partyMul;
                        sp *= (double)partyMul;
                        exp_premium *= (double)partyMul;
                        sp_premium *= (double)partyMul;
                        Creature overhitAttacker = this.getOverhitAttacker();
                        if (this.isOverhit()
                           && overhitAttacker != null
                           && overhitAttacker.getActingPlayer() != null
                           && attacker == overhitAttacker.getActingPlayer()) {
                           attacker.sendPacket(SystemMessageId.OVER_HIT);
                           exp += (double)this.calculateOverhitExp((long)exp);
                           exp_premium += (double)this.calculateOverhitExp((long)exp_premium);
                        }

                        if (partyDmg > 0) {
                           attackerParty.distributeXpAndSp((long)exp_premium, (int)sp_premium, (long)exp, (int)sp, rewardedMembers, partyLvl, partyDmg, this);
                        }
                     } else if (World.getInstance().getAroundCharacters(attacker).contains(this)) {
                        int levelDiff = attacker.getLevel() - this.getLevel();
                        double[] expSp = this.calculateExpAndSp(levelDiff, damage, totalDamage, attacker);
                        double exp = expSp[0];
                        double sp = expSp[1];
                        if (attacker.isPlayer() && attacker.getPremiumBonus().isPersonal()) {
                           exp *= attacker.getPremiumBonus().getRateXp();
                           sp *= attacker.getPremiumBonus().getRateSp();
                        }

                        if (this.getChampionTemplate() != null) {
                           exp *= this.getChampionTemplate().expMultiplier;
                           sp *= this.getChampionTemplate().spMultiplier;
                        }

                        exp *= (double)(1.0F - penalty);
                        Creature overhitAttacker = this.getOverhitAttacker();
                        if (this.isOverhit()
                           && overhitAttacker != null
                           && overhitAttacker.getActingPlayer() != null
                           && attacker == overhitAttacker.getActingPlayer()) {
                           attacker.sendPacket(SystemMessageId.OVER_HIT);
                           exp += (double)this.calculateOverhitExp((long)exp);
                        }

                        if (!attacker.isDead()) {
                           exp *= attacker.getRExp();
                           sp *= attacker.getRSp();
                           long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
                           int addsp = (int)attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
                           attacker.addExpAndSp(addexp, addsp, this.useVitalityRate());
                           if (addexp > 0L) {
                              if (!attacker.getNevitSystem().isActive()
                                 && attacker.getNevitSystem().getTime() > 0
                                 && !attacker.isInsideZone(ZoneId.PEACE)
                                 && attacker.getLevel() - this.getLevel() <= 9) {
                                 int nevitPoints = Math.round((float)(addexp / (long)(this.getLevel() * this.getLevel()) * 100L / 20L));
                                 attacker.getNevitSystem().addPoints(nevitPoints);
                              }

                              attacker.updateVitalityPoints(this.getVitalityPoints(damage), true, false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void addAttackerToAttackByList(Creature player) {
      if (player != null && player != this && !this.getAttackByList().contains(player)) {
         this.getAttackByList().add(player);
      }
   }

   public void addDamage(Creature attacker, int damage, Skill skill) {
      if (attacker != null) {
         if (!this.isDead()) {
            try {
               if (this.isWalker() && !this.isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this)) {
                  WalkingManager.getInstance().stopMoving(this, false, true);
               }

               this.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, Integer.valueOf(damage));
               if (attacker.isPlayable()) {
                  Player player = attacker.getActingPlayer();
                  if (player != null && this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null) {
                     for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
                        quest.notifyAttack(this, player, damage, attacker.isSummon(), skill);
                     }
                  }
               }
            } catch (Exception var7) {
               _log.log(Level.SEVERE, "", (Throwable)var7);
            }
         }
      }
   }

   public void addDamageHate(Creature attacker, int damage, int aggro) {
      if (attacker != null) {
         Player targetPlayer = attacker.isTrap() ? ((TrapInstance)attacker).getOwner() : attacker.getActingPlayer();
         Attackable.AggroInfo ai = attacker.isTrap()
            ? this._aggroList.computeIfAbsent((Creature)(targetPlayer != null ? targetPlayer : attacker), Attackable.AggroInfo::new)
            : this._aggroList.computeIfAbsent(attacker, Attackable.AggroInfo::new);
         if (Config.ALLOW_DAMAGE_LIMIT) {
            DamageLimit limit = DamageLimitParser.getInstance().getDamageLimit(this.getId());
            if (limit != null) {
               int damageLimit = limit.getDamage();
               if (damageLimit > 0 && damage > damageLimit) {
                  damage = damageLimit;
               }
            }
         }

         if ((double)damage > this.getCurrentHp()) {
            damage = (int)this.getCurrentHp();
         }

         ai.addDamage(damage);
         ai.addHate(aggro);
         if (damage > 1 && attacker.isPlayable()) {
            Player player = attacker.getActingPlayer();
            if (player != null && this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null) {
               for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
                  quest.notifyAttack(this, player, damage, attacker.isSummon(), null);
               }
            }
         }

         if (targetPlayer != null && aggro == 0) {
            this.addDamageHate((Creature)(attacker.isTrap() ? targetPlayer : attacker), 0, damage > 2 ? (int)((double)damage * Config.PATK_HATE_MOD) : 1);
            if (this.getAI().getIntention() == CtrlIntention.IDLE) {
               this.getAI().setIntention(CtrlIntention.ACTIVE);
            }

            if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null) {
               for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER)) {
                  quest.notifyAggroRangeEnter(this, targetPlayer, attacker.isSummon());
               }
            }
         } else if (targetPlayer == null && aggro == 0) {
            aggro = 1;
            ai.addHate(1);
         }

         if (aggro > 0 && this.getAI().getIntention() == CtrlIntention.IDLE) {
            this.getAI().setIntention(CtrlIntention.ACTIVE);
         }
      }
   }

   public void reduceHate(Creature target, int amount) {
      if (!(this.getAI() instanceof GuardAI) && !(this.getAI() instanceof FortGuardAI)) {
         if (target == null) {
            Creature mostHated = this.getMostHated();
            if (mostHated == null) {
               ((DefaultAI)this.getAI()).setGlobalAggro(-25);
            } else {
               for(Attackable.AggroInfo ai : this.getAggroList().values()) {
                  if (ai == null) {
                     return;
                  }

                  ai.addHate(-amount);
               }

               amount = this.getHating(mostHated);
               if (amount <= 0) {
                  ((DefaultAI)this.getAI()).setGlobalAggro(-25);
                  this.clearAggroList();
                  this.getAI().setIntention(CtrlIntention.ACTIVE);
                  this.setWalking();
               }
            }
         } else {
            Attackable.AggroInfo ai = this.getAggroList().get(target);
            if (ai != null) {
               ai.addHate(-amount);
               if (ai.getHate() <= 0 && this.getMostHated() == null) {
                  ((DefaultAI)this.getAI()).setGlobalAggro(-25);
                  this.clearAggroList();
                  this.getAI().setIntention(CtrlIntention.ACTIVE);
                  this.setWalking();
               }
            }
         }
      } else {
         this.stopHating(target);
         this.setTarget(null);
         this.getAI().setIntention(CtrlIntention.IDLE);
      }
   }

   public void stopHating(Creature target) {
      if (target != null) {
         Attackable.AggroInfo ai = this.getAggroList().get(target);
         if (ai != null) {
            ai.stopHate();
         }
      }
   }

   public Creature getMostHated() {
      if (!this.getAggroList().isEmpty() && !this.isAlikeDead()) {
         Creature mostHated = null;
         int maxHate = 0;

         for(Attackable.AggroInfo ai : this.getAggroList().values()) {
            if (ai != null && ai.checkHate(this) > maxHate) {
               mostHated = ai.getAttacker();
               maxHate = ai.getHate();
            }
         }

         return mostHated;
      } else {
         return null;
      }
   }

   public List<Creature> get2MostHated() {
      if (!this.getAggroList().isEmpty() && !this.isAlikeDead()) {
         Creature mostHated = null;
         Creature secondMostHated = null;
         int maxHate = 0;
         List<Creature> result = new ArrayList<>();

         for(Attackable.AggroInfo ai : this.getAggroList().values()) {
            if (ai != null && ai.checkHate(this) > maxHate) {
               secondMostHated = mostHated;
               mostHated = ai.getAttacker();
               maxHate = ai.getHate();
            }
         }

         result.add(mostHated);
         if (this.getAttackByList().contains(secondMostHated)) {
            result.add(secondMostHated);
         } else {
            result.add(null);
         }

         return result;
      } else {
         return null;
      }
   }

   public List<Creature> getHateList() {
      if (!this.getAggroList().isEmpty() && !this.isAlikeDead()) {
         List<Creature> result = new ArrayList<>();

         for(Attackable.AggroInfo ai : this.getAggroList().values()) {
            if (ai != null) {
               ai.checkHate(this);
               result.add(ai.getAttacker());
            }
         }

         return result;
      } else {
         return null;
      }
   }

   public int getHating(Creature target) {
      if (!this.getAggroList().isEmpty() && target != null) {
         Attackable.AggroInfo ai = this.getAggroList().get(target);
         if (ai == null) {
            return 0;
         } else {
            if (ai.getAttacker() instanceof Player) {
               Player act = (Player)ai.getAttacker();
               if (act.isInvisible() || act.isSpawnProtected()) {
                  this.getAggroList().remove(target);
                  return 0;
               }
            }

            if (!ai.getAttacker().isVisible() || ai.getAttacker().isInvisible()) {
               this.getAggroList().remove(target);
               return 0;
            } else if (ai.getAttacker().isAlikeDead()) {
               ai.stopHate();
               return 0;
            } else {
               return ai.getHate();
            }
         }
      } else {
         return 0;
      }
   }

   private void calcVipPointsReward(long totalPoints) {
      Map<Object, Attackable.GroupInfo> groupsInfo = new HashMap<>();
      double totalHp = this.getMaxHp();

      for(Attackable.AggroInfo ai : this.getAggroList().values()) {
         Player player = ai.getAttacker().getActingPlayer();
         if (player != null) {
            Object key = player.getParty() != null
               ? (player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty())
               : player.getActingPlayer();
            Attackable.GroupInfo info = groupsInfo.get(key);
            if (info == null) {
               info = new Attackable.GroupInfo();
               groupsInfo.put(key, info);
            }

            if (key instanceof CommandChannel) {
               for(Player p : (CommandChannel)key) {
                  if (p.isInRangeZ(this, (long)Config.ALT_PARTY_RANGE2)) {
                     info._players.add(p);
                  }
               }
            } else if (key instanceof Party) {
               for(Player p : ((Party)key).getMembers()) {
                  if (p.isInRangeZ(this, (long)Config.ALT_PARTY_RANGE2)) {
                     info._players.add(p);
                  }
               }
            } else {
               info._players.add(player);
            }

            info._reward += (long)ai.getDamage();
         }
      }

      for(Attackable.GroupInfo groupInfo : groupsInfo.values()) {
         HashSet<Player> players = groupInfo._players;
         int perPlayer = (int)Math.round((double)(totalPoints * groupInfo._reward) / (totalHp * (double)players.size()));

         for(Player player : players) {
            if (player != null) {
               int var21 = (int)Math.round(
                  (double)perPlayer * ExperienceParser.getInstance().penaltyModifier((long)this.calculateLevelDiffForDrop(player.getLevel()), 9.0)
               );
               if (var21 != 0) {
                  player.setVipPoints(player.getVipPoints() + (long)var21);
               }
            }
         }
      }
   }

   public void doItemDrop(Creature lastAttacker, Creature mainDamageDealer) {
      this.doItemDrop(this.getTemplate(), lastAttacker, mainDamageDealer);
   }

   public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker, Creature mainDamageDealer) {
      if (mainDamageDealer != null) {
         Player player = mainDamageDealer.getActingPlayer();
         if (player != null) {
            if (!player.isFakePlayer()) {
               if (Config.ALLOW_VIP_SYSTEM) {
                  VipNpcTemplate vipNpc = VipManager.getInstance().getVipNpcTemplate(this.getId());
                  if (vipNpc != null) {
                     this.calcVipPointsReward(vipNpc.getPoints());
                  } else {
                     player.setVipPoints(player.getVipPoints() + 1L);
                  }
               }

               if (this.isMonster() && this.getReflectionId() == 0) {
                  CursedWeaponsManager.getInstance().checkDrop(this, player);
               }

               if (!this.isSiegeGuard() || !Config.EPAULETTE_ONLY_FOR_REG) {
                  player.getCounters().addAchivementInfo("killbyId", this.getId(), -1L, false, this.isRaid(), false);
                  if (this.isMonster() && !this.isRaid() && !this.isMinion()) {
                     player.getCounters().addAchivementInfo("monsterKiller", this.getId(), -1L, false, false, false);
                  }

                  for(Entry<RewardType, RewardList> entry : npcTemplate.getRewards().entrySet()) {
                     this.rollRewards(entry, lastAttacker, mainDamageDealer);
                  }

                  if (this.getChampionTemplate() != null) {
                     player.getCounters().addAchivementInfo("championKiller", this.getId(), -1L, false, false, false);
                     double mod = 1.0 * ExperienceParser.getInstance().penaltyModifier((long)this.calculateLevelDiffForDrop(player.getLevel()), 9.0);
                     if (mod > 0.0) {
                        for(ChampionRewardItem ri : this.getChampionTemplate().rewards) {
                           if (ri != null && Rnd.get(100) < ri.getDropChance()) {
                              int count = Rnd.get(ri.getMinCount(), ri.getMaxCount());
                              Item itemTemplate = ItemsParser.getInstance().getTemplate(ri.getItemId());
                              if (itemTemplate != null) {
                                 ItemHolder item = new ItemHolder(ri.getItemId(), (long)count);
                                 if ((!player.getUseAutoLoot() && !Config.AUTO_LOOT && !player.getFarmSystem().isAutofarming() || itemTemplate.isHerb())
                                    && !this.isFlying()
                                    && (!player.getUseAutoLootHerbs() && !Config.AUTO_LOOT_HERBS || !itemTemplate.isHerb())) {
                                    if (Config.AUTO_LOOT_BY_ID_SYSTEM) {
                                       if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0) {
                                          player.doAutoLoot(this, item);
                                       } else {
                                          this.dropItem(player, item);
                                       }
                                    } else {
                                       this.dropItem(player, item);
                                    }
                                 } else {
                                    player.doAutoLoot(this, itemTemplate.getId(), (long)count);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (!EventsDropManager.getInstance().getEventRules().isEmpty()) {
                     int[] rewardItem = EventsDropManager.getInstance().calculateRewardItem(npcTemplate, mainDamageDealer);
                     if (rewardItem[0] > 0 && rewardItem[1] > 0) {
                        ItemHolder item = new ItemHolder(rewardItem[0], (long)rewardItem[1]);
                        if (player.getUseAutoLoot() || Config.AUTO_LOOT || player.getFarmSystem().isAutofarming() || this.isFlying()) {
                           player.doAutoLoot(this, item.getId(), item.getCount());
                        } else if (Config.AUTO_LOOT_BY_ID_SYSTEM) {
                           if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0) {
                              player.doAutoLoot(this, item);
                           } else {
                              this.dropItem(player, item);
                           }
                        } else {
                           this.dropItem(player, item);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void rollRewards(Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager) {
      RewardType type = entry.getKey();
      RewardList list = entry.getValue();
      if (type != RewardType.SWEEP || this.isSpoil()) {
         Creature activeChar = type == RewardType.SWEEP ? lastAttacker : topDamager;
         Player activePlayer = activeChar.getActingPlayer();
         if (activePlayer != null) {
            int diff = this.calculateLevelDiffForDrop(topDamager.getLevel());
            double mod = 1.0;
            double penaltyMod = ExperienceParser.getInstance().penaltyModifier((long)diff, 9.0);
            List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, 1.0, this);
            switch(type) {
               case SWEEP:
                  this._sweepItems = rewardItems;
                  break;
               default:
                  for(RewardItem drop : rewardItems) {
                     if (!this.isSeeded() || this.getSeedType() == 0 || drop.isAdena()) {
                        if (this.isFlying()
                           || !drop.isHerb()
                              && (
                                 (activePlayer.getUseAutoLoot() || activePlayer.getFarmSystem().isAutofarming()) && !this.isRaid()
                                    || !this.isRaid() && Config.AUTO_LOOT
                                    || this.isRaid() && Config.AUTO_LOOT_RAIDS
                              )
                           || (activePlayer.getUseAutoLootHerbs() || Config.AUTO_LOOT_HERBS) && drop.isHerb()) {
                           activePlayer.doAutoLoot(this, drop._itemId, (long)((int)drop._count));
                        } else if (Config.AUTO_LOOT_BY_ID_SYSTEM) {
                           if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, drop._itemId) >= 0) {
                              activePlayer.doAutoLoot(this, new ItemHolder(drop._itemId, (long)((int)drop._count)));
                           } else {
                              this.dropItem(activePlayer, drop._itemId, (long)((int)drop._count));
                           }
                        } else {
                           this.dropItem(activePlayer, drop._itemId, (long)((int)drop._count));
                        }

                        if (this.isRaid() && !this.isRaidMinion()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
                           sm.addCharName(this);
                           sm.addItemName(drop._itemId);
                           sm.addItemNumber((long)((int)drop._count));
                           this.broadcastPacket(sm);
                        }
                     }
                  }
            }
         }
      }
   }

   public ItemInstance dropItem(Player mainDamageDealer, ItemHolder item) {
      if (Config.DISABLE_ITEM_DROP_LIST.contains(item.getId())) {
         return null;
      } else {
         ItemInstance ditem = null;

         for(int i = 0; (long)i < item.getCount(); ++i) {
            Location pos = Location.findAroundPosition(this, 100);
            if (ItemsParser.getInstance().getTemplate(item.getId()) != null) {
               ditem = ItemsParser.getInstance().createItem("Loot", item.getId(), item.getCount(), mainDamageDealer, this);
               ditem.getDropProtection().protect(mainDamageDealer, this.isRaid());
               ditem.dropMe(this, pos);
               if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId())
                  && (Config.AUTODESTROY_ITEM_AFTER > 0 && !ditem.getItem().isHerb() || Config.HERB_AUTO_DESTROY_TIME > 0 && ditem.getItem().isHerb())) {
                  ItemsAutoDestroy.getInstance().addItem(ditem);
               }

               ditem.setProtected(false);
               if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
                  break;
               }
            } else {
               _log.log(Level.SEVERE, "Item doesn't exist so cannot be dropped. Item ID: " + item.getId());
            }
         }

         return ditem;
      }
   }

   public ItemInstance dropItem(Player lastAttacker, int itemId, long itemCount) {
      return this.dropItem(lastAttacker, new ItemHolder(itemId, itemCount));
   }

   public ItemInstance getActiveWeapon() {
      return null;
   }

   public boolean noTarget() {
      return this.getAggroList().isEmpty();
   }

   public boolean containsTarget(Creature player) {
      return this.getAggroList().containsKey(player);
   }

   public void clearAggroList() {
      this.getAggroList().clear();
      if (this.hasAI()) {
         this.getAI().getTargetList().clear();
         this.getAI().stopAutoAttack();
      }

      this._overhit = false;
      this._overhitDamage = 0.0;
      this._overhitAttacker = null;
   }

   @Override
   public boolean isSweepActive() {
      this.sweepLock.lock();

      boolean var1;
      try {
         var1 = this._sweepItems != null && this._sweepItems.size() > 0;
      } finally {
         this.sweepLock.unlock();
      }

      return var1;
   }

   public List<Item> getSpoilLootItems() {
      List<Item> lootItems = new ArrayList<>();
      if (this._sweepItems != null) {
         for(RewardItem item : this._sweepItems) {
            lootItems.add(ItemsParser.getInstance().createDummyItem(item._itemId).getItem());
         }
      }

      return lootItems;
   }

   public List<RewardItem> takeSweep() {
      this.sweepLock.lock();

      List var2;
      try {
         List<RewardItem> sweep = this._sweepItems;
         this.clearSweep();
         var2 = sweep;
      } finally {
         this.sweepLock.unlock();
      }

      return var2;
   }

   public void clearSweep() {
      this.sweepLock.lock();

      try {
         this._isSpoil = false;
         this._isSpoiledBy = 0;
         this._sweepItems = null;
      } finally {
         this.sweepLock.unlock();
      }
   }

   public synchronized ItemHolder[] takeHarvest() {
      ItemHolder[] harvest = this._harvestItems;
      this._harvestItems = null;
      return harvest;
   }

   public boolean isOldCorpse(Player attacker, int remainingTime, boolean sendMessage) {
      if (this.isDead() && DecayTaskManager.getInstance().getRemainingTime(this) < (long)remainingTime) {
         if (sendMessage && attacker != null) {
            attacker.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean checkSpoilOwner(Player sweeper, boolean sendMessage) {
      if (sweeper.getObjectId() != this.getIsSpoiledBy() && !sweeper.isInLooterParty(this.getIsSpoiledBy())) {
         if (sendMessage) {
            sweeper.sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
         }

         return false;
      } else {
         return true;
      }
   }

   public void overhitEnabled(boolean status) {
      this._overhit = status;
   }

   public void setOverhitValues(Creature attacker, double damage) {
      double overhitDmg = -(this.getCurrentHp() - damage);
      if (overhitDmg < 0.0) {
         this.overhitEnabled(false);
         this._overhitDamage = 0.0;
         this._overhitAttacker = null;
      } else {
         this.overhitEnabled(true);
         this._overhitDamage = overhitDmg;
         this._overhitAttacker = attacker;
      }
   }

   public Creature getOverhitAttacker() {
      return this._overhitAttacker;
   }

   public double getOverhitDamage() {
      return this._overhitDamage;
   }

   public boolean isOverhit() {
      return this._overhit;
   }

   private double[] calculateExpAndSp(int diff, int damage, long totalDamage, Creature attacker) {
      double xp = (double)this.getExpReward(attacker) * (double)damage / (double)totalDamage;
      if (Config.ALT_GAME_EXPONENT_XP != 0.0F && (float)Math.abs(diff) >= Config.ALT_GAME_EXPONENT_XP) {
         xp = 0.0;
      }

      double sp = (double)this.getSpReward(attacker) * (double)damage / (double)totalDamage;
      if (Config.ALT_GAME_EXPONENT_SP != 0.0F && (float)Math.abs(diff) >= Config.ALT_GAME_EXPONENT_XP) {
         sp = 0.0;
      }

      if (Config.ALT_GAME_EXPONENT_XP == 0.0F && Config.ALT_GAME_EXPONENT_SP == 0.0F) {
         if (diff < -5) {
            diff = -5;
         }

         if (diff > 5) {
            double pow = Math.pow(0.8333333333333334, (double)(diff - 5));
            xp *= pow;
            sp *= pow;
         }
      }

      xp = Math.max(0.0, xp);
      sp = Math.max(0.0, sp);
      return new double[]{xp, sp};
   }

   public long calculateOverhitExp(long normalExp) {
      double overhitPercentage = this.getOverhitDamage() * 100.0 / this.getMaxHp();
      if (overhitPercentage > 25.0) {
         overhitPercentage = 25.0;
      }

      double overhitExp = overhitPercentage / 100.0 * (double)normalExp;
      return Math.round(overhitExp);
   }

   @Override
   public boolean isAttackable() {
      return true;
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_SPAWN), 100L);
      this.setSpoil(false);
      this.clearAggroList();
      this.clearAbsorbers();
      this._harvestItems = null;
      this._seeded = false;
      this._seedType = 0;
      this._seederObjId = 0;
      this.overhitEnabled(false);
      this._sweepItems = null;
      this.setWalking();
      if (this.getChampionTemplate() != null) {
         for(AbnormalEffect effect : this.getChampionTemplate().abnormalEffect) {
            if (effect != null) {
               this.startAbnormalEffect(effect);
            }
         }
      }

      if (!this.isInActiveRegion() && this.hasAI() && !this.isGlobalAI()) {
         this.getAI().stopAITask();
      }
   }

   public boolean isSpoil() {
      return this._isSpoil;
   }

   public void setSpoil(boolean isSpoil) {
      this._isSpoil = isSpoil;
   }

   public final int getIsSpoiledBy() {
      return this._isSpoiledBy;
   }

   public final void setIsSpoiledBy(int value) {
      this._isSpoiledBy = value;
   }

   public void setSeeded(Player seeder) {
      if (this._seedType != 0 && this._seederObjId == seeder.getObjectId()) {
         this.setSeeded(this._seedType, seeder.getLevel());
      }
   }

   public void setSeeded(int id, Player seeder) {
      if (!this._seeded) {
         this._seedType = id;
         this._seederObjId = seeder.getObjectId();
      }
   }

   private void setSeeded(int id, int seederLvl) {
      this._seeded = true;
      this._seedType = id;
      int count = 1;
      Set<Integer> skillIds = this.getTemplate().getSkills().keySet();
      if (skillIds != null) {
         for(int skillId : skillIds) {
            switch(skillId) {
               case 4303:
                  count *= 2;
                  break;
               case 4304:
                  count *= 3;
                  break;
               case 4305:
                  count *= 4;
                  break;
               case 4306:
                  count *= 5;
                  break;
               case 4307:
                  count *= 6;
                  break;
               case 4308:
                  count *= 7;
                  break;
               case 4309:
                  count *= 8;
                  break;
               case 4310:
                  count *= 9;
            }
         }
      }

      int diff = this.getLevel() - (ManorParser.getInstance().getSeedLevel(this._seedType) - 5);
      if (diff > 0) {
         count += diff;
      }

      this._harvestItems = new ItemHolder[]{
         new ItemHolder(ManorParser.getInstance().getCropType(this._seedType), (long)((double)count * Config.RATE_DROP_MANOR))
      };
   }

   public int getSeederId() {
      return this._seederObjId;
   }

   public int getSeedType() {
      return this._seedType;
   }

   public boolean isSeeded() {
      return this._seeded;
   }

   public final void setOnKillDelay(int delay) {
      this._onKillDelay = delay;
   }

   public final int getOnKillDelay() {
      return this._onKillDelay;
   }

   public final void setFindTargetDelay(int delay) {
      this._findTargetDelay = System.currentTimeMillis() + (long)delay;
   }

   public final long getFindTargetDelay() {
      return this._findTargetDelay;
   }

   @Override
   public boolean hasRandomAnimation() {
      return Config.MAX_MONSTER_ANIMATION > 0 && this.isRandomAnimationEnabled() && !(this instanceof GrandBossInstance);
   }

   @Override
   public boolean isMob() {
      return true;
   }

   protected void setCommandChannelTimer(Attackable.CommandChannelTimer commandChannelTimer) {
      this._commandChannelTimer = commandChannelTimer;
   }

   public Attackable.CommandChannelTimer getCommandChannelTimer() {
      return this._commandChannelTimer;
   }

   public CommandChannel getFirstCommandChannelAttacked() {
      return this._firstCommandChannelAttacked;
   }

   public void setFirstCommandChannelAttacked(CommandChannel firstCommandChannelAttacked) {
      this._firstCommandChannelAttacked = firstCommandChannelAttacked;
   }

   public long getCommandChannelLastAttack() {
      return this._commandChannelLastAttack;
   }

   public void setCommandChannelLastAttack(long channelLastAttack) {
      this._commandChannelLastAttack = channelLastAttack;
   }

   public void returnHome() {
      this.clearAggroList();
      if (this.hasAI() && this.getSpawn() != null) {
         this.getAI().setIntention(CtrlIntention.MOVING, this.getSpawn().getLocation());
      }
   }

   public double getVitalityPoints(int damage) {
      if (damage <= 0) {
         return 0.0;
      } else {
         double divider = this.getTemplate().getBaseVitalityDivider();
         return divider == 0.0 ? 0.0 : -Math.min((double)damage, this.getMaxHp()) / divider;
      }
   }

   public boolean useVitalityRate() {
      return this.getChampionTemplate() == null || this.getChampionTemplate().useVitalityRate;
   }

   @Override
   public boolean isRaid() {
      return this._isRaid;
   }

   @Override
   public boolean isEpicRaid() {
      return this._isEpicRaid;
   }

   public void setIsEpicRaid(boolean isEpicRaid) {
      this._isEpicRaid = isEpicRaid;
   }

   public void setIsRaid(boolean isRaid) {
      this._isRaid = isRaid;
   }

   @Override
   public boolean isSiegeGuard() {
      return this._isSiegeGuard;
   }

   public void setIsSiegeGuard(boolean isSiegeGuard) {
      this._isSiegeGuard = isSiegeGuard;
   }

   public void setIsRaidMinion(boolean val) {
      this._isRaid = val;
      this._isRaidMinion = val;
   }

   @Override
   public boolean isRaidMinion() {
      return this._isRaidMinion;
   }

   @Override
   public boolean isLethalImmune() {
      return this._isLethalImmune;
   }

   public void setIsLethalImmune(boolean isLethalImmune) {
      this._isLethalImmune = isLethalImmune;
   }

   @Override
   public boolean isGlobalAI() {
      return this._isGlobalAI;
   }

   public void setIsGlobalAI(boolean isGlobalAI) {
      this._isGlobalAI = isGlobalAI;
   }

   @Override
   public boolean isMinion() {
      return this.getLeader() != null;
   }

   public Attackable getLeader() {
      return null;
   }

   public boolean canShowLevelInTitle() {
      return !this.getName().equals("Chest");
   }

   public void addAbsorber(Player attacker) {
      if (attacker != null) {
         if (!(this.getCurrentHpPercents() > 50.0)) {
            if (this._absorbersIds == null) {
               this._absorbersIds = new TIntHashSet();
            }

            this._absorbersIds.add(attacker.getObjectId());
         }
      }
   }

   public boolean isAbsorbed(Player player) {
      if (this._absorbersIds == null) {
         return false;
      } else {
         return this._absorbersIds.contains(player.getObjectId());
      }
   }

   public void clearAbsorbers() {
      if (this._absorbersIds != null) {
         this._absorbersIds.clear();
      }
   }

   @Override
   public boolean canBeAttacked() {
      return true;
   }

   @Override
   public Location getMinionPosition() {
      return Location.findPointToStay(this, 100, 150, false);
   }

   @Override
   public void addInfoObject(GameObject object) {
      if (object.isPlayer() && this.getAI().getIntention() == CtrlIntention.IDLE) {
         this.getAI().setIntention(CtrlIntention.ACTIVE, null);
      }
   }

   @Override
   public void removeInfoObject(GameObject object) {
      super.removeInfoObject(object);
      if (object.isAttackable()) {
         this.getAggroList().remove(object);
      }

      if (this.hasAI()) {
         if (this.getAggroList().isEmpty()) {
            this.getAI().getTargetList().clear();
            this.getAI().stopAutoAttack();
         }

         this.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
      }
   }

   public static final class AggroInfo {
      private final Creature _attacker;
      private int _hate = 0;
      private int _damage = 0;

      AggroInfo(Creature pAttacker) {
         this._attacker = pAttacker;
      }

      public final Creature getAttacker() {
         return this._attacker;
      }

      public final int getHate() {
         return this._hate;
      }

      public final int checkHate(Creature owner) {
         if (this._attacker.isAlikeDead() || !this._attacker.isVisible()) {
            this._hate = 0;
         }

         return this._hate;
      }

      public final void addHate(int value) {
         this._hate = (int)Math.min((long)this._hate + (long)value, 999999999L);
      }

      public final void stopHate() {
         this._hate = 0;
      }

      public final int getDamage() {
         return this._damage;
      }

      public final void addDamage(int value) {
         this._damage = (int)Math.min((long)this._damage + (long)value, 999999999L);
      }

      @Override
      public final boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj instanceof Attackable.AggroInfo) {
            return ((Attackable.AggroInfo)obj).getAttacker() == this._attacker;
         } else {
            return false;
         }
      }

      @Override
      public final int hashCode() {
         return this._attacker.getObjectId();
      }
   }

   private static class CommandChannelTimer implements Runnable {
      private final Attackable _monster;

      public CommandChannelTimer(Attackable monster) {
         this._monster = monster;
      }

      @Override
      public void run() {
         if (System.currentTimeMillis() - this._monster.getCommandChannelLastAttack() > (long)Config.LOOT_RAIDS_PRIVILEGE_INTERVAL) {
            this._monster.setCommandChannelTimer(null);
            this._monster.setFirstCommandChannelAttacked(null);
            this._monster.setCommandChannelLastAttack(0L);
         } else {
            ThreadPoolManager.getInstance().schedule(this, 10000L);
         }
      }
   }

   private class GroupInfo {
      public HashSet<Player> _players = new HashSet<>();
      public long _reward = 0L;

      public GroupInfo() {
      }
   }

   protected static class OnKillByMobNotifyTask implements Runnable {
      private final Npc _attackable;
      private final Quest _quest;
      private final Npc _killer;

      public OnKillByMobNotifyTask(Npc attackable, Quest quest, Npc killer) {
         this._attackable = attackable;
         this._quest = quest;
         this._killer = killer;
      }

      @Override
      public void run() {
         this._quest.notifyKillByMob(this._attackable, this._killer);
      }
   }

   protected static class OnKillNotifyTask implements Runnable {
      private final Attackable _attackable;
      private final Quest _quest;
      private final Player _killer;
      private final boolean _isSummon;

      public OnKillNotifyTask(Attackable attackable, Quest quest, Player killer, boolean isSummon) {
         this._attackable = attackable;
         this._quest = quest;
         this._killer = killer;
         this._isSummon = isSummon;
      }

      @Override
      public void run() {
         if (this._quest != null && this._attackable != null && this._killer != null) {
            this._quest.notifyKill(this._attackable, this._killer, this._isSummon);
         }
      }
   }

   protected final class RewardInfo {
      private final Player _attacker;
      private int _damage = 0;

      public RewardInfo(Player attacker, int damage) {
         this._attacker = attacker;
         this._damage = damage;
      }

      public Player getAttacker() {
         return this._attacker;
      }

      public void addDamage(int damage) {
         this._damage += damage;
      }

      public int getDamage() {
         return this._damage;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj instanceof Attackable.RewardInfo) {
            return ((Attackable.RewardInfo)obj)._attacker == this._attacker;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this._attacker.getObjectId();
      }
   }
}
