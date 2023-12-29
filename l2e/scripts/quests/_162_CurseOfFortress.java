package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _162_CurseOfFortress extends Quest {
   private static final String qn = "_162_CurseOfFortress";
   private static final int BONE_FRAGMENT = 1158;
   private static final int ELF_SKULL = 1159;
   private static final int BONE_SHIELD = 625;

   public _162_CurseOfFortress(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30147);
      this.addTalkId(30147);
      this.addKillId(new int[]{20033, 20345, 20371, 20463, 20464, 20504});
      this.questItemIds = new int[]{1158, 1159};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_162_CurseOfFortress");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30147-04.htm")) {
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
      QuestState st = player.getQuestState("_162_CurseOfFortress");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  htmltext = "30147-00.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 12 && player.getLevel() <= 21) {
                  htmltext = "30147-02.htm";
               } else {
                  htmltext = "30147-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1159) < 3L && st.getQuestItemsCount(1158) < 10L) {
                  htmltext = "30147-05.htm";
               } else {
                  htmltext = "30147-06.htm";
                  st.takeItems(1159, -1L);
                  st.takeItems(1158, -1L);
                  st.rewardItems(57, 24000L);
                  st.addExpAndSp(22652, 1004);
                  st.giveItems(625, 1L);
                  st.unset("cond");
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_162_CurseOfFortress");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(4) == 1) {
            switch(npc.getId()) {
               case 20033:
               case 20345:
               case 20371:
                  if (st.getQuestItemsCount(1159) < 3L) {
                     st.giveItems(1159, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
                  break;
               case 20463:
               case 20464:
               case 20504:
                  if (st.getQuestItemsCount(1158) < 10L) {
                     st.giveItems(1158, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
            }

            if (st.getQuestItemsCount(1158) >= 10L && st.getQuestItemsCount(1159) >= 3L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _162_CurseOfFortress(162, "_162_CurseOfFortress", "");
   }
}
