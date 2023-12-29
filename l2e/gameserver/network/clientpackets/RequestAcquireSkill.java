package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.SquadTrainer;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.FishermanInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.instance.VillageMasterInstance;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillDone;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAcquireSkill extends GameClientPacket {
   private static final String[] QUEST_VAR_NAMES = new String[]{"EmergentAbility65-", "EmergentAbility70-", "ClassAbility75-", "ClassAbility80-"};
   private int _id;
   private int _level;
   private AcquireSkillType _skillType;
   private int _subType;

   @Override
   protected void readImpl() {
      this._id = this.readD();
      this._level = this.readD();
      this._skillType = AcquireSkillType.getAcquireSkillType(this.readD());
      if (this._skillType == AcquireSkillType.SUBPLEDGE) {
         this._subType = this.readD();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._level >= 1 && this._level <= Integer.MAX_VALUE && this._id >= 1 && this._id <= Integer.MAX_VALUE) {
            Npc trainer = activeChar.getLastFolkNPC();
            if (trainer instanceof NpcInstance) {
               if (trainer.canInteract(activeChar) || activeChar.isGM()) {
                  if (activeChar.getWeightPenalty() < 3 && activeChar.isInventoryUnder90(true)) {
                     Skill skill = SkillsParser.getInstance().getInfo(this._id, this._level);
                     if (skill == null) {
                        _log.warning(
                           RequestAcquireSkill.class.getSimpleName()
                              + ": Player "
                              + activeChar.getName()
                              + " is trying to learn a null skill Id: "
                              + this._id
                              + " level: "
                              + this._level
                              + "!"
                        );
                     } else {
                        int prevSkillLevel = activeChar.getSkillLevel(this._id);
                        if (prevSkillLevel > 0 && this._skillType != AcquireSkillType.TRANSFER && this._skillType != AcquireSkillType.SUBPLEDGE) {
                           if (prevSkillLevel == this._level) {
                              _log.warning(
                                 "Player "
                                    + activeChar.getName()
                                    + " is trying to learn a skill that already knows, Id: "
                                    + this._id
                                    + " level: "
                                    + this._level
                                    + "!"
                              );
                              return;
                           }

                           if (prevSkillLevel != this._level - 1) {
                              activeChar.sendPacket(SystemMessageId.PREVIOUS_LEVEL_SKILL_NOT_LEARNED);
                              Util.handleIllegalPlayerAction(
                                 activeChar,
                                 ""
                                    + activeChar.getName()
                                    + " is requesting skill Id: "
                                    + this._id
                                    + " level "
                                    + this._level
                                    + " without knowing it's previous level!"
                              );
                              return;
                           }
                        }

                        SkillLearn s = SkillTreesParser.getInstance().getSkillLearn(this._skillType, this._id, this._level, activeChar);
                        if (s != null) {
                           switch(this._skillType) {
                              case CLASS:
                                 if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                    this.giveSkill(activeChar, trainer, skill);
                                 }
                                 break;
                              case TRANSFORM:
                                 if (!canTransform(activeChar)) {
                                    activeChar.sendPacket(SystemMessageId.NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION);
                                    Util.handleIllegalPlayerAction(
                                       activeChar,
                                       ""
                                          + activeChar.getName()
                                          + " is requesting skill Id: "
                                          + this._id
                                          + " level "
                                          + this._level
                                          + " without required quests!"
                                    );
                                    return;
                                 }

                                 if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                    this.giveSkill(activeChar, trainer, skill);
                                 }
                                 break;
                              case FISHING:
                                 if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                    this.giveSkill(activeChar, trainer, skill);
                                 }
                                 break;
                              case PLEDGE:
                                 if (!activeChar.isClanLeader()) {
                                    return;
                                 }

                                 Clan clan = activeChar.getClan();
                                 int repCost = s.getLevelUpSp();
                                 if (clan.getReputationScore() >= repCost) {
                                    if (Config.LIFE_CRYSTAL_NEEDED) {
                                       for(ItemHolder item : s.getRequiredItems()) {
                                          if (!activeChar.destroyItemByItemId("Consume", item.getId(), item.getCount(), trainer, false)) {
                                             activeChar.sendPacket(SystemMessageId.ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL);
                                             VillageMasterInstance.showPledgeSkillList(activeChar);
                                             return;
                                          }

                                          SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                                          sm.addItemName(item.getId());
                                          sm.addItemNumber(item.getCount());
                                          activeChar.sendPacket(sm);
                                       }
                                    }

                                    clan.takeReputationScore(repCost, true);
                                    SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                                    cr.addNumber(repCost);
                                    activeChar.sendPacket(cr);
                                    clan.addNewSkill(skill);
                                    clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
                                    activeChar.sendPacket(new AcquireSkillDone());
                                    VillageMasterInstance.showPledgeSkillList(activeChar);
                                 } else {
                                    activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
                                    VillageMasterInstance.showPledgeSkillList(activeChar);
                                 }
                                 break;
                              case SUBPLEDGE:
                                 if (!activeChar.isClanLeader()) {
                                    return;
                                 }

                                 Clan clan = activeChar.getClan();
                                 if (clan.getFortId() == 0 && clan.getCastleId() == 0) {
                                    return;
                                 }

                                 if (trainer instanceof SquadTrainer) {
                                    if (!clan.isLearnableSubPledgeSkill(skill, this._subType)) {
                                       activeChar.sendPacket(SystemMessageId.SQUAD_SKILL_ALREADY_ACQUIRED);
                                       Util.handleIllegalPlayerAction(
                                          activeChar,
                                          ""
                                             + activeChar.getName()
                                             + " is requesting skill Id: "
                                             + this._id
                                             + " level "
                                             + this._level
                                             + " without knowing it's previous level!"
                                       );
                                       return;
                                    }

                                    int rep = s.getLevelUpSp();
                                    if (clan.getReputationScore() < rep) {
                                       activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
                                       return;
                                    }

                                    for(ItemHolder item : s.getRequiredItems()) {
                                       if (!activeChar.destroyItemByItemId("SubSkills", item.getId(), item.getCount(), trainer, false)) {
                                          activeChar.sendPacket(SystemMessageId.ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL);
                                          return;
                                       }

                                       SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                                       sm.addItemName(item.getId());
                                       sm.addItemNumber(item.getCount());
                                       activeChar.sendPacket(sm);
                                    }

                                    if (rep > 0) {
                                       clan.takeReputationScore(rep, true);
                                       SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                                       cr.addNumber(rep);
                                       activeChar.sendPacket(cr);
                                    }

                                    clan.addNewSkill(skill, this._subType);
                                    clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
                                    activeChar.sendPacket(new AcquireSkillDone());
                                    ((SquadTrainer)trainer).showSubUnitSkillList(activeChar);
                                 }
                                 break;
                              case TRANSFER:
                                 if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                    this.giveSkill(activeChar, trainer, skill);
                                 }
                                 break;
                              case SUBCLASS:
                                 if (activeChar.isSubClassActive()) {
                                    activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
                                    Util.handleIllegalPlayerAction(
                                       activeChar,
                                       ""
                                          + activeChar.getName()
                                          + " is requesting skill Id: "
                                          + this._id
                                          + " level "
                                          + this._level
                                          + " while Sub-Class is active!"
                                    );
                                    return;
                                 }

                                 QuestState st = activeChar.getQuestState("SubClassSkills");
                                 if (st == null) {
                                    Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest("SubClassSkills");
                                    if (subClassSkilllsQuest == null) {
                                       _log.warning(
                                          "Null SubClassSkills quest, for Sub-Class skill Id: "
                                             + this._id
                                             + " level: "
                                             + this._level
                                             + " for player "
                                             + activeChar.getName()
                                             + "!"
                                       );
                                       return;
                                    }

                                    st = subClassSkilllsQuest.newQuestState(activeChar);
                                 }

                                 for(String varName : QUEST_VAR_NAMES) {
                                    for(int i = 1; i <= Config.MAX_SUBCLASS; ++i) {
                                       String itemOID = st.getGlobalQuestVar(varName + i);
                                       if (!itemOID.isEmpty() && !itemOID.endsWith(";") && !itemOID.equals("0")) {
                                          if (Util.isDigit(itemOID)) {
                                             int itemObjId = Integer.parseInt(itemOID);
                                             ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjId);
                                             if (item != null) {
                                                for(ItemHolder itemIdCount : s.getRequiredItems()) {
                                                   if (item.getId() == itemIdCount.getId()) {
                                                      if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                                         this.giveSkill(activeChar, trainer, skill);
                                                         st.saveGlobalQuestVar(varName + i, skill.getId() + ";");
                                                      }

                                                      return;
                                                   }
                                                }
                                             } else {
                                                _log.warning(
                                                   "Inexistent item for object Id "
                                                      + itemObjId
                                                      + ", for Sub-Class skill Id: "
                                                      + this._id
                                                      + " level: "
                                                      + this._level
                                                      + " for player "
                                                      + activeChar.getName()
                                                      + "!"
                                                );
                                             }
                                          } else {
                                             _log.warning(
                                                "Invalid item object Id "
                                                   + itemOID
                                                   + ", for Sub-Class skill Id: "
                                                   + this._id
                                                   + " level: "
                                                   + this._level
                                                   + " for player "
                                                   + activeChar.getName()
                                                   + "!"
                                             );
                                          }
                                       }
                                    }
                                 }

                                 activeChar.sendPacket(SystemMessageId.ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL);
                                 this.showSkillList(trainer, activeChar);
                                 break;
                              case COLLECT:
                                 if (this.checkPlayerSkill(activeChar, trainer, s)) {
                                    this.giveSkill(activeChar, trainer, skill);
                                 }
                                 break;
                              default:
                                 _log.warning("Recived Wrong Packet Data in Aquired Skill, unknown skill type:" + this._skillType);
                           }
                        }
                     }
                  } else {
                     activeChar.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                  }
               }
            }
         } else {
            Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " sent wrong Packet Data in Aquired Skill");
         }
      }
   }

   private boolean checkPlayerSkill(Player player, Npc trainer, SkillLearn s) {
      if (s == null || s.getId() != this._id || s.getLvl() != this._level) {
         return false;
      } else if (s.getGetLevel() > player.getLevel()) {
         player.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
         Util.handleIllegalPlayerAction(
            player,
            ""
               + player.getName()
               + ", level "
               + player.getLevel()
               + " is requesting skill Id: "
               + this._id
               + " level "
               + this._level
               + " without having minimum required level, "
               + s.getGetLevel()
               + "!"
         );
         return false;
      } else {
         int levelUpSp = s.getCalculatedLevelUpSp(player.getClassId(), player.getLearningClass());
         if (levelUpSp > 0 && levelUpSp > player.getSp()) {
            player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
            this.showSkillList(trainer, player);
            return false;
         } else if (!Config.DIVINE_SP_BOOK_NEEDED && this._id == 1405) {
            return true;
         } else {
            if (!s.getPreReqSkills().isEmpty()) {
               for(SkillHolder skill : s.getPreReqSkills()) {
                  if (player.getSkillLevel(skill.getId()) != skill.getLvl()) {
                     if (skill.getId() == 617) {
                        player.sendPacket(SystemMessageId.YOU_MUST_LEARN_ONYX_BEAST_SKILL);
                     } else {
                        player.sendPacket(SystemMessageId.ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL);
                     }

                     return false;
                  }
               }
            }

            if (!s.getRequiredItems().isEmpty()) {
               long reqItemCount = 0L;

               for(ItemHolder item : s.getRequiredItems()) {
                  reqItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
                  if (reqItemCount < item.getCount()) {
                     player.sendPacket(SystemMessageId.ITEM_OR_PREREQUISITES_MISSING_TO_LEARN_SKILL);
                     this.showSkillList(trainer, player);
                     return false;
                  }
               }

               for(ItemHolder itemIdCount : s.getRequiredItems()) {
                  if (!player.destroyItemByItemId("SkillLearn", itemIdCount.getId(), itemIdCount.getCount(), trainer, true)) {
                     Util.handleIllegalPlayerAction(
                        player,
                        ""
                           + player.getName()
                           + ", level "
                           + player.getLevel()
                           + " lose required item Id: "
                           + itemIdCount.getId()
                           + " to learn skill while learning skill Id: "
                           + this._id
                           + " level "
                           + this._level
                           + "!"
                     );
                  }
               }
            }

            if (levelUpSp > 0) {
               player.setSp(player.getSp() - levelUpSp);
               StatusUpdate su = new StatusUpdate(player);
               su.addAttribute(13, player.getSp());
               player.sendPacket(su);
            }

            return true;
         }
      }
   }

   private void giveSkill(Player player, Npc trainer, Skill skill) {
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
      sm.addSkillName(skill);
      player.sendPacket(sm);
      player.sendPacket(new AcquireSkillDone());
      player.addSkill(skill, true);
      player.sendSkillList(false);
      player.updateShortCuts(this._id, this._level);
      this.showSkillList(trainer, player);
      if (this._id >= 1368 && this._id <= 1372) {
         player.sendPacket(new ExStorageMaxCount(player));
      }

      if (trainer.getTemplate().getEventQuests().containsKey(Quest.QuestEventType.ON_SKILL_LEARN)) {
         for(Quest quest : trainer.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_LEARN)) {
            quest.notifyAcquireSkill(trainer, player, skill, this._skillType);
         }
      }
   }

   private void showSkillList(Npc trainer, Player player) {
      if (this._skillType != AcquireSkillType.TRANSFORM && this._skillType != AcquireSkillType.SUBCLASS && this._skillType != AcquireSkillType.TRANSFER) {
         if (trainer instanceof FishermanInstance) {
            FishermanInstance.showFishSkillList(player);
         } else {
            NpcInstance.showSkillList(player, trainer, player.getLearningClass());
         }
      }
   }

   public static boolean canTransform(Player player) {
      if (Config.ALLOW_TRANSFORM_WITHOUT_QUEST) {
         return true;
      } else {
         QuestState st = player.getQuestState("_136_MoreThanMeetsTheEye");
         return st != null && st.isCompleted();
      }
   }
}
