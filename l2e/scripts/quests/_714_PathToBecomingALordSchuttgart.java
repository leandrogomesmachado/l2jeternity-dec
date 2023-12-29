package l2e.scripts.quests;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _714_PathToBecomingALordSchuttgart extends Quest {
   private static final String qn = "_714_PathToBecomingALordSchuttgart";
   private static final int AUGUST = 35555;
   private static final int NEWYEAR = 31961;
   private static final int YASENI = 31958;
   private static final int GOLEM_SHARD = 17162;
   private static final int[] GOLEMS = new int[]{22809, 22810, 22811, 22812};
   private static final int CASTLE_ID = 9;

   public _714_PathToBecomingALordSchuttgart(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35555);
      this.addTalkId(new int[]{35555, 31961, 31958});
      this.addKillId(GOLEMS);
      this.registerQuestItems(new int[]{17162});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = this.getQuestState(player, false);
      String htmltext = null;
      if (st == null) {
         return htmltext;
      } else {
         Castle castle = CastleManager.getInstance().getCastleById(9);
         switch(event) {
            case "35555-02.htm":
            case "35555-06.htm":
            case "31961-03.htm":
               htmltext = event;
               break;
            case "35555-04.htm":
               if (st.isCreated()) {
                  st.startQuest();
                  htmltext = event;
               }
               break;
            case "35555-08.htm":
               if (st.isCond(1)) {
                  st.setCond(2);
                  htmltext = event;
               }
               break;
            case "31961-04.htm":
               if (st.isCond(2)) {
                  st.setCond(3);
                  htmltext = event;
               }
               break;
            case "31958-02.htm":
               if (st.isCond(4)) {
                  st.setCond(5);
                  htmltext = event;
               }
               break;
            case "35555-13.htm":
               if (st.isCond(7)) {
                  if (castle.getSiege().getIsInProgress()) {
                     return "35555-12a.htm";
                  }

                  for(Fort fort : FortManager.getInstance().getForts()) {
                     if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                        return "35555-12a.htm";
                     }

                     if (!fort.isBorderFortress() && fort.getContractedCastleId() != 9) {
                        return "35555-12b.htm";
                     }
                  }

                  NpcSay packet = new NpcSay(
                     npc.getObjectId(),
                     23,
                     npc.getId(),
                     NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_SCHUTTGART
                  );
                  packet.addStringParameter(player.getName());
                  npc.broadcastPacket(packet);
                  castle.getTerritory().changeOwner(castle.getOwner());
                  st.exitQuest(true, true);
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      QuestState st = this.getQuestState(talker, true);
      String htmltext = getNoQuestMsg(talker);
      Castle castle = CastleManager.getInstance().getCastleById(9);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         switch(npc.getId()) {
            case 31958:
               switch(st.getCond()) {
                  case 4:
                     return "31958-01.htm";
                  case 5:
                     return "31958-03.htm";
                  case 6:
                     if (getQuestItemsCount(talker, 17162) >= 300L) {
                        takeItems(talker, 17162, -1L);
                        st.setCond(7);
                        htmltext = "31958-04.htm";
                     }

                     return htmltext;
                  case 7:
                     return "31958-05.htm";
                  default:
                     return htmltext;
               }
            case 31961:
               switch(st.getCond()) {
                  case 2:
                     return "31961-02.htm";
                  case 3:
                     QuestState qs1 = this.getQuestState(talker, false);
                     qs1 = talker.getQuestState(_114_ResurrectionOfAnOldManager.class.getSimpleName());
                     QuestState qs2 = this.getQuestState(talker, false);
                     qs2 = talker.getQuestState(_120_PavelsResearch.class.getSimpleName());
                     QuestState qs3 = this.getQuestState(talker, false);
                     qs3 = talker.getQuestState(_121_PavelTheGiant.class.getSimpleName());
                     if (qs3 == null || !qs3.isCompleted()) {
                        return "31961-07.htm";
                     } else if (qs1 == null || !qs1.isCompleted()) {
                        return "31961-05.htm";
                     } else {
                        if (qs2 != null && qs2.isCompleted()) {
                           st.setCond(4);
                           htmltext = "31961-01.htm";
                        } else {
                           htmltext = "31961-06.htm";
                        }

                        return htmltext;
                     }
                  case 4:
                     return "31961-01.htm";
                  default:
                     return htmltext;
               }
            case 35555:
               switch(st.getState()) {
                  case 0:
                     htmltext = talker.getId() == talker.getClan().getLeaderId() && castle.getTerritory().getLordObjectId() != talker.getObjectId()
                        ? "35555-01.htm"
                        : "35555-03.htm";
                     break;
                  case 1:
                     switch(st.getCond()) {
                        case 1:
                           htmltext = "35555-06.htm";
                           break;
                        case 2:
                        case 3:
                           htmltext = "35555-09.htm";
                           break;
                        case 4:
                           htmltext = "35555-10.htm";
                           break;
                        case 5:
                        case 6:
                           htmltext = "35555-11.htm";
                           break;
                        case 7:
                           htmltext = "35555-12.htm";
                     }
                  case 2:
                     htmltext = getAlreadyCompletedMsg(talker);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = this.getRandomPartyMemberState(killer, 5, 3, npc);
      if (st != null && giveItemRandomly(killer, npc, 17162, 1L, 300L, 1.0, true)) {
         st.setCond(6);
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _714_PathToBecomingALordSchuttgart(714, "_714_PathToBecomingALordSchuttgart", "");
   }
}
