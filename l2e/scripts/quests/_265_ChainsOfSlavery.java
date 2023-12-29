package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _265_ChainsOfSlavery extends Quest {
   private static final String qn = "_265_ChainsOfSlavery";
   private static final int KRISTIN = 30357;
   private static final int IMP = 20004;
   private static final int IMP_ELDER = 20005;
   private static final int IMP_SHACKLES = 1368;
   private static final int NEWBIE_REWARD = 4;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _265_ChainsOfSlavery(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30357);
      this.addTalkId(30357);
      this.addKillId(20004);
      this.addKillId(20005);
      this.questItemIds = new int[]{1368};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_265_ChainsOfSlavery");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30357-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30357-06.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_265_ChainsOfSlavery");
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
            if (player.getRace().ordinal() != 2) {
               htmltext = "30357-00.htm";
               st.exitQuest(true);
            } else if (player.getLevel() < 6) {
               htmltext = "30357-01.htm";
               st.exitQuest(true);
            } else {
               htmltext = "30357-02.htm";
            }
         } else {
            long count = st.getQuestItemsCount(1368);
            if (count > 0L) {
               if (count >= 10L) {
                  st.giveItems(57, 12L * count + 500L);
               } else {
                  st.giveItems(57, 12L * count);
               }

               st.takeItems(1368, -1L);
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

               htmltext = "30357-05.htm";
            } else {
               htmltext = "30357-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_265_ChainsOfSlavery");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (cond == 1 && Rnd.getChance(5 + npcId - 20004)) {
            st.giveItems(1368, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _265_ChainsOfSlavery(265, "_265_ChainsOfSlavery", "");
   }
}
