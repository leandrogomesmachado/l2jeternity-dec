package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _240_ImTheOnlyOneYouCanTrust extends Quest {
   private static final String qn = "_240_ImTheOnlyOneYouCanTrust";
   private static final int STAKATOFANGS = 14879;
   private static final int KINTAIJIN = 32640;
   private static final int[] MOBS = new int[]{22617, 22624, 22625, 22626};

   public _240_ImTheOnlyOneYouCanTrust(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32640);
      this.addTalkId(32640);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{14879};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32640-3.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 81) {
                  htmltext = "32640-1.htm";
               } else {
                  htmltext = "32640-0.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(st.getInt("cond")) {
                  case 1:
                     return "32640-8.htm";
                  case 2:
                     st.takeItems(14879, -1L);
                     st.addExpAndSp(589542, 36800);
                     st.giveItems(57, 147200L);
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                     htmltext = "32640-9.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = "32640-10.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
      if (st != null && st.getState() == 1) {
         if (getRandom(100) <= 50) {
            st.giveItems(14879, 1L);
            if (st.getQuestItemsCount(14879) >= 25L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _240_ImTheOnlyOneYouCanTrust(240, "_240_ImTheOnlyOneYouCanTrust", "");
   }
}
