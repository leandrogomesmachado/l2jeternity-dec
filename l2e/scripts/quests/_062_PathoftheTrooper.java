package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _062_PathoftheTrooper extends Quest {
   public _062_PathoftheTrooper(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32197);
      this.addTalkId(32197);
      this.addTalkId(32194);
      this.addKillId(20014);
      this.addKillId(20038);
      this.addKillId(20062);
      this.questItemIds = new int[]{9749, 9750, 9752, 9751};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32197-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32194-02.htm")) {
            st.setCond(2, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte id = st.getState();
         int cond = st.getCond();
         if (id == 2) {
            htmltext = "32197-07.htm";
         }

         if (npcId == 32197) {
            if (id == 0) {
               if (player.getClassId() != ClassId.maleSoldier) {
                  htmltext = "32197-00b.htm";
                  st.exitQuest(false);
               } else if (player.getLevel() < 18) {
                  htmltext = "32197-00a.htm";
                  st.exitQuest(false);
               } else {
                  htmltext = "32197-01.htm";
               }
            } else if (cond < 4) {
               htmltext = "32197-03.htm";
            } else if (cond == 4) {
               st.takeItems(9752, -1L);
               st.setCond(5, true);
               htmltext = "32197-04.htm";
            } else if (cond == 5) {
               if (st.getQuestItemsCount(9751) < 1L) {
                  htmltext = "32197-05.htm";
               } else {
                  st.takeItems(9751, -1L);
                  st.giveItems(9753, 1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equals("")) {
                     st.calcReward(this.getId());
                     st.calcExpAndSp(this.getId());
                     st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  }

                  st.exitQuest(false, true);
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  htmltext = "32197-06.htm";
               }
            }
         } else if (npcId == 32194) {
            if (cond == 1) {
               htmltext = "32194-01.htm";
            } else if (cond == 2) {
               if (st.getQuestItemsCount(9749) < 5L) {
                  htmltext = "32194-03.htm";
               } else {
                  st.takeItems(9749, -1L);
                  st.setCond(3, true);
                  htmltext = "32194-04.htm";
               }
            } else if (cond == 3) {
               if (st.getQuestItemsCount(9750) < 10L) {
                  htmltext = "32194-05.htm";
               } else {
                  st.takeItems(9750, -1L);
                  st.giveItems(9752, 1L);
                  st.setCond(4, true);
                  htmltext = "32194-06.htm";
               }
            } else if (cond > 3) {
               htmltext = "32194-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         npc.getId();
         st.getCond();
         if (st.isCond(2) && npc.getId() == 20014) {
            st.calcDoDropItems(this.getId(), 9749, npc.getId(), 5);
            long count = st.getQuestItemsCount(9749);
            if (count >= 5L) {
               st.playSound("ItemSound.quest_middle");
            }
         }

         if (st.isCond(3) && npc.getId() == 20038) {
            st.calcDoDropItems(this.getId(), 9750, npc.getId(), 10);
            long count = st.getQuestItemsCount(9749);
            if (count >= 10L) {
               st.playSound("ItemSound.quest_middle");
            }
         }

         if (st.isCond(5) && npc.getId() == 20062) {
            st.calcDoDropItems(this.getId(), 9751, npc.getId(), 1);
            long count = st.getQuestItemsCount(9749);
            if (count >= 1L) {
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _062_PathoftheTrooper(62, _062_PathoftheTrooper.class.getSimpleName(), "");
   }
}
