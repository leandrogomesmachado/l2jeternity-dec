package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _338_AlligatorHunter extends Quest {
   private static final String qn = "_338_AlligatorHunter";
   private static final int ALLIGATOR = 20135;
   private static final int ENVERUN = 30892;
   private static final int ALLIGATOR_PELTS = 4337;

   public _338_AlligatorHunter(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30892);
      this.addTalkId(30892);
      this.addKillId(20135);
      this.questItemIds = new int[]{4337};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_338_AlligatorHunter");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30892-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30892-05.htm")) {
            long count = st.getQuestItemsCount(4337);
            if (count > 0L) {
               if (count > 10L) {
                  count = count * 60L + 3430L;
               } else {
                  count *= 60L;
               }

               st.takeItems(4337, -1L);
               st.rewardItems(57, count);
            } else {
               htmltext = "30892-04.htm";
            }
         } else if ("30892-08.htm".equals(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_338_AlligatorHunter");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 40 && player.getLevel() <= 47) {
                  htmltext = "30892-01.htm";
               } else {
                  htmltext = "30892-00.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(4337) > 0L) {
                  htmltext = "30892-03.htm";
               } else {
                  htmltext = "30892-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_338_AlligatorHunter");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(10) < 5) {
            st.giveItems(4337, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _338_AlligatorHunter(338, "_338_AlligatorHunter", "");
   }
}
