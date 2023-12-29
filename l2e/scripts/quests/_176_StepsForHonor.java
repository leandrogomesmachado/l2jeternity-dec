package l2e.scripts.quests;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _176_StepsForHonor extends Quest {
   private static final String qn = "_176_StepsForHonor";
   public static final int RAPIDUS = 36479;

   public _176_StepsForHonor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(36479);
      this.addTalkId(36479);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_176_StepsForHonor");
      if (st == null) {
         return htmltext;
      } else {
         int count = st.getInt("count");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 80) {
                  htmltext = "36479-1.htm";
               } else {
                  htmltext = "36479-low.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(st.getInt("cond")) {
                  case 1:
                     htmltext = "36479-1.htm";
                     if (count >= 9) {
                        st.set("count", String.valueOf(0));
                        htmltext = "36479-2.htm";
                        st.set("cond", "3");
                     } else {
                        htmltext = "36479-1fail.htm";
                     }

                     return htmltext;
                  case 2:
                  case 4:
                  case 6:
                  default:
                     return htmltext;
                  case 3:
                     htmltext = "36479-2.htm";
                     if (count >= 18) {
                        st.set("count", String.valueOf(0));
                        htmltext = "36479-3.htm";
                        st.set("cond", "5");
                     } else {
                        htmltext = "36479-2fail.htm";
                     }

                     return htmltext;
                  case 5:
                     htmltext = "36479-3.htm";
                     if (count >= 27) {
                        st.set("count", String.valueOf(0));
                        htmltext = "36479-4.htm";
                        st.set("cond", "7");
                     } else {
                        htmltext = "36479-3fail.htm";
                     }

                     return htmltext;
                  case 7:
                     htmltext = "36479-4.htm";
                     if (count < 36) {
                        htmltext = "36479-4fail.htm";
                     }

                     return htmltext;
                  case 8:
                     st.set("count", String.valueOf(0));
                     htmltext = "36479-end.htm";
                     st.giveItems(14603, 1L);
                     st.setState((byte)2);
                     st.playSound("ItemSound.quest_finish");
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onDeath(Creature killer, Creature victim, QuestState st) {
      if (!killer.isPlayer() && !victim.isPlayer()) {
         return "";
      } else if (victim.getLevel() < 61) {
         return "";
      } else {
         Player killerPl = (Player)killer;
         Player victimPl = (Player)victim;
         if (victimPl.getSiegeSide() == killerPl.getSiegeSide()) {
            return "";
         } else {
            int count = st.getInt("count");
            switch(st.getInt("cond")) {
               case 1:
                  if (count < 9) {
                     st.set("count", String.valueOf(++count));
                     if (count == 9) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "2");
                     }
                  }
               case 2:
               case 4:
               case 6:
               default:
                  break;
               case 3:
                  if (count < 18) {
                     st.set("count", String.valueOf(++count));
                     if (count == 18) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "4");
                     }
                  }
                  break;
               case 5:
                  if (count < 27) {
                     st.set("count", String.valueOf(++count));
                     if (count == 27) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "6");
                     }
                  }
                  break;
               case 7:
                  if (count < 36) {
                     st.set("count", String.valueOf(++count));
                     if (count == 36) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "8");
                     }
                  }
            }

            return "";
         }
      }
   }

   public static void main(String[] args) {
      new _176_StepsForHonor(176, "_176_StepsForHonor", "");
   }
}
