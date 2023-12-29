package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _274_AgainstWolfMen extends Quest {
   private static final String qn = "_274_AgainstWolfMen";
   private static final int MARAKU_WEREWOLF_HEAD = 1477;
   private static final int NECKLACE_OF_VALOR = 1507;
   private static final int NECKLACE_OF_COURAGE = 1506;
   private static final int MARAKU_WOLFMEN_TOTEM = 1501;

   public _274_AgainstWolfMen(int scriptId, String name, String descr) {
      super(scriptId, name, descr);
      this.addStartNpc(30569);
      this.addTalkId(30569);
      this.addKillId(new int[]{20363, 20364});
      this.questItemIds = new int[]{1477};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_274_AgainstWolfMen");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30569-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_274_AgainstWolfMen");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         long totems = st.getQuestItemsCount(1501);
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 3) {
                  if (player.getLevel() > 8) {
                     if (st.getQuestItemsCount(1507) <= 0L && st.getQuestItemsCount(1506) <= 0L) {
                        htmltext = "30569-07.htm";
                        st.exitQuest(true);
                     } else {
                        htmltext = "30569-02.htm";
                     }
                  } else {
                     htmltext = "30569-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30569-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1477) < 40L) {
                  htmltext = "30569-04.htm";
               } else {
                  int amount = 3500;
                  if (totems > 0L) {
                     amount = (int)((long)amount + 600L * totems);
                  }

                  htmltext = "30569-05.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.giveItems(57, (long)amount);
                  st.takeItems(1477, -1L);
                  st.takeItems(1501, -1L);
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_274_AgainstWolfMen");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            long count = st.getQuestItemsCount(1477);
            if (count < 40L) {
               if (count < 39L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               }

               st.giveItems(1477, 1L);
               if (getRandom(100) <= 15) {
                  st.giveItems(1501, 1L);
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _274_AgainstWolfMen(274, "_274_AgainstWolfMen", "");
   }
}
