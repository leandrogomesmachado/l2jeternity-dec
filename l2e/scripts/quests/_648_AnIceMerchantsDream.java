package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _648_AnIceMerchantsDream extends Quest {
   public _648_AnIceMerchantsDream(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32020, 32023});
      this.addTalkId(new int[]{32020, 32023});

      for(int i = 22080; i <= 22098; ++i) {
         if (i != 22095) {
            this.addKillId(i);
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32020-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32020-07.htm")) {
            int silver = (int)st.getQuestItemsCount(8077);
            int black = (int)st.getQuestItemsCount(8078);
            st.takeItems(8077, (long)silver);
            st.calcRewardPerItem(this.getId(), 1, silver);
            if (black > 0) {
               st.takeItems(8078, (long)black);
               st.calcRewardPerItem(this.getId(), 2, black);
            }
         } else if (event.equalsIgnoreCase("32020-09.htm")) {
            st.exitQuest(true, true);
         } else if (event.equalsIgnoreCase("32023-04.htm")) {
            st.playSound("ItemSound2.broken_key");
            st.takeItems(8077, 1L);
         } else if (event.equalsIgnoreCase("32023-05.htm")) {
            if (st.getRandom(100) <= 25) {
               st.giveItems(8078, 1L);
               st.playSound("ItemSound3.sys_enchant_sucess");
            } else {
               htmltext = "32023-06.htm";
               st.playSound("ItemSound3.sys_enchant_failed");
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         int silver = (int)st.getQuestItemsCount(8077);
         int black = (int)st.getQuestItemsCount(8078);
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 53) {
                  htmltext = "32020-01.htm";
               } else {
                  htmltext = "32020-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npc.getId() == 32020) {
                  if (cond == 1) {
                     if (silver <= 0 && black <= 0) {
                        htmltext = "32020-04.htm";
                     } else {
                        QuestState st2 = player.getQuestState("_115_TheOtherSideOfTruth");
                        htmltext = "32020-05.htm";
                        if (st2 != null && st2.isCompleted()) {
                           htmltext = "32020-10.htm";
                           st.setCond(2, true);
                        }
                     }
                  } else if (cond == 2) {
                     if (silver <= 0 && black <= 0) {
                        htmltext = "32020-04a.htm";
                     } else {
                        htmltext = "32020-10.htm";
                     }
                  }
               } else if (npc.getId() == 32023) {
                  if (st.getState() == 0) {
                     htmltext = "32023-00.htm";
                  } else if (silver > 0) {
                     htmltext = "32023-02.htm";
                  } else {
                     htmltext = "32023-01.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember != null) {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.getCond() >= 1) {
            st.calcDropItems(this.getId(), 8077, npc.getId(), Integer.MAX_VALUE);
            if (st.getCond() >= 2) {
               st.calcDropItems(this.getId(), 8057, npc.getId(), Integer.MAX_VALUE);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _648_AnIceMerchantsDream(648, _648_AnIceMerchantsDream.class.getSimpleName(), "");
   }
}
