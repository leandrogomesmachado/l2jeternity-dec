package l2e.scripts.custom;

import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.RequestAcquireSkill;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class AvantGarde extends AbstractNpcAI {
   private static final int AVANT_GARDE = 32323;
   private static final int[] ITEMS = new int[]{
      10280, 10281, 10282, 10283, 10284, 10285, 10286, 10287, 10288, 10289, 10290, 10291, 10292, 10293, 10294, 10612
   };
   private static final String[] QUEST_VAR_NAMES = new String[]{"EmergentAbility65-", "EmergentAbility70-", "ClassAbility75-", "ClassAbility80-"};

   public AvantGarde() {
      super(AvantGarde.class.getSimpleName(), "custom");
      this.addStartNpc(32323);
      this.addTalkId(32323);
      this.addFirstTalkId(32323);
      this.addAcquireSkillId(new int[]{32323});
   }

   @Override
   public String onAcquireSkill(Npc npc, Player player, Skill skill, AcquireSkillType type) {
      switch(type) {
         case TRANSFORM:
            showTransformSkillList(player);
            break;
         case SUBCLASS:
            showSubClassSkillList(player);
      }

      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "32323-02.htm":
         case "32323-02a.htm":
         case "32323-02b.htm":
         case "32323-02c.htm":
         case "32323-05.htm":
         case "32323-05a.htm":
         case "32323-05no.htm":
         case "32323-06.htm":
         case "32323-06no.htm":
            htmltext = event;
            break;
         case "LearnTransformationSkill":
            if (RequestAcquireSkill.canTransform(player)) {
               showTransformSkillList(player);
            } else {
               htmltext = "32323-03.htm";
            }
            break;
         case "BuyTransformationItems":
            if (RequestAcquireSkill.canTransform(player)) {
               MultiSellParser.getInstance().separateAndSend(32323001, player, npc, false);
            } else {
               htmltext = "32323-04.htm";
            }
            break;
         case "LearnSubClassSkill":
            if (!RequestAcquireSkill.canTransform(player)) {
               htmltext = "32323-04.htm";
            }

            if (player.isSubClassActive()) {
               htmltext = "32323-08.htm";
            } else {
               boolean hasItems = false;

               for(int id : ITEMS) {
                  if (player.getInventory().getItemByItemId(id) != null) {
                     hasItems = true;
                     break;
                  }
               }

               if (hasItems) {
                  showSubClassSkillList(player);
               } else {
                  htmltext = "32323-08.htm";
               }
            }
            break;
         case "CancelCertification":
            if (player.getSubClasses().size() == 0) {
               htmltext = "32323-07.htm";
            } else if (player.isSubClassActive()) {
               htmltext = "32323-08.htm";
            } else if (player.getAdena() < (long)Config.FEE_DELETE_SUBCLASS_SKILLS) {
               htmltext = "32323-08no.htm";
            } else {
               QuestState st = player.getQuestState("SubClassSkills");
               if (st == null) {
                  st = QuestManager.getInstance().getQuest("SubClassSkills").newQuestState(player);
               }

               int activeCertifications = 0;

               for(String varName : QUEST_VAR_NAMES) {
                  for(int i = 1; i <= Config.MAX_SUBCLASS; ++i) {
                     String qvar = st.getGlobalQuestVar(varName + i);
                     if (!qvar.isEmpty() && (qvar.endsWith(";") || !qvar.equals("0"))) {
                        ++activeCertifications;
                     }
                  }
               }

               if (activeCertifications == 0) {
                  htmltext = "32323-10no.htm";
               } else {
                  for(String varName : QUEST_VAR_NAMES) {
                     for(int i = 1; i <= Config.MAX_SUBCLASS; ++i) {
                        String qvarName = varName + i;
                        String qvar = st.getGlobalQuestVar(qvarName);
                        if (qvar.endsWith(";")) {
                           String skillIdVar = qvar.replace(";", "");
                           if (Util.isDigit(skillIdVar)) {
                              int skillId = Integer.parseInt(skillIdVar);
                              Skill sk = SkillsParser.getInstance().getInfo(skillId, 1);
                              if (sk != null) {
                                 player.removeSkill(sk);
                                 st.saveGlobalQuestVar(qvarName, "0");
                              }
                           } else {
                              this._log.warning("Invalid Sub-Class Skill Id: " + skillIdVar + " for player " + player.getName() + "!");
                           }
                        } else if (!qvar.isEmpty() && !qvar.equals("0")) {
                           if (Util.isDigit(qvar)) {
                              int itemObjId = Integer.parseInt(qvar);
                              ItemInstance itemInstance = player.getInventory().getItemByObjectId(itemObjId);
                              if (itemInstance != null) {
                                 player.destroyItem("CancelCertification", itemObjId, 1L, player, false);
                              } else {
                                 itemInstance = player.getWarehouse().getItemByObjectId(itemObjId);
                                 if (itemInstance != null) {
                                    this._log.warning("Somehow " + player.getName() + " put a certification book into warehouse!");
                                    player.getWarehouse().destroyItem("CancelCertification", itemInstance, 1L, player, Boolean.valueOf(false));
                                 } else {
                                    this._log.warning("Somehow " + player.getName() + " deleted a certification book!");
                                 }
                              }

                              st.saveGlobalQuestVar(qvarName, "0");
                           } else {
                              this._log.warning("Invalid item object Id: " + qvar + " for player " + player.getName() + "!");
                           }
                        }
                     }
                  }

                  player.reduceAdena("Cleanse", (long)Config.FEE_DELETE_SUBCLASS_SKILLS, npc, true);
                  htmltext = "32323-09no.htm";
                  player.sendSkillList(false);
               }

               for(int itemId : ITEMS) {
                  ItemInstance item = player.getInventory().getItemByItemId(itemId);
                  if (item != null) {
                     this._log
                        .warning(
                           this.getClass().getName()
                              + ": player "
                              + player
                              + " had 'extra' certification skill books while cancelling sub-class certifications!"
                        );
                     player.destroyItem("CancelCertificationExtraBooks", item, npc, false);
                  }
               }
            }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return "32323-01.htm";
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      return "32323-01.htm";
   }

   public static void showSubClassSkillList(Player player) {
      List<SkillLearn> subClassSkills = SkillTreesParser.getInstance().getAvailableSubClassSkills(player);
      AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.SUBCLASS);
      int count = 0;

      for(SkillLearn s : subClassSkills) {
         if (SkillsParser.getInstance().getInfo(s.getId(), s.getLvl()) != null) {
            ++count;
            asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), 0, 0);
         }
      }

      if (count > 0) {
         player.sendPacket(asl);
      } else {
         player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
      }
   }

   public static void showTransformSkillList(Player player) {
      List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableTransformSkills(player);
      AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.TRANSFORM);
      int counts = 0;

      for(SkillLearn s : skills) {
         if (SkillsParser.getInstance().getInfo(s.getId(), s.getLvl()) != null) {
            ++counts;
            asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getLevelUpSp(), 0);
         }
      }

      if (counts == 0) {
         int minlevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getTransformSkillTree());
         if (minlevel > 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
            sm.addNumber(minlevel);
            player.sendPacket(sm);
         } else {
            player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
         }
      } else {
         player.sendPacket(asl);
      }
   }

   public static void main(String[] args) {
      new AvantGarde();
   }
}
