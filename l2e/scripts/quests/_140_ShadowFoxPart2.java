package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _140_ShadowFoxPart2 extends Quest {
   private static final String qn = "_140_ShadowFoxPart2";
   private static final int KLUCK = 30895;
   private static final int XENOVIA = 30912;
   private static final int CRYSTAL = 10347;
   private static final int OXYDE = 10348;
   private static final int CRYPT = 10349;
   private static final int[] NPC = new int[]{20789, 20790, 20791, 20792};

   public _140_ShadowFoxPart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(30895);
      this.addTalkId(30895);
      this.addTalkId(30912);

      for(int mob : NPC) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10347, 10348, 10349};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_140_ShadowFoxPart2");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30895-02.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30895-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30895-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.unset("talk");
            st.exitQuest(false);
            st.giveItems(57, 18775L);
            if (player.getLevel() >= 37 && player.getLevel() <= 42) {
               st.addExpAndSp(30000, 2000);
            }
         } else if (event.equalsIgnoreCase("30912-07.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30912-09.htm")) {
            st.takeItems(10347, 5L);
            if (st.getRandom(100) <= 60) {
               st.giveItems(10348, 1L);
               if (st.getQuestItemsCount(10348) >= 3L) {
                  htmltext = "30912-09b.htm";
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
                  st.takeItems(10347, -1L);
                  st.takeItems(10348, -1L);
                  st.giveItems(10349, 1L);
               }
            } else {
               htmltext = "30912-09a.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_140_ShadowFoxPart2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      QuestState qs = player.getQuestState("_139_ShadowFoxPart1");
      if (qs != null && qs.getState() == 2 && st.getState() == 0) {
         st.setState((byte)1);
      }

      npc.showChatWindow(player);
      return null;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_140_ShadowFoxPart2");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         int talk = st.getInt("talk");
         if (id == 0) {
            return htmltext;
         } else {
            if (id == 2) {
               htmltext = getAlreadyCompletedMsg(player);
            } else if (npcId == 30895) {
               if (cond == 0) {
                  if (player.getLevel() >= 37) {
                     htmltext = "30895-01.htm";
                  } else {
                     htmltext = "30895-00.htm";
                     st.exitQuest(true);
                  }
               } else if (cond == 1) {
                  htmltext = "30895-02.htm";
               } else if (cond == 2 || cond == 3) {
                  htmltext = "30895-06.htm";
               } else if (cond == 4) {
                  if (cond == talk) {
                     htmltext = "30895-08.htm";
                  } else {
                     htmltext = "30895-07.htm";
                     st.takeItems(10349, -1L);
                     st.set("talk", "1");
                  }
               }
            } else if (npcId == 30912) {
               if (cond == 2) {
                  htmltext = "30912-01.htm";
               } else if (cond == 3) {
                  if (st.getQuestItemsCount(10347) >= 5L) {
                     htmltext = "30912-08.htm";
                  } else {
                     htmltext = "30912-07.htm";
                  }
               } else if (cond == 4) {
                  htmltext = "30912-10.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_140_ShadowFoxPart2");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 3 && st.getRandom(100) <= 80) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(10347, 1L);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _140_ShadowFoxPart2(140, "_140_ShadowFoxPart2", "");
   }
}
