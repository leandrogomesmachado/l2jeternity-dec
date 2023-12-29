package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10290_LandDragonConqueror extends Quest {
   public _10290_LandDragonConqueror(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30755);
      this.addTalkId(30755);
      this.addKillId(new int[]{29019, 29066, 29067, 29068});
      this.questItemIds = new int[]{15523, 15522};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30755-07.htm")) {
            st.giveItems(15522, 1L);
            st.startQuest();
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
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 83 && st.getQuestItemsCount(3865) >= 1L) {
                  htmltext = "30755-01.htm";
               } else if (player.getLevel() < 83) {
                  htmltext = "30755-02.htm";
               } else {
                  htmltext = "30755-04.htm";
               }
               break;
            case 1:
               if (st.isCond(1) && st.getQuestItemsCount(15522) >= 1L) {
                  htmltext = "30755-08.htm";
               } else if (st.isCond(1) && st.getQuestItemsCount(15522) == 0L) {
                  st.giveItems(15522, 1L);
                  htmltext = "30755-09.htm";
               } else if (st.isCond(2)) {
                  st.takeItems(15523, 1L);
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
                  st.exitQuest(false, true);
                  htmltext = "30755-10.htm";
               }
               break;
            case 2:
               htmltext = "30755-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (player.getParty() != null) {
            for(Player partyMember : player.getParty().getMembers()) {
               QuestState qs = partyMember.getQuestState(this.getName());
               if (qs != null && qs.isCond(1) && qs.calcDropItems(this.getId(), 15523, npc.getId(), 1)) {
                  qs.takeItems(15522, 1L);
                  qs.setCond(2);
               }
            }
         } else if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 15523, npc.getId(), 1)) {
            st.takeItems(15522, 1L);
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10290_LandDragonConqueror(10290, _10290_LandDragonConqueror.class.getSimpleName(), "");
   }
}
