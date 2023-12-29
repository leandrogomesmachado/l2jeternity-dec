package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _431_WeddingMarch extends Quest {
   private static final String qn = "_431_WeddingMarch";
   private static final int MELODY_MAESTRO_KANTABILON_ID = 31042;
   private static final int SILVER_CRYSTAL_ID = 7540;
   private static final int LIENRIKS_ID = 20786;
   private static final int LIENRIKS_LAD_ID = 20787;
   private static final int WEDDING_ECHO_CRYSTAL_ID = 7062;

   public _431_WeddingMarch(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31042);
      this.addTalkId(31042);
      this.addKillId(20786);
      this.addKillId(20787);
      this.questItemIds = new int[]{7540};
   }

   @Override
   public final String onEvent(String event, QuestState st) {
      String htmltext = event;
      if (event.equalsIgnoreCase("1")) {
         htmltext = "31042-02.htm";
         st.set("cond", "1");
         st.setState((byte)1);
         st.playSound("ItemSound.quest_accept");
      } else if (event.equalsIgnoreCase("3") && st.getQuestItemsCount(7540) == 50L) {
         st.giveItems(7062, 25L);
         st.takeItems(7540, 50L);
         htmltext = "31042-05.htm";
         st.playSound("ItemSound.quest_finish");
         st.exitQuest(true);
      }

      return htmltext;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_431_WeddingMarch");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               htmltext = "31042-01.htm";
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "31042-03.htm";
               } else if (cond == 2) {
                  htmltext = "31042-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_431_WeddingMarch");
         if (st.getInt("cond") == 1 && st.dropQuestItems(7540, 1, 1, 50L, false, 100.0F, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _431_WeddingMarch(431, "_431_WeddingMarch", "");
   }
}
