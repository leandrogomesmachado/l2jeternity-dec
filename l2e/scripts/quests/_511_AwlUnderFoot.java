package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _511_AwlUnderFoot extends Quest {
   public _511_AwlUnderFoot(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(
         new int[]{
            35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364
         }
      );
      this.addTalkId(
         new int[]{
            35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364
         }
      );
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("FortressWarden-09.htm")) {
         st.startQuest();
      } else if (event.equalsIgnoreCase("FortressWarden-11.htm")) {
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
               htmltext = "FortressWarden-05.htm";
            } else {
               htmltext = "FortressWarden-00.htm";
               st.exitQuest(true);
            }
            break;
         case 1:
            if (st.isCond(1)) {
               long count = st.getQuestItemsCount(9797);
               if (count > 0L) {
                  htmltext = "FortressWarden-10.htm";
                  st.takeItems(9797, -1L);
                  st.rewardItems(9912, count);
               } else {
                  htmltext = "FortressWarden-09.htm";
               }
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _511_AwlUnderFoot(511, _511_AwlUnderFoot.class.getSimpleName(), "");
   }
}
