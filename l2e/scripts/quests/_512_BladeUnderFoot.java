package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _512_BladeUnderFoot extends Quest {
   public _512_BladeUnderFoot(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411});
      this.addTalkId(new int[]{36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("CastleWarden-04.htm")) {
         st.startQuest();
      } else if (event.equalsIgnoreCase("CastleWarden-09.htm")) {
         st.exitQuest(true, true);
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      switch(st.getState()) {
         case 0:
            if (player.getLevel() >= getMinLvl(this.getId())) {
               htmltext = "CastleWarden-03.htm";
            } else {
               htmltext = "CastleWarden-00.htm";
               st.exitQuest(true);
            }
            break;
         case 1:
            if (st.isCond(1)) {
               long count = st.getQuestItemsCount(9797);
               if (count > 0L) {
                  htmltext = "CastleWarden-08.htm";
                  st.takeItems(9797, -1L);
                  st.rewardItems(9912, count);
               } else {
                  htmltext = "CastleWarden-04.htm";
               }
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _512_BladeUnderFoot(512, _512_BladeUnderFoot.class.getSimpleName(), "");
   }
}
