package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _276_TotemOfTheHestui extends Quest {
   private static final String qn = "_276_TotemOfTheHestui";
   private static final int TANAPI = 30571;
   private static final int KASHA_PARASITE = 1480;
   private static final int KASHA_CRYSTAL = 1481;
   private static final int HESTUIS_TOTEM = 1500;
   private static final int LEATHER_PANTS = 29;

   public _276_TotemOfTheHestui(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30571);
      this.addTalkId(30571);
      this.addKillId(new int[]{20479, 27044});
      this.questItemIds = new int[]{1480, 1481};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_276_TotemOfTheHestui");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30571-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_276_TotemOfTheHestui");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 3) {
                  if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                     htmltext = "30571-02.htm";
                  } else {
                     htmltext = "30571-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30571-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1481) == 0L) {
                  htmltext = "30571-04.htm";
               } else {
                  htmltext = "30571-05.htm";
                  st.takeItems(1481, -1L);
                  st.takeItems(1480, -1L);
                  st.giveItems(1500, 1L);
                  st.giveItems(29, 1L);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
                  showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_276_TotemOfTheHestui");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            switch(npc.getId()) {
               case 20479:
                  if (st.getInt("cond") == 1 && st.getQuestItemsCount(1481) == 0L) {
                     long count = st.getQuestItemsCount(1480);
                     int random = getRandom(100);
                     if ((count < 70L || random >= 90)
                        && (count < 65L || random >= 75)
                        && (count < 60L || random >= 60)
                        && (count < 52L || random >= 45)
                        && (count < 50L || random >= 30)) {
                        st.playSound("ItemSound.quest_itemget");
                        st.giveItems(1480, 1L);
                     } else {
                        st.addSpawn(27044, npc);
                        st.takeItems(1480, count);
                     }
                  }
                  break;
               case 27044:
                  if (st.getInt("cond") == 1 && st.getQuestItemsCount(1481) == 0L) {
                     st.playSound("ItemSound.quest_middle");
                     st.giveItems(1481, 1L);
                     st.set("cond", "2");
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _276_TotemOfTheHestui(276, "_276_TotemOfTheHestui", "");
   }
}
