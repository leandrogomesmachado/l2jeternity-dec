package l2e.scripts.quests;

import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _655_AGrandPlanForTamingWildBeasts extends Quest {
   private static final int MESSENGER = 35627;
   private static final int STONE = 8084;
   private static final int TRAINER_LICENSE = 8293;
   private static final SiegableHall BEAST_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(63);

   public _655_AGrandPlanForTamingWildBeasts(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35627);
      this.addTalkId(35627);
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 35627) {
            switch(st.getState()) {
               case 0:
                  if (BEAST_STRONGHOLD.getSiege().getAttackers().size() >= 5) {
                     htmltext = "35627-00.htm";
                  } else {
                     htmltext = "35627-01.htm";
                     st.setState((byte)1);
                     st.set("cond", "1");
                     st.playSound("ItemSound.quest_accept");
                  }
                  break;
               case 1:
                  if (st.getQuestItemsCount(8084) < 10L) {
                     htmltext = "35627-02.htm";
                  } else {
                     st.takeItems(8084, 10L);
                     st.giveItems(8293, 1L);
                     st.exitQuest(true);
                     htmltext = "35627-03.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void checkCrystalofPurity(Player player) {
      QuestState st = player.getQuestState(_655_AGrandPlanForTamingWildBeasts.class.getSimpleName());
      if (st != null && st.isCond(1) && st.getQuestItemsCount(8084) < 10L) {
         st.giveItems(8084, 1L);
      }
   }

   public static void main(String[] args) {
      new _655_AGrandPlanForTamingWildBeasts(655, _655_AGrandPlanForTamingWildBeasts.class.getSimpleName(), "");
   }
}
