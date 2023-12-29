package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _160_NerupasRequest extends Quest {
   private static final String qn = "_160_NerupasRequest";
   private static final int NERUPA = 30370;
   private static final int UNOREN = 30147;
   private static final int CREAMEES = 30149;
   private static final int JULIA = 30152;
   private static final int SILVERY_SPIDERSILK = 1026;
   private static final int UNOS_RECEIPT = 1027;
   private static final int CELS_TICKET = 1028;
   private static final int NIGHTSHADE_LEAF = 1029;
   private static final int LESSER_HEALING_POTION = 1060;

   public _160_NerupasRequest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30370);
      this.addTalkId(30370);
      this.addTalkId(30147);
      this.addTalkId(30149);
      this.addTalkId(30152);
      this.questItemIds = new int[]{1026, 1027, 1028, 1029};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_160_NerupasRequest");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30370-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1026, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_160_NerupasRequest");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 1) {
                  if (player.getLevel() >= 3 && player.getLevel() <= 7) {
                     htmltext = "30370-03.htm";
                  } else {
                     htmltext = "30370-02.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30370-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30147:
                     if (cond == 1) {
                        st.set("cond", "2");
                        htmltext = "30147-01.htm";
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(1026, 1L);
                        st.giveItems(1027, 1L);
                        return htmltext;
                     } else {
                        if (cond == 2) {
                           htmltext = "30147-02.htm";
                        } else if (cond == 4) {
                           htmltext = "30147-03.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30149:
                     if (cond == 2) {
                        st.set("cond", "3");
                        htmltext = "30149-01.htm";
                        st.takeItems(1027, 1L);
                        st.giveItems(1028, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return htmltext;
                     } else {
                        if (cond == 3) {
                           htmltext = "30149-02.htm";
                        } else if (cond == 4) {
                           htmltext = "30149-03.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30152:
                     if (cond == 3) {
                        st.set("cond", "4");
                        htmltext = "30152-01.htm";
                        st.takeItems(1028, -1L);
                        st.giveItems(1029, 1L);
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond == 4) {
                        return "30152-02.htm";
                     }

                     return htmltext;
                  case 30370:
                     if (cond >= 1 && cond <= 3) {
                        htmltext = "30370-05.htm";
                     } else if (cond == 4 && st.getQuestItemsCount(1029) == 1L) {
                        htmltext = "30370-06.htm";
                        st.playSound("ItemSound.quest_finish");
                        st.takeItems(1029, 1L);
                        st.rewardItems(1060, 5L);
                        st.addExpAndSp(1000, 0);
                        st.unset("cond");
                        st.exitQuest(false);
                        return htmltext;
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
      new _160_NerupasRequest(160, "_160_NerupasRequest", "");
   }
}
