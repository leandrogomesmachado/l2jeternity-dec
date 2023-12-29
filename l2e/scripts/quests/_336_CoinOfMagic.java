package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _336_CoinOfMagic extends Quest {
   private static final int[][] PROMOTE = new int[][]{new int[0], new int[0], {3492, 3474, 3476, 3495, 3484, 3486}, {3473, 3485, 3491, 3475, 3483, 3494}};
   private static final int[][] EXCHANGE_LEVEL = new int[][]{
      {30696, 3}, {30673, 3}, {30183, 3}, {30165, 2}, {30200, 2}, {30688, 2}, {30847, 1}, {30092, 1}, {30078, 1}
   };
   private static final int[][] DROPLIST = new int[][]{
      {20584, 3472},
      {20585, 3472},
      {20587, 3472},
      {20604, 3472},
      {20678, 3472},
      {20583, 3482},
      {20663, 3482},
      {20235, 3482},
      {20146, 3482},
      {20240, 3482},
      {20245, 3482},
      {20568, 3490},
      {20569, 3490},
      {20685, 3490},
      {20572, 3490},
      {20161, 3490},
      {20575, 3490},
      {21003, 3472},
      {21006, 3482},
      {21008, 3472},
      {20674, 3482},
      {21282, 3472},
      {21284, 3472},
      {21283, 3472},
      {21287, 3482},
      {21288, 3482},
      {21286, 3482},
      {21521, 3490},
      {21526, 3472},
      {21531, 3472},
      {21539, 3490}
   };

   public _336_CoinOfMagic(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30232);
      this.addTalkId(new int[]{30232, 30702, 30696, 30183, 30200, 30165, 30847, 30092, 30078, 30688, 30673});

      for(int[] mob : DROPLIST) {
         this.addKillId(mob[0]);
      }

      this.addKillId(new int[]{20644, 20645});
      this.questItemIds = new int[]{3811, 3812, 3813, 3814, 3815};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("30702-06.htm")) {
            if (cond < 7) {
               st.setCond(7, true);
            }
         } else if (event.equalsIgnoreCase("30232-22.htm")) {
            if (cond < 6) {
               st.setCond(6, true);
            }
         } else if (event.equalsIgnoreCase("30232-23.htm")) {
            if (cond < 5) {
               st.setCond(5, true);
            }
         } else if (event.equalsIgnoreCase("30702-02.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30232-05.htm")) {
            st.startQuest();
            st.giveItems(3811, 1L);
         } else if (event.equalsIgnoreCase("30232-04.htm") || event.equalsIgnoreCase("30232-18a.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         } else if (event.equalsIgnoreCase("raise")) {
            htmltext = this.promote(st);
         }

         return htmltext;
      }
   }

   private String promote(QuestState st) {
      int grade = st.getInt("grade");
      String html;
      if (grade == 1) {
         html = "30232-15.htm";
      } else {
         int h = 0;

         for(int i : PROMOTE[grade]) {
            if (st.getQuestItemsCount(i) > 0L) {
               ++h;
            }
         }

         if (h == 6) {
            for(int i : PROMOTE[grade]) {
               st.takeItems(i, 1L);
            }

            html = "30232-" + String.valueOf(19 - grade) + ".htm";
            st.takeItems(3812 + grade, -1L);
            st.giveItems(3811 + grade, 1L);
            st.set("grade", String.valueOf(grade - 1));
            if (grade == 3) {
               st.setCond(9, true);
            } else if (grade == 2) {
               st.setCond(11, true);
            }
         } else {
            html = "30232-" + String.valueOf(16 - grade) + ".htm";
            if (grade == 3) {
               st.setCond(8, true);
            } else if (grade == 2) {
               st.setCond(9, true);
            }
         }
      }

      return html;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte id = st.getState();
         int grade = st.getInt("grade");
         if (npcId == 30232) {
            if (id == 0) {
               if (st.getPlayer().getLevel() < 40) {
                  htmltext = "30232-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30232-02.htm";
               }
            } else if (st.getQuestItemsCount(3811) > 0L) {
               if (st.getQuestItemsCount(3812) > 0L) {
                  st.takeItems(3812, -1L);
                  st.takeItems(3811, -1L);
                  st.giveItems(3815, 1L);
                  st.set("grade", "3");
                  st.setCond(4);
                  st.playSound("ItemSound.quest_fanfare_middle");
                  htmltext = "30232-07.htm";
               } else {
                  htmltext = "30232-06.htm";
               }
            } else if (grade == 3) {
               htmltext = "30232-12.htm";
            } else if (grade == 2) {
               htmltext = "30232-11.htm";
            } else if (grade == 1) {
               htmltext = "30232-10.htm";
            }
         } else if (npcId == 30702) {
            if (st.getQuestItemsCount(3811) > 0L && grade == 0) {
               htmltext = "30702-01.htm";
            } else if (grade == 3) {
               htmltext = "30702-05.htm";
            }
         } else {
            for(int[] e : EXCHANGE_LEVEL) {
               if (npcId == e[0] && grade <= e[1]) {
                  htmltext = npcId + "-01.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      switch(npc.getId()) {
         case 20644:
         case 20645:
            Player member = this.getRandomPartyMember(player, 2);
            if (member != null) {
               QuestState st = member.getQuestState(this.getName());
               if (st != null && st.calcDropItems(this.getId(), 3812, npc.getId(), 1)) {
                  st.setCond(3, true);
               }
            }

            return super.onKill(npc, player, isSummon);
         default:
            Player member = this.getRandomPartyMemberState(player, (byte)1);
            if (member != null) {
               QuestState st = member.getQuestState(this.getName());
               if (st != null) {
                  for(int[] info : DROPLIST) {
                     if (info[0] == npc.getId()) {
                        st.calcDropItems(this.getId(), info[1], npc.getId(), Integer.MAX_VALUE);
                        break;
                     }
                  }
               }
            }

            return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _336_CoinOfMagic(336, _336_CoinOfMagic.class.getSimpleName(), "");
   }
}
