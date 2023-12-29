package l2e.fake;

import java.util.logging.Level;
import l2e.fake.ai.FakePlayerAI;
import l2e.fake.model.FakeSupport;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.actor.templates.player.PcTemplate;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;

public class FakePlayer extends Player {
   private FakePlayerAI _fakeAi;
   private boolean _underControl = false;

   public boolean isUnderControl() {
      return this._underControl;
   }

   public void setUnderControl(boolean underControl) {
      this._underControl = underControl;
   }

   public FakePlayer(int objectId, PcTemplate template, String accountName, PcAppearance app) {
      super(objectId, template, accountName, app);
   }

   public FakePlayerAI getFakeAi() {
      return this._fakeAi;
   }

   public void setFakeAi(FakePlayerAI fakeAi) {
      this._fakeAi = fakeAi;
   }

   public void assignDefaultAI(boolean isPassive) {
      try {
         this.setFakeAi(FakeSupport.getAIbyClassId(this.getClassId(), isPassive).getConstructor(FakePlayer.class).newInstance(this));
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public boolean checkUseMagicConditions(Skill skill, boolean forceUse, boolean dontMove) {
      if (skill == null) {
         return false;
      } else if (this.isDead() || this.isOutOfControl()) {
         this.sendActionFailed();
         return false;
      } else if (this.isSkillDisabled(skill)) {
         return false;
      } else {
         SkillType sklType = skill.getSkillType();
         if (this.isFishing() && sklType != SkillType.PUMPING && sklType != SkillType.REELING && sklType != SkillType.FISHING) {
            return false;
         } else if (this.inObserverMode()) {
            this.abortCast();
            return false;
         } else if (this.isSitting()) {
            if (skill.isToggle()) {
               Effect effect = this.getFirstEffect(skill.getId());
               if (effect != null) {
                  effect.exit();
                  return false;
               }
            }

            return false;
         } else {
            if (skill.isToggle()) {
               Effect effect = this.getFirstEffect(skill.getId());
               if (effect != null) {
                  if (skill.getId() != 60) {
                     effect.exit();
                  }

                  this.sendActionFailed();
                  return false;
               }
            }

            if (this.isFakeDeath()) {
               this.sendActionFailed();
               return false;
            } else {
               GameObject target = null;
               TargetType sklTargetType = skill.getTargetType();
               Location worldPosition = this.getCurrentSkillWorldPosition();
               if (sklTargetType == TargetType.GROUND && worldPosition == null) {
                  _log.info("WorldPosition is null for skill: " + skill.getNameEn() + ", player: " + this.getName() + ".");
                  this.sendActionFailed();
                  return false;
               } else {
                  switch(sklTargetType) {
                     case AURA:
                     case FRONT_AURA:
                     case BEHIND_AURA:
                     case AURA_CORPSE_MOB:
                     case PARTY:
                     case PARTY_CLAN:
                     case CLAN:
                     case GROUND:
                     case SELF:
                     case AREA_SUMMON:
                        target = this;
                        break;
                     case PET:
                     case SUMMON:
                        target = this.getSummon();
                        break;
                     default:
                        target = this.getTarget();
                  }

                  if (target == null) {
                     this.sendActionFailed();
                     return false;
                  } else if (!(target instanceof DoorInstance)
                     || ((DoorInstance)target).isAutoAttackable(this)
                        && (!((DoorInstance)target).isOpenableBySkill() || skill.getSkillType() == SkillType.UNLOCK)) {
                     if (this.isInDuel() && target instanceof Playable) {
                        Player cha = target.getActingPlayer();
                        if (cha.getDuelId() != this.getDuelId()) {
                           this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                           this.sendActionFailed();
                           return false;
                        }
                     }

                     if (!skill.checkCondition(this, target, false, false)) {
                        this.sendActionFailed();
                        return false;
                     } else {
                        if (skill.isOffensive()) {
                           if (this.isInsidePeaceZone(this, target)) {
                              this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
                              this.sendActionFailed();
                              return false;
                           }

                           if (this.isInOlympiadMode() && !this.isOlympiadStart()) {
                              this.sendActionFailed();
                              return false;
                           }

                           if (!target.isAutoAttackable(this) && !forceUse) {
                              switch(sklTargetType) {
                                 case AURA:
                                 case FRONT_AURA:
                                 case BEHIND_AURA:
                                 case AURA_CORPSE_MOB:
                                 case PARTY:
                                 case PARTY_CLAN:
                                 case CLAN:
                                 case GROUND:
                                 case SELF:
                                 case AREA_SUMMON:
                                    break;
                                 default:
                                    this.sendActionFailed();
                                    return false;
                              }
                           }

                           if (dontMove) {
                              if (sklTargetType == TargetType.GROUND) {
                                 if (!this.isInsideRadius(
                                    worldPosition.getX(),
                                    worldPosition.getY(),
                                    worldPosition.getZ(),
                                    (int)((double)skill.getCastRange() + this.getColRadius()),
                                    false,
                                    false
                                 )) {
                                    this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                    this.sendActionFailed();
                                    return false;
                                 }
                              } else if (skill.getCastRange() > 0
                                 && !this.isInsideRadius(target, (int)((double)skill.getCastRange() + this.getColRadius()), false, false)) {
                                 this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                 this.sendActionFailed();
                                 return false;
                              }
                           }
                        }

                        if (!skill.isOffensive() && target instanceof MonsterInstance && !forceUse) {
                           switch(sklTargetType) {
                              case AURA:
                              case FRONT_AURA:
                              case BEHIND_AURA:
                              case AURA_CORPSE_MOB:
                              case PARTY:
                              case PARTY_CLAN:
                              case CLAN:
                              case GROUND:
                              case SELF:
                              case PET:
                              case SUMMON:
                              case CORPSE_MOB:
                              case AREA_CORPSE_MOB:
                                 break;
                              case AREA_SUMMON:
                              default:
                                 switch(sklType) {
                                    case DELUXE_KEY_UNLOCK:
                                    case UNLOCK:
                                       break;
                                    default:
                                       this.sendActionFailed();
                                       return false;
                                 }
                           }
                        }

                        if (skill.hasEffectType(EffectType.SPOIL) && !(target instanceof MonsterInstance)) {
                           this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                           this.sendActionFailed();
                           return false;
                        } else {
                           switch(sklTargetType) {
                              case AURA_CORPSE_MOB:
                              case PET:
                              case SUMMON:
                              case CORPSE_MOB:
                              case AREA_CORPSE_MOB:
                              default:
                                 if (!this.checkPvpSkill(target, skill) && !this.getAccessLevel().allowPeaceAttack()) {
                                    this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                                    this.sendActionFailed();
                                    return false;
                                 }
                              case AURA:
                              case FRONT_AURA:
                              case BEHIND_AURA:
                              case PARTY:
                              case PARTY_CLAN:
                              case CLAN:
                              case GROUND:
                              case SELF:
                              case AREA_SUMMON:
                              case UNDEAD:
                                 return true;
                           }
                        }
                     }
                  } else {
                     this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                     this.sendActionFailed();
                     return false;
                  }
               }
            }
         }
      }
   }

   public void forceAutoAttack(Creature target) {
      if (target == null) {
         this.rndWalk();
      } else if (!this.isInsidePeaceZone(this, target)) {
         if (!GeoEngine.canSeeTarget(this, target, false)) {
            this.rndWalk();
         } else {
            this.getAI().setIntention(CtrlIntention.ATTACK, target);
         }
      }
   }

   public synchronized void despawnPlayer() {
      try {
         this.setOnlineStatus(false, true);
         this.abortAttack();
         this.abortCast();
         this.stopMove(null);
         this.setTarget(null);
         if (this.isFlying()) {
            this.removeSkill(SkillsParser.FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
         }

         this.stopAllTimers();

         for(Creature character : World.getInstance().getAroundCharacters(this)) {
            if (!(character instanceof StaticObjectInstance) && character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
               character.abortCast();
            }
         }

         for(Effect effect : this.getAllEffects()) {
            if (effect.getSkill().isToggle()) {
               effect.exit();
            } else {
               switch(effect.getEffectType()) {
                  case SIGNET_GROUND:
                  case SIGNET_EFFECT:
                     effect.exit();
               }
            }
         }

         this.decayMe();
         if (this.getParty() != null) {
            this.getParty().removePartyMember(this, Party.messageType.Disconnected);
         }

         if (this.getSummon() != null) {
            this.getSummon().unSummon(this);
         }

         if (OlympiadManager.getInstance().isRegistered(this) || this.getOlympiadGameId() != -1) {
            OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
         }

         if (this.getClan() != null) {
            ClanMember clanMember = this.getClan().getClanMember(this.getObjectId());
            if (clanMember != null) {
               clanMember.setPlayerInstance(null);
            }
         }

         if (this.getActiveRequester() != null) {
            this.setActiveRequester(null);
            this.cancelActiveTrade();
         }

         if (this.isGM()) {
            AdminParser.getInstance().deleteGm(this);
         }

         if (this.inObserverMode()) {
            this.setXYZInvisible(this.getLastX(), this.getLastY(), this.getLastZ());
         }

         if (this.getVehicle() != null) {
            this.getVehicle().oustPlayer(this);
         }

         this.getInventory().deleteMe();
         this.clearWarehouse();
         this.getFreight().deleteMe();
         this.clearRefund();
         if (this.isCursedWeaponEquipped()) {
            CursedWeaponsManager.getInstance().getCursedWeapon(this.getCursedWeaponEquippedId()).setPlayer(null);
         }

         if (this.getClanId() > 0) {
            this.getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
         }

         World.getInstance().removeFromAllPlayers(this);
         this.notifyFriends();
         this.getBlockList().playerLogout();
      } catch (Exception var5) {
         _log.log(Level.WARNING, "Exception on deleteMe()" + var5.getMessage(), (Throwable)var5);
      }
   }

   public void heal() {
      this.setCurrentCp(this.getMaxCp());
      this.setCurrentHp(this.getMaxHp());
      this.setCurrentMp(this.getMaxMp());
   }
}
