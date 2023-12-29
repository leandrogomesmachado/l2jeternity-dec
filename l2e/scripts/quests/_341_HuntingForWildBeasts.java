package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _341_HuntingForWildBeasts extends Quest {
   private static final String qn = "_341_HuntingForWildBeasts";
   private static final int PANO = 30078;
   private static final int BEAR_SKIN = 4259;

   public _341_HuntingForWildBeasts(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30078);
      this.addTalkId(30078);
      this.addKillId(new int[]{20203, 20021, 20310, 20143});
      this.questItemIds = new int[]{4259};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_341_HuntingForWildBeasts");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30078-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_341_HuntingForWildBeasts");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 20 && player.getLevel() <= 24) {
                  htmltext = "30078-01.htm";
               } else {
                  htmltext = "30078-00.htm";
                  st.exitQuest(false);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(4259) >= 20L) {
                  htmltext = "30078-04.htm";
                  st.takeItems(4259, -1L);
                  st.rewardItems(57, 3710L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  htmltext = "30078-03.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_341_HuntingForWildBeasts");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            st.dropQuestItems(4259, 1, 20L, 400000, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _341_HuntingForWildBeasts(341, "_341_HuntingForWildBeasts", "");
   }
}
