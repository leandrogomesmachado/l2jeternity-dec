package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public final class _700_CursedLife extends Quest {
   private static final String qn = "_700_CursedLife";
   private static final int ORBYU = 32560;
   private static final int[] MOBS = new int[]{22602, 22603, 22604, 22605};
   private static final int SWALLOWED_SKULL = 13872;
   private static final int SWALLOWED_STERNUM = 13873;
   private static final int SWALLOWED_BONES = 13874;

   public _700_CursedLife(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32560);
      this.addTalkId(32560);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{13872, 13873, 13874};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_700_CursedLife");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32560-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32560-quit.htm")) {
            st.unset("cond");
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_700_CursedLife");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32560) {
            QuestState first = player.getQuestState("_10273_GoodDayToFly");
            if (first != null && first.getState() == 2 && st.getState() == 0 && player.getLevel() >= 75) {
               htmltext = "32560-01.htm";
            } else {
               switch(st.getState()) {
                  case 0:
                     htmltext = "32560-00.htm";
                     break;
                  case 1:
                     long count1 = st.getQuestItemsCount(13874);
                     long count2 = st.getQuestItemsCount(13873);
                     long count3 = st.getQuestItemsCount(13872);
                     if (count1 <= 0L && count2 <= 0L && count3 <= 0L) {
                        htmltext = "32560-04.htm";
                     } else {
                        long reward = count1 * 500L + count2 * 5000L + count3 * 50000L;
                        st.takeItems(13874, -1L);
                        st.takeItems(13873, -1L);
                        st.takeItems(13872, -1L);
                        st.giveItems(57, reward);
                        st.playSound("ItemSound.quest_itemget");
                        htmltext = "32560-06.htm";
                     }
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_700_CursedLife");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && ArrayUtils.contains(MOBS, npc.getId())) {
            int chance = st.getRandom(100);
            if (chance < 5) {
               st.giveItems(13872, 1L);
            } else if (chance < 20) {
               st.giveItems(13873, 1L);
            } else {
               st.giveItems(13874, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _700_CursedLife(700, "_700_CursedLife", "");
   }
}
