package l2e.scripts.quests;

import l2e.commons.util.Util;
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

public final class _716_PathToBecomingALordRune extends Quest {
   private static final String qn = "_716_PathToBecomingALordRune";
   private static final int FREDERICK = 35509;
   private static final int AGRIPEL = 31348;
   private static final int INNOCENTIN = 31328;
   private static final int[] PAGANS = new int[]{
      22136,
      22137,
      22138,
      22139,
      22140,
      22141,
      22142,
      22143,
      22144,
      22145,
      22146,
      22147,
      22148,
      22149,
      22150,
      22151,
      22152,
      22153,
      22154,
      22155,
      22156,
      22157,
      22158,
      22159,
      22160,
      22161,
      22163,
      22164,
      22165,
      22166,
      22167,
      22168,
      22169,
      22170,
      22171,
      22172,
      22173,
      22174,
      22175,
      22176,
      22194
   };
   private static final int CASTLE_ID = 8;

   public _716_PathToBecomingALordRune(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35509);
      this.addTalkId(new int[]{35509, 31348, 31328});
      this.addKillId(PAGANS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = this.getQuestState(player, false);
      String htmltext = null;
      if (st == null) {
         return htmltext;
      } else {
         switch(event) {
            case "35509-02.htm":
            case "31348-02.htm":
            case "31328-04.htm":
            case "31348-06.htm":
            case "31348-07.htm":
            case "31348-08.htm":
               htmltext = event;
               break;
            case "35509-04.htm":
               if (st.isCreated()) {
                  st.startQuest();
                  htmltext = event;
               }
               break;
            case "31348-03.htm":
               if (st.isCond(2)) {
                  st.setCond(3);
                  htmltext = event;
               }
               break;
            case "35509-16.htm":
               QuestState qs0 = this.getQuestState(player.getClan().getLeader().getPlayerInstance(), false);
               if (qs0.isCond(4)) {
                  qs0.set("clanmember", player.getId());
                  qs0.setCond(5);
                  htmltext = event;
               }
               break;
            case "31328-05.htm":
               QuestState qs0 = this.getQuestState(player.getClan().getLeader().getPlayerInstance(), false);
               if (qs0.isCond(5)) {
                  qs0.setMemoState(0);
                  qs0.setCond(6);
                  htmltext = event;
               }
               break;
            case "31348-10.htm":
               if (st.isCond(7)) {
                  st.setCond(8);
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
      Castle castle = CastleManager.getInstance().getCastleById(8);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         switch(npc.getId()) {
            case 31328:
               Player leader = talker.getClan().getLeader().getPlayerInstance();
               if (talker != leader) {
                  QuestState qs0 = this.getQuestState(leader, false);
                  if (st.getMemoState() >= 100) {
                     htmltext = "31328-06.htm";
                  } else if (leader.isOnline()) {
                     if (talker.getId() == qs0.getInt("clanmember")) {
                        htmltext = "31328-03.htm";
                     } else {
                        htmltext = "31328-03a.htm";
                     }
                  } else {
                     htmltext = "31328-01.htm";
                  }
               }
               break;
            case 31348:
               switch(st.getCond()) {
                  case 2:
                     return "31348-01.htm";
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                     return "31348-04.htm";
                  case 7:
                     if (st.getMemoState() >= 100) {
                        htmltext = "31348-09.htm";
                     } else {
                        htmltext = "31348-05.htm";
                     }

                     return htmltext;
                  case 8:
                     htmltext = "31348-11.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 35509:
               switch(st.getState()) {
                  case 0:
                     Player leader = talker.getClan().getLeader().getPlayerInstance();
                     if (talker != leader) {
                        if (leader != null) {
                           QuestState qs0 = this.getQuestState(leader, false);
                           if (qs0 != null && qs0.isCond(4)) {
                              if (Util.checkIfInRange(1500, talker, leader, true) && leader.isOnline()) {
                                 htmltext = "35509-15.htm";
                              } else {
                                 htmltext = "35509-14.htm";
                              }
                           } else if (qs0 != null && qs0.isCond(5)) {
                              htmltext = "35509-17.htm";
                           } else {
                              htmltext = "35509-13.htm";
                           }
                        } else {
                           htmltext = "35509-17.htm";
                        }
                     } else if (castle.getTerritory().getLordObjectId() != talker.getObjectId()) {
                        htmltext = "35509-01.htm";
                     }
                     break;
                  case 1:
                     switch(st.getCond()) {
                        case 1:
                           QuestState qs1 = this.getQuestState(talker, false);
                           qs1 = talker.getQuestState(_021_HiddenTruth.class.getSimpleName());
                           QuestState qs2 = this.getQuestState(talker, false);
                           qs2 = talker.getQuestState("_025_HidingBehindTheTruth");
                           if (qs1 != null && qs1.isCompleted() && qs2 != null && qs2.isCompleted()) {
                              st.setCond(2);
                              htmltext = "35509-05.htm";
                           } else {
                              htmltext = "35509-06.htm";
                           }
                           break;
                        case 2:
                           htmltext = "35509-07.htm";
                           break;
                        case 3:
                           st.setCond(4);
                           htmltext = "35509-09.htm";
                           break;
                        case 4:
                           htmltext = "35509-10.htm";
                           break;
                        case 5:
                           htmltext = "35509-18.htm";
                           break;
                        case 6:
                           st.setCond(7);
                           htmltext = "35509-19.htm";
                           break;
                        case 7:
                           htmltext = "35509-20.htm";
                           break;
                        case 8:
                           if (castle.getSiege().getIsInProgress()) {
                              return "35509-21a.htm";
                           }

                           for(Fort fort : FortManager.getInstance().getForts()) {
                              if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                                 return "35509-21a.htm";
                              }

                              if (!fort.isBorderFortress() && fort.getContractedCastleId() != 8) {
                                 return "35509-21b.htm";
                              }
                           }

                           NpcSay packet = new NpcSay(
                              npc.getObjectId(),
                              23,
                              npc.getId(),
                              NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_RUNE
                           );
                           packet.addStringParameter(talker.getName());
                           npc.broadcastPacket(packet);
                           castle.getTerritory().changeOwner(castle.getOwner());
                           st.exitQuest(true, true);
                           htmltext = "35509-21.htm";
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
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else if (killer.getClan() == null) {
         return null;
      } else {
         Castle castle = CastleManager.getInstance().getCastleById(8);
         Player leader = castle.getOwner().getLeader().getPlayerInstance();
         if (st.isStarted() && leader != null) {
            QuestState qs = this.getQuestState(leader, false);
            if (qs != null && qs.isCond(7) && leader.isOnline() && killer != leader && killer.getId() == qs.getInt("clanmember")) {
               if (qs.getMemoState() < 100) {
                  qs.setMemoState(qs.getMemoState() + 1);
               } else {
                  playSound(leader, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
               }
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _716_PathToBecomingALordRune(716, "_716_PathToBecomingALordRune", "");
   }
}
