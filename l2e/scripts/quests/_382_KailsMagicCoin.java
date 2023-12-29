package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _382_KailsMagicCoin extends Quest {
   public _382_KailsMagicCoin(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30687);
      this.addTalkId(30687);
      this.addKillId(new int[]{21017, 21019, 21020, 21022});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30687-03.htm")) {
            if (player.getLevel() >= 55 && st.getQuestItemsCount(5898) > 0L) {
               st.startQuest();
            } else {
               htmltext = "30687-01.htm";
               st.exitQuest(true);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         if (st.getQuestItemsCount(5898) == 0L || player.getLevel() < 55) {
            htmltext = "30687-01.htm";
            st.exitQuest(true);
         } else if (cond == 0) {
            htmltext = "30687-02.htm";
         } else {
            htmltext = "30687-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player member = this.getRandomPartyMemberState(player, (byte)1);
      if (member != null) {
         QuestState st = member.getQuestState(this.getName());
         if (st != null && st.getQuestItemsCount(5898) > 0L) {
            if (npc.getId() == 21022) {
               st.calcDropItems(this.getId(), 5961 + getRandom(3), npc.getId(), Integer.MAX_VALUE);
            } else {
               int itemId = npc.getId() == 21017 ? 5961 : (npc.getId() == 21019 ? 5962 : 5963);
               st.calcDropItems(this.getId(), itemId, npc.getId(), Integer.MAX_VALUE);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _382_KailsMagicCoin(382, _382_KailsMagicCoin.class.getSimpleName(), "");
   }
}
