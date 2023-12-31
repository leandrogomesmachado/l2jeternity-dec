package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _617_GatherTheFlames extends Quest {
   public _617_GatherTheFlames(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{31271, 31539});
      this.addTalkId(new int[]{32049, 31271, 31539});

      for(int mobs = 22634; mobs < 22650; ++mobs) {
         this.addKillId(mobs);
      }

      for(int mobs = 18799; mobs < 18804; ++mobs) {
         this.addKillId(mobs);
      }

      this.questItemIds = new int[]{7264};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long torches = st.getQuestItemsCount(7264);
         if (event.equalsIgnoreCase("31539-03.htm")) {
            if (player.getLevel() >= 74) {
               st.startQuest();
            } else {
               htmltext = "31539-02.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31271-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31539-05.htm") && torches >= 1000L) {
            htmltext = "31539-07.htm";
            st.takeItems(7264, 1000L);
            st.calcReward(this.getId(), getRandom(1, 10));
         } else if (event.equalsIgnoreCase("31539-08.htm")) {
            st.takeItems(7264, -1L);
            st.exitQuest(true);
         } else if (event.startsWith("reward")) {
            int rewardId = Integer.parseInt(event.substring(7));
            if (rewardId > 0) {
               if (torches >= 1200L) {
                  st.takeItems(7264, 1200L);
                  st.calcReward(this.getId(), rewardId);
                  return null;
               }

               htmltext = "Incorrect item count";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 31271:
               if (st.isCreated()) {
                  htmltext = player.getLevel() >= 74 ? "31271-01.htm" : "31271-02.htm";
               } else {
                  htmltext = "31271-04.htm";
               }
               break;
            case 31539:
               if (st.isCreated()) {
                  htmltext = player.getLevel() >= 74 ? "31539-01.htm" : "31539-02.htm";
               } else {
                  htmltext = st.getQuestItemsCount(7264) >= 1000L ? "31539-04.htm" : "31539-05.htm";
               }
               break;
            case 32049:
               if (st.isStarted()) {
                  htmltext = st.getQuestItemsCount(7264) >= 1200L ? "32049-01.htm" : "32049-02.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(1)) {
            st.calcDropItems(this.getId(), 7264, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _617_GatherTheFlames(617, _617_GatherTheFlames.class.getSimpleName(), "");
   }
}
