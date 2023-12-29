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

public final class _715_PathToBecomingALordGoddard extends Quest {
   private static final String qn = "_715_PathToBecomingALordGoddard";
   private static final int ALFRED = 35363;
   private static final int WATER_SPIRIT = 25316;
   private static final int FLAME_SPIRIT = 25306;
   private static final int CASTLE_ID = 7;

   public _715_PathToBecomingALordGoddard(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35363);
      this.addTalkId(35363);
      this.addKillId(new int[]{25316, 25306});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = this.getQuestState(player, false);
      String htmltext = null;
      if (st == null) {
         return htmltext;
      } else {
         Castle castle = CastleManager.getInstance().getCastleById(7);
         switch(event) {
            case "35363-02.htm":
               htmltext = event;
               break;
            case "35363-04a.htm":
               if (st.isCreated()) {
                  st.startQuest();
                  htmltext = event;
               }
               break;
            case "35363-05.htm":
               if (st.isCond(1)) {
                  st.setCond(2);
                  htmltext = event;
               }
               break;
            case "35363-06.htm":
               if (st.isCond(1)) {
                  st.setCond(3);
                  htmltext = event;
               }
               break;
            case "35363-12.htm":
               if (st.isCond(7)) {
                  if (castle.getSiege().getIsInProgress()) {
                     return "35363-11a.htm";
                  }

                  for(Fort fort : FortManager.getInstance().getForts()) {
                     if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                        return "35363-11a.htm";
                     }

                     if (!fort.isBorderFortress() && fort.getContractedCastleId() != 7) {
                        return "35363-11b.htm";
                     }
                  }

                  NpcSay packet = new NpcSay(
                     npc.getObjectId(),
                     23,
                     npc.getId(),
                     NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GODDARD_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GODDARD
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
      Castle castle = CastleManager.getInstance().getCastleById(7);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = talker.getId() == talker.getClan().getLeaderId() && castle.getTerritory().getLordObjectId() != talker.getObjectId()
                  ? "35363-01.htm"
                  : "35363-03.htm";
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     htmltext = "35363-04a.htm";
                     break;
                  case 2:
                     htmltext = "35363-07.htm";
                     break;
                  case 3:
                     htmltext = "35363-08.htm";
                     break;
                  case 4:
                     htmltext = "35363-09.htm";
                     st.setCond(6);
                     break;
                  case 5:
                     htmltext = "35363-10.htm";
                     st.setCond(7);
                     break;
                  case 6:
                     htmltext = "35363-09.htm";
                     break;
                  case 7:
                     htmltext = "35363-10.htm";
                     break;
                  case 8:
                  case 9:
                     htmltext = "35363-11.htm";
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(talker);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (killer.getClan() == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         Player leader = killer.getClan().getLeader().getPlayerInstance();
         if (leader != null && leader.isOnline()) {
            QuestState st = leader.getQuestState("_715_PathToBecomingALordGoddard");
            if (st == null) {
               return super.onKill(npc, killer, isSummon);
            } else {
               switch(st.getCond()) {
                  case 2:
                     if (npc.getId() == 25306) {
                        st.setCond(4);
                     }
                     break;
                  case 3:
                     if (npc.getId() == 25316) {
                        st.setCond(5);
                     }
                  case 4:
                  case 5:
                  default:
                     break;
                  case 6:
                     if (npc.getId() == 25316) {
                        st.setCond(9);
                     }
                     break;
                  case 7:
                     if (npc.getId() == 25306) {
                        st.setCond(8);
                     }
               }

               return super.onKill(npc, killer, isSummon);
            }
         } else {
            return super.onKill(npc, killer, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _715_PathToBecomingALordGoddard(715, "_715_PathToBecomingALordGoddard", "");
   }
}
