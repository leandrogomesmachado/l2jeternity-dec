package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _727_HopeWithinTheDarkness extends Quest {
   public _727_HopeWithinTheDarkness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411});
      this.addTalkId(new int[]{36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411});
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("CastleWarden-05.htm")) {
         st.startQuest();
      }

      return event;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.getClan() != null && npc.getCastle() != null && player.getClan().getCastleId() == npc.getCastle().getId()) {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId())) {
                  htmltext = "CastleWarden-01.htm";
               } else {
                  htmltext = "CastleWarden-04.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.isCond(1)) {
                  htmltext = Rnd.chance(50) ? "CastleWarden-06.htm" : "CastleWarden-13.htm";
               } else if (st.isCond(2)) {
                  st.calcReward(this.getId());
                  st.exitQuest(true, true);
                  htmltext = "CastleWarden-14.htm";
               }
         }

         return htmltext;
      } else {
         return "CastleWarden-03.htm";
      }
   }

   public static void main(String[] args) {
      new _727_HopeWithinTheDarkness(727, _727_HopeWithinTheDarkness.class.getSimpleName(), "");
   }
}
