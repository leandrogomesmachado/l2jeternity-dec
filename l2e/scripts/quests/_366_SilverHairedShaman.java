package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _366_SilverHairedShaman extends Quest {
   private static final String qn = "_366_SilverHairedShaman";
   private static final int DIETER = 30111;
   private static final int HAIR = 5874;

   public _366_SilverHairedShaman(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30111);
      this.addTalkId(30111);
      this.addKillId(new int[]{20986, 20987, 20988});
      this.questItemIds = new int[]{5874};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_366_SilverHairedShaman");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30111-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30111-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_366_SilverHairedShaman");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 48 && player.getLevel() <= 58) {
                  htmltext = "30111-1.htm";
               } else {
                  htmltext = "30111-0.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long count = st.getQuestItemsCount(5874);
               if (count == 0L) {
                  htmltext = "30111-3.htm";
               } else {
                  htmltext = "30111-4.htm";
                  st.takeItems(5874, -1L);
                  st.rewardItems(57, 12070L + 500L * count);
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
         QuestState st = partyMember.getQuestState("_366_SilverHairedShaman");
         if (st.getRandom(100) < 55) {
            st.rewardItems(5874, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _366_SilverHairedShaman(366, "_366_SilverHairedShaman", "");
   }
}
