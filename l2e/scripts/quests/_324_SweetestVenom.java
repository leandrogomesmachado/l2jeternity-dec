package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _324_SweetestVenom extends Quest {
   private static final String qn = "_324_SweetestVenom";
   private static int VENOM_SAC = 1077;

   public _324_SweetestVenom(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30351);
      this.addTalkId(30351);
      this.addKillId(new int[]{20034, 20038, 20043});
      this.questItemIds = new int[]{VENOM_SAC};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_324_SweetestVenom");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30351-04.htm")) {
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
      QuestState st = player.getQuestState("_324_SweetestVenom");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 18) {
                  htmltext = "30351-03.htm";
               } else {
                  htmltext = "30351-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(VENOM_SAC) < 10L) {
                  htmltext = "30351-05.htm";
               } else {
                  st.takeItems(VENOM_SAC, -1L);
                  st.giveItems(57, 5810L);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30351-06.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_324_SweetestVenom");
      if (st == null) {
         return null;
      } else {
         int chance = 22 + (npc.getId() - 20000 ^ 34) / 4;
         int count = (int)st.getQuestItemsCount(VENOM_SAC);
         if (count < 10 && st.getRandom(100) < chance) {
            st.giveItems(VENOM_SAC, 1L);
            if (count == 9) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _324_SweetestVenom(324, "_324_SweetestVenom", "");
   }
}
