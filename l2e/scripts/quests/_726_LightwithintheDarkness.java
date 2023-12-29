package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _726_LightwithintheDarkness extends Quest {
   public _726_LightwithintheDarkness(int questId, String name, String descr) {
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
         return getNoQuestMsg(player);
      } else {
         if (event.equalsIgnoreCase("FortWarden-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("reward")) {
            st.calcReward(this.getId());
            st.exitQuest(true, true);
            return null;
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      QuestState qs = player.getQuestState(_727_HopeWithinTheDarkness.class.getSimpleName());
      if (qs != null) {
         st.exitQuest(true);
         return "FortWarden-01b.htm";
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId())) {
                  htmltext = "FortWarden-01.htm";
               } else {
                  htmltext = "FortWarden-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.isCond(2)) {
                  htmltext = "FortWarden-06.htm";
               } else {
                  htmltext = "FortWarden-05.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _726_LightwithintheDarkness(726, _726_LightwithintheDarkness.class.getSimpleName(), "");
   }
}
