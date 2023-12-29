package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10282_ToTheSeedOfAnnihilation extends Quest {
   public _10282_ToTheSeedOfAnnihilation(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32733);
      this.addTalkId(32733);
      this.addTalkId(32734);
      this.questItemIds = new int[]{15512};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32733-07.htm")) {
            st.giveItems(15512, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("32734-02.htm")) {
            st.takeItems(15512, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
         if (st.isCompleted()) {
            if (npc.getId() == 32733) {
               htmltext = "32733-09.htm";
            } else if (npc.getId() == 32734) {
               htmltext = "32734-03.htm";
            }
         } else if (st.getState() == 0) {
            if (player.getLevel() >= 84) {
               htmltext = "32733-01.htm";
            } else {
               htmltext = "32733-00.htm";
            }
         } else if (st.isCond(1)) {
            if (npc.getId() == 32733) {
               htmltext = "32733-08.htm";
            } else if (npc.getId() == 32734) {
               htmltext = "32734-01.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10282_ToTheSeedOfAnnihilation(10282, _10282_ToTheSeedOfAnnihilation.class.getSimpleName(), "");
   }
}
