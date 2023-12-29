package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _143_FallenAngelRequestOfDusk extends Quest {
   private static final String qn = "_143_FallenAngelRequestOfDusk";
   private static final int NATOOLS = 30894;
   private static final int TOBIAS = 30297;
   private static final int CASIAN = 30612;
   private static final int ROCK = 32368;
   private static final int ANGEL = 32369;
   private static final int SEALED_PATH = 10354;
   private static final int PATH = 10355;
   private static final int EMPTY_CRYSTAL = 10356;
   private static final int MEDICINE = 10357;
   private static final int MESSAGE = 10358;
   private int isAngelSpawned = 0;

   public _143_FallenAngelRequestOfDusk(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addTalkId(new int[]{30894, 30297, 30612, 32368, 32369});
      this.questItemIds = new int[]{10354, 10355, 10356, 10357, 10358};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_143_FallenAngelRequestOfDusk");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30894-01.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30894-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(10354, 1L);
         } else if (event.equalsIgnoreCase("30297-04.htm")) {
            st.set("cond", "3");
            st.unset("talk");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(10355, 1L);
            st.giveItems(10356, 1L);
         } else if (event.equalsIgnoreCase("30612-07.htm")) {
            st.set("cond", "4");
            st.unset("talk");
            st.giveItems(10357, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32368-02.htm")) {
            if (this.isAngelSpawned == 0) {
               addSpawn(32369, -21882, 186730, -4320, 0, false, 900000L);
               this.isAngelSpawned = 1;
               this.startQuestTimer("angel_cleanup", 900000L, null, player);
            }
         } else if (event.equalsIgnoreCase("32369-10.htm")) {
            st.set("cond", "5");
            st.unset("talk");
            st.takeItems(10356, -1L);
            st.giveItems(10358, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("angel_cleanup") && this.isAngelSpawned == 1) {
            this.isAngelSpawned = 0;
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_143_FallenAngelRequestOfDusk");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int id = st.getState();
         if (id == 0) {
            return htmltext;
         } else {
            if (id == 2) {
               htmltext = getAlreadyCompletedMsg(player);
            } else if (npcId == 30894) {
               if (cond == 1) {
                  htmltext = "30894-01.htm";
               } else if (cond == 2) {
                  htmltext = "30894-04.htm";
               }
            } else if (npcId == 30297) {
               if (cond == 2) {
                  if (st.getInt("talk") == 1) {
                     htmltext = "30297-02.htm";
                  } else {
                     htmltext = "30297-01.htm";
                     st.takeItems(10354, -1L);
                     st.set("talk", "1");
                  }
               } else if (cond == 3) {
                  htmltext = "30297-05.htm";
               } else if (cond == 5) {
                  htmltext = "30297-06.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.giveItems(57, 89046L);
                  st.takeItems(10358, -1L);
                  st.exitQuest(false);
                  if (player.getLevel() >= 38 && player.getLevel() <= 43) {
                     st.addExpAndSp(223036, 13901);
                  }
               }
            } else if (npcId == 30612) {
               if (cond == 3) {
                  if (st.getInt("talk") == 1) {
                     htmltext = "30612-02.htm";
                  } else {
                     htmltext = "30612-01.htm";
                     st.takeItems(10355, -1L);
                     st.set("talk", "1");
                  }
               } else if (cond == 4) {
                  htmltext = "30612-07.htm";
               }
            } else if (npcId == 32368) {
               if (cond == 4) {
                  htmltext = "32368-01.htm";
               }
            } else if (npcId == 32369) {
               if (cond == 4) {
                  if (st.getInt("talk") == 1) {
                     htmltext = "32369-02.htm";
                  } else {
                     htmltext = "32369-01.htm";
                     st.takeItems(10357, -1L);
                     st.set("talk", "1");
                  }
               } else if (cond == 5) {
                  htmltext = "32369-10.htm";
               }
            }

            return htmltext;
         }
      }
   }

   public static void main(String[] args) {
      new _143_FallenAngelRequestOfDusk(143, "_143_FallenAngelRequestOfDusk", "");
   }
}
