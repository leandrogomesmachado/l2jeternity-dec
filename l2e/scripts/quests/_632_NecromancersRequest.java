package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _632_NecromancersRequest extends Quest {
   private static final String qn = "_632_NecromancersRequest";
   private static final int[] VAMPIRES = new int[]{21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595};
   private static final int[] UNDEADS = new int[]{21547, 21548, 21549, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579};
   private static final int VAMPIRE_HEART = 7542;
   private static final int ZOMBIE_BRAIN = 7543;

   public _632_NecromancersRequest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31522);
      this.addTalkId(31522);
      this.addKillId(VAMPIRES);
      this.addKillId(UNDEADS);
      this.questItemIds = new int[]{7542, 7543};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_632_NecromancersRequest");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31522-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31522-06.htm")) {
            if (st.getQuestItemsCount(7542) > 199L) {
               st.takeItems(7542, -1L);
               st.rewardItems(57, 120000L);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "31522-09.htm";
            }
         } else if (event.equalsIgnoreCase("31522-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_632_NecromancersRequest");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 63) {
                  st.exitQuest(true);
                  htmltext = "31522-01.htm";
               } else {
                  htmltext = "31522-02.htm";
               }
               break;
            case 1:
               htmltext = st.getQuestItemsCount(7542) >= 200L ? "31522-05.htm" : "31522-04.htm";
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
         QuestState st = partyMember.getQuestState("_632_NecromancersRequest");
         int npcId = npc.getId();

         for(int undead : UNDEADS) {
            if (undead == npcId) {
               st.dropItems(7543, 1, -1L, 330000);
               return null;
            }
         }

         if (st.getInt("cond") == 1 && st.dropItems(7542, 1, 200L, 500000)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _632_NecromancersRequest(632, "_632_NecromancersRequest", "");
   }
}
