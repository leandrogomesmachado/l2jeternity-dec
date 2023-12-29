package l2e.scripts.quests;

import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _604_DaimonTheWhiteEyedPart2 extends Quest {
   public static Npc _npc = null;

   public _604_DaimonTheWhiteEyedPart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31683);
      this.addTalkId(31683);
      this.addTalkId(31541);
      this.addKillId(25290);
      this.questItemIds = new int[]{7192, 7193, 7194};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         if (event.equalsIgnoreCase("31683-02.htm")) {
            if (player.getLevel() < 73) {
               st.exitQuest(true);
               htmltext = "31683-00b.htm";
            } else {
               st.takeItems(7192, 1L);
               st.giveItems(7193, 1L);
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("31541-02.htm")) {
            if (st.getQuestItemsCount(7193) == 0L) {
               htmltext = "31541-04.htm";
            } else if (_npc != null) {
               htmltext = "31541-03.htm";
            } else if (ServerVariables.getLong(this.getName(), 0L) + 10800000L > System.currentTimeMillis()) {
               htmltext = "31541-05.htm";
            } else {
               st.takeItems(7193, 1L);
               _npc = st.addSpawn(25290, 186320, -43904, -3175);
               if (_npc != null) {
                  _npc.broadcastPacket(new NpcSay(_npc.getObjectId(), 0, _npc.getId(), NpcStringId.OH_WHERE_I_BE_WHO_CALL_ME), 2000);
               }

               st.setCond(2, true);
               st.startQuestTimer("DAIMON_Fail", 12000000L);
            }
         } else if (event.equalsIgnoreCase("31683-04.htm")) {
            if (st.getQuestItemsCount(7194) >= 1L) {
               htmltext = "list.htm";
            } else {
               st.exitQuest(true);
               htmltext = "31683-05.htm";
            }
         } else {
            if (event.equalsIgnoreCase("INT_MEN")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 1);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("INT_WIT")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 2);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("MEN_INT")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 3);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("MEN_WIT")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 4);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("WIT_INT")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 5);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("WIT_MEN")) {
               st.takeItems(7194, 1L);
               st.calcReward(this.getId(), 6);
               st.exitQuest(true, true);
               return null;
            }

            if (event.equalsIgnoreCase("DAIMON_Fail") && _npc != null) {
               _npc.broadcastPacket(
                  new NpcSay(_npc.getObjectId(), 0, _npc.getId(), NpcStringId.I_CARRY_THE_POWER_OF_DARKNESS_AND_HAVE_RETURNED_FROM_THE_ABYSS), 2000
               );
               _npc.deleteMe();
               _npc = null;
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
         switch(st.getState()) {
            case 0:
               if (npc.getId() == 31683 && cond == 0) {
                  if (st.getQuestItemsCount(7192) >= 1L) {
                     htmltext = "31683-01.htm";
                  } else {
                     htmltext = "31683-00a.htm";
                  }
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31541:
                     if (cond == 1) {
                        if (ServerVariables.getLong(this.getName(), 0L) + 10800000L > System.currentTimeMillis()) {
                           htmltext = "31541-05.htm";
                        } else {
                           htmltext = "31541-01.htm";
                        }
                     } else if (cond == 2) {
                        if (_npc != null) {
                           htmltext = "31541-03.htm";
                        } else if (ServerVariables.getLong(this.getName(), 0L) + 10800000L > System.currentTimeMillis()) {
                           htmltext = "31541-05.htm";
                        } else {
                           _npc = st.addSpawn(25290, 186320, -43904, -3175);
                           _npc.broadcastPacket(new NpcSay(_npc.getObjectId(), 0, _npc.getId(), NpcStringId.OH_WHERE_I_BE_WHO_CALL_ME), 2000);
                           st.startQuestTimer("DAIMON_Fail", 12000000L);
                        }
                     } else if (cond == 3) {
                        htmltext = "31541-05.htm";
                     }
                     break;
                  case 31683:
                     if (cond == 1) {
                        htmltext = "31683-02a.htm";
                     } else if (cond == 3) {
                        if (st.getQuestItemsCount(7194) >= 1L) {
                           htmltext = "31683-03.htm";
                        } else {
                           htmltext = "31683-06.htm";
                        }
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 25290) {
         for(Player partyMember : this.getMembersCond(player, npc, "cond")) {
            if (partyMember != null) {
               QuestState st = partyMember.getQuestState(this.getName());
               if (st != null) {
                  if (st.getQuestItemsCount(7193) > 0L) {
                     st.takeItems(7193, 1L);
                  }

                  st.giveItems(7194, 1L);
                  st.setCond(3, true);
               }
            }
         }

         if (_npc != null) {
            _npc = null;
         }
      }

      return null;
   }

   public static void main(String[] args) {
      new _604_DaimonTheWhiteEyedPart2(604, _604_DaimonTheWhiteEyedPart2.class.getSimpleName(), "");
   }
}
