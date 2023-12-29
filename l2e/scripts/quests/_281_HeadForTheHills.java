package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _281_HeadForTheHills extends Quest {
   private static final int NEWBIE_REWARD = 4;

   public _281_HeadForTheHills(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32173);
      this.addTalkId(32173);
      this.addKillId(new int[]{22234, 22235, 22236, 22237, 22238, 22239});
      this.questItemIds = new int[]{9796};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long hills = st.getQuestItemsCount(9796);
         if (event.equalsIgnoreCase("32173-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32173-06.htm")) {
            int newbie = player.getNewbie();
            if ((newbie | 4) != newbie) {
               player.setNewbie(newbie | 4);
               if (player.isMageClass()) {
                  st.calcReward(this.getId(), 1);
                  st.playTutorialVoice("tutorial_voice_027");
               } else {
                  st.calcReward(this.getId(), 2);
                  st.playTutorialVoice("tutorial_voice_026");
               }

               showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
            }

            st.calcRewardPerItem(this.getId(), 3, (int)hills);
            st.takeItems(9796, -1L);
         } else if (event.equalsIgnoreCase("32173-07.htm")) {
            if (hills < 50L) {
               htmltext = "32173-07a.htm";
            } else {
               int newbie = player.getNewbie();
               if ((newbie | 4) != newbie) {
                  player.setNewbie(newbie | 4);
                  if (player.isMageClass()) {
                     st.calcReward(this.getId(), 1);
                     st.playTutorialVoice("tutorial_voice_027");
                  } else {
                     st.calcReward(this.getId(), 2);
                     st.playTutorialVoice("tutorial_voice_026");
                  }

                  showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
               }

               st.takeItems(9796, 50L);
               st.calcReward(this.getId(), 4, true);
            }
         } else if (event.equalsIgnoreCase("32173-09.htm")) {
            st.takeItems(9796, -1L);
            st.exitQuest(true, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte state = st.getState();
         if (npcId == 32173) {
            if (state == 0) {
               if (player.getLevel() < getMinLvl(this.getId())) {
                  htmltext = "32173-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "32173-01.htm";
               }
            } else if (state == 1) {
               if (st.getQuestItemsCount(9796) > 0L) {
                  htmltext = "32173-05.htm";
               } else {
                  htmltext = "32173-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player member = this.getRandomPartyMemberState(player, (byte)1);
      if (member != null) {
         QuestState st = member.getQuestState(this.getName());
         if (st != null && npc.getId() >= 22234 && npc.getId() <= 22239) {
            st.calcDropItems(this.getId(), 9796, npc.getId(), Integer.MAX_VALUE);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _281_HeadForTheHills(281, _281_HeadForTheHills.class.getSimpleName(), "");
   }
}
