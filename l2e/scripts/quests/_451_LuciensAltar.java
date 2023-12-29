package l2e.scripts.quests;

import java.util.Calendar;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _451_LuciensAltar extends Quest {
   private static final String qn = "_451_LuciensAltar";
   private static final int DAICHIR = 30537;
   private static final int REPLENISHED_BEAD = 14877;
   private static final int DISCHARGED_BEAD = 14878;
   private static final int[][] ALTARS = new int[][]{{32706, 1}, {32707, 2}, {32708, 4}, {32709, 8}, {32710, 16}};
   private static final int RESET_HOUR = 6;
   private static final int RESET_MIN = 30;

   public _451_LuciensAltar(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30537);
      this.addTalkId(30537);

      for(int[] i : ALTARS) {
         this.addTalkId(i[0]);
      }

      this.questItemIds = new int[]{14877, 14878};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_451_LuciensAltar");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30537-03.htm")) {
            st.set("cond", "1");
            st.set("altars_state", "0");
            st.setState((byte)1);
            st.giveItems(14877, 5L);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_451_LuciensAltar");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30537) {
            if (st.getInt("cond") == 0) {
               String reset = st.get("reset");
               long remain = 0L;
               if (reset != null && this.isDigit(reset)) {
                  remain = Long.parseLong(reset) - System.currentTimeMillis();
               }

               if (remain <= 0L) {
                  if (player.getLevel() >= 80) {
                     htmltext = "30537-01.htm";
                  } else {
                     htmltext = "30537-00.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30537-06.htm";
               }
            } else if (st.getInt("cond") == 1) {
               if (st.getQuestItemsCount(14878) >= 1L) {
                  htmltext = "30537-04a.htm";
               } else {
                  htmltext = "30537-04.htm";
               }
            } else if (st.getInt("cond") == 2) {
               htmltext = "30537-05.htm";
               st.giveItems(57, 127690L);
               st.takeItems(14878, 5L);
               st.setState((byte)2);
               st.unset("cond");
               st.unset("altars_state");
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               Calendar reset = Calendar.getInstance();
               reset.set(12, 30);
               if (reset.get(11) >= 6) {
                  reset.add(5, 1);
               }

               reset.set(11, 6);
               st.set("reset", String.valueOf(reset.getTimeInMillis()));
            }
         } else if (st.getInt("cond") == 1) {
            int idx = 0;

            for(int[] i : ALTARS) {
               if (i[0] == npc.getId()) {
                  idx = i[1];
                  break;
               }
            }

            if (idx != 0) {
               int state = st.getInt("altars_state");
               if ((state & idx) == 0) {
                  st.set("altars_state", String.valueOf(state | idx));
                  st.takeItems(14877, 1L);
                  st.giveItems(14878, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  if (st.getQuestItemsCount(14878) == 5L) {
                     st.set("cond", "2");
                     st.playSound("ItemSound.quest_middle");
                  }

                  htmltext = "recharge.htm";
               } else {
                  htmltext = "findother.htm";
               }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _451_LuciensAltar(451, "_451_LuciensAltar", "");
   }
}
