package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _251_NoSecrets extends Quest {
   private static final int[] MOB = new int[]{22775, 22776, 22778};
   private static final int[] MOB1 = new int[]{22780, 22782, 22783, 22784, 22785};

   public _251_NoSecrets(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30201);
      this.addTalkId(30201);

      for(int npcId : MOB) {
         this.addKillId(npcId);
      }

      for(int npcId : MOB1) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{15508, 15509};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30201-05.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() > 81 ? "30201-01.htm" : "30201-02.htm";
               break;
            case 1:
               if (st.isCond(1)) {
                  htmltext = "30201-06.htm";
               } else if (st.isCond(2) && st.getQuestItemsCount(15508) >= 10L && st.getQuestItemsCount(15509) >= 5L) {
                  htmltext = "30201-07.htm";
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
                  st.exitQuest(false, true);
               }
               break;
            case 2:
               htmltext = "30201-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isStarted() && st.isCond(1)) {
         int npcId = npc.getId();
         if (Util.contains(MOB, npcId)) {
            st.calcDoDropItems(this.getId(), 15508, npc.getId(), 10);
         } else if (Util.contains(MOB1, npcId)) {
            st.calcDoDropItems(this.getId(), 15509, npc.getId(), 5);
         }

         if (st.getQuestItemsCount(15508) >= 10L && st.getQuestItemsCount(15509) >= 5L) {
            st.setCond(2, true);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _251_NoSecrets(251, _251_NoSecrets.class.getSimpleName(), "");
   }
}
