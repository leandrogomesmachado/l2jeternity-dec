package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _619_RelicsOfTheOldEmpire extends Quest {
   private static final String qn = "_619_RelicsOfTheOldEmpire";
   private static int GHOST_OF_ADVENTURER = 31538;
   private static int RELICS = 7254;
   private static int ENTRANCE = 7075;
   private static int[] RCP_REWARDS = new int[]{6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

   public _619_RelicsOfTheOldEmpire(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(GHOST_OF_ADVENTURER);
      this.addTalkId(GHOST_OF_ADVENTURER);

      for(int id = 21396; id <= 21434; ++id) {
         this.addKillId(id);
      }

      this.addKillId(new int[]{21798, 21799, 21800});

      for(int id = 18120; id <= 18256; ++id) {
         this.addKillId(id);
      }

      this.questItemIds = new int[]{RELICS};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_619_RelicsOfTheOldEmpire");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31538-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31538-09.htm")) {
            if (st.getQuestItemsCount(RELICS) >= 1000L) {
               htmltext = "31538-09.htm";
               st.takeItems(RELICS, 1000L);
               st.giveItems(RCP_REWARDS[getRandom(RCP_REWARDS.length)], 1L);
            } else {
               htmltext = "31538-06.htm";
            }
         } else if (event.equalsIgnoreCase("31538-10.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_619_RelicsOfTheOldEmpire");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 74) {
                  htmltext = "31538-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31538-01.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(RELICS) >= 1000L) {
                  htmltext = "31538-04.htm";
               } else if (st.getQuestItemsCount(ENTRANCE) >= 1L) {
                  htmltext = "31538-06.htm";
               } else {
                  htmltext = "31538-07.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_619_RelicsOfTheOldEmpire");
         st.dropItemsAlways(RELICS, 1, -1L);
         st.dropItems(ENTRANCE, 1, -1L, 50000);
         return null;
      }
   }

   public static void main(String[] args) {
      new _619_RelicsOfTheOldEmpire(619, "_619_RelicsOfTheOldEmpire", "");
   }
}
