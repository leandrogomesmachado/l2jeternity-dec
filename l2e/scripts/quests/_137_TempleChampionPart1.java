package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _137_TempleChampionPart1 extends Quest {
   private static final String qn = "_137_TempleChampionPart1";
   private static final int SYLVAIN = 30070;
   private static final int[] MOBS = new int[]{20083, 20144, 20199, 20200, 20201, 20202};
   private static final int FRAGMENT = 10340;
   private static final int EXECUTOR = 10334;
   private static final int MISSIONARY = 10339;

   public _137_TempleChampionPart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30070);
      this.addTalkId(30070);
      this.addKillId(MOBS);
      this.questItemIds = new int[]{10340};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_137_TempleChampionPart1");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         switch(event) {
            case "30070-02.htm":
               st.startQuest();
               break;
            case "30070-05.htm":
               st.set("talk", "1");
               break;
            case "30070-06.htm":
               st.set("talk", "2");
               break;
            case "30070-08.htm":
               st.unset("talk");
               st.setCond(2, true);
               break;
            case "30070-16.htm":
               if (st.isCond(2) && st.hasQuestItems(10334) && st.hasQuestItems(10339)) {
                  st.takeItems(10334, -1L);
                  st.takeItems(10339, -1L);
                  st.rewardItems(57, 69146L);
                  if (player.getLevel() < 41) {
                     st.addExpAndSp(219975, 13047);
                  }

                  st.exitQuest(false, true);
               }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_137_TempleChampionPart1");
      if (st == null) {
         return htmltext;
      } else if (st.isCompleted()) {
         return getAlreadyCompletedMsg(player);
      } else {
         switch(st.getInt("cond")) {
            case 1:
               switch(st.getInt("talk")) {
                  case 1:
                     htmltext = "30070-05.htm";
                     return htmltext;
                  case 2:
                     htmltext = "30070-06.htm";
                     return htmltext;
                  default:
                     htmltext = "30070-03.htm";
                     return htmltext;
               }
            case 2:
               htmltext = "30070-08.htm";
               break;
            case 3:
               if (st.getInt("talk") == 1) {
                  htmltext = "30070-10.htm";
               } else if (st.getQuestItemsCount(10340) >= 30L) {
                  st.set("talk", "1");
                  htmltext = "30070-09.htm";
                  st.takeItems(10340, -1L);
               }
               break;
            default:
               htmltext = player.getLevel() >= 35 && st.hasQuestItems(10334) && st.hasQuestItems(10339) ? "30070-01.htm" : "30070-00.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_137_TempleChampionPart1");
      if (st != null && st.isStarted() && st.isCond(2) && st.getQuestItemsCount(10340) < 30L) {
         st.giveItems(10340, 1L);
         if (st.getQuestItemsCount(10340) >= 30L) {
            st.setCond(3, true);
         } else {
            st.playSound("ItemSound.quest_itemget");
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _137_TempleChampionPart1(137, "_137_TempleChampionPart1", "");
   }
}
