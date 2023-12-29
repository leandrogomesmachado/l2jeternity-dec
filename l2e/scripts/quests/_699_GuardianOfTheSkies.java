package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public final class _699_GuardianOfTheSkies extends Quest {
   private static final String qn = "_699_GuardianOfTheSkies";
   private static final int LEKON = 32557;
   private static final int GOLDEN_FEATHER = 13871;
   private static final int[] MOBS = new int[]{22614, 22615, 25623, 25633};
   private static final int DROP_CHANCE = 80;

   public _699_GuardianOfTheSkies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32557);
      this.addTalkId(32557);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{13871};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_699_GuardianOfTheSkies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32557-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32557-quit.htm")) {
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
      QuestState st = player.getQuestState("_699_GuardianOfTheSkies");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32557) {
            QuestState first = player.getQuestState("_10273_GoodDayToFly");
            if (first != null && first.getState() == 2 && st.getState() == 0 && player.getLevel() >= 75) {
               htmltext = "32557-01.htm";
            } else {
               switch(st.getState()) {
                  case 0:
                     htmltext = "32557-00.htm";
                     break;
                  case 1:
                     long count = st.getQuestItemsCount(13871);
                     if (count > 0L) {
                        st.takeItems(13871, -1L);
                        st.giveItems(57, count * 2300L);
                        st.playSound("ItemSound.quest_itemget");
                        htmltext = "32557-06.htm";
                     } else {
                        htmltext = "32557-04.htm";
                     }
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_699_GuardianOfTheSkies");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && ArrayUtils.contains(MOBS, npc.getId())) {
            int chance = (int)(80.0F * Config.RATE_QUEST_DROP);
            int numItems = chance / 100;
            chance %= 100;
            if (st.getRandom(100) < chance) {
               ++numItems;
            }

            if (numItems > 0) {
               st.giveItems(13871, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _699_GuardianOfTheSkies(699, "_699_GuardianOfTheSkies", "");
   }
}
