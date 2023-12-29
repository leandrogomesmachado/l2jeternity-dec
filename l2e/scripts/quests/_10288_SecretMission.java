package l2e.scripts.quests;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10288_SecretMission extends Quest {
   public _10288_SecretMission(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{31350, 32780});
      this.addTalkId(new int[]{31350, 32757, 32780});
      this.addFirstTalkId(32780);
      this.registerQuestItems(new int[]{15529});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 31350) {
            if (event.equalsIgnoreCase("31350-05.htm")) {
               st.giveItems(15529, 1L);
               st.startQuest();
            }
         } else if (npc.getId() == 32757 && event.equalsIgnoreCase("32757-03.htm")) {
            st.takeItems(15529, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         } else if (npc.getId() == 32780) {
            if (st.getState() == 1) {
               if (event.equalsIgnoreCase("32780-05.html")) {
                  st.setCond(2, true);
               }
            } else if (st.getState() == 2 && event.equalsIgnoreCase("teleport")) {
               player.teleToLocation(118833, -80589, -2688, true);
               return null;
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 31350) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82) {
                     htmltext = "31350-01.htm";
                  } else {
                     htmltext = "31350-00.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(1)) {
                     htmltext = "31350-06.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "31350-07.htm";
                  }
                  break;
               case 2:
                  htmltext = "31350-08.htm";
            }
         } else if (npc.getId() == 32780) {
            if (st.isCond(1)) {
               htmltext = "32780-03.html";
            } else if (st.isCond(2)) {
               htmltext = "32780-06.html";
            }
         } else if (npc.getId() == 32757 && st.isCond(2)) {
            return "32757-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         Quest q = QuestManager.getInstance().getQuest(this.getName());
         st = q.newQuestState(player);
      }

      if (npc.getId() == 32780) {
         return st.getState() == 2 ? "32780-01.html" : "32780-00.html";
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _10288_SecretMission(10288, _10288_SecretMission.class.getSimpleName(), "");
   }
}
