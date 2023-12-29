package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _376_GiantsExploration1 extends Quest {
   public _376_GiantsExploration1(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31147);
      this.addTalkId(31147);
      this.addKillId(new int[]{22670, 22671, 22672, 22673, 22674, 22675, 22676, 22677});
      this.questItemIds = new int[]{14841};
   }

   private String onExchangeRequest(String event, QuestState st, int id, long rem) {
      if (st.getQuestItemsCount(14836) >= rem
         && st.getQuestItemsCount(14837) >= rem
         && st.getQuestItemsCount(14838) >= rem
         && st.getQuestItemsCount(14839) >= rem
         && st.getQuestItemsCount(14840) >= rem) {
         st.takeItems(14836, rem);
         st.takeItems(14837, rem);
         st.takeItems(14838, rem);
         st.takeItems(14839, rem);
         st.takeItems(14840, rem);
         st.calcReward(this.getId(), id);
         st.playSound("ItemSound.quest_finish");
         return "31147-ok.htm";
      } else {
         return "31147-no.htm";
      }
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
         } else if (this.isDigit(event)) {
            int id = Integer.parseInt(event);
            if (id == 9967) {
               htmltext = this.onExchangeRequest(event, st, 1, 10L);
            } else if (id == 9968) {
               htmltext = this.onExchangeRequest(event, st, 2, 10L);
            } else if (id == 9969) {
               htmltext = this.onExchangeRequest(event, st, 3, 10L);
            } else if (id == 9970) {
               htmltext = this.onExchangeRequest(event, st, 4, 10L);
            } else if (id == 9971) {
               htmltext = this.onExchangeRequest(event, st, 5, 10L);
            } else if (id == 9972) {
               htmltext = this.onExchangeRequest(event, st, 6, 10L);
            } else if (id == 9973) {
               htmltext = this.onExchangeRequest(event, st, 7, 10L);
            } else if (id == 9974) {
               htmltext = this.onExchangeRequest(event, st, 8, 10L);
            } else if (id == 9975) {
               htmltext = this.onExchangeRequest(event, st, 9, 10L);
            } else if (id == 9628) {
               htmltext = this.onExchangeRequest(event, st, 10, 1L);
            } else if (id == 9629) {
               htmltext = this.onExchangeRequest(event, st, 11, 1L);
            } else if (id == 9630) {
               htmltext = this.onExchangeRequest(event, st, 12, 1L);
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
            if (st.getQuestItemsCount(14836) > 0L
               && st.getQuestItemsCount(14837) > 0L
               && st.getQuestItemsCount(14838) > 0L
               && st.getQuestItemsCount(14839) > 0L
               && st.getQuestItemsCount(14840) > 0L) {
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
            st.calcDropItems(this.getId(), 14841, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _376_GiantsExploration1(376, _376_GiantsExploration1.class.getSimpleName(), "");
   }
}
