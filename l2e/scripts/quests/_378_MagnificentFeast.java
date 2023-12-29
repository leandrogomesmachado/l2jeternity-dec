package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _378_MagnificentFeast extends Quest {
   private static final String qn = "_378_MagnificentFeast";
   private static final int RANSPO = 30594;
   private static final int WINE_15 = 5956;
   private static final int WINE_30 = 5957;
   private static final int WINE_60 = 5958;
   private static final int MUSICALS_SCORE = 4421;
   private static final int JSALAD_RECIPE = 1455;
   private static final int JSAUCE_RECIPE = 1456;
   private static final int JSTEAK_RECIPE = 1457;
   private static final int RITRON_DESSERT = 5959;
   Map<String, int[]> Reward_list = new HashMap<>();

   public _378_MagnificentFeast(int questId, String name, String descr) {
      super(questId, name, descr);
      this.Reward_list.put("9", new int[]{847, 1, 5700});
      this.Reward_list.put("10", new int[]{846, 2, 0});
      this.Reward_list.put("12", new int[]{909, 1, 25400});
      this.Reward_list.put("17", new int[]{846, 2, 1200});
      this.Reward_list.put("18", new int[]{879, 1, 6900});
      this.Reward_list.put("20", new int[]{890, 2, 8500});
      this.Reward_list.put("33", new int[]{879, 1, 8100});
      this.Reward_list.put("34", new int[]{910, 1, 0});
      this.Reward_list.put("36", new int[]{848, 1, 2200});
      this.addStartNpc(30594);
      this.addTalkId(30594);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_378_MagnificentFeast");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30594-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30594-4a.htm")) {
            if (st.getQuestItemsCount(5956) >= 1L) {
               st.set("cond", "2");
               st.set("score", "1");
               st.takeItems(5956, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30594-4.htm";
            }
         } else if (event.equalsIgnoreCase("30594-4b.htm")) {
            if (st.getQuestItemsCount(5957) >= 1L) {
               st.set("cond", "2");
               st.set("score", "2");
               st.takeItems(5957, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30594-4.htm";
            }
         } else if (event.equalsIgnoreCase("30594-4c.htm")) {
            if (st.getQuestItemsCount(5958) >= 1L) {
               st.set("cond", "2");
               st.set("score", "4");
               st.takeItems(5958, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30594-4.htm";
            }
         } else if (event.equalsIgnoreCase("30594-6.htm")) {
            if (st.getQuestItemsCount(4421) >= 1L) {
               st.set("cond", "3");
               st.takeItems(4421, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30594-5.htm";
            }
         } else {
            int score = st.getInt("score");
            if (event.equalsIgnoreCase("30594-8a.htm")) {
               if (st.getQuestItemsCount(1455) >= 1L) {
                  st.set("cond", "4");
                  st.takeItems(1455, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("score", String.valueOf(score + 8));
               } else {
                  htmltext = "30594-8.htm";
               }
            } else if (event.equalsIgnoreCase("30594-8b.htm")) {
               if (st.getQuestItemsCount(1456) >= 1L) {
                  st.set("cond", "4");
                  st.takeItems(1456, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("score", String.valueOf(score + 16));
               } else {
                  htmltext = "30594-8.htm";
               }
            } else if (event.equalsIgnoreCase("30594-8c.htm")) {
               if (st.getQuestItemsCount(1457) >= 1L) {
                  st.set("cond", "4");
                  st.takeItems(1457, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("score", String.valueOf(score + 32));
               } else {
                  htmltext = "30594-8.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_378_MagnificentFeast");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 20 && player.getLevel() <= 30) {
                  htmltext = "30594-1.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30594-0.htm";
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30594-3.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(4421) >= 1L) {
                     htmltext = "30594-5a.htm";
                  } else {
                     htmltext = "30594-5.htm";
                  }
               } else if (cond == 3) {
                  htmltext = "30594-7.htm";
               } else if (cond == 4) {
                  String score = st.get("score");
                  if (this.Reward_list.containsKey(score) && st.getQuestItemsCount(5959) >= 1L) {
                     htmltext = "30594-10.htm";
                     st.takeItems(5959, 1L);
                     st.giveItems(this.Reward_list.get(score)[0], (long)((int[])this.Reward_list.get(score))[1]);
                     int adena = this.Reward_list.get(score)[2];
                     if (adena > 0) {
                        st.rewardItems(57, (long)adena);
                     }

                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                  } else {
                     htmltext = "30594-9.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _378_MagnificentFeast(378, "_378_MagnificentFeast", "");
   }
}
