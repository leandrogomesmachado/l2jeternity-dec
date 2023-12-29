package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _124_MeetingTheElroki extends Quest {
   private static final String qn = "_124_MeetingTheElroki";
   private final int MARQUEZ = 32113;
   private final int MUSHIKA = 32114;
   private final int ASAMAH = 32115;
   private final int KARAKAWEI = 32117;
   private final int MANTARASA = 32118;
   private final int MUSHIKA_EGG = 8778;

   public _124_MeetingTheElroki(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32113);
      this.addTalkId(32113);
      this.addTalkId(32114);
      this.addTalkId(32115);
      this.addTalkId(32117);
      this.addTalkId(32118);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_124_MeetingTheElroki");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("32113-02.htm")) {
            st.setState((byte)1);
         } else if (event.equalsIgnoreCase("32113-03.htm")) {
            if (cond == 0) {
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (event.equalsIgnoreCase("32113-04.htm")) {
            if (cond == 1) {
               st.set("cond", "2");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            }
         } else if (event.equalsIgnoreCase("32114-02.htm")) {
            if (cond == 2) {
               st.set("cond", "3");
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (event.equalsIgnoreCase("32115-04.htm")) {
            if (cond == 3) {
               st.set("cond", "4");
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (event.equalsIgnoreCase("32117-02.htm")) {
            if (cond == 4) {
               st.set("progress", "1");
            }
         } else if (event.equalsIgnoreCase("32117-03.htm")) {
            if (cond == 4) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (event.equalsIgnoreCase("32118-02.htm")) {
            st.giveItems(8778, 1L);
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_124_MeetingTheElroki");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 32113) {
                  if (player.getLevel() < 75) {
                     htmltext = "32113-01a.htm";
                     st.exitQuest(false);
                  } else {
                     htmltext = "32113-01.htm";
                  }
               }
               break;
            case 1:
               if (npcId == 32113) {
                  if (cond == 1) {
                     htmltext = "32113-03.htm";
                  } else if (cond == 2) {
                     htmltext = "32113-04a.htm";
                  }
               } else if (npcId == 32114) {
                  if (cond == 2) {
                     htmltext = "32114-01.htm";
                  }
               } else if (npcId == 32115) {
                  if (cond == 3) {
                     htmltext = "32115-01.htm";
                  } else if (cond == 6) {
                     htmltext = "32115-05.htm";
                     st.takeItems(8778, 1L);
                     st.giveItems(57, 100013L);
                     st.unset("cond");
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
               } else if (npcId == 32117) {
                  if (cond == 4) {
                     htmltext = "32117-01.htm";
                     if (st.getInt("progress") == 1) {
                        htmltext = "32117-02.htm";
                     }
                  } else if (cond == 5) {
                     htmltext = "32117-04.htm";
                  }
               } else if (npcId == 32118 && cond == 5) {
                  htmltext = "32118-01.htm";
               }
               break;
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _124_MeetingTheElroki(124, "_124_MeetingTheElroki", "");
   }
}
