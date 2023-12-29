package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _004_LongLiveThePaagrioLord extends Quest {
   public _004_LongLiveThePaagrioLord(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30578);
      this.addTalkId(30578);
      this.addTalkId(30585);
      this.addTalkId(30566);
      this.addTalkId(30562);
      this.addTalkId(30560);
      this.addTalkId(30559);
      this.addTalkId(30587);
      this.questItemIds = new int[]{1541, 1542, 1543, 1544, 1545, 1546};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30578-03.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int cond = st.getCond();
      switch(npc.getId()) {
         case 30559:
            htmltext = giveItem(st, npc.getId(), 1545, this.getRegisteredItemIds());
            break;
         case 30560:
            htmltext = giveItem(st, npc.getId(), 1544, this.getRegisteredItemIds());
            break;
         case 30562:
            htmltext = giveItem(st, npc.getId(), 1543, this.getRegisteredItemIds());
            break;
         case 30566:
            htmltext = giveItem(st, npc.getId(), 1541, this.getRegisteredItemIds());
            break;
         case 30578:
            switch(st.getState()) {
               case 0:
                  if (player.getRace().ordinal() != 3) {
                     htmltext = "30578-00.htm";
                     st.exitQuest(true);
                     return htmltext;
                  } else {
                     if (player.getLevel() >= 2) {
                        htmltext = "30578-02.htm";
                     } else {
                        htmltext = "30578-01.htm";
                        st.exitQuest(true);
                     }

                     return htmltext;
                  }
               case 1:
                  switch(cond) {
                     case 1:
                        return "30578-04.htm";
                     case 2:
                        htmltext = "30578-06.htm";
                        st.takeItems(1541, 1L);
                        st.takeItems(1542, 1L);
                        st.takeItems(1543, 1L);
                        st.takeItems(1544, 1L);
                        st.takeItems(1545, 1L);
                        st.takeItems(1546, 1L);
                        st.calcExpAndSp(this.getId());
                        st.calcReward(this.getId());
                        st.exitQuest(false, true);
                        showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                        return htmltext;
                     default:
                        return htmltext;
                  }
               case 2:
                  htmltext = getAlreadyCompletedMsg(player);
                  return htmltext;
               default:
                  return htmltext;
            }
         case 30585:
            htmltext = giveItem(st, npc.getId(), 1542, this.getRegisteredItemIds());
            break;
         case 30587:
            htmltext = giveItem(st, npc.getId(), 1546, this.getRegisteredItemIds());
      }

      return htmltext;
   }

   private static String giveItem(QuestState st, int npcId, int itemId, int... items) {
      if (!st.isStarted()) {
         return getNoQuestMsg(st.getPlayer());
      } else if (st.hasQuestItems(itemId)) {
         return npcId + "-02.htm";
      } else {
         st.giveItems(itemId, 1L);
         st.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
         if (st.hasQuestItems(items)) {
            st.setCond(2, true);
         }

         return npcId + "-01.htm";
      }
   }

   public static void main(String[] args) {
      new _004_LongLiveThePaagrioLord(4, _004_LongLiveThePaagrioLord.class.getSimpleName(), "");
   }
}
