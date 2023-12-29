package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _016_TheComingDarkness extends Quest {
   public _016_TheComingDarkness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31517);
      this.addTalkId(31517);
      this.addTalkId(31512);
      this.addTalkId(31513);
      this.addTalkId(31514);
      this.addTalkId(31515);
      this.addTalkId(31516);
      this.questItemIds = new int[]{7167};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         switch(event) {
            case "31517-2.htm":
               st.startQuest();
               st.giveItems(7167, 5L);
               break;
            case "31512-1.htm":
            case "31513-1.htm":
            case "31514-1.htm":
            case "31515-1.htm":
            case "31516-1.htm":
               int npcId = Integer.parseInt(event.replace("-1.htm", ""));
               if (cond == npcId - 31511 && st.hasQuestItems(7167)) {
                  st.takeItems(7167, 1L);
                  st.setCond(cond + 1, true);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               QuestState st2 = player.getQuestState("_017_LightAndDarkness");
               if (st2 == null || st2.getState() != 2) {
                  htmltext = "31517-0b.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 61) {
                  htmltext = "31517-0.htm";
               } else {
                  htmltext = "31517-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 31517) {
                  if (cond > 0 && cond < 6) {
                     if (st.getQuestItemsCount(7167) > 0L) {
                        htmltext = "31517-3a.htm";
                     } else {
                        htmltext = "31517-3b.htm";
                     }
                  } else if (st.isCond(6)) {
                     htmltext = "31517-3.htm";
                     st.calcExpAndSp(this.getId());
                     st.exitQuest(false, true);
                  }
               } else if (npcId - 31511 == st.getCond()) {
                  htmltext = npcId + "-0.htm";
               } else {
                  htmltext = npcId + "-1.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _016_TheComingDarkness(16, _016_TheComingDarkness.class.getSimpleName(), "");
   }
}
