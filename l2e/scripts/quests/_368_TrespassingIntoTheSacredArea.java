package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _368_TrespassingIntoTheSacredArea extends Quest {
   private static final String qn = "_368_TrespassingIntoTheSacredArea";
   private static final int RESTINA = 30926;
   private static final int FANG = 5881;

   public _368_TrespassingIntoTheSacredArea(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30926);
      this.addTalkId(30926);
      this.addKillId(new int[]{20794, 20795, 20796, 20797});
      this.questItemIds = new int[]{5881};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_368_TrespassingIntoTheSacredArea");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30926-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30926-05.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_368_TrespassingIntoTheSacredArea");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 36 && player.getLevel() <= 48) {
                  htmltext = "30926-01.htm";
               } else {
                  htmltext = "30926-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long fangs = st.getQuestItemsCount(5881);
               if (fangs == 0L) {
                  htmltext = "30926-03.htm";
               } else {
                  long reward = 250L * fangs + (long)(fangs > 10L ? 5730 : 2000);
                  htmltext = "30926-04.htm";
                  st.takeItems(5881, -1L);
                  st.rewardItems(57, reward);
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
         QuestState st = partyMember.getQuestState("_368_TrespassingIntoTheSacredArea");
         int chance = (int)(33.0F * Config.RATE_QUEST_DROP);
         int numItems = chance / 100;
         chance %= 100;
         if (st.getRandom(100) < chance) {
            ++numItems;
         }

         if (numItems > 0) {
            st.giveItems(5881, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _368_TrespassingIntoTheSacredArea(368, "_368_TrespassingIntoTheSacredArea", "");
   }
}
