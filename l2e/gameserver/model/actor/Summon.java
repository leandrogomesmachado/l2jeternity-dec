package l2e.gameserver.model.actor;

import java.util.Set;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.character.SummonAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.data.parser.DamageLimitParser;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.events.SummonEvents;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.actor.stat.SummonStat;
import l2e.gameserver.model.actor.status.SummonStatus;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.DamageLimit;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PetInventory;
import l2e.gameserver.model.items.type.ActionType;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowUpdate;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.PetDelete;
import l2e.gameserver.network.serverpackets.PetInfo;
import l2e.gameserver.network.serverpackets.PetItemList;
import l2e.gameserver.network.serverpackets.PetStatusUpdate;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TeleportToLocation;
import l2e.gameserver.taskmanager.DecayTaskManager;

public abstract class Summon extends Playable {
   private Player _owner;
   private int _attackRange = 36;
   private boolean _follow = true;
   private boolean _previousFollowStatus = true;
   protected boolean _restoreSummon = true;
   private int _shotsMask = 0;
   private boolean _cancelAction = false;
   private static final int[] PASSIVE_SUMMONS = new int[]{
      12564,
      12621,
      14702,
      14703,
      14704,
      14705,
      14706,
      14707,
      14708,
      14709,
      14710,
      14711,
      14712,
      14713,
      14714,
      14715,
      14716,
      14717,
      14718,
      14719,
      14720,
      14721,
      14722,
      14723,
      14724,
      14725,
      14726,
      14727,
      14728,
      14729,
      14730,
      14731,
      14732,
      14733,
      14734,
      14735,
      14736
   };

   public Summon(int objectId, NpcTemplate template, Player owner) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Summon);
      this.setReflectionId(owner.getReflectionId());
      this._showSummonAnimation = true;
      this._owner = owner;
      this.getAI();
      this.setXYZInvisible(owner.getX() + 20, owner.getY() + 20, owner.getZ() + 100);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      if (Config.SUMMON_STORE_SKILL_COOLTIME && !this.isTeleporting()) {
         this.restoreEffects();
      }

      this.setFollowStatus(true);
      this.updateAndBroadcastStatus(0);

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         player.sendPacket(RelationChanged.update(player, this, player));
      }

      Party party = this.getOwner().getParty();
      if (party != null) {
         party.broadcastToPartyMembers(this.getOwner(), new ExPartyPetWindowAdd(this));
      }

      this.setShowSummonAnimation(false);
      this._restoreSummon = false;
      this.getStatus().startHpMpRegeneration();
   }

   public SummonStat getStat() {
      return (SummonStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new SummonStat(this));
   }

   public SummonStatus getStatus() {
      return (SummonStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new SummonStatus(this));
   }

   @Override
   protected CharacterAI initAI() {
      return new SummonAI(this);
   }

   @Override
   public void initCharEvents() {
      this.setCharEvents(new SummonEvents(this));
   }

   public SummonEvents getEvents() {
      return (SummonEvents)super.getEvents();
   }

   public NpcTemplate getTemplate() {
      return (NpcTemplate)super.getTemplate();
   }

   public abstract int getSummonType();

   @Override
   public final void stopAllEffects() {
      super.stopAllEffects();
      this.updateAndBroadcastStatus(1);
   }

   @Override
   public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
      super.stopAllEffectsExceptThoseThatLastThroughDeath();
      this.updateAndBroadcastStatus(1);
   }

   @Override
   public void updateAbnormalEffect() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         player.sendPacket(new NpcInfo.SummonInfo(this, player, 1));
      }
   }

   public boolean isMountable() {
      return false;
   }

   public long getExpForThisLevel() {
      return this.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel() ? 0L : ExperienceParser.getInstance().getExpForLevel(this.getLevel());
   }

   public long getExpForNextLevel() {
      return this.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel() - 1 ? 0L : ExperienceParser.getInstance().getExpForLevel(this.getLevel() + 1);
   }

   @Override
   public final int getKarma() {
      return this.getOwner() != null ? this.getOwner().getKarma() : 0;
   }

   @Override
   public final byte getPvpFlag() {
      return this.getOwner() != null ? this.getOwner().getPvpFlag() : 0;
   }

   @Override
   public final int getTeam() {
      return this.getOwner() != null ? this.getOwner().getTeam() : 0;
   }

   public final Player getOwner() {
      return this._owner;
   }

   @Override
   public final int getId() {
      return this.getTemplate().getId();
   }

   public int getSoulShotsPerHit() {
      return this.getLevel() / 27 + 1;
   }

   public int getSpiritShotsPerHit() {
      return this.getLevel() / 58 + 1;
   }

   public void followOwner() {
      this.setFollowStatus(true);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.isNoblesseBlessed()) {
         this.stopEffects(EffectType.NOBLESSE_BLESSING);
         this.storeEffect(true);
      } else {
         this.storeEffect(false);
      }

      Player owner = this.getOwner();
      if (owner != null) {
         for(Npc TgMob : World.getInstance().getAroundNpc(this)) {
            if (TgMob instanceof Attackable && !((Attackable)TgMob).isDead()) {
               Attackable.AggroInfo info = ((Attackable)TgMob).getAggroList().get(this);
               if (info != null) {
                  ((Attackable)TgMob).addDamageHate(owner, info.getDamage(), info.getHate());
               }
            }
         }
      }

      if (this.isPhoenixBlessed() && this.getOwner() != null) {
         this.getOwner().reviveRequest(this.getOwner(), null, true);
      }

      if (this.isServitor()) {
         DecayTaskManager.getInstance().add(this);
      }

      super.onDeath(killer);
   }

   public void doDie(Creature killer, boolean decayed) {
      if (!decayed) {
         DecayTaskManager.getInstance().add(this);
      }

      super.onDeath(killer);
   }

   public void stopDecay() {
      DecayTaskManager.getInstance().cancel(this);
   }

   @Override
   public void onDecay() {
      this.deleteMe(this._owner);
   }

   @Override
   public void broadcastStatusUpdate() {
      super.broadcastStatusUpdate();
      this.updateAndBroadcastStatus(1);
   }

   public void deleteMe(Player owner) {
      owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
      if (this.getInventory() != null) {
         this.getInventory().destroyAllItems("pet deleted", this.getOwner(), this);
      }

      this.decayMe();
      owner.setPet(null);
      super.deleteMe();
   }

   public void unSummon(Player owner) {
      if (this.isVisible() && !this.isDead()) {
         this.getAI().stopFollow();
         owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
         Party party;
         if ((party = owner.getParty()) != null) {
            party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
         }

         if (this.getInventory() != null && this.getInventory().getSize() > 0) {
            this.getOwner().setPetInvItems(true);
            this.sendPacket(SystemMessageId.ITEMS_IN_PET_INVENTORY);
         } else {
            this.getOwner().setPetInvItems(false);
         }

         this.store();
         this.storeEffect(true);
         owner.setPet(null);
         if (this.hasAI()) {
            this.getAI().stopAITask();
         }

         this.stopAllEffects();
         this.decayMe();
         this.setTarget(null);
      }
   }

   public int getAttackRange() {
      return this._attackRange;
   }

   public void setAttackRange(int range) {
      this._attackRange = range < 36 ? 36 : range;
   }

   public void setFollowStatus(boolean state) {
      this._follow = state;
      if (this._follow) {
         this.getAI().setIntention(CtrlIntention.FOLLOW, this.getOwner());
      } else {
         this.getAI().setIntention(CtrlIntention.IDLE, null);
      }
   }

   public boolean isInFollowStatus() {
      return this._follow;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return this._owner.isAutoAttackable(attacker);
   }

   public int getControlObjectId() {
      return 0;
   }

   public Weapon getActiveWeapon() {
      return null;
   }

   public PetInventory getInventory() {
      return null;
   }

   public void doPickupItem(GameObject object) {
   }

   public void setRestoreSummon(boolean val) {
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      return null;
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   public Weapon getSecondaryWeaponItem() {
      return null;
   }

   @Override
   public boolean isInvul() {
      return super.isInvul() || this.getOwner().isSpawnProtected();
   }

   @Override
   public Party getParty() {
      return this._owner == null ? null : this._owner.getParty();
   }

   @Override
   public boolean isInParty() {
      return this._owner != null && this._owner.isInParty();
   }

   @Override
   public boolean useMagic(Skill skill, boolean forceUse, boolean dontMove, boolean msg) {
      if (skill == null || this.isDead() || this.getOwner() == null) {
         return false;
      } else if (skill.isPassive()) {
         return false;
      } else if (this.isCastingNow()) {
         return false;
      } else {
         this.getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
         GameObject target = null;
         switch(skill.getTargetType()) {
            case OWNER_PET:
               target = this.getOwner();
               break;
            case PARTY:
            case AURA:
            case FRONT_AURA:
            case BEHIND_AURA:
            case SELF:
            case AURA_CORPSE_MOB:
            case AURA_UNDEAD_ENEMY:
            case COMMAND_CHANNEL:
               target = this;
               break;
            default:
               target = skill.getFirstOfTargetList(this);
         }

         if (target == null) {
            this.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
            return false;
         } else if (!GeoEngine.canSeeTarget(this, target, false)) {
            this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
            this.sendActionFailed();
            return false;
         } else if (this.isSkillDisabled(skill)) {
            this.sendPacket(SystemMessageId.PET_SKILL_CANNOT_BE_USED_RECHARCHING);
            return false;
         } else if (!skill.checkCondition(this, target, false, true)) {
            this.sendActionFailed();
            return false;
         } else if (this.getCurrentMp() < (double)(this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill))) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
            return false;
         } else if (this.getCurrentHp() <= (double)skill.getHpConsume()) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
            return false;
         } else if (!skill.isOffensive()) {
            this.getAI().setIntention(CtrlIntention.CAST, skill, target);
            return true;
         } else {
            label98:
            if (this.getOwner() == target) {
               return false;
            } else if (this.isInsidePeaceZone(this, target) && !this.getOwner().getAccessLevel().allowPeaceAttack()) {
               this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
               return false;
            } else if (this.getOwner().isInOlympiadMode() && !this.getOwner().isOlympiadStart()) {
               this.sendActionFailed();
               return false;
            } else {
               if (target.getActingPlayer() != null
                  && this.getOwner().getSiegeState() > 0
                  && this.getOwner().isInsideZone(ZoneId.SIEGE)
                  && target.getActingPlayer().getSiegeState() == this.getOwner().getSiegeState()
                  && target.getActingPlayer() != this.getOwner()
                  && target.getActingPlayer().getSiegeSide() == this.getOwner().getSiegeSide()) {
                  if (!SiegeManager.getInstance().canAttackSameSiegeSide()) {
                     if (TerritoryWarManager.getInstance().isTWInProgress()) {
                        this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                     } else {
                        this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                     }

                     this.sendActionFailed();
                     return false;
                  }

                  Clan clan1 = target.getActingPlayer().getClan();
                  Clan clan2 = this.getOwner().getClan();
                  if (clan1 != null
                     && clan2 != null
                     && (clan1.getAllyId() != 0 && clan2.getAllyId() != 0 && clan1.getAllyId() == clan2.getAllyId() || clan1.getId() == clan2.getId())) {
                     if (TerritoryWarManager.getInstance().isTWInProgress()) {
                        this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                     } else {
                        this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                     }

                     this.sendActionFailed();
                     return false;
                  }
               }

               if (target.isDoor()) {
                  if (!target.isAutoAttackable(this.getOwner())) {
                     return false;
                  }
                  break label98;
               } else if (!target.canBeAttacked() && !this.getOwner().getAccessLevel().allowPeaceAttack()) {
                  return false;
               } else {
                  if (!target.isAutoAttackable(this)
                     && !forceUse
                     && !target.isNpc()
                     && skill.getTargetType() != TargetType.AURA
                     && skill.getTargetType() != TargetType.FRONT_AURA
                     && skill.getTargetType() != TargetType.BEHIND_AURA
                     && skill.getTargetType() != TargetType.CLAN
                     && skill.getTargetType() != TargetType.PARTY
                     && skill.getTargetType() != TargetType.SELF) {
                     return false;
                  }
                  break label98;
               }
            }
         }
      }
   }

   @Override
   public void setIsImmobilized(boolean value) {
      super.setIsImmobilized(value);
      if (value) {
         this._previousFollowStatus = this.isInFollowStatus();
         if (this._previousFollowStatus) {
            this.setFollowStatus(false);
         }
      } else {
         this.setFollowStatus(this._previousFollowStatus);
      }
   }

   public void setOwner(Player newOwner) {
      this._owner = newOwner;
   }

   @Override
   public void sendDamageMessage(Creature target, int damage, Skill skill, boolean mcrit, boolean pcrit, boolean miss) {
      if (!miss && this.getOwner() != null) {
         if (Config.ALLOW_DAMAGE_LIMIT && target.isNpc()) {
            DamageLimit limit = DamageLimitParser.getInstance().getDamageLimit(target.getId());
            if (limit != null) {
               int damageLimit = skill != null ? (skill.isMagic() ? limit.getMagicDamage() : limit.getPhysicDamage()) : limit.getDamage();
               if (damageLimit > 0 && damage > damageLimit) {
                  damage = damageLimit;
               }
            }
         }

         if (target.getObjectId() != this.getOwner().getObjectId()) {
            if (pcrit || mcrit) {
               if (this.isServitor()) {
                  this.sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
               } else {
                  this.sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
               }
            }

            if (this.getOwner().isInOlympiadMode()
               && target instanceof Player
               && ((Player)target).isInOlympiadMode()
               && ((Player)target).getOlympiadGameId() == this.getOwner().getOlympiadGameId()) {
               OlympiadGameManager.getInstance().notifyCompetitorDamage(target.getActingPlayer(), damage);
            }

            SystemMessage sm;
            if (target.isInvul() && !(target instanceof NpcInstance)) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
               sm.addNpcName(this);
               sm.addCharName(target);
               sm.addNumber(damage);
            }

            this.sendPacket(sm);
         }
      }
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
      super.reduceCurrentHp(damage, attacker, skill);
      if (this.getOwner() != null && attacker != null) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
         sm.addNpcName(this);
         sm.addCharName(attacker);
         sm.addNumber((int)damage);
         this.sendPacket(sm);
      }
   }

   @Override
   public void doCast(Skill skill) {
      Player actingPlayer = this.getActingPlayer();
      if (!actingPlayer.checkPvpSkill(this.getTarget(), skill, true) && !actingPlayer.getAccessLevel().allowPeaceAttack()) {
         actingPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         actingPlayer.sendActionFailed();
      } else {
         super.doCast(skill);
      }
   }

   @Override
   public boolean isInCombat() {
      return this.getOwner() != null && this.getOwner().isInCombat();
   }

   @Override
   public Player getActingPlayer() {
      return this.getOwner();
   }

   @Override
   public final void broadcastPacket(GameServerPacket mov) {
      if (this.getOwner() != null) {
         mov.setInvisible(this.getOwner().isInvisible());
      }

      super.broadcastPacket(mov);
   }

   @Override
   public final void broadcastPacket(GameServerPacket mov, int radiusInKnownlist) {
      if (this.getOwner() != null) {
         mov.setInvisible(this.getOwner().isInvisible());
      }

      super.broadcastPacket(mov, radiusInKnownlist);
   }

   public void updateAndBroadcastStatus(int val) {
      if (this.getOwner() != null) {
         this.sendPacket(new PetInfo(this, val));
         this.sendPacket(new PetStatusUpdate(this));
         if (this.isVisible()) {
            this.broadcastNpcInfo(val);
         }

         Party party = this.getOwner().getParty();
         if (party != null) {
            party.broadcastToPartyMembers(this.getOwner(), new ExPartyPetWindowUpdate(this));
         }

         this.updateEffectIcons(true);
      }
   }

   public void broadcastNpcInfo(int val) {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != this.getOwner()) {
            player.sendPacket(new NpcInfo.SummonInfo(this, player, val));
         }
      }
   }

   public boolean isHungry() {
      return false;
   }

   public int getWeapon() {
      return 0;
   }

   public int getArmor() {
      return 0;
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (activeChar == this.getOwner()) {
         activeChar.sendPacket(new PetInfo(this, 0));
         this.updateEffectIcons(true);
         if (this.isPet()) {
            activeChar.sendPacket(new PetItemList(this.getInventory().getItems()));
         }
      } else {
         activeChar.sendPacket(new NpcInfo.SummonInfo(this, activeChar, 0));
      }

      if (this.isMoving() || this.isInFollowStatus()) {
         activeChar.sendPacket(new MoveToLocation(this));
      }
   }

   @Override
   public void onTeleported() {
      super.onTeleported();
      this.sendPacket(new TeleportToLocation(this, this.getX(), this.getY(), this.getZ(), this.getHeading()));
   }

   @Override
   public String toString() {
      return super.toString() + "(" + this.getId() + ") Owner: " + this.getOwner();
   }

   @Override
   public boolean isUndead() {
      return this.getTemplate().isUndead();
   }

   public void switchMode() {
   }

   public void cancelAction() {
      if (!this.isMovementDisabled()) {
         this.setCancelAction(true);
         this.getAI().setIntention(CtrlIntention.ACTIVE);
      }
   }

   public void doAttack() {
      if (this.getOwner() != null) {
         if (this.isCancelAction()) {
            this.setCancelAction(false);
         }

         GameObject target = this.getOwner().getTarget();
         if (target != null) {
            this.setTarget(target);
            this.getAI().setIntention(CtrlIntention.ATTACK, target);
         }
      }
   }

   public final boolean canAttack(GameObject target, boolean ctrlPressed) {
      if (this.getOwner() == null) {
         return false;
      } else if (target != null && this != target && this.getOwner() != target) {
         int npcId = this.getId();
         if (Util.contains(PASSIVE_SUMMONS, npcId)) {
            this.getOwner().sendActionFailed();
            return false;
         } else if (this.isBetrayed()) {
            this.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
            this.sendActionFailed();
            return false;
         } else {
            if (this.isAttackingDisabled()) {
               if (!this.isAttackingNow()) {
                  return false;
               }

               this.getAI().setIntention(CtrlIntention.ATTACK, target);
            }

            if (this.isPet() && this.getLevel() - this.getOwner().getLevel() > 20) {
               this.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
               this.sendActionFailed();
               return false;
            } else if (this.getOwner().isInOlympiadMode() && !this.getOwner().isOlympiadStart()) {
               this.getOwner().sendActionFailed();
               return false;
            } else if (target.getActingPlayer() != null
               && this.getOwner().getSiegeState() > 0
               && this.getOwner().isInsideZone(ZoneId.SIEGE)
               && target.getActingPlayer().getSiegeSide() == this.getOwner().getSiegeSide()) {
               if (TerritoryWarManager.getInstance().isTWInProgress()) {
                  this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
               } else {
                  this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
               }

               this.sendActionFailed();
               return false;
            } else if (!this.getOwner().getAccessLevel().allowPeaceAttack() && this.getOwner().isInsidePeaceZone(this, target)) {
               this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
               return false;
            } else if (this.isLockedTarget()) {
               this.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
               return false;
            } else if (!target.isAutoAttackable(this.getOwner()) && !ctrlPressed && !target.isNpc()) {
               this.setFollowStatus(false);
               this.getAI().setIntention(CtrlIntention.FOLLOW, target);
               this.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return false;
            } else {
               return target.isDoor() || npcId != 14839 && npcId != 14737;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void sendPacket(GameServerPacket mov) {
      if (this.getOwner() != null) {
         this.getOwner().sendPacket(mov);
      }
   }

   @Override
   public void sendPacket(SystemMessageId id) {
      if (this.getOwner() != null) {
         this.getOwner().sendPacket(id);
      }
   }

   @Override
   public boolean isSummon() {
      return true;
   }

   @Override
   public Summon getSummon() {
      return this;
   }

   @Override
   public boolean isChargedShot(ShotType type) {
      return (this._shotsMask & type.getMask()) == type.getMask();
   }

   @Override
   public void setChargedShot(ShotType type, boolean charged) {
      if (charged) {
         this._shotsMask |= type.getMask();
      } else {
         this._shotsMask &= ~type.getMask();
      }
   }

   @Override
   public void rechargeShots(boolean physical, boolean magic) {
      if (this.getOwner().getAutoSoulShot() != null && !this.getOwner().getAutoSoulShot().isEmpty()) {
         for(int itemId : this.getOwner().getAutoSoulShot()) {
            ItemInstance item = this.getOwner().getInventory().getItemByItemId(itemId);
            if (item != null) {
               if (magic && item.getItem().getDefaultAction() == ActionType.summon_spiritshot) {
                  IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                  if (handler != null) {
                     handler.useItem(this.getOwner(), item, false);
                  }
               }

               if (physical && item.getItem().getDefaultAction() == ActionType.summon_soulshot) {
                  IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                  if (handler != null) {
                     handler.useItem(this.getOwner(), item, false);
                  }
               }
            } else {
               this.getOwner().removeAutoSoulShot(itemId);
            }
         }
      }
   }

   @Override
   public int getClanId() {
      return this.getOwner() != null ? this.getOwner().getClanId() : 0;
   }

   @Override
   public int getAllyId() {
      return this.getOwner() != null ? this.getOwner().getAllyId() : 0;
   }

   @Override
   public boolean isInCategory(CategoryType type) {
      return CategoryParser.getInstance().isInCategory(type, this.getId());
   }

   @Override
   public <E extends AbstractFightEvent> E getEvent(Class<E> eventClass) {
      return (E)(this.getOwner() != null ? this.getOwner().getEvent(eventClass) : super.getEvent(eventClass));
   }

   @Override
   public Set<AbstractFightEvent> getFightEvents() {
      return this.getOwner() != null ? this.getOwner().getFightEvents() : super.getFightEvents();
   }

   @Override
   public boolean isCancelAction() {
      return this._cancelAction;
   }

   public void setCancelAction(boolean val) {
      this._cancelAction = val;
   }

   @Override
   public boolean hasPremiumBonus() {
      return this._owner == null ? false : this._owner.hasPremiumBonus();
   }

   @Override
   public PremiumBonus getPremiumBonus() {
      return this._owner.getPremiumBonus();
   }

   public String getSummonName(Player player, Summon summon) {
      return summon.getName() != null && !summon.getName().isEmpty()
         ? summon.getName()
         : (player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? summon.getTemplate().getNameRu() : summon.getTemplate().getName());
   }
}
