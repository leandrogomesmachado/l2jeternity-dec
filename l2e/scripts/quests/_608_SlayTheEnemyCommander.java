package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _608_SlayTheEnemyCommander extends Quest {
   private static final String qn = "_608_SlayTheEnemyCommander";
   private static final int MOS_HEAD = 7236;
   private static final int TOTEM = 7220;

   public _608_SlayTheEnemyCommander(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31370);
      this.addTalkId(31370);
      this.addKillId(25312);
      this.questItemIds = new int[]{7236};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_608_SlayTheEnemyCommander");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31370-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31370-07.htm")) {
            if (st.getQuestItemsCount(7236) == 1L) {
               st.takeItems(7236, -1L);
               st.giveItems(7220, 1L);
               st.addExpAndSp(10000, 0);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31370-06.htm";
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
      QuestState st = player.getQuestState("_608_SlayTheEnemyCommander");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = "31370-01.htm";
               break;
            case 1:
               if (st.getQuestItemsCount(7236) > 0L) {
                  htmltext = "31370-05.htm";
               } else {
                  htmltext = "31370-06.htm";
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
      QuestState st = player.getQuestState("_608_SlayTheEnemyCommander");
      if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false)) {
         st.giveItems(7236, 1L);
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _608_SlayTheEnemyCommander(608, "_608_SlayTheEnemyCommander", "");
   }
}
