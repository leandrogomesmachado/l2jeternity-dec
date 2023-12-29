package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class PurchaseBracelet extends Quest {
   private static final String qn = "PurchaseBracelet";
   private static final int NPC = 30098;
   private static final int ANGEL_BRACELET = 10320;
   private static final int DEVIL_BRACELET = 10326;
   private static final int ADENA = 57;
   private static final int BIG_RED_NIBLE_FISH = 6471;
   private static final int GREAT_CODRAN = 5094;
   private static final int MEMENTO_MORI = 9814;
   private static final int EARTH_EGG = 9816;
   private static final int NONLIVING_NUCLEUS = 9817;
   private static final int DRAGON_HEART = 9815;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("PurchaseBracelet");
      if (st == null) {
         return event;
      } else {
         String htmltext;
         if (st.getQuestItemsCount(6471) >= 20L
            && st.getQuestItemsCount(5094) >= 50L
            && st.getQuestItemsCount(9814) >= 4L
            && st.getQuestItemsCount(9816) >= 5L
            && st.getQuestItemsCount(9817) >= 5L
            && st.getQuestItemsCount(9815) >= 3L
            && st.getQuestItemsCount(57) >= 7500000L) {
            st.takeItems(6471, 25L);
            st.takeItems(5094, 50L);
            st.takeItems(9814, 4L);
            st.takeItems(9816, 5L);
            st.takeItems(9817, 5L);
            st.takeItems(9815, 3L);
            st.takeItems(57, 7500000L);
            htmltext = "";
            if (event.equals("Little_Devil")) {
               st.giveItems(10326, 1L);
            } else if (event.equals("Little_Angel")) {
               st.giveItems(10320, 1L);
            }
         } else {
            htmltext = "30098-no.htm";
         }

         st.exitQuest(true);
         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState("PurchaseBracelet");
      if (st == null) {
         st = this.newQuestState(player);
      }

      return "30098.htm";
   }

   public PurchaseBracelet(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30098);
      this.addTalkId(30098);
   }

   public static void main(String[] args) {
      new PurchaseBracelet(-1, "PurchaseBracelet", "custom");
   }
}
