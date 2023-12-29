package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _142_FallenAngelRequestOfDawn extends Quest {
   private static final String qn = "_142_FallenAngelRequestOfDawn";
   private static final int NATOOLS = 30894;
   private static final int RAYMOND = 30289;
   private static final int CASIAN = 30612;
   private static final int ROCK = 32368;
   private static final int CRYPT = 10351;
   private static final int FRAGMENT = 10352;
   private static final int BLOOD = 10353;
   private static final int[] MOBs = new int[]{20079, 20080, 20081, 20082, 20084, 20086, 20087, 20088, 20089, 20090, 27338};
   private int isAngelSpawned = 0;

   public _142_FallenAngelRequestOfDawn(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addTalkId(new int[]{30894, 30289, 30612, 32368});

      for(int mob : MOBs) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10351, 10352, 10353};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_142_FallenAngelRequestOfDawn");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30894-01.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30894-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(10351, 1L);
         } else if (event.equalsIgnoreCase("30289-04.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30612-07.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32368-02.htm")) {
            if (this.isAngelSpawned == 0) {
               addSpawn(27338, -21882, 186730, -4320, 0, false, 900000L);
               this.isAngelSpawned = 1;
               this.startQuestTimer("angel_cleanup", 900000L, null, player);
            }
         } else if (event.equalsIgnoreCase("angel_cleanup") && this.isAngelSpawned == 1) {
            this.isAngelSpawned = 0;
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_142_FallenAngelRequestOfDawn");
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
            } else if (npcId == 30289) {
               if (cond == 2) {
                  if (st.getInt("talk") == 1) {
                     htmltext = "30289-02.htm";
                  } else {
                     htmltext = "30289-01.htm";
                     st.takeItems(10351, -1L);
                     st.set("talk", "1");
                  }
               } else if (cond == 3) {
                  htmltext = "30289-05.htm";
               } else if (cond == 6) {
                  htmltext = "30289-06.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
                  st.giveItems(57, 92676L);
                  st.takeItems(10353, -1L);
                  if (player.getLevel() >= 38 && player.getLevel() <= 43) {
                     st.addExpAndSp(223036, 13091);
                  }
               }
            } else if (npcId == 30612) {
               if (cond == 3) {
                  htmltext = "30612-01.htm";
               } else if (cond == 4) {
                  htmltext = "30612-07.htm";
               }
            } else if (npcId == 32368) {
               if (cond == 5) {
                  htmltext = "32368-01.htm";
               }

               if (st.getInt("talk") != 1) {
                  st.takeItems(10353, -1L);
                  st.set("talk", "1");
               } else if (cond == 6) {
                  htmltext = "32368-03.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_142_FallenAngelRequestOfDawn");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         if (npc.getId() == 27338) {
            if (cond == 5) {
               st.set("cond", "6");
               st.playSound("ItemSound.quest_middle");
               st.giveItems(10353, 1L);
               this.isAngelSpawned = 0;
            }
         } else if (cond == 4 && st.getQuestItemsCount(10352) < 30L) {
            st.dropQuestItems(10352, 1, 30L, 20, true);
            if (st.getQuestItemsCount(10352) >= 30L) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _142_FallenAngelRequestOfDawn(142, "_142_FallenAngelRequestOfDawn", "");
   }
}
