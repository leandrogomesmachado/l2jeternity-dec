package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _642_APowerfulPrimevalCreature extends Quest {
   public _642_APowerfulPrimevalCreature(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32105);
      this.addTalkId(32105);
      this.addKillId(new int[]{22196, 22197, 22198, 22199, 22215, 22216, 22217, 22218, 22223, 18344});
      this.questItemIds = new int[]{8774, 8775};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long count_tissue = st.getQuestItemsCount(8774);
         long count_egg = st.getQuestItemsCount(8775);
         if (event.equalsIgnoreCase("32105-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32105-06a.htm")) {
            st.takeItems(8774, -1L);
            st.calcRewardPerItem(this.getId(), 1, (int)count_tissue);
         } else if (event.equalsIgnoreCase("32105-07.htm")) {
            if (count_tissue < 150L || count_egg == 0L) {
               htmltext = "32105-07a.htm";
            }
         } else if (this.isDigit(event)) {
            if (count_tissue >= 150L && count_egg >= 1L) {
               htmltext = "32105-08.htm";
               st.takeItems(8774, 150L);
               st.takeItems(8775, 1L);
               st.calcReward(this.getId(), Integer.parseInt(event));
            } else {
               htmltext = "32105-07a.htm";
            }
         } else if (event.equalsIgnoreCase("32105-10.htm")) {
            if (count_tissue >= 450L) {
               htmltext = "32105-10.htm";
            } else {
               htmltext = "32105-11.htm";
            }
         } else if (event.equalsIgnoreCase("32105-09.htm")) {
            st.exitQuest(true, true);
         } else if (this.isDigit(event)) {
            if (count_tissue >= 450L) {
               htmltext = "32105-10.htm";
               st.takeItems(8774, 450L);
               st.calcReward(this.getId(), Integer.parseInt(event));
            } else {
               htmltext = "32105-11.htm";
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
         long count = st.getQuestItemsCount(8774);
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getCond();
         if (id == 0) {
            if (npcId == 32105 & cond == 0) {
               if (player.getLevel() >= getMinLvl(this.getId())) {
                  htmltext = "32105-01.htm";
               } else {
                  htmltext = "32105-00.htm";
                  st.exitQuest(true);
               }
            }
         } else if (id == 1 && npcId == 32105 & cond == 1) {
            if (count == 0L) {
               htmltext = "32105-05.htm";
            } else {
               htmltext = "32105-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player member = this.getRandomPartyMemberState(player, (byte)1);
      if (member != null) {
         QuestState st = member.getQuestState(this.getName());
         if (st != null && st.isCond(1)) {
            if (npc.getId() == 18344) {
               st.calcDropItems(this.getId(), 8775, npc.getId(), Integer.MAX_VALUE);
            } else {
               st.calcDropItems(this.getId(), 8774, npc.getId(), Integer.MAX_VALUE);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _642_APowerfulPrimevalCreature(642, _642_APowerfulPrimevalCreature.class.getSimpleName(), "");
   }
}
