package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _011_SecretMeetingWithKetraOrcs extends Quest {
   public _011_SecretMeetingWithKetraOrcs(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31296);
      this.addTalkId(31296);
      this.addTalkId(31256);
      this.addTalkId(31371);
      this.questItemIds = new int[]{7231};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31296-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31256-02.htm")) {
            st.giveItems(7231, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31371-02.htm")) {
            st.takeItems(7231, 1L);
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
               case 31256:
                  switch(cond) {
                     case 1:
                        return "31256-01.htm";
                     case 2:
                        return "31256-03.htm";
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
               case 31371:
                  switch(cond) {
                     case 2:
                        if (st.getQuestItemsCount(7231) > 0L) {
                           htmltext = "31371-01.htm";
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
      new _011_SecretMeetingWithKetraOrcs(11, _011_SecretMeetingWithKetraOrcs.class.getSimpleName(), "");
   }
}
