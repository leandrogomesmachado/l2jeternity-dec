package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public final class _403_PathToRogue extends Quest {
   private static final int[] STOLEN_ITEMS = new int[]{1186, 1187, 1188, 1189};

   private _403_PathToRogue(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30379);
      this.addTalkId(new int[]{30379, 30425});
      this.addKillId(new int[]{20035, 20042, 20045, 20051, 20054, 20060, 27038});
      this.questItemIds = new int[]{1180, 1181, 1182, 1183, 1184, 1185, 1186, 1187, 1188, 1189};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30379_2")) {
            if (player.getClassId().getId() == 0 && !st.isCompleted()) {
               if (player.getLevel() > 17) {
                  if (player.getInventory().getInventoryItemCount(1190, -1) != 0L) {
                     htmltext = "30379-04.htm";
                  } else {
                     htmltext = "30379-05.htm";
                  }
               } else {
                  htmltext = "30379-03.htm";
               }
            } else if (player.getClassId().getId() == 7) {
               htmltext = "30379-02a.htm";
            } else {
               htmltext = "30379-02.htm";
            }
         } else if (!st.isCompleted()) {
            if (event.equalsIgnoreCase("1")) {
               st.startQuest();
               st.giveItems(1180, 1L);
               htmltext = "30379-06.htm";
            } else if (event.equalsIgnoreCase("30425_1")) {
               st.takeItems(1180, -1L);
               if (st.getQuestItemsCount(1181) == 0L) {
                  st.giveItems(1181, 1L);
               }

               if (st.getQuestItemsCount(1182) == 0L) {
                  st.giveItems(1182, 1L);
               }

               st.setCond(2, true);
               htmltext = "30425-05.htm";
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
         if (npc.getId() == 30379) {
            if (cond == 0) {
               htmltext = "30379-01.htm";
            } else if (st.getQuestItemsCount(1184) == 0L) {
               if (hasQuestItems(player, new int[]{1186, 1187, 1188, 1189})) {
                  takeItems(player, 1181, 1L);
                  takeItems(player, 1182, 1L);
                  takeItems(player, 1185, 1L);
                  takeItems(player, 1186, 1L);
                  takeItems(player, 1187, 1L);
                  takeItems(player, 1188, 1L);
                  takeItems(player, 1189, 1L);
                  String done = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (done == null || done.isEmpty()) {
                     if (player.getLevel() >= 20) {
                        st.addExpAndSp(320534, 20232);
                     } else if (player.getLevel() == 19) {
                        st.addExpAndSp(456128, 26930);
                     } else {
                        st.addExpAndSp(591724, 33628);
                     }
                  }

                  st.calcReward(this.getId());
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.exitQuest(false, true);
                  htmltext = "30379-09.htm";
               } else if (st.getQuestItemsCount(1180) != 0L) {
                  htmltext = "30379-07.htm";
               } else if (st.getQuestItemsCount(1181) != 0L && st.getQuestItemsCount(1182) != 0L && st.getQuestItemsCount(1185) == 0L) {
                  htmltext = "30379-10.htm";
               } else {
                  htmltext = "30379-11.htm";
               }
            } else {
               st.takeItems(1184, -1L);
               st.giveItems(1185, 1L);
               st.setCond(5, true);
               htmltext = "30379-08.htm";
            }
         } else {
            if (!st.isStarted()) {
               return htmltext;
            }

            if (st.getQuestItemsCount(1180) != 0L) {
               htmltext = "30425-01.htm";
            } else if (st.getQuestItemsCount(1184) != 0L) {
               htmltext = "30425-08.htm";
            } else if (st.getQuestItemsCount(1185) != 0L) {
               htmltext = "30425-08.htm";
            } else if (st.getQuestItemsCount(1183) >= 10L) {
               st.takeItems(1183, -1L);
               st.giveItems(1184, 1L);
               st.setCond(4, true);
               htmltext = "30425-07.htm";
            } else {
               htmltext = "30425-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = this.getQuestState(player, false);
      if (st != null && st.isStarted()) {
         if (npc.getId() == 27038) {
            if (hasQuestItems(player, 1185)) {
               int randomItem = STOLEN_ITEMS[getRandom(STOLEN_ITEMS.length)];
               if (!hasQuestItems(player, randomItem)) {
                  st.giveItems(randomItem, 1L);
                  if (hasQuestItems(player, STOLEN_ITEMS)) {
                     st.setCond(6, true);
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            }
         } else if (st.calcDropItems(this.getId(), 1183, npc.getId(), 10)) {
            st.setCond(3, true);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _403_PathToRogue(403, _403_PathToRogue.class.getSimpleName(), "");
   }
}
