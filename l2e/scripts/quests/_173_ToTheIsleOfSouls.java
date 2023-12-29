package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _173_ToTheIsleOfSouls extends Quest {
   private static final String qn = "_173_ToTheIsleOfSouls";
   private static int GALLADUCCI = 30097;
   private static int GENTLER = 30094;
   private static int SCROLL_OF_ESCAPE_KAMAEL_VILLAGE = 9716;
   private static int MARK_OF_TRAVELER = 7570;
   private static int GWAINS_DOCUMENT = 7563;
   private static int MAGIC_SWORD_HILT = 7568;

   public _173_ToTheIsleOfSouls(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(GALLADUCCI);
      this.addTalkId(GALLADUCCI);
      this.addTalkId(GENTLER);
      this.questItemIds = new int[]{GWAINS_DOCUMENT, MAGIC_SWORD_HILT};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_173_ToTheIsleOfSouls");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30097-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(GWAINS_DOCUMENT, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30094-02.htm")) {
            st.set("cond", "2");
            st.takeItems(GWAINS_DOCUMENT, -1L);
            st.giveItems(MAGIC_SWORD_HILT, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30097-06.htm")) {
            st.takeItems(MAGIC_SWORD_HILT, -1L);
            st.takeItems(MARK_OF_TRAVELER, -1L);
            st.giveItems(SCROLL_OF_ESCAPE_KAMAEL_VILLAGE, 1L);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_173_ToTheIsleOfSouls");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == GALLADUCCI) {
                  if (st.getQuestItemsCount(MARK_OF_TRAVELER) > 0L && player.getRace().ordinal() == 5) {
                     htmltext = "30097-02.htm";
                  } else {
                     htmltext = "30097-01.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == GALLADUCCI) {
                  if (cond == 1) {
                     htmltext = "30097-04.htm";
                  } else if (cond == 2) {
                     htmltext = "30097-05.htm";
                  }
               } else if (npcId == GENTLER) {
                  if (cond == 1) {
                     htmltext = "30094-01.htm";
                  } else if (cond == 2) {
                     htmltext = "30094-03.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _173_ToTheIsleOfSouls(173, "_173_ToTheIsleOfSouls", "");
   }
}
