package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _012_SecretMeetingWithVarkaSilenos extends Quest {
   public _012_SecretMeetingWithVarkaSilenos(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31296);
      this.addTalkId(31296);
      this.addTalkId(31258);
      this.addTalkId(31378);
      this.questItemIds = new int[]{7232};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31296-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31258-02.htm")) {
            st.giveItems(7232, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31378-02.htm")) {
            st.takeItems(7232, 1L);
            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
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
      int npcId = npc.getId();
      switch(st.getState()) {
         case 0:
            if (npcId == 31296) {
               if (player.getLevel() >= 74) {
                  htmltext = "31296-01.htm";
               } else {
                  htmltext = "31296-02.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(npcId) {
               case 31258:
                  switch(cond) {
                     case 1:
                        return "31258-01.htm";
                     case 2:
                        return "31258-03.htm";
                     default:
                        return htmltext;
                  }
               case 31296:
                  switch(cond) {
                     case 1:
                        return "31296-04.htm";
                     default:
                        return htmltext;
                  }
               case 31378:
                  switch(cond) {
                     case 2:
                        if (st.getQuestItemsCount(7232) > 0L) {
                           htmltext = "31378-01.htm";
                        }

                        return htmltext;
                     default:
                        return htmltext;
                  }
               default:
                  return htmltext;
            }
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _012_SecretMeetingWithVarkaSilenos(12, _012_SecretMeetingWithVarkaSilenos.class.getSimpleName(), "");
   }
}
