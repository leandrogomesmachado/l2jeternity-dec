package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _625_TheFinestIngredientsPart2 extends Quest {
   public static Npc _npc = null;

   public _625_TheFinestIngredientsPart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31521);
      this.addTalkId(new int[]{31521, 31542});
      this.addKillId(25296);
      this.questItemIds = new int[]{7209, 7210};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         int _state = st.getState();
         int cond = st.getCond();
         if (event.equalsIgnoreCase("jeremy_q0625_0104.htm") && _state == 0) {
            if (st.getQuestItemsCount(7205) == 0L) {
               st.exitQuest(true);
               htmltext = "jeremy_q0625_0102.htm";
            } else {
               st.startQuest();
               st.takeItems(7205, 1L);
               st.giveItems(7209, 1L);
            }
         } else if (event.equalsIgnoreCase("jeremy_q0625_0301.htm") && _state == 1 && cond == 3) {
            if (st.getQuestItemsCount(7210) == 0L) {
               htmltext = "jeremy_q0625_0302.htm";
            } else {
               st.takeItems(7210, 1L);
               st.calcReward(this.getId(), Rnd.get(1, 6));
            }

            st.exitQuest(true, true);
         } else if (event.equalsIgnoreCase("yetis_table_q0625_0201.htm") && _state == 1 && cond == 1) {
            if (ServerVariables.getLong(this.getName(), 0L) + 10800000L > System.currentTimeMillis()) {
               htmltext = "yetis_table_q0625_0204.htm";
            } else if (st.getQuestItemsCount(7209) == 0L) {
               htmltext = "yetis_table_q0625_0203.htm";
            } else if (_npc != null) {
               htmltext = "yetis_table_q0625_0202.htm";
            } else {
               st.takeItems(7209, 1L);
               st.setCond(2, true);
               ThreadPoolManager.getInstance().schedule(new _625_TheFinestIngredientsPart2.BumbalumpSpawner(st), 1000L);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (npcId == 31521) {
                  if (player.getLevel() < 73) {
                     st.exitQuest(true);
                     htmltext = "jeremy_q0625_0103.htm";
                  } else if (st.getQuestItemsCount(7205) == 0L) {
                     st.exitQuest(true);
                     htmltext = "jeremy_q0625_0102.htm";
                  } else {
                     st.set("cond", "0");
                     htmltext = "jeremy_q0625_0101.htm";
                  }
               }
               break;
            case 1:
               if (npcId == 31521) {
                  if (cond == 1) {
                     htmltext = "jeremy_q0625_0105.htm";
                  } else if (cond == 2) {
                     htmltext = "jeremy_q0625_0202.htm";
                  } else if (cond == 3) {
                     htmltext = "jeremy_q0625_0201.htm";
                  }
               } else if (npcId == 31542) {
                  if (ServerVariables.getLong(this.getName(), 0L) + 10800000L > System.currentTimeMillis()) {
                     htmltext = "yetis_table_q0625_0204.htm";
                  } else if (cond == 1) {
                     htmltext = "yetis_table_q0625_0101.htm";
                  } else if (cond == 2) {
                     if (_npc != null) {
                        htmltext = "yetis_table_q0625_0202.htm";
                     } else {
                        ThreadPoolManager.getInstance().schedule(new _625_TheFinestIngredientsPart2.BumbalumpSpawner(st), 1000L);
                        htmltext = "yetis_table_q0625_0201.htm";
                     }
                  } else if (cond == 3) {
                     htmltext = "yetis_table_q0625_0204.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      for(Player partyMember : this.getMembersCond(player, npc, "cond")) {
         if (partyMember != null) {
            QuestState st = partyMember.getQuestState(this.getName());
            if (st != null && (st.getCond() == 1 || st.getCond() == 2)) {
               if (st.getQuestItemsCount(7209) > 0L) {
                  st.takeItems(7209, 1L);
               }

               st.giveItems(7210, 1L);
               st.setCond(3, true);
            }
         }
      }

      if (_npc != null) {
         _npc = null;
      }

      return null;
   }

   public static void main(String[] args) {
      new _625_TheFinestIngredientsPart2(625, _625_TheFinestIngredientsPart2.class.getSimpleName(), "");
   }

   public class BumbalumpSpawner implements Runnable {
      private int tiks = 0;

      public BumbalumpSpawner(QuestState st) {
         if (_625_TheFinestIngredientsPart2._npc == null) {
            _625_TheFinestIngredientsPart2._npc = st.addSpawn(25296, 158240, -121536, -2253);
         }
      }

      @Override
      public void run() {
         if (_625_TheFinestIngredientsPart2._npc != null) {
            if (this.tiks == 0) {
               _625_TheFinestIngredientsPart2._npc
                  .broadcastPacket(
                     new NpcSay(
                        _625_TheFinestIngredientsPart2._npc.getObjectId(), 0, _625_TheFinestIngredientsPart2._npc.getId(), NpcStringId.I_WILL_TASTE_YOUR_BLOOD
                     ),
                     2000
                  );
            }

            if (this.tiks < 1200 && _625_TheFinestIngredientsPart2._npc != null) {
               ++this.tiks;
               if (this.tiks == 1200) {
                  _625_TheFinestIngredientsPart2._npc
                     .broadcastPacket(
                        new NpcSay(
                           _625_TheFinestIngredientsPart2._npc.getObjectId(),
                           0,
                           _625_TheFinestIngredientsPart2._npc.getId(),
                           NpcStringId.CURSE_THOSE_WHO_DEFY_THE_GODS
                        ),
                        2000
                     );
               }

               ThreadPoolManager.getInstance().schedule(this, 1000L);
            } else {
               _625_TheFinestIngredientsPart2._npc.deleteMe();
               _625_TheFinestIngredientsPart2._npc = null;
            }
         }
      }
   }
}
