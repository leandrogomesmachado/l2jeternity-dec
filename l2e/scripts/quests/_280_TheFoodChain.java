package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _280_TheFoodChain extends Quest {
   private static final String qn = "_280_TheFoodChain";
   private static int BIXON = 32175;
   private static int YOUNG_GREY_KELTIR = 22229;
   private static int GREY_KELTIR = 22230;
   private static int DOMINANT_GREY_KELTIR = 22231;
   private static int BLACK_WOLF = 22232;
   private static int DOMINANT_BLACK_WOLF = 22233;
   private static int[] REWARDS = new int[]{28, 35, 116};
   private static int KELTIR_TOOTH = 9809;
   private static int WOLF_TOOTH = 9810;

   public _280_TheFoodChain(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(BIXON);
      this.addTalkId(BIXON);
      this.addKillId(YOUNG_GREY_KELTIR);
      this.addKillId(GREY_KELTIR);
      this.addKillId(DOMINANT_GREY_KELTIR);
      this.addKillId(BLACK_WOLF);
      this.addKillId(DOMINANT_BLACK_WOLF);
      this.questItemIds = new int[]{KELTIR_TOOTH, WOLF_TOOTH};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_280_TheFoodChain");
      if (st == null) {
         return null;
      } else {
         long KELTIR_TOOTH_COUNT = st.getQuestItemsCount(KELTIR_TOOTH);
         long WOLF_TOOTH_COUNT = st.getQuestItemsCount(WOLF_TOOTH);
         if (event.equalsIgnoreCase("32175-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("ADENA")) {
            st.takeItems(KELTIR_TOOTH, -1L);
            st.takeItems(WOLF_TOOTH, -1L);
            st.giveItems(57, (KELTIR_TOOTH_COUNT + WOLF_TOOTH_COUNT) * 2L);
            htmltext = "32175-06.htm";
         } else if (event.equalsIgnoreCase("ITEM")) {
            if (KELTIR_TOOTH_COUNT + WOLF_TOOTH_COUNT < 25L) {
               htmltext = "32175-09.htm";
            } else {
               htmltext = "32175-06.htm";
               if (KELTIR_TOOTH_COUNT > 25L) {
                  st.giveItems(REWARDS[getRandom(REWARDS.length)], 1L);
                  st.takeItems(KELTIR_TOOTH, 25L);
               } else {
                  st.giveItems(REWARDS[getRandom(REWARDS.length)], 1L);
                  st.takeItems(KELTIR_TOOTH, KELTIR_TOOTH_COUNT);
                  st.takeItems(WOLF_TOOTH, 25L - KELTIR_TOOTH_COUNT);
               }
            }
         } else if (event.equalsIgnoreCase("32175-08.htm")) {
            st.takeItems(KELTIR_TOOTH, -1L);
            st.takeItems(WOLF_TOOTH, -1L);
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_280_TheFoodChain");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 3) {
                  htmltext = "32175-01.htm";
               } else {
                  htmltext = "32175-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               htmltext = st.getQuestItemsCount(KELTIR_TOOTH) <= 0L && st.getQuestItemsCount(WOLF_TOOTH) <= 0L ? "32175-04.htm" : "32175-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_280_TheFoodChain");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.getInt("cond") == 1) {
            if ((npcId == YOUNG_GREY_KELTIR || npcId == GREY_KELTIR || npcId == DOMINANT_GREY_KELTIR) && Rnd.chance(95)) {
               st.giveItems(KELTIR_TOOTH, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if ((npcId == BLACK_WOLF || npcId == DOMINANT_BLACK_WOLF) && Rnd.chance(75)) {
               st.giveItems(WOLF_TOOTH, 3L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _280_TheFoodChain(280, "_280_TheFoodChain", "");
   }
}
