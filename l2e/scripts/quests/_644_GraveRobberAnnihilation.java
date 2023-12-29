package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _644_GraveRobberAnnihilation extends Quest {
   private static final String qn = "_644_GraveRobberAnnihilation";
   Map<String, int[]> Rewards = new HashMap<>();
   private static final int KARUDA = 32017;
   private static final int GOODS = 8088;

   public _644_GraveRobberAnnihilation(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32017);
      this.addTalkId(32017);
      this.addKillId(new int[]{22003, 22004, 22005, 22006, 22008});
      this.Rewards.put("var", new int[]{1865, 30});
      this.Rewards.put("ask", new int[]{1867, 40});
      this.Rewards.put("ior", new int[]{1869, 30});
      this.Rewards.put("coa", new int[]{1870, 30});
      this.Rewards.put("cha", new int[]{1871, 30});
      this.Rewards.put("abo", new int[]{1872, 40});
      this.questItemIds = new int[]{8088};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_644_GraveRobberAnnihilation");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32017-02.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (this.Rewards.containsKey(event)) {
            if (st.getQuestItemsCount(8088) == 120L) {
               htmltext = "32017-04.htm";
               st.takeItems(8088, -1L);
               st.rewardItems(this.Rewards.get(event)[0], (long)((int[])this.Rewards.get(event))[1]);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "32017-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_644_GraveRobberAnnihilation");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 20 && player.getLevel() <= 33) {
                  htmltext = "32017-01.htm";
               } else {
                  htmltext = "32017-06.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  htmltext = "32017-05.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(8088) == 120L) {
                     htmltext = "32017-03.htm";
                  } else {
                     htmltext = "32017-07.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_644_GraveRobberAnnihilation");
      if (st == null) {
         return null;
      } else {
         Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
         if (partyMember == null) {
            return null;
         } else {
            if (st.getQuestItemsCount(8088) < 120L && st.getRandom(10) < 5) {
               st.giveItems(8088, 1L);
               if (st.getQuestItemsCount(8088) == 120L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _644_GraveRobberAnnihilation(644, "_644_GraveRobberAnnihilation", "");
   }
}
