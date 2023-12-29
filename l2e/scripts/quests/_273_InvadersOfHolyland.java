package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _273_InvadersOfHolyland extends Quest {
   private static final String qn = "_273_InvadersOfHolyland";
   public final int BLACK_SOULSTONE = 1475;
   public final int RED_SOULSTONE = 1476;
   private static final int NEWBIE_REWARD = 4;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _273_InvadersOfHolyland(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30566);
      this.addTalkId(30566);
      this.addKillId(20311);
      this.addKillId(20312);
      this.addKillId(20313);
      this.questItemIds = new int[]{1475, 1476};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_273_InvadersOfHolyland");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30566-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30566-07.htm")) {
            st.set("cond", "0");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30566-08.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_273_InvadersOfHolyland");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0) {
            st.set("cond", "0");
         }

         if (cond == 0) {
            if (player.getRace().ordinal() != 3) {
               htmltext = "30566-00.htm";
               st.exitQuest(true);
            } else if (player.getLevel() < 6) {
               htmltext = "30566-01.htm";
               st.exitQuest(true);
            } else {
               htmltext = "30566-02.htm";
            }
         } else if (cond > 0) {
            if (st.getQuestItemsCount(1475) == 0L && st.getQuestItemsCount(1476) == 0L) {
               htmltext = "30566-04.htm";
            } else {
               long red = st.getQuestItemsCount(1476);
               long black = st.getQuestItemsCount(1475);
               if (red + black == 0L) {
                  htmltext = "30566-04.htm";
               } else if (red == 0L) {
                  htmltext = "30566-05.htm";
                  if (black > 9L) {
                     st.giveItems(57, black * 3L + 1500L);
                  } else {
                     st.giveItems(57, black * 3L);
                  }

                  st.takeItems(1475, black);
                  st.playSound("ItemSound.quest_finish");
               } else {
                  htmltext = "30566-06.htm";
                  long amount = 0L;
                  if (black >= 1L) {
                     amount = black * 3L;
                     st.takeItems(1475, black);
                  }

                  amount += red * 10L;
                  if (black + red > 9L) {
                     amount += 1800L;
                  }

                  st.takeItems(1476, red);
                  st.giveItems(57, amount);
                  st.playSound("ItemSound.quest_finish");
               }

               if (red + black != 0L) {
                  int newbie = player.getNewbie();
                  if ((newbie | 4) != newbie) {
                     player.setNewbie(newbie | 4);
                     st.showQuestionMark(false, 26);
                     if (player.getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                     } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                     }

                     showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  }
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_273_InvadersOfHolyland");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20311) {
            if (cond == 1) {
               if (Rnd.getChance(90)) {
                  st.giveItems(1475, 1L);
               } else {
                  st.giveItems(1476, 1L);
               }

               st.playSound("ItemSound.quest_itemget");
            }
         } else if (npcId == 20312) {
            if (cond == 1) {
               if (Rnd.getChance(87)) {
                  st.giveItems(1475, 1L);
               } else {
                  st.giveItems(1476, 1L);
               }

               st.playSound("ItemSound.quest_itemget");
            }
         } else if (npcId == 20313 && cond == 1) {
            if (Rnd.getChance(77)) {
               st.giveItems(1475, 1L);
            } else {
               st.giveItems(1476, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _273_InvadersOfHolyland(273, "_273_InvadersOfHolyland", "");
   }
}
