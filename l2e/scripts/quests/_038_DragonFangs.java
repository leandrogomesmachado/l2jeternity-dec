package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _038_DragonFangs extends Quest {
   public _038_DragonFangs(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30386);
      this.addTalkId(30386);
      this.addTalkId(30034);
      this.addTalkId(30344);
      this.addKillId(20356);
      this.addKillId(21101);
      this.addKillId(21100);
      this.addKillId(20357);
      this.questItemIds = new int[]{7174, 7176, 7177, 7175, 7173};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("30386-02.htm") && cond == 0) {
            st.startQuest();
         }

         if (event.equalsIgnoreCase("30386-04.htm") && cond == 2) {
            st.takeItems(7173, 100L);
            st.giveItems(7174, 1L);
            st.setCond(3, true);
         }

         if (event.equalsIgnoreCase("30034-02a.htm") && cond == 3) {
            st.takeItems(7174, 1L);
            st.giveItems(7176, 1L);
            st.setCond(4, true);
         }

         if (event.equalsIgnoreCase("30344-02a.htm") && cond == 4) {
            st.takeItems(7176, 1L);
            st.giveItems(7177, 1L);
            st.setCond(5, true);
         }

         if (event.equalsIgnoreCase("30034-04a.htm") && cond == 5) {
            st.takeItems(7177, 1L);
            st.setCond(6, true);
         }

         if (event.equalsIgnoreCase("30034-06a.htm") && cond == 7 & st.getQuestItemsCount(7175) == 50L) {
            htmltext = "30034-06.htm";
            st.takeItems(7175, 50L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId(), getRandom(3) + 1);
            st.exitQuest(false, true);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         if (npcId == 30386 && cond == 0) {
            if (player.getLevel() < 19) {
               htmltext = "30386-01a.htm";
               st.exitQuest(true);
            } else if (player.getLevel() >= 19) {
               htmltext = "30386-01.htm";
            }
         }

         if (npcId == 30386 && cond == 1) {
            htmltext = "30386-02a.htm";
         }

         if (npcId == 30386 && cond == 2 && st.getQuestItemsCount(7173) == 100L) {
            htmltext = "30386-03.htm";
         }

         if (npcId == 30386 && cond == 3) {
            htmltext = "30386-03a.htm";
         }

         if (npcId == 30034 && cond == 3 && st.getQuestItemsCount(7174) == 1L) {
            htmltext = "30034-01.htm";
         }

         if (npcId == 30034 && cond == 4) {
            htmltext = "30034-02b.htm";
         }

         if (npcId == 30034 && cond == 5 && st.getQuestItemsCount(7177) == 1L) {
            htmltext = "30034-03.htm";
         }

         if (npcId == 30034 && cond == 6) {
            htmltext = "30034-05a.htm";
         }

         if (npcId == 30034 && cond == 7 && st.getQuestItemsCount(7175) == 50L) {
            htmltext = "30034-05.htm";
         }

         if (npcId == 30344 && cond == 4 && st.getQuestItemsCount(7176) == 1L) {
            htmltext = "30344-01.htm";
         }

         if (npcId == 30344 && cond == 5) {
            htmltext = "30344-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if ((npc.getId() == 20357 || npc.getId() == 21100) && st.isCond(1) && st.calcDropItems(this.getId(), 7173, npc.getId(), 100)) {
            st.setCond(2);
         }

         if ((npc.getId() == 20356 || npc.getId() == 21101) && st.isCond(6) && st.calcDropItems(this.getId(), 7175, npc.getId(), 50)) {
            st.setCond(7);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _038_DragonFangs(38, _038_DragonFangs.class.getSimpleName(), "");
   }
}
