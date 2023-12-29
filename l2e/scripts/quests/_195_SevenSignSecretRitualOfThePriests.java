package l2e.scripts.quests;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _195_SevenSignSecretRitualOfThePriests extends Quest {
   public _195_SevenSignSecretRitualOfThePriests(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31001);
      this.addTalkId(new int[]{31001, 32576, 30289, 30969, 32579, 32577, 32581});
      this.questItemIds = new int[]{13822, 13823};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (event.equalsIgnoreCase("31001-4.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32576-1.htm")) {
            st.setCond(2, true);
            st.giveItems(13822, 1L);
         } else if (event.equalsIgnoreCase("30289-3.htm")) {
            st.setCond(3, true);
            player.doCast(SkillsParser.getInstance().getInfo(6204, 1));
         } else if (event.equalsIgnoreCase("30289-6.htm")) {
            if (player.isTransformed()) {
               player.untransform();
            }

            player.doCast(SkillsParser.getInstance().getInfo(6204, 1));
         } else if (event.equalsIgnoreCase("30289-7.htm")) {
            if (player.isTransformed()) {
               player.untransform();
            }
         } else if (event.equalsIgnoreCase("30289-10.htm")) {
            if (player.isTransformed()) {
               player.untransform();
            }

            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("32581-3.htm")) {
            ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
            if (world != null) {
               Reflection inst = world.getReflection();
               inst.setDuration(300000);
               inst.setEmptyDestroyTime(0L);
            }

            player.setReflectionId(0);
            player.teleToLocation(-12532, 122329, -2984, true);
         } else if (event.equalsIgnoreCase("30969-2.htm")) {
            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("wrong")) {
            player.teleToLocation(-78240, 205858, -7856, false);
            htmltext = "32577-2.htm";
         } else if (event.equalsIgnoreCase("empty")) {
            return null;
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 31001) {
                  if (player.getLevel() >= 79) {
                     QuestState qs = player.getQuestState("_194_SevenSignContractOfMammon");
                     if (qs != null) {
                        if (qs.isCompleted()) {
                           htmltext = "31001-0.htm";
                        } else {
                           htmltext = "31001-0b.htm";
                        }
                     }
                  } else {
                     htmltext = "31001-0a.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               switch(npcId) {
                  case 30289:
                     switch(cond) {
                        case 2:
                           return "30289-0.htm";
                        case 3:
                           if (player.getInventory().getItemByItemId(13823) != null) {
                              htmltext = "30289-8.htm";
                           } else {
                              htmltext = "30289-5.htm";
                           }

                           return htmltext;
                        case 4:
                           htmltext = "30289-11.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 30969:
                     if (cond == 4) {
                        htmltext = "30969-0.htm";
                     }

                     return htmltext;
                  case 31001:
                     if (cond == 1) {
                        htmltext = "31001-5.htm";
                     }

                     return htmltext;
                  case 32576:
                     switch(cond) {
                        case 1:
                           return "32576-0.htm";
                        case 2:
                           return "32576-2.htm";
                        default:
                           return htmltext;
                     }
                  case 32577:
                     return "32577-0.htm";
                  case 32579:
                     int ref = player.getReflectionId();
                     if (ref != 0) {
                        ReflectionManager.getInstance().destroyReflection(ref);
                     }

                     player.setReflectionId(0);
                     player.teleToLocation(-12532, 122329, -2984, true);
                     return "32579-0.htm";
                  case 32581:
                     htmltext = "32581-0.htm";
                     if (npc.getSpawn().getX() == -81393 && npc.getSpawn().getY() == 205565) {
                        if (player.getInventory().getItemByItemId(13823) == null) {
                           st.giveItems(13823, 1L);
                        }

                        htmltext = "32581-1.htm";
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _195_SevenSignSecretRitualOfThePriests(195, _195_SevenSignSecretRitualOfThePriests.class.getSimpleName(), "");
   }
}
