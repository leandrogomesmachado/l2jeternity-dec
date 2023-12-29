package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;

public final class SubClassSkills extends Quest {
   private static final int[] _allCertSkillIds = new int[]{
      631,
      632,
      633,
      634,
      637,
      638,
      639,
      640,
      641,
      642,
      643,
      644,
      645,
      646,
      647,
      648,
      650,
      651,
      652,
      653,
      654,
      655,
      656,
      657,
      658,
      659,
      660,
      661,
      662,
      799,
      800,
      801,
      802,
      803,
      804,
      1489,
      1490,
      1491
   };
   private static final int[][] _certSkillsByLevel = new int[][]{
      {631, 632, 633, 634},
      {631, 632, 633, 634},
      {637, 638, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648, 650, 651, 652, 653, 654, 655, 799, 800, 801, 802, 803, 804, 1489, 1490, 1491},
      {656, 657, 658, 659, 660, 661, 662}
   };
   private static final int[] _allCertItemIds = new int[]{
      10280, 10281, 10282, 10283, 10284, 10285, 10286, 10287, 10288, 10289, 10290, 10291, 10292, 10293, 10294, 10612
   };
   private static final int[][] _certItemsByLevel = new int[][]{
      {10280}, {10280}, {10612, 10281, 10282, 10283, 10284, 10285, 10286, 10287}, {10288, 10289, 10290, 10291, 10292, 10293, 10294}
   };
   private static final String[] VARS = new String[]{"EmergentAbility65-", "EmergentAbility70-", "ClassAbility75-", "ClassAbility80-"};

   public SubClassSkills(int id, String name, String descr) {
      super(id, name, descr);
      this.setOnEnterWorld(true);
   }

   @Override
   public String onEnterWorld(Player player) {
      if (!Config.SKILL_CHECK_ENABLE) {
         return null;
      } else if (player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && !Config.SKILL_CHECK_GM) {
         return null;
      } else {
         List<Skill> certSkills = this.getCertSkills(player);
         if (player.isSubClassActive()) {
            if (certSkills != null) {
               for(Skill s : certSkills) {
                  Util.handleIllegalPlayerAction(
                     player,
                     ""
                        + player.getName()
                        + " has cert skill on subclass :"
                        + s.getNameEn()
                        + "("
                        + s.getId()
                        + "/"
                        + s.getLevel()
                        + "), class:"
                        + ClassListParser.getInstance().getClass(player.getClassId()).getClassName()
                  );
                  if (Config.SKILL_CHECK_REMOVE) {
                     player.removeSkill(s);
                  }
               }
            }

            return null;
         } else {
            int[][] cSkills = new int[certSkills.size()][2];

            Skill skill;
            for(int i = certSkills.size(); --i >= 0; cSkills[i][1] = skill.getLevel()) {
               skill = certSkills.get(i);
               cSkills[i][0] = skill.getId();
            }

            List<ItemInstance> certItems = this.getCertItems(player);
            int[][] cItems = new int[certItems.size()][2];

            ItemInstance item;
            for(int i = certItems.size(); --i >= 0; cItems[i][1] = (int)Math.min(item.getCount(), 2147483647L)) {
               item = certItems.get(i);
               cItems[i][0] = item.getObjectId();
            }

            QuestState st = player.getQuestState("SubClassSkills");
            if (st == null) {
               st = this.newQuestState(player);
            }

            int i = VARS.length;

            while(--i >= 0) {
               for(int j = Config.MAX_SUBCLASS; j > 0; --j) {
                  String qName = VARS[i] + String.valueOf(j);
                  String qValue = st.getGlobalQuestVar(qName);
                  if (qValue != null && !qValue.isEmpty()) {
                     if (qValue.endsWith(";")) {
                        try {
                           int id = Integer.parseInt(qValue.replace(";", ""));
                           Skill skill = null;
                           if (certSkills == null) {
                              Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - no certified skills found");
                           } else {
                              if (cSkills != null) {
                                 int index = certSkills.size();

                                 while(--index >= 0) {
                                    if (cSkills[index][0] == id) {
                                       skill = certSkills.get(index);
                                       cSkills[index][1]--;
                                       break;
                                    }
                                 }
                              }

                              if (skill != null) {
                                 if (!Util.contains(_certSkillsByLevel[i], id)) {
                                    Util.handleIllegalPlayerAction(
                                       player, "Invalid cert variable WITH skill:" + qName + "=" + qValue + " - skill does not match certificate level"
                                    );
                                 }
                              } else {
                                 Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - skill not found");
                              }
                           }
                        } catch (NumberFormatException var14) {
                           Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - not a number");
                        }
                     } else {
                        try {
                           int id = Integer.parseInt(qValue);
                           if (id != 0) {
                              ItemInstance item = null;
                              if (certItems == null) {
                                 Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - no cert item found in inventory");
                              } else {
                                 if (cItems != null) {
                                    int index = certItems.size();

                                    while(--index >= 0) {
                                       if (cItems[index][0] == id) {
                                          item = certItems.get(index);
                                          cItems[index][1]--;
                                          break;
                                       }
                                    }
                                 }

                                 if (item != null) {
                                    if (!Util.contains(_certItemsByLevel[i], item.getId())) {
                                       Util.handleIllegalPlayerAction(
                                          player, "Invalid cert variable:" + qName + "=" + qValue + " - item found but does not match certificate level"
                                       );
                                    }
                                 } else {
                                    Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - item not found");
                                 }
                              }
                           }
                        } catch (NumberFormatException var15) {
                           Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + "=" + qValue + " - not a number");
                        }
                     }
                  }
               }
            }

            if (certSkills != null && cSkills != null) {
               i = cSkills.length;

               while(--i >= 0) {
                  if (cSkills[i][1] != 0) {
                     Skill skill = certSkills.get(i);
                     if (cSkills[i][1] > 0) {
                        if (cSkills[i][1] == skill.getLevel()) {
                           Util.handleIllegalPlayerAction(
                              player,
                              "" + player.getName() + " has invalid cert skill :" + skill.getNameEn() + "(" + skill.getId() + "/" + skill.getLevel() + ")"
                           );
                        } else {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + " has invalid cert skill :"
                                 + skill.getNameEn()
                                 + "("
                                 + skill.getId()
                                 + "/"
                                 + skill.getLevel()
                                 + "), level too high"
                           );
                        }

                        if (Config.SKILL_CHECK_REMOVE) {
                           player.removeSkill(skill);
                        }
                     } else {
                        Util.handleIllegalPlayerAction(
                           player, "Invalid cert skill :" + skill.getNameEn() + "(" + skill.getId() + "/" + skill.getLevel() + "), level too low"
                        );
                     }
                  }
               }
            }

            if (certItems != null && cItems != null) {
               i = cItems.length;

               while(--i >= 0) {
                  if (cItems[i][1] != 0) {
                     ItemInstance item = certItems.get(i);
                     Util.handleIllegalPlayerAction(player, "Invalid cert item without variable or with wrong count:" + item.getObjectId());
                  }
               }
            }

            return null;
         }
      }
   }

   private List<Skill> getCertSkills(Player player) {
      List<Skill> tmp = new ArrayList<>();

      for(Skill s : player.getAllSkills()) {
         if (s != null && Arrays.binarySearch(_allCertSkillIds, s.getId()) >= 0) {
            tmp.add(s);
         }
      }

      return tmp;
   }

   private List<ItemInstance> getCertItems(Player player) {
      List<ItemInstance> tmp = new ArrayList<>();

      for(ItemInstance i : player.getInventory().getItems()) {
         if (i != null && Arrays.binarySearch(_allCertItemIds, i.getId()) >= 0) {
            tmp.add(i);
         }
      }

      return tmp;
   }

   public static void main(String[] args) {
      new SubClassSkills(-1, "SubClassSkills", "custom");
   }
}
