package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _628_HuntGoldenRam extends Quest {
   public _628_HuntGoldenRam(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31554);
      this.addTalkId(31554);

      for(int npcId = 21508; npcId <= 21518; ++npcId) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{7248, 7249};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31554-02.htm")) {
            st.startQuest();
            if (hasQuestItems(player, 7247)) {
               st.setCond(3);
               htmltext = "31554-05.htm";
            } else if (hasQuestItems(player, 7246)) {
               st.setCond(2);
               htmltext = "31554-04.htm";
            }
         } else if (event.equalsIgnoreCase("31554-03a.htm")) {
            if (st.getQuestItemsCount(7248) >= 100L && st.isCond(1)) {
               st.takeItems(7248, -1L);
               st.giveItems(7246, 1L);
               st.setCond(2, true);
               htmltext = "31554-04.htm";
            }
         } else if (event.equalsIgnoreCase("31554-07.htm")) {
            st.exitQuest(true, true);
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
               if (player.getLevel() >= 66) {
                  htmltext = "31554-01.htm";
               } else {
                  htmltext = "31554-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.isCond(1)) {
                  if (st.getQuestItemsCount(7248) >= 100L) {
                     htmltext = "31554-03.htm";
                  } else {
                     htmltext = "31554-03a.htm";
                  }
               } else if (st.isCond(2)) {
                  if (st.getQuestItemsCount(7248) >= 100L && st.getQuestItemsCount(7249) >= 100L) {
                     htmltext = "31554-05.htm";
                     st.takeItems(7248, -1L);
                     st.takeItems(7249, -1L);
                     st.takeItems(7246, 1L);
                     st.calcReward(this.getId());
                     st.setCond(3, true);
                  } else if (!st.hasQuestItems(7248) && !st.hasQuestItems(7249)) {
                     htmltext = "31554-04b.htm";
                  } else {
                     htmltext = "31554-04a.htm";
                  }
               } else if (st.isCond(3)) {
                  htmltext = "31554-05a.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            if (npc.getId() >= 21508 && npc.getId() <= 21512) {
               if (st.isCond(1) || st.isCond(2)) {
                  st.calcDoDropItems(this.getId(), 7248, npc.getId(), 100);
               }
            } else if (npc.getId() >= 21513 && npc.getId() <= 21518 && st.isCond(2)) {
               st.calcDoDropItems(this.getId(), 7249, npc.getId(), 100);
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _628_HuntGoldenRam(628, _628_HuntGoldenRam.class.getSimpleName(), "");
   }
}
