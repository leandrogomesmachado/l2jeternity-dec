package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _005_MinersFavor extends Quest {
   public _005_MinersFavor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30554);
      this.addTalkId(30554);
      this.addTalkId(30517);
      this.addTalkId(30518);
      this.addTalkId(30520);
      this.addTalkId(30526);
      this.questItemIds = new int[]{1547, 1552, 1548, 1549, 1550, 1551};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30554-03.htm")) {
            st.giveItems(1547, 1L);
            st.giveItems(1552, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("30526-02.htm")) {
            st.takeItems(1552, -1L);
            st.giveItems(1549, 1L);
            if (st.getQuestItemsCount(1547) > 0L
               && st.getQuestItemsCount(1548) > 0L
               && st.getQuestItemsCount(1549) > 0L
               && st.getQuestItemsCount(1550) > 0L
               && st.getQuestItemsCount(1551) > 0L) {
               st.setCond(2, true);
            }
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
      int cond = st.getCond();
      int npcId = npc.getId();
      switch(st.getState()) {
         case 0:
            if (player.getLevel() >= 2) {
               htmltext = "30554-02.htm";
            } else {
               htmltext = "30554-01.htm";
               st.exitQuest(true);
            }
            break;
         case 1:
            switch(cond) {
               case 1:
                  if (npcId == 30554) {
                     return "30554-04.htm";
                  } else if (npcId == 30517) {
                     if (st.getQuestItemsCount(1547) > 0L) {
                        if (st.getQuestItemsCount(1550) == 0L) {
                           htmltext = "30517-01.htm";
                           st.giveItems(1550, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "30517-02.htm";
                        }

                        return htmltext;
                     }

                     return htmltext;
                  } else if (npcId == 30518) {
                     if (st.getQuestItemsCount(1547) > 0L && st.getQuestItemsCount(1550) > 0L) {
                        if (st.getQuestItemsCount(1548) == 0L) {
                           htmltext = "30518-01.htm";
                           st.giveItems(1548, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "30518-02.htm";
                        }

                        return htmltext;
                     }

                     return htmltext;
                  } else {
                     if (npcId == 30520) {
                        if (st.getQuestItemsCount(1547) > 0L && st.getQuestItemsCount(1548) > 0L) {
                           if (st.getQuestItemsCount(1551) == 0L) {
                              htmltext = "30520-01.htm";
                              st.giveItems(1551, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30520-02.htm";
                           }

                           return htmltext;
                        }
                     } else if (npcId == 30526 && st.getQuestItemsCount(1547) > 0L && st.getQuestItemsCount(1551) > 0L) {
                        if (st.getQuestItemsCount(1549) == 0L && st.getQuestItemsCount(1552) > 0L) {
                           htmltext = "30526-01.htm";
                        } else {
                           htmltext = "30526-03.htm";
                        }

                        return htmltext;
                     }

                     return htmltext;
                  }
               case 2:
                  if (npcId == 30554) {
                     htmltext = "30554-06.htm";
                     st.calcExpAndSp(this.getId());
                     st.calcReward(this.getId());
                     st.exitQuest(false, true);
                     showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  }

                  return htmltext;
               default:
                  return htmltext;
            }
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _005_MinersFavor(5, _005_MinersFavor.class.getSimpleName(), "");
   }
}
