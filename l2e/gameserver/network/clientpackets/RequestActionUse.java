package l2e.gameserver.network.clientpackets;

import java.util.Arrays;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.character.SummonAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.model.NextAction;
import l2e.gameserver.data.holder.SummonSkillsHolder;
import l2e.gameserver.data.parser.BotReportParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.BabyPetInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ChairSit;
import l2e.gameserver.network.serverpackets.ExAskCoupleAction;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.RecipeShopManageList;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestActionUse extends GameClientPacket {
   private static final int SIN_EATER_ID = 12564;
   private static final int SWITCH_STANCE_ID = 6054;
   private static final NpcStringId[] NPC_STRINGS = new NpcStringId[]{
      NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH,
      NpcStringId.HEY_WHAT_DO_YOU_EXPECT_OF_ME,
      NpcStringId.UGGGGGH_PUSH_ITS_NOT_COMING_OUT,
      NpcStringId.AH_I_MISSED_THE_MARK
   };
   private int _actionId;
   private boolean _ctrlPressed;
   private boolean _shiftPressed;

   @Override
   protected void readImpl() {
      this._actionId = this.readD();
      this._ctrlPressed = this.readD() == 1;
      this._shiftPressed = this.readC() == 1;
   }

   @Override
   protected void runImpl() {
      final Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if ((!activeChar.isFakeDeathNow() || this._actionId == 0) && !activeChar.isDead() && !activeChar.isOutOfControl()) {
            Effect ef = null;
            if ((ef = activeChar.getFirstEffect(EffectType.ACTION_BLOCK)) != null && !ef.checkCondition(this._actionId)) {
               activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
               activeChar.sendActionFailed();
            } else {
               if (activeChar.isTransformed()) {
                  int[] allowedActions = activeChar.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
                  if (Arrays.binarySearch(allowedActions, this._actionId) < 0) {
                     this.sendActionFailed();
                     Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " used action which he does not have! Id = " + this._actionId);
                     if (Config.DEBUG) {
                        _log.warning(
                           "Player "
                              + activeChar
                              + " used action which he does not have! Id = "
                              + this._actionId
                              + " transform: "
                              + activeChar.getTransformation()
                        );
                     }

                     return;
                  }
               }

               Summon summon = activeChar.getSummon();
               final GameObject target = activeChar.getTarget();
               switch(this._actionId) {
                  case 0:
                     if (!activeChar.isSitting() && activeChar.isMoving() && !activeChar.isFakeDeathNow()) {
                        NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVING, new NextAction.NextActionCallback() {
                           @Override
                           public void doWork() {
                              RequestActionUse.this.useSit(activeChar, target);
                           }
                        });
                        activeChar.getAI().setNextAction(nextAction);
                     } else {
                        this.useSit(activeChar, target);
                     }
                     break;
                  case 1:
                     if (activeChar.isRunning()) {
                        activeChar.setWalking();
                     } else {
                        activeChar.setRunning();
                     }
                     break;
                  case 10:
                     activeChar.tryOpenPrivateSellStore(false);
                     break;
                  case 12:
                     this.tryBroadcastSocial(2);
                     break;
                  case 13:
                     this.tryBroadcastSocial(3);
                     break;
                  case 14:
                     this.tryBroadcastSocial(4);
                     break;
                  case 15:
                     if (this.validateSummon(summon, true)) {
                        summon.cancelAction();
                        ((SummonAI)summon.getAI()).notifyFollowStatusChange();
                     }
                     break;
                  case 16:
                     if (this.validateSummon(summon, true) && summon.canAttack(target, this._ctrlPressed)) {
                        summon.doAttack();
                     }
                     break;
                  case 17:
                     if (this.validateSummon(summon, true)) {
                        summon.cancelAction();
                     }
                     break;
                  case 19:
                     if (this.validateSummon(summon, true)) {
                        if (summon.isDead()) {
                           this.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
                        } else if (summon.isAttackingNow() || summon.isInCombat() || summon.isMovementDisabled()) {
                           this.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
                        } else if (summon.isHungry()) {
                           if (!((PetInstance)summon).getPetData().getFood().isEmpty()) {
                              this.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
                           } else {
                              this.sendPacket(SystemMessageId.THE_HELPER_PET_CANNOT_BE_RETURNED);
                           }
                        } else {
                           summon.unSummon(activeChar);
                        }
                     }
                     break;
                  case 21:
                     if (this.validateSummon(summon, false)) {
                        summon.cancelAction();
                        ((SummonAI)summon.getAI()).notifyFollowStatusChange();
                     }
                     break;
                  case 22:
                     if (this.validateSummon(summon, false) && summon.canAttack(target, this._ctrlPressed)) {
                        summon.doAttack();
                     }
                     break;
                  case 23:
                     if (this.validateSummon(summon, false)) {
                        summon.cancelAction();
                     }
                     break;
                  case 24:
                     this.tryBroadcastSocial(6);
                     break;
                  case 25:
                     this.tryBroadcastSocial(5);
                     break;
                  case 26:
                     this.tryBroadcastSocial(7);
                     break;
                  case 28:
                     activeChar.tryOpenPrivateBuyStore();
                     break;
                  case 29:
                     this.tryBroadcastSocial(8);
                     break;
                  case 30:
                     this.tryBroadcastSocial(9);
                     break;
                  case 31:
                     this.tryBroadcastSocial(10);
                     break;
                  case 32:
                     this.useSkill(4230, false);
                     break;
                  case 33:
                     this.tryBroadcastSocial(11);
                     break;
                  case 34:
                     this.tryBroadcastSocial(12);
                     break;
                  case 35:
                     this.tryBroadcastSocial(13);
                     break;
                  case 36:
                     this.useSkill(4259, false);
                     break;
                  case 37:
                     if (activeChar.isAlikeDead()) {
                        this.sendActionFailed();
                        return;
                     }

                     if (activeChar.getPrivateStoreType() != 0) {
                        activeChar.setPrivateStoreType(0);
                        activeChar.broadcastCharInfo();
                     }

                     if (activeChar.isSitting()) {
                        activeChar.standUp();
                     }

                     this.sendPacket(new RecipeShopManageList(activeChar, true));
                     break;
                  case 38:
                     activeChar.mountPlayer(summon);
                     break;
                  case 39:
                     this.useSkill(4138, false);
                     break;
                  case 41:
                     if (this.validateSummon(summon, false)) {
                        if (target == null || !target.isDoor() && !(target instanceof SiegeFlagInstance)) {
                           this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                        } else {
                           this.useSkill(4230, false);
                        }
                     }
                     break;
                  case 42:
                     this.useSkill(4378, activeChar, false);
                     break;
                  case 43:
                     this.useSkill(4137, false);
                     break;
                  case 44:
                     this.useSkill(4139, false);
                     break;
                  case 45:
                     this.useSkill(4025, activeChar, false);
                     break;
                  case 46:
                     this.useSkill(4261, false);
                     break;
                  case 47:
                     this.useSkill(4260, false);
                     break;
                  case 48:
                     this.useSkill(4068, false);
                     break;
                  case 51:
                     if (activeChar.isAlikeDead()) {
                        this.sendActionFailed();
                        return;
                     }

                     if (activeChar.isSellingBuffs()) {
                        this.sendActionFailed();
                        return;
                     }

                     if (activeChar.getPrivateStoreType() != 0) {
                        activeChar.setPrivateStoreType(0);
                        activeChar.broadcastCharInfo();
                     }

                     if (activeChar.isSitting()) {
                        activeChar.standUp();
                     }

                     this.sendPacket(new RecipeShopManageList(activeChar, false));
                     break;
                  case 52:
                     if (this.validateSummon(summon, false)) {
                        if (!summon.isAttackingNow() && !summon.isInCombat()) {
                           summon.unSummon(activeChar);
                        } else {
                           this.sendPacket(SystemMessageId.SERVITOR_NOT_RETURN_IN_BATTLE);
                        }
                     }
                     break;
                  case 53:
                     if (this.validateSummon(summon, false) && target != null && summon != target && !summon.isMovementDisabled()) {
                        summon.setFollowStatus(false);
                        summon.getAI().setIntention(CtrlIntention.MOVING, target.getLocation());
                     }
                     break;
                  case 54:
                     if (this.validateSummon(summon, true) && target != null && summon != target && !summon.isMovementDisabled()) {
                        summon.setFollowStatus(false);
                        summon.getAI().setIntention(CtrlIntention.MOVING, target.getLocation());
                     }
                     break;
                  case 61:
                     activeChar.tryOpenPrivateSellStore(true);
                     break;
                  case 62:
                     this.tryBroadcastSocial(14);
                     break;
                  case 65:
                     if (Config.BOTREPORT_ENABLE) {
                        BotReportParser.getInstance().reportBot(activeChar);
                     } else {
                        activeChar.sendMessage("This action is disable.");
                     }
                     break;
                  case 66:
                     this.tryBroadcastSocial(15);
                     break;
                  case 67:
                     if (activeChar.isInAirShip() && activeChar.getAirShip().setCaptain(activeChar)) {
                        activeChar.broadcastCharInfo();
                     }
                     break;
                  case 68:
                     if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar) && activeChar.getAirShip().setCaptain(null)) {
                        activeChar.broadcastCharInfo();
                     }
                     break;
                  case 69:
                     AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
                     break;
                  case 70:
                     if (activeChar.isInAirShip()) {
                        if (activeChar.getAirShip().isCaptain(activeChar)) {
                           if (activeChar.getAirShip().setCaptain(null)) {
                              activeChar.broadcastCharInfo();
                           }
                        } else if (activeChar.getAirShip().isInDock()) {
                           activeChar.getAirShip().oustPlayer(activeChar);
                        }
                     }
                     break;
                  case 71:
                  case 72:
                  case 73:
                     this.useCoupleSocial(this._actionId - 55);
                     break;
                  case 1000:
                     if (target != null && target.isDoor()) {
                        this.useSkill(4079, false);
                     }
                     break;
                  case 1001:
                     if (this.validateSummon(summon, true) && summon.getId() == 12564) {
                        summon.broadcastPacket(new NpcSay(summon.getObjectId(), 22, summon.getId(), NPC_STRINGS[Rnd.get(NPC_STRINGS.length)]));
                     }
                     break;
                  case 1003:
                     this.useSkill(4710, true);
                     break;
                  case 1004:
                     this.useSkill(4711, activeChar, true);
                     break;
                  case 1005:
                     this.useSkill(4712, true);
                     break;
                  case 1006:
                     this.useSkill(4713, activeChar, true);
                     break;
                  case 1007:
                     this.useSkill(4699, activeChar, false);
                     break;
                  case 1008:
                     this.useSkill(4700, activeChar, false);
                     break;
                  case 1009:
                     this.useSkill(4701, false);
                     break;
                  case 1010:
                     this.useSkill(4702, activeChar, false);
                     break;
                  case 1011:
                     this.useSkill(4703, activeChar, false);
                     break;
                  case 1012:
                     this.useSkill(4704, false);
                     break;
                  case 1013:
                     this.useSkill(4705, false);
                     break;
                  case 1014:
                     this.useSkill(4706, false);
                     break;
                  case 1015:
                     this.useSkill(4707, false);
                     break;
                  case 1016:
                     this.useSkill(4709, false);
                     break;
                  case 1017:
                     this.useSkill(4708, false);
                     break;
                  case 1031:
                     this.useSkill(5135, false);
                     break;
                  case 1032:
                     this.useSkill(5136, false);
                     break;
                  case 1033:
                     this.useSkill(5137, false);
                     break;
                  case 1034:
                     this.useSkill(5138, false);
                     break;
                  case 1035:
                     this.useSkill(5139, false);
                     break;
                  case 1036:
                     this.useSkill(5142, false);
                     break;
                  case 1037:
                     this.useSkill(5141, false);
                     break;
                  case 1038:
                     this.useSkill(5140, false);
                     break;
                  case 1039:
                     if (target != null && target.isDoor()) {
                        this.useSkill(5110, false);
                     }
                     break;
                  case 1040:
                     if (target != null && target.isDoor()) {
                        this.useSkill(5111, false);
                     }
                     break;
                  case 1041:
                     this.useSkill(5442, true);
                     break;
                  case 1042:
                     this.useSkill(5444, true);
                     break;
                  case 1043:
                     this.useSkill(5443, true);
                     break;
                  case 1044:
                     this.useSkill(5445, true);
                     break;
                  case 1045:
                     this.useSkill(5584, true);
                     break;
                  case 1046:
                     this.useSkill(5585, true);
                     break;
                  case 1047:
                     this.useSkill(5580, false);
                     break;
                  case 1048:
                     this.useSkill(5581, false);
                     break;
                  case 1049:
                     this.useSkill(5582, false);
                     break;
                  case 1050:
                     this.useSkill(5583, false);
                     break;
                  case 1051:
                     this.useSkill(5638, false);
                     break;
                  case 1052:
                     this.useSkill(5639, false);
                     break;
                  case 1053:
                     this.useSkill(5640, false);
                     break;
                  case 1054:
                     this.useSkill(5643, false);
                     break;
                  case 1055:
                     this.useSkill(5647, false);
                     break;
                  case 1056:
                     this.useSkill(5648, false);
                     break;
                  case 1057:
                     this.useSkill(5646, false);
                     break;
                  case 1058:
                     this.useSkill(5652, false);
                     break;
                  case 1059:
                     this.useSkill(5653, false);
                     break;
                  case 1060:
                     this.useSkill(5654, false);
                     break;
                  case 1061:
                     this.useSkill(5745, true);
                     break;
                  case 1062:
                     this.useSkill(5746, true);
                     break;
                  case 1063:
                     this.useSkill(5747, true);
                     break;
                  case 1064:
                     this.useSkill(5748, true);
                     break;
                  case 1065:
                     this.useSkill(5753, true);
                     break;
                  case 1066:
                     this.useSkill(5749, true);
                     break;
                  case 1067:
                     this.useSkill(5750, true);
                     break;
                  case 1068:
                     this.useSkill(5751, true);
                     break;
                  case 1069:
                     this.useSkill(5752, true);
                     break;
                  case 1070:
                     this.useSkill(5771, true);
                     break;
                  case 1071:
                     this.useSkill(5761, true);
                     break;
                  case 1072:
                     this.useSkill(6046, true);
                     break;
                  case 1073:
                     this.useSkill(6047, true);
                     break;
                  case 1074:
                     this.useSkill(6048, true);
                     break;
                  case 1075:
                     this.useSkill(6049, true);
                     break;
                  case 1076:
                     this.useSkill(6050, true);
                     break;
                  case 1077:
                     this.useSkill(6051, true);
                     break;
                  case 1078:
                     this.useSkill(6052, true);
                     break;
                  case 1079:
                     this.useSkill(6053, true);
                     break;
                  case 1080:
                     this.useSkill(6041, false);
                     break;
                  case 1081:
                     this.useSkill(6042, false);
                     break;
                  case 1082:
                     this.useSkill(6043, false);
                     break;
                  case 1083:
                     this.useSkill(6044, false);
                     break;
                  case 1084:
                     if (summon instanceof BabyPetInstance) {
                        this.useSkill(6054, true);
                     }
                     break;
                  case 1086:
                     this.useSkill(6094, false);
                     break;
                  case 1087:
                     this.useSkill(6095, false);
                     break;
                  case 1088:
                     this.useSkill(6096, false);
                     break;
                  case 1089:
                     this.useSkill(6199, true);
                     break;
                  case 1090:
                     this.useSkill(6205, true);
                     break;
                  case 1091:
                     this.useSkill(6206, true);
                     break;
                  case 1092:
                     this.useSkill(6207, true);
                     break;
                  case 1093:
                     this.useSkill(6618, true);
                     break;
                  case 1094:
                     this.useSkill(6681, true);
                     break;
                  case 1095:
                     this.useSkill(6619, true);
                     break;
                  case 1096:
                     this.useSkill(6682, true);
                     break;
                  case 1097:
                     this.useSkill(6683, true);
                     break;
                  case 1098:
                     this.useSkill(6684, true);
                     break;
                  case 5000:
                     this.useSkill(23155, true);
                     break;
                  case 5001:
                     this.useSkill(23167, true);
                     break;
                  case 5002:
                     this.useSkill(23168, true);
                     break;
                  case 5003:
                     this.useSkill(5749, true);
                     break;
                  case 5004:
                     this.useSkill(5750, true);
                     break;
                  case 5005:
                     this.useSkill(5751, true);
                     break;
                  case 5006:
                     this.useSkill(5771, true);
                     break;
                  case 5007:
                     this.useSkill(6046, true);
                     break;
                  case 5008:
                     this.useSkill(6047, true);
                     break;
                  case 5009:
                     this.useSkill(6048, true);
                     break;
                  case 5010:
                     this.useSkill(6049, true);
                     break;
                  case 5011:
                     this.useSkill(6050, true);
                     break;
                  case 5012:
                     this.useSkill(6051, true);
                     break;
                  case 5013:
                     this.useSkill(6052, true);
                     break;
                  case 5014:
                     this.useSkill(6053, true);
                     break;
                  case 5015:
                     this.useSkill(6054, true);
                     break;
                  default:
                     if (Config.DEBUG) {
                        _log.warning(activeChar.getName() + ": unhandled action type " + this._actionId);
                     }
               }
            }
         } else {
            this.sendActionFailed();
         }
      }
   }

   protected boolean useSit(Player activeChar, GameObject target) {
      if (activeChar.getMountType() != MountType.NONE) {
         return false;
      } else if (!activeChar.isSitting()
         && target instanceof StaticObjectInstance
         && ((StaticObjectInstance)target).getType() == 1
         && activeChar.isInsideRadius(target, 150, false, false)) {
         ChairSit cs = new ChairSit(activeChar, ((StaticObjectInstance)target).getId());
         this.sendPacket(cs);
         activeChar.setSittingObject((StaticObjectInstance)target);
         activeChar.sitDown();
         activeChar.broadcastPacket(cs);
         return true;
      } else {
         if (activeChar.isFakeDeathNow()) {
            activeChar.stopEffects(EffectType.FAKE_DEATH);
         } else if (activeChar.isSitting()) {
            activeChar.standUp();
         } else {
            activeChar.sitDown();
         }

         return true;
      }
   }

   private void useSkill(int skillId, GameObject target, boolean pet) {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         Summon summon = activeChar.getSummon();
         if (this.validateSummon(summon, pet)) {
            if (summon instanceof BabyPetInstance && !((BabyPetInstance)summon).isInSupportMode() && skillId != 6054) {
               this.sendPacket(SystemMessageId.PET_AUXILIARY_MODE_CANNOT_USE_SKILLS);
            } else {
               int lvl = 0;
               if (summon.isPet()) {
                  if (summon.getLevel() - activeChar.getLevel() > 20) {
                     this.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
                     return;
                  }

                  lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
               } else {
                  lvl = SummonSkillsHolder.getInstance().getAvailableLevel(activeChar, summon, skillId);
               }

               if (lvl > 0) {
                  summon.setTarget(target);
                  summon.useMagic(SkillsParser.getInstance().getInfo(skillId, lvl), this._ctrlPressed, this._shiftPressed, true);
               }

               if (skillId == 6054) {
                  summon.switchMode();
               }
            }
         }
      }
   }

   private void useSkill(int skillId, boolean pet) {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         this.useSkill(skillId, activeChar.getTarget(), pet);
      }
   }

   private boolean validateSummon(Summon summon, boolean checkPet) {
      if (summon == null || (!checkPet || !summon.isPet()) && !summon.isServitor()) {
         if (checkPet) {
            this.sendPacket(SystemMessageId.DONT_HAVE_PET);
         } else {
            this.sendPacket(SystemMessageId.DONT_HAVE_SERVITOR);
         }

         return false;
      } else if (summon.isBetrayed()) {
         this.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
         return false;
      } else {
         return true;
      }
   }

   private void tryBroadcastSocial(int id) {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (activeChar.isFishing()) {
            this.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
         } else {
            if (activeChar.canMakeSocialAction()) {
               activeChar.broadcastPacket(new l2e.gameserver.network.serverpackets.SocialAction(activeChar.getObjectId(), id));
            }
         }
      }
   }

   private void useCoupleSocial(final int id) {
      final Player requester = this.getActiveChar();
      if (requester != null) {
         GameObject target = requester.getTarget();
         if (target != null && target.isPlayer()) {
            int distance = (int)Math.sqrt(requester.getPlanDistanceSq(target));
            if (distance > 125 || distance < 15 || requester.getObjectId() == target.getObjectId()) {
               this.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
            } else if (requester.isActionsDisabled()) {
               this.sendActionFailed();
            } else if (requester.isInStoreMode() || requester.isInCraftMode()) {
               SystemMessage sm = SystemMessage.getSystemMessage(
                  SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
               );
               sm.addPcName(requester);
               this.sendPacket(sm);
            } else if (requester.isInCombat() || requester.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(requester)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
               sm.addPcName(requester);
               this.sendPacket(sm);
            } else if (requester.isFishing()) {
               this.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
            } else if (requester.getKarma() > 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
               sm.addPcName(requester);
               this.sendPacket(sm);
            } else if (requester.isInOlympiadMode()) {
               SystemMessage sm = SystemMessage.getSystemMessage(
                  SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
               );
               sm.addPcName(requester);
               this.sendPacket(sm);
            } else if (requester.isInSiege()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
               sm.addPcName(requester);
               this.sendPacket(sm);
            } else {
               if (requester.isInHideoutSiege()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(
                     SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                  );
                  sm.addPcName(requester);
                  this.sendPacket(sm);
               }

               if (requester.isMounted() || requester.isFlyingMounted() || requester.isInBoat() || requester.isInAirShip()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(
                     SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                  );
                  sm.addPcName(requester);
                  this.sendPacket(sm);
               } else if (requester.isTransformed()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                  sm.addPcName(requester);
                  this.sendPacket(sm);
               } else if (requester.isAlikeDead()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                  sm.addPcName(requester);
                  this.sendPacket(sm);
               } else {
                  final Player partner = target.getActingPlayer();
                  if (partner.isInStoreMode() || partner.isInCraftMode()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isInCombat() || partner.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(partner)) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.getMultiSociaAction() > 0) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isFishing()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.getKarma() > 0) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isInOlympiadMode()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isInHideoutSiege()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isInSiege()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isMounted() || partner.isFlyingMounted() || partner.isInBoat() || partner.isInAirShip()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isTeleporting()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isTransformed()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(
                        SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION
                     );
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (partner.isAlikeDead()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                  } else if (requester.isAllSkillsDisabled() || partner.isAllSkillsDisabled()) {
                     this.sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
                  } else if (requester.isInFightEvent() && !requester.getFightEvent().isFriend(requester, (Creature)target)) {
                     requester.sendMessage("You cannot request couple action while player is your enemy!");
                  } else {
                     requester.setMultiSocialAction(id, partner.getObjectId());
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1);
                     sm.addPcName(partner);
                     this.sendPacket(sm);
                     if (requester.getAI().getIntention() != CtrlIntention.IDLE || partner.getAI().getIntention() != CtrlIntention.IDLE) {
                        NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVING, new NextAction.NextActionCallback() {
                           @Override
                           public void doWork() {
                              partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
                           }
                        });
                        requester.getAI().setNextAction(nextAction);
                     } else if (!requester.isCastingNow() && !requester.isCastingSimultaneouslyNow()) {
                        partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
                     } else {
                        NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.CAST, new NextAction.NextActionCallback() {
                           @Override
                           public void doWork() {
                              partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
                           }
                        });
                        requester.getAI().setNextAction(nextAction);
                     }
                  }
               }
            }
         } else {
            this.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return this._actionId != 10 && this._actionId != 28;
   }
}
