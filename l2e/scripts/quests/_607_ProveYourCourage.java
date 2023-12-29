package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _607_ProveYourCourage extends Quest {
   private static final String qn = "_607_ProveYourCourage";
   private static final int KADUN_ZU_KETRA = 31370;
   private static final int VARKAS_HERO_SHADITH = 25309;
   private static final int HEAD_OF_SHADITH = 7235;
   private static final int TOTEM_OF_VALOR = 7219;
   private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
   private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
   private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;

   public _607_ProveYourCourage(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31370);
      this.addTalkId(31370);
      this.addKillId(25309);
      this.questItemIds = new int[]{7235};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_607_ProveYourCourage");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31370-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31370-4.htm")) {
            if (st.getQuestItemsCount(7235) >= 1L) {
               st.takeItems(7235, -1L);
               st.giveItems(7219, 1L);
               st.addExpAndSp(0, 10000);
               st.unset("cond");
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31370-2r.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_607_ProveYourCourage");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (cond == 0) {
            if (player.getLevel() >= 75) {
               if (st.getQuestItemsCount(7213) != 1L && st.getQuestItemsCount(7214) != 1L && st.getQuestItemsCount(7215) != 1L) {
                  htmltext = "31370-00.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31370-1.htm";
               }
            } else {
               htmltext = "31370-0.htm";
               st.exitQuest(true);
            }
         } else if (cond == 1 && st.getQuestItemsCount(7235) == 0L) {
            htmltext = "31370-2r.htm";
         } else if (cond == 2 && st.getQuestItemsCount(7235) >= 1L) {
            htmltext = "31370-3.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_607_ProveYourCourage");
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 25309) {
            if (player.getParty() != null) {
               for(Player plr : player.getParty().getMembers()) {
                  QuestState qs = plr.getQuestState("_607_ProveYourCourage");
                  if (qs.getInt("cond") == 1) {
                     qs.giveItems(7235, 1L);
                     qs.set("cond", "2");
                     qs.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (st.getInt("cond") == 1) {
               st.giveItems(7235, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _607_ProveYourCourage(607, "_607_ProveYourCourage", "");
   }
}
