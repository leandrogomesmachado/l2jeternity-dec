package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _257_TheGuardIsBusy extends Quest {
   private static final String qn = "_257_TheGuardIsBusy";
   private static final int GLUDIO_LORDS_MARK = 1084;
   private static final int ORC_AMULET = 752;
   private static final int ORC_NECKLACE = 1085;
   private static final int WEREWOLF_FANG = 1086;
   private static final int ADENA = 57;
   private static final int NEWBIE_REWARD = 4;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _257_TheGuardIsBusy(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30039);
      this.addTalkId(30039);
      this.addKillId(20130);
      this.addKillId(20131);
      this.addKillId(20132);
      this.addKillId(20342);
      this.addKillId(20343);
      this.addKillId(20006);
      this.addKillId(20093);
      this.addKillId(20096);
      this.addKillId(20098);
      this.questItemIds = new int[]{1084, 752, 1085, 1086};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_257_TheGuardIsBusy");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30039-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1084, 1L);
         } else if (event.equalsIgnoreCase("30039-05.htm")) {
            st.takeItems(1084, 1L);
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_257_TheGuardIsBusy");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         if (id == 0) {
            st.set("cond", "0");
         }

         if (st.getInt("cond") == 0) {
            if (player.getLevel() >= 6) {
               htmltext = "30039-02.htm";
            } else {
               htmltext = "30039-01.htm";
               st.exitQuest(true);
            }
         } else {
            long orc_a = st.getQuestItemsCount(752);
            long orc_n = st.getQuestItemsCount(1085);
            long wer_f = st.getQuestItemsCount(1086);
            if (orc_a == 0L && orc_n == 0L && wer_f == 0L) {
               htmltext = "30039-04.htm";
            } else {
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

               st.giveItems(57, 5L * orc_a + 15L * orc_n + 10L * wer_f);
               st.takeItems(752, -1L);
               st.takeItems(1085, -1L);
               st.takeItems(1086, -1L);
               htmltext = "30039-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_257_TheGuardIsBusy");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int chance = 5;
         int item;
         if (npcId == 20130 || npcId == 20131 || npcId == 20006) {
            item = 752;
         } else if (npcId != 20093 && npcId != 20096 && npcId != 20098) {
            item = 1086;
            if (npcId == 20343) {
               chance = 4;
            } else if (npcId == 20342) {
               chance = 2;
            }
         } else {
            item = 1085;
         }

         if (st.getQuestItemsCount(1084) == 1L && getRandom(10) < chance) {
            st.giveItems(item, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return "";
      }
   }

   public static void main(String[] args) {
      new _257_TheGuardIsBusy(257, "_257_TheGuardIsBusy", "");
   }
}
