package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _504_CompetitionfortheBanditStronghold extends Quest {
   private static final int MESSENGER = 35437;
   private static final int TARLK_AMULET = 4332;
   private static final int TROPHY_OF_ALLIANCE = 5009;
   private static final int[] MOBS = new int[]{20570, 20571, 20572, 20573, 20574};
   private static final SiegableHall BANDIT_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(35);

   public _504_CompetitionfortheBanditStronghold(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35437);
      this.addTalkId(35437);

      for(int mob : MOBS) {
         this.addKillId(mob);
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 35437) {
            switch(st.getState()) {
               case 0:
                  if (BANDIT_STRONGHOLD.getSiege().getAttackers().size() >= 5) {
                     htmltext = "35437-00.htm";
                  } else {
                     htmltext = "35437-01.htm";
                     st.setState((byte)1);
                     st.set("cond", "1");
                     st.playSound("ItemSound.quest_accept");
                  }
                  break;
               case 1:
                  if (st.getQuestItemsCount(4332) < 30L) {
                     htmltext = "35437-02.htm";
                  } else {
                     st.takeItems(4332, 30L);
                     st.rewardItems(5009, 1L);
                     st.exitQuest(true);
                     htmltext = "35437-03.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else if (!Util.contains(MOBS, npc.getId())) {
         return null;
      } else {
         if (st.isStarted() && st.isCond(1)) {
            st.giveItems(4332, 1L);
            if (st.getQuestItemsCount(4332) < 30L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.setCond(2, true);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _504_CompetitionfortheBanditStronghold(504, _504_CompetitionfortheBanditStronghold.class.getSimpleName(), "");
   }
}
