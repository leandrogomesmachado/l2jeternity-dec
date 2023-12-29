package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _377_GiantsExploration2 extends Quest {
   public _377_GiantsExploration2(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31147);
      this.addTalkId(31147);
      this.addKillId(new int[]{22661, 22662, 22663, 22664, 22665, 22666, 22667, 22668, 22669});
      this.questItemIds = new int[]{14847};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31147-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31147-quit.htm")) {
            st.exitQuest(true, true);
         } else if (event.equalsIgnoreCase("rewardBook")) {
            if (st.getQuestItemsCount(14842) >= 5L
               && st.getQuestItemsCount(14843) >= 5L
               && st.getQuestItemsCount(14844) >= 5L
               && st.getQuestItemsCount(14845) >= 5L
               && st.getQuestItemsCount(14846) >= 5L) {
               st.takeItems(14842, 5L);
               st.takeItems(14843, 5L);
               st.takeItems(14844, 5L);
               st.takeItems(14845, 5L);
               st.takeItems(14846, 5L);
               st.calcReward(this.getId(), 1, true);
               st.playSound("ItemSound.quest_finish");
               htmltext = "31147-ok.htm";
            } else {
               htmltext = "31147-no.htm";
            }
         } else if (event.equals("randomReward")) {
            if (st.getQuestItemsCount(14842) >= 1L
               && st.getQuestItemsCount(14843) >= 1L
               && st.getQuestItemsCount(14844) >= 1L
               && st.getQuestItemsCount(14845) >= 1L
               && st.getQuestItemsCount(14846) >= 1L) {
               st.takeItems(14842, 1L);
               st.takeItems(14843, 1L);
               st.takeItems(14844, 1L);
               st.takeItems(14845, 1L);
               st.takeItems(14846, 1L);
               st.calcReward(this.getId(), 2, true);
               st.playSound("ItemSound.quest_finish");
               htmltext = "31147-ok.htm";
            } else {
               htmltext = "31147-no.htm";
            }
         } else if (this.isDigit(event)) {
            if (st.getQuestItemsCount(14842) >= 1L
               && st.getQuestItemsCount(14843) >= 1L
               && st.getQuestItemsCount(14844) >= 1L
               && st.getQuestItemsCount(14845) >= 1L
               && st.getQuestItemsCount(14846) >= 1L) {
               int itemId = Integer.parseInt(event);
               st.takeItems(14842, 1L);
               st.takeItems(14843, 1L);
               st.takeItems(14844, 1L);
               st.takeItems(14845, 1L);
               st.takeItems(14846, 1L);
               if (itemId == 9628) {
                  st.calcReward(this.getId(), 3);
               } else if (itemId == 9629) {
                  st.calcReward(this.getId(), 4);
               } else if (itemId == 9630) {
                  st.calcReward(this.getId(), 5);
               }

               st.playSound("ItemSound.quest_finish");
               htmltext = "31147-ok.htm";
            } else {
               htmltext = "31147-no.htm";
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
         if (st.getState() == 1) {
            if (st.getQuestItemsCount(14842) > 0L
               && st.getQuestItemsCount(14843) > 0L
               && st.getQuestItemsCount(14844) > 0L
               && st.getQuestItemsCount(14845) > 0L
               && st.getQuestItemsCount(14846) > 0L) {
               htmltext = "31147-03.htm";
            } else {
               htmltext = "31147-02a.htm";
            }
         } else if (player.getLevel() >= 79) {
            htmltext = "31147-01.htm";
         } else {
            htmltext = "31147-00.htm";
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
         if (st != null) {
            st.calcDropItems(this.getId(), 14847, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _377_GiantsExploration2(377, _377_GiantsExploration2.class.getSimpleName(), "");
   }
}
