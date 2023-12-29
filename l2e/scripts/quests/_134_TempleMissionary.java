package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _134_TempleMissionary extends Quest {
   private static final String qn = "_134_TempleMissionary";
   private static final int GLYVKA = 30067;
   private static final int ROUKE = 31418;
   private static final int MARSHLANDS_TRAITOR = 27339;
   private static final int[] mobs = new int[]{20157, 20229, 20230, 20231, 20232, 20233, 20234};
   private static final int FRAGMENT = 10335;
   private static final int GIANTS_TOOL = 10336;
   private static final int REPORT = 10337;
   private static final int REPORT2 = 10338;
   private static final int BADGE = 10339;
   private static final int FRAGMENT_CHANCE = 66;

   public _134_TempleMissionary(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30067);
      this.addTalkId(30067);
      this.addTalkId(31418);

      for(int mob : mobs) {
         this.addKillId(mob);
      }

      this.addKillId(27339);
      this.questItemIds = new int[]{10335, 10336, 10337, 10338};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_134_TempleMissionary");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30067-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30067-04.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30067-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.unset("Report");
            st.giveItems(57, 15100L);
            st.giveItems(10339, 1L);
            st.addExpAndSp(30000, 2000);
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("31418-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31418-07.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(10338, 1L);
            st.unset("Report");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_134_TempleMissionary");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 35) {
                  htmltext = "30067-01.htm";
               } else {
                  htmltext = "30067-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 30067) {
                  if (cond == 1) {
                     return "30067-02.htm";
                  }

                  if (cond == 2 || cond == 3 || cond == 4) {
                     htmltext = "30067-05.htm";
                  } else if (cond == 5) {
                     if (st.getInt("Report") == 1) {
                        htmltext = "30067-07.htm";
                     }

                     if (st.getQuestItemsCount(10338) > 0L) {
                        st.takeItems(10338, -1L);
                        st.set("Report", "1");
                        htmltext = "30067-06.htm";
                     }
                  }
               }

               if (npcId == 31418) {
                  if (cond == 2) {
                     htmltext = "31418-01.htm";
                  } else if (cond == 3) {
                     long Tools = st.getQuestItemsCount(10335) / 10L;
                     if (Tools < 1L) {
                        htmltext = "31418-03.htm";
                     }

                     st.takeItems(10335, Tools * 10L);
                     st.giveItems(10336, Tools);
                     htmltext = "31418-04.htm";
                  } else if (cond == 4) {
                     if (st.getInt("Report") == 1) {
                        htmltext = "31418-06.htm";
                     }

                     if (st.getQuestItemsCount(10337) > 2L) {
                        st.takeItems(10335, -1L);
                        st.takeItems(10336, -1L);
                        st.takeItems(10337, -1L);
                        st.set("Report", "1");
                        htmltext = "31418-05.htm";
                     }
                  } else if (cond == 5) {
                     htmltext = "31418-08.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_134_TempleMissionary");
      if (st == null) {
         return null;
      } else {
         if (st.getCond() == 3) {
            if (npc.getId() == 27339) {
               st.giveItems(10337, 1L);
               if (st.getQuestItemsCount(10337) < 3L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "4");
               }
            } else if (st.getQuestItemsCount(10336) < 1L) {
               if (Rnd.chance(66)) {
                  st.giveItems(10335, 1L);
               }
            } else {
               st.takeItems(10336, 1L);
               if (Rnd.chance(45)) {
                  st.addSpawn(27339, npc, true, 900000);
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _134_TempleMissionary(134, "_134_TempleMissionary", "");
   }
}
