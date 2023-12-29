package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _151_CureForFeverDisease extends Quest {
   private static final String qn = "_151_CureForFeverDisease";
   private static final int POISON_SAC = 703;
   private static final int FEVER_MEDICINE = 704;
   private static final int ELIAS = 30050;
   private static final int YOHANES = 30032;

   public _151_CureForFeverDisease(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30050);
      this.addTalkId(new int[]{30050, 30032});
      this.addKillId(new int[]{20103, 20106, 20108});
      this.questItemIds = new int[]{704, 703};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_151_CureForFeverDisease");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30050-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_151_CureForFeverDisease");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                  htmltext = "30050-02.htm";
               } else {
                  htmltext = "30050-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30032:
                     if (cond == 2) {
                        htmltext = "30032-01.htm";
                        st.set("cond", "3");
                        st.takeItems(703, 1L);
                        st.giveItems(704, 1L);
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond == 3) {
                        return "30032-02.htm";
                     }

                     return htmltext;
                  case 30050:
                     if (cond == 1) {
                        return "30050-04.htm";
                     } else {
                        if (cond == 2) {
                           htmltext = "30050-05.htm";
                        } else if (cond == 3) {
                           htmltext = "30050-06.htm";
                           st.takeItems(704, 1L);
                           st.giveItems(102, 1L);
                           st.addExpAndSp(13106, 613);
                           st.exitQuest(false);
                           st.playSound("ItemSound.quest_finish");
                           showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                           return htmltext;
                        }

                        return htmltext;
                     }
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_151_CureForFeverDisease");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && getRandom(5) == 0) {
            st.set("cond", "2");
            st.giveItems(703, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _151_CureForFeverDisease(151, "_151_CureForFeverDisease", "");
   }
}
