package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _613_ProveYourCourage extends Quest {
   private static final String qn = "_613_ProveYourCourage";
   private static final int HEKATON_HEAD = 7240;
   private static final int VALOR_FEATHER = 7229;
   private static final int VARKA_ALLIANCE_THREE = 7223;

   public _613_ProveYourCourage(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31377);
      this.addTalkId(31377);
      this.addKillId(25299);
      this.questItemIds = new int[]{7240};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_613_ProveYourCourage");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31377-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31377-07.htm")) {
            if (st.getQuestItemsCount(7240) == 1L) {
               st.takeItems(7240, -1L);
               st.giveItems(7229, 1L);
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
      QuestState st = player.getQuestState("_613_ProveYourCourage");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 75 ? (st.hasQuestItems(7223) ? "31377-01.htm" : "31377-02.htm") : "31377-03.htm";
               break;
            case 1:
               if (st.getQuestItemsCount(7240) == 1L) {
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
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false)) {
         st.giveItems(7240, 1L);
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _613_ProveYourCourage(613, "_613_ProveYourCourage", "");
   }
}
