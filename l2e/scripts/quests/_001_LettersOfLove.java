package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _001_LettersOfLove extends Quest {
   public _001_LettersOfLove(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30048);
      this.addTalkId(30048);
      this.addTalkId(30006);
      this.addTalkId(30033);
      this.questItemIds = new int[]{687, 688, 1079, 1080};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30048-05.htm")) {
            st.startQuest();
            st.giveItems(687, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int npcId = npc.getId();
      int cond = st.getCond();
      switch(st.getState()) {
         case 0:
            if (npcId == 30048) {
               if (player.getLevel() >= 2 && cond == 0) {
                  htmltext = "30048-02.htm";
               } else {
                  htmltext = "30048-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            if (npcId == 30048) {
               switch(cond) {
                  case 2:
                     if (st.getQuestItemsCount(688) > 0L) {
                        htmltext = "30048-07.htm";
                        st.takeItems(688, -1L);
                        st.giveItems(1079, 1L);
                        st.setCond(3, true);
                     }

                     return htmltext;
                  case 3:
                     if (st.getQuestItemsCount(1079) > 0L) {
                        htmltext = "30048-08.htm";
                     }

                     return htmltext;
                  case 4:
                     if (st.getQuestItemsCount(1080) > 0L) {
                        htmltext = "30048-09.htm";
                        st.takeItems(1080, -1L);
                        st.calcExpAndSp(this.getId());
                        st.calcReward(this.getId());
                        st.exitQuest(false, true);
                        showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                     }

                     return htmltext;
                  default:
                     htmltext = "30048-06.htm";
               }
            } else if (npcId == 30006) {
               switch(cond) {
                  case 1:
                     if (st.getQuestItemsCount(687) > 0L) {
                        htmltext = "30006-01.htm";
                        st.takeItems(687, -1L);
                        st.giveItems(688, 1L);
                        st.setCond(2, true);
                     }

                     return htmltext;
                  default:
                     if (cond > 1) {
                        htmltext = "30006-02.htm";
                     }
               }
            } else if (npcId == 30033) {
               switch(cond) {
                  case 3:
                     if (st.getQuestItemsCount(1079) > 0L) {
                        htmltext = "30033-01.htm";
                        st.takeItems(1079, -1L);
                        st.giveItems(1080, 1L);
                        st.setCond(4, true);
                     }

                     return htmltext;
                  default:
                     if (cond > 3) {
                        htmltext = "30033-02.htm";
                     }
               }
            }
            break;
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _001_LettersOfLove(1, _001_LettersOfLove.class.getSimpleName(), "");
   }
}
