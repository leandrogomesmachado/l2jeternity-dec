package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _904_DragonTrophyAntharas extends Quest {
   public _904_DragonTrophyAntharas(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30755);
      this.addTalkId(30755);
      this.addKillId(29068);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30755-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30755-07.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(true, true);
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
         int cond = st.getCond();
         if (npc.getId() == 30755) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 84) {
                     if (st.getQuestItemsCount(3865) > 0L) {
                        htmltext = "30755-01.htm";
                     } else {
                        htmltext = "30755-00b.htm";
                     }
                  } else {
                     htmltext = "30755-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (cond == 1) {
                     htmltext = "30755-05.htm";
                  } else if (cond == 2) {
                     htmltext = "30755-06.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 29068) {
         if (player.getParty() != null) {
            if (player.getParty().getCommandChannel() != null) {
               for(Player ccMember : player.getParty().getCommandChannel()) {
                  if (ccMember != null && ccMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     this.rewardPlayer(ccMember);
                  }
               }
            } else {
               for(Player partyMember : player.getParty().getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     this.rewardPlayer(partyMember);
                  }
               }
            }
         } else {
            this.rewardPlayer(player);
         }
      }

      return null;
   }

   private void rewardPlayer(Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _904_DragonTrophyAntharas(904, _904_DragonTrophyAntharas.class.getSimpleName(), "");
   }
}
