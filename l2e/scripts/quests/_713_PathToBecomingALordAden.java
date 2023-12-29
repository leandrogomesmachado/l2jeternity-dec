package l2e.scripts.quests;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _713_PathToBecomingALordAden extends Quest {
   private static final String qn = "_713_PathToBecomingALordAden";
   private static final int LOGAN = 35274;
   private static final int ORVEN = 30857;
   private static final int TAIK_SEEKER = 20666;
   private static final int TAIK_LEADER = 20669;
   private static final int CASTLE_ID = 5;
   private static final int REQUIRED_CLAN_MEMBERS = 5;

   public _713_PathToBecomingALordAden(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35274);
      this.addTalkId(new int[]{35274, 30857});
      this.addKillId(new int[]{20666, 20669});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      String htmltext = null;
      if (qs == null) {
         return htmltext;
      } else {
         Castle castle = CastleManager.getInstance().getCastleById(5);
         switch(event) {
            case "30857-02.htm":
               htmltext = event;
               break;
            case "35274-03.htm":
               if (qs.isCreated()) {
                  qs.startQuest();
                  htmltext = event;
               }
               break;
            case "30857-03.htm":
               if (qs.isCond(1)) {
                  qs.setMemoState(0);
                  qs.setCond(2);
                  htmltext = event;
               }
               break;
            case "35274-06.htm":
               if (qs.isCond(7)) {
                  if (castle.getSiege().getIsInProgress()) {
                     return "35274-05a.htm";
                  }

                  for(Fort fort : FortManager.getInstance().getForts()) {
                     if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                        return "35274-05a.htm";
                     }

                     if (!fort.isBorderFortress() && fort.getContractedCastleId() != 5) {
                        return "35274-05b.htm";
                     }
                  }

                  NpcSay packet = new NpcSay(
                     npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_ADEN
                  );
                  packet.addStringParameter(player.getName());
                  npc.broadcastPacket(packet);
                  castle.getTerritory().changeOwner(castle.getOwner());
                  qs.exitQuest(true, true);
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      QuestState qs = this.getQuestState(talker, true);
      String htmltext = getNoQuestMsg(talker);
      Castle castle = CastleManager.getInstance().getCastleById(5);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         switch(npc.getId()) {
            case 30857:
               switch(qs.getCond()) {
                  case 1:
                     return "30857-01.htm";
                  case 2:
                     return "30857-04.htm";
                  case 3:
                  case 4:
                  case 6:
                  default:
                     return htmltext;
                  case 5:
                     int clanMemberCount = 0;

                     for(ClanMember clanMember : talker.getClan().getMembers()) {
                        Player member = clanMember.getPlayerInstance();
                        if (member != null && member.isOnline() && member.getId() != talker.getId()) {
                           QuestState st = this.getQuestState(member, false);
                           st = member.getQuestState(_359_ForSleeplessDeadmen.class.getSimpleName());
                           if (st.isCompleted()) {
                              ++clanMemberCount;
                           }
                        }
                     }

                     if (clanMemberCount >= 5) {
                        qs.setCond(7);
                        htmltext = "30857-06.htm";
                     } else {
                        htmltext = "30857-05.htm";
                     }

                     return htmltext;
                  case 7:
                     return "30857-07.htm";
               }
            case 35274:
               switch(qs.getState()) {
                  case 0:
                     htmltext = talker.getId() == talker.getClan().getLeaderId() && castle.getTerritory().getLordObjectId() != talker.getObjectId()
                        ? "35274-01.htm"
                        : "35274-02.htm";
                     break;
                  case 1:
                     switch(qs.getCond()) {
                        case 1:
                        case 2:
                        case 3:
                        case 5:
                           htmltext = "35274-04.htm";
                        case 4:
                        case 6:
                        default:
                           break;
                        case 7:
                           htmltext = "35274-05.htm";
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
      QuestState qs = killer.getQuestState("_713_PathToBecomingALordAden");
      if (qs != null && qs.isCond(2)) {
         if (qs.getMemoState() < 100) {
            qs.setMemoState(qs.getMemoState() + 1);
         } else {
            qs.setCond(5);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _713_PathToBecomingALordAden(713, "_713_PathToBecomingALordAden", "");
   }
}
