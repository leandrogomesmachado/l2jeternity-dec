package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _260_HuntTheOrcs extends Quest {
   private static final String qn = "_260_HuntTheOrcs";
   private static final int RAYEN = 30221;
   private static final int ORC_AMULET = 1114;
   private static final int ORCS_NECKLACE = 1115;
   private static final int KABOO_ORC = 20468;
   private static final int KABOO_ORC_ARCHER = 20469;
   private static final int KABOO_ORC_GRUNT = 20470;
   private static final int KABOO_ORC_FIGHTER = 20471;
   private static final int KABOO_ORC_FIGHTER_LEADER = 20472;
   private static final int KABOO_ORC_FIGHTER_LIEUTENANT = 20473;
   private static final int NEWBIE_REWARD = 4;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _260_HuntTheOrcs(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30221);
      this.addTalkId(30221);
      this.addKillId(new int[]{20468, 20469, 20470, 20471, 20472, 20473});
      this.questItemIds = new int[]{1114, 1115};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_260_HuntTheOrcs");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30221-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30221-06.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_260_HuntTheOrcs");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 1) {
                  if (player.getLevel() >= 6 && player.getLevel() <= 16) {
                     htmltext = "30221-02.htm";
                  } else {
                     htmltext = "30221-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30221-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long amulet = st.getQuestItemsCount(1114);
               long necklace = st.getQuestItemsCount(1115);
               if (amulet == 0L && necklace == 0L) {
                  htmltext = "30221-04.htm";
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

                  htmltext = "30221-05.htm";
                  st.takeItems(1114, -1L);
                  st.takeItems(1115, -1L);
                  st.giveItems(57, amulet * 5L + necklace * 15L);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_260_HuntTheOrcs");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            switch(npc.getId()) {
               case 20468:
               case 20469:
               case 20470:
                  if (getRandom(10) < 4) {
                     st.giveItems(1114, 1L);
                  }
                  break;
               case 20471:
               case 20472:
               case 20473:
                  if (getRandom(10) < 4) {
                     st.giveItems(1115, 1L);
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _260_HuntTheOrcs(260, "_260_HuntTheOrcs", "");
   }
}
