package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public class _901_HowLavasaurusesAreMade extends Quest {
   private static final String qn = "_901_HowLavasaurusesAreMade";
   private static final int ROONEY = 32049;
   private static final int TOTEM_OF_BODY = 21899;
   private static final int TOTEM_OF_SPIRIT = 21900;
   private static final int TOTEM_OF_COURAGE = 21901;
   private static final int TOTEM_OF_FORTITUDE = 21902;
   private static final int LAVASAURUS_STONE_FRAGMENT = 21909;
   private static final int LAVASAURUS_HEAD_FRAGMENT = 21910;
   private static final int LAVASAURUS_BODY_FRAGMENT = 21911;
   private static final int LAVASAURUS_HORN_FRAGMENT = 21912;
   private static final int[] KILLING_MONSTERS = new int[]{18799, 18800, 18801, 18802, 18803};
   private static final int DROP_CHANCE = 5;

   public _901_HowLavasaurusesAreMade(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32049);
      this.addTalkId(32049);

      for(int mobs : KILLING_MONSTERS) {
         this.addKillId(mobs);
      }

      this.questItemIds = new int[]{21909, 21910, 21911, 21912};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_901_HowLavasaurusesAreMade");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32049-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32049-12a.htm")) {
            st.giveItems(21899, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(QuestState.QuestType.DAILY);
         } else if (event.equalsIgnoreCase("32049-12b.htm")) {
            st.giveItems(21900, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(QuestState.QuestType.DAILY);
         } else if (event.equalsIgnoreCase("32049-12c.htm")) {
            st.giveItems(21902, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(QuestState.QuestType.DAILY);
         } else if (event.equalsIgnoreCase("32049-12d.htm")) {
            st.giveItems(21901, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(QuestState.QuestType.DAILY);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_901_HowLavasaurusesAreMade");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 32049) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 76) {
                     htmltext = "32049-01.htm";
                  } else {
                     htmltext = "32049-00.htm";
                  }
                  break;
               case 1:
                  if (cond == 1) {
                     htmltext = "32049-04.htm";
                  } else if (cond == 2) {
                     if (st.getInt("collect") == 1) {
                        htmltext = "32049-07.htm";
                     } else if (st.getQuestItemsCount(21909) >= 10L
                        && st.getQuestItemsCount(21910) >= 10L
                        && st.getQuestItemsCount(21911) >= 10L
                        && st.getQuestItemsCount(21912) >= 10L) {
                        htmltext = "32049-05.htm";
                        st.takeItems(21909, -1L);
                        st.takeItems(21910, -1L);
                        st.takeItems(21911, -1L);
                        st.takeItems(21912, -1L);
                        st.set("collect", "1");
                     } else {
                        htmltext = "32049-06.htm";
                     }
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 76) {
                        htmltext = "32049-01.htm";
                     } else {
                        htmltext = "32049-00.htm";
                     }
                  } else {
                     htmltext = "32049-01a.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_901_HowLavasaurusesAreMade");
         if (st == null) {
            return null;
         } else {
            int id = st.getState();
            int cond = st.getInt("cond");
            if (id == 1 && cond == 1) {
               if (ArrayUtils.contains(KILLING_MONSTERS, npc.getId())) {
                  if (getRandom(100) < 5 && st.getQuestItemsCount(21909) < 10L) {
                     st.giveItems(21909, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (getRandom(100) < 5 && st.getQuestItemsCount(21910) < 10L) {
                     st.giveItems(21910, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (getRandom(100) < 5 && st.getQuestItemsCount(21911) < 10L) {
                     st.giveItems(21911, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (getRandom(100) < 5 && st.getQuestItemsCount(21912) < 10L) {
                     st.giveItems(21912, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }

               if (st.getQuestItemsCount(21909) >= 10L
                  && st.getQuestItemsCount(21910) >= 10L
                  && st.getQuestItemsCount(21911) >= 10L
                  && st.getQuestItemsCount(21912) >= 10L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _901_HowLavasaurusesAreMade(901, "_901_HowLavasaurusesAreMade", "");
   }
}
