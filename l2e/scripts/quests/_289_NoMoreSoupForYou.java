package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _289_NoMoreSoupForYou extends Quest {
   public static final String qn = "_289_NoMoreSoupForYou";
   public static final int STAN = 30200;
   public static final int RATE = 5;
   public static final int SOUP = 15712;
   private static final int[] MOBS = new int[]{18908, 22779, 22786, 22787, 22788};
   private static final int[][] WEAPONS = new int[][]{{10377, 1}, {10401, 1}, {10401, 2}, {10401, 3}, {10401, 4}, {10401, 5}, {10401, 6}};
   private static final int[][] ARMORS = new int[][]{
      {15812, 1},
      {15813, 1},
      {15814, 1},
      {15791, 1},
      {15787, 1},
      {15784, 1},
      {15781, 1},
      {15778, 1},
      {15775, 1},
      {15774, 5},
      {15773, 5},
      {15772, 5},
      {15693, 5},
      {15657, 5},
      {15654, 5},
      {15651, 5},
      {15648, 5},
      {15645, 5}
   };

   public _289_NoMoreSoupForYou(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30200);
      this.addTalkId(30200);

      for(int i : MOBS) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_289_NoMoreSoupForYou");
      if (st == null) {
         return event;
      } else {
         int b = getRandom(18);
         int c = getRandom(7);
         if (event.equalsIgnoreCase("30200-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30200-05.htm")) {
            if (st.getQuestItemsCount(15712) >= 500L) {
               st.giveItems(WEAPONS[c][0], (long)WEAPONS[c][1]);
               st.takeItems(15712, 500L);
               st.playSound("ItemSound.quest_accept");
               htmltext = "30200-04.htm";
            } else {
               htmltext = "30200-07.htm";
            }
         } else if (event.equalsIgnoreCase("30200-06.htm")) {
            if (st.getQuestItemsCount(15712) >= 100L) {
               st.giveItems(ARMORS[b][0], (long)ARMORS[b][1]);
               st.takeItems(15712, 100L);
               st.playSound("ItemSound.quest_accept");
               htmltext = "30200-04.htm";
            } else {
               htmltext = "30200-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_289_NoMoreSoupForYou");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30200) {
            switch(st.getState()) {
               case 0:
                  QuestState PREV = player.getQuestState("_252_ItSmellsDelicious");
                  if (PREV != null && PREV.getState() == 2 && player.getLevel() >= 82) {
                     htmltext = "30200-01.htm";
                  } else {
                     htmltext = "30200-00.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     if (st.getQuestItemsCount(15712) >= 100L) {
                        htmltext = "30200-04.htm";
                     } else {
                        htmltext = "30200-03.htm";
                     }
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_289_NoMoreSoupForYou");
      if (st != null && st.getState() == 1) {
         int npcId = npc.getId();
         if (Util.contains(MOBS, npcId)) {
            st.giveItems(15712, 5L);
            st.playSound("ItemSound.quest_itemget");
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _289_NoMoreSoupForYou(289, "_289_NoMoreSoupForYou", "");
   }
}
