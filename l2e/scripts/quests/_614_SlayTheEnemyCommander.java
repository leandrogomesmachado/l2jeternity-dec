package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _614_SlayTheEnemyCommander extends Quest {
   private static final String qn = "_614_SlayTheEnemyCommander";
   private static final int TAYR_HEAD = 7241;
   private static final int WISDOM_FEATHER = 7230;
   private static final int VARKA_ALLIANCE_FOUR = 7224;

   public _614_SlayTheEnemyCommander(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31377);
      this.addTalkId(31377);
      this.addKillId(25302);
      this.questItemIds = new int[]{7241};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_614_SlayTheEnemyCommander");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31377-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31377-07.htm")) {
            if (st.getQuestItemsCount(7241) == 1L) {
               st.takeItems(7241, -1L);
               st.giveItems(7230, 1L);
               st.addExpAndSp(10000, 0);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31377-06.htm";
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_614_SlayTheEnemyCommander");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 75 ? (st.hasQuestItems(7224) ? "31377-01.htm" : "31377-02.htm") : "31377-03.htm";
               break;
            case 1:
               if (st.getQuestItemsCount(7241) > 0L) {
                  htmltext = "31377-05.htm";
               } else {
                  htmltext = "31377-06.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      this.executeForEachPlayer(player, npc, isSummon, true, false);
      return super.onKill(npc, player, isSummon);
   }

   @Override
   public void actionForEachPlayer(Player player, Npc npc, boolean isSummon) {
      QuestState st = player.getQuestState("_614_SlayTheEnemyCommander");
      if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false)) {
         st.giveItems(7241, 1L);
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _614_SlayTheEnemyCommander(614, "_614_SlayTheEnemyCommander", "");
   }
}
