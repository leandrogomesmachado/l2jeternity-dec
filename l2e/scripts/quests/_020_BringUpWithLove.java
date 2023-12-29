package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _020_BringUpWithLove extends Quest {
   public _020_BringUpWithLove(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31537);
      this.addTalkId(31537);
      this.addFirstTalkId(31537);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         switch(event) {
            case "31537-12.htm":
               st.startQuest();
               break;
            case "31537-03.htm":
               if (hasQuestItems(player, 15473)) {
                  return "31537-03a.htm";
               }

               giveItems(player, 15473, 1L);
               break;
            case "31537-15.htm":
               takeItems(player, 7185, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
               break;
            case "31537-21.htm":
               if (player.getLevel() < 82) {
                  return "31537-23.htm";
               }

               if (hasQuestItems(player, 15473)) {
                  return "31537-22.htm";
               }

               giveItems(player, 15473, 1L);
         }

         return event;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         this.newQuestState(player);
      }

      return "31537-20.htm";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() < 82 ? "31537-00.htm" : "31537-01.htm";
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     htmltext = "31537-13.htm";
                     break;
                  case 2:
                     htmltext = "31537-14.htm";
               }
         }

         return htmltext;
      }
   }

   public static void checkJewelOfInnocence(Player player) {
      QuestState st = player.getQuestState(_020_BringUpWithLove.class.getSimpleName());
      if (st != null && st.isCond(1) && !st.hasQuestItems(7185) && getRandom(20) == 0) {
         st.giveItems(7185, 1L);
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _020_BringUpWithLove(20, _020_BringUpWithLove.class.getSimpleName(), "");
   }
}
