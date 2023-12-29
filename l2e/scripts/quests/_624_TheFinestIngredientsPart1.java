package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _624_TheFinestIngredientsPart1 extends Quest {
   public _624_TheFinestIngredientsPart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31521);
      this.addTalkId(31521);
      this.addKillId(new int[]{21319, 21321, 21317, 21314});
      this.questItemIds = new int[]{7202, 7203, 7204};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31521-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31521-05.htm")) {
            if (st.getQuestItemsCount(7202) >= 50L && st.getQuestItemsCount(7203) >= 50L && st.getQuestItemsCount(7204) >= 50L) {
               st.takeItems(7202, -1L);
               st.takeItems(7203, -1L);
               st.takeItems(7204, -1L);
               st.calcReward(this.getId());
               st.exitQuest(true, true);
            } else {
               st.setCond(1);
               htmltext = "31521-07.htm";
            }
         }

         return htmltext;
      }
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
               if (player.getLevel() >= 73) {
                  htmltext = "31521-01.htm";
               } else {
                  htmltext = "31521-03.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.isCond(3)) {
                  if (st.getQuestItemsCount(7202) >= 50L && st.getQuestItemsCount(7203) >= 50L && st.getQuestItemsCount(7204) >= 50L) {
                     htmltext = "31521-04.htm";
                  } else {
                     htmltext = "31521-07.htm";
                  }
               } else {
                  htmltext = "31521-06.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember != null && partyMember.isInsideRadius(npc, 1500, true, false)) {
         QuestState st = partyMember.getQuestState(this.getName());
         switch(npc.getId()) {
            case 21314:
               st.calcDoDropItems(this.getId(), 7203, npc.getId(), 50);
            case 21315:
            case 21316:
            case 21318:
            case 21320:
            default:
               break;
            case 21317:
            case 21321:
               st.calcDoDropItems(this.getId(), 7204, npc.getId(), 50);
               break;
            case 21319:
               st.calcDoDropItems(this.getId(), 7202, npc.getId(), 50);
         }

         if (st.getQuestItemsCount(7202) >= 50L && st.getQuestItemsCount(7203) >= 50L && st.getQuestItemsCount(7204) >= 50L) {
            st.setCond(3, true);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _624_TheFinestIngredientsPart1(624, _624_TheFinestIngredientsPart1.class.getSimpleName(), "");
   }
}
