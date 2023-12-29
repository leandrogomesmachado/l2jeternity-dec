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

public class _708_PathToBecomingALordGludio extends Quest {
   private static final String qn = "_708_PathToBecomingALordGludio";
   private static final int SAYRES = 35100;
   private static final int PINTER = 30298;
   private static final int BATHIS = 30332;
   private static final int HEADLESS_ARMOR = 13848;
   private static final int VARNISH = 1865;
   private static final int ANIMAL_SKIN = 1867;
   private static final int IRON_ORE = 1869;
   private static final int COKES = 1879;
   private static final int HEADLESS_KNIGHT = 27393;
   private static final int[] MOBS = new int[]{20045, 20051, 20099, 27393};
   private static final int GLUDIO_CASTLE = 1;

   public _708_PathToBecomingALordGludio(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35100);
      this.addTalkId(new int[]{35100, 30298, 30332});
      this.addKillId(MOBS);
      this.questItemIds = new int[]{13848};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_708_PathToBecomingALordGludio");
      if (st == null) {
         return event;
      } else {
         Castle castle = CastleManager.getInstance().getCastleById(1);
         switch(event) {
            case "35100-02.htm":
            case "30298-04.htm":
               htmltext = event;
               break;
            case "35100-04.htm":
               if (st.isCreated()) {
                  st.startQuest();
                  htmltext = event;
               }
               break;
            case "35100-08.htm":
               if (st.isCond(1)) {
                  st.setCond(2);
                  htmltext = event;
               }
               break;
            case "35100-12.htm":
               QuestState qs0 = this.getQuestState(player.getClan().getLeader().getPlayerInstance(), false);
               if (qs0.isCond(2)) {
                  qs0.set("clanmember", player.getId());
                  qs0.setCond(3);
                  htmltext = event.replace("%name%", player.getName());
               }
               break;
            case "30298-05.htm":
               QuestState qs0 = this.getQuestState(player.getClan().getLeader().getPlayerInstance(), false);
               if (qs0.isCond(3)) {
                  qs0.setCond(4);
                  htmltext = event;
               }
               break;
            case "30298-09.htm":
               QuestState qs0 = this.getQuestState(player.getClan().getLeader().getPlayerInstance(), false);
               if (qs0.isCond(4)
                  && getQuestItemsCount(player, 1865) >= 100L
                  && getQuestItemsCount(player, 1867) >= 100L
                  && getQuestItemsCount(player, 1869) >= 100L
                  && getQuestItemsCount(player, 1879) >= 50L) {
                  qs0.setCond(5);
                  htmltext = event;
               }
               break;
            case "30332-02.htm":
               if (st.isCond(5)) {
                  st.setCond(6);
                  htmltext = event;
               }
               break;
            case "30332-05.htm":
               if (st.isCond(7) && getQuestItemsCount(player, 13848) >= 1L) {
                  takeItems(player, 13848, -1L);
                  npc.broadcastPacket(
                     new NpcSay(
                        npc.getObjectId(),
                        23,
                        npc.getId(),
                        NpcStringId.LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECOME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT_YOU_CAN_NOW_REST_EASY
                     )
                  );
                  st.setCond(9);
                  htmltext = event;
               }
               break;
            case "35100-23.htm":
               if (st.isCond(9)) {
                  if (castle.getSiege().getIsInProgress()) {
                     return "35100-22a.htm";
                  }

                  for(Fort fort : FortManager.getInstance().getForts()) {
                     if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                        return "35100-22a.htm";
                     }

                     if (!fort.isBorderFortress() && fort.getContractedCastleId() != 1) {
                        return "35100-22b.htm";
                     }
                  }

                  NpcSay packet = new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_GLUDIO_LONG_MAY_HE_REIGN);
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
      Castle castle = CastleManager.getInstance().getCastleById(1);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         switch(npc.getId()) {
            case 30298:
               Player leader = talker.getClan().getLeader().getPlayerInstance();
               if (talker != leader) {
                  QuestState qs0 = this.getQuestState(leader, false);
                  if (leader.isOnline()) {
                     if (talker.getId() == qs0.getInt("clanmember")) {
                        switch(qs0.getCond()) {
                           case 3:
                              htmltext = "30298-03.htm";
                              break;
                           case 4:
                              if (getQuestItemsCount(talker, 1865) >= 100L
                                 && getQuestItemsCount(talker, 1867) >= 100L
                                 && getQuestItemsCount(talker, 1869) >= 100L
                                 && getQuestItemsCount(talker, 1879) >= 50L) {
                                 htmltext = "30298-08.htm";
                              } else {
                                 htmltext = "30298-07.htm";
                              }
                              break;
                           case 5:
                              htmltext = "30298-12.htm";
                        }
                     } else {
                        htmltext = "30298-03a.htm";
                     }
                  } else {
                     htmltext = "30298-01.htm";
                  }
               }
               break;
            case 30332:
               switch(st.getCond()) {
                  case 5:
                     return "30332-01.htm";
                  case 6:
                     return "30332-03.htm";
                  case 7:
                     if (getQuestItemsCount(talker, 13848) >= 1L) {
                        htmltext = "30332-04.htm";
                     }

                     return htmltext;
                  case 8:
                  default:
                     return htmltext;
                  case 9:
                     return "30332-06.htm";
               }
            case 35100:
               switch(st.getState()) {
                  case 0:
                     Player leader = talker.getClan().getLeader().getPlayerInstance();
                     if (talker != leader) {
                        QuestState qs0 = this.getQuestState(leader, false);
                        if (qs0.isCond(2)) {
                           if (Util.checkIfInRange(1500, talker, leader, true) && leader.isOnline()) {
                              htmltext = "35100-11.htm";
                           } else {
                              htmltext = "35100-10.htm";
                           }
                        } else if (qs0.isCond(3)) {
                           htmltext = "35100-13a.htm";
                        } else {
                           htmltext = "35100-09.htm";
                        }
                     } else if (castle.getTerritory().getLordObjectId() != talker.getObjectId()) {
                        htmltext = "35100-01.htm";
                     }
                     break;
                  case 1:
                     switch(st.getCond()) {
                        case 1:
                           htmltext = "35100-06.htm";
                           break;
                        case 2:
                           htmltext = "35100-14.htm";
                           break;
                        case 3:
                           htmltext = "35100-15.htm";
                           break;
                        case 4:
                           htmltext = "35100-16.htm";
                           break;
                        case 5:
                           htmltext = "35100-18.htm";
                           break;
                        case 6:
                           htmltext = "35100-19.htm";
                           break;
                        case 7:
                           htmltext = "35100-20.htm";
                           break;
                        case 8:
                           htmltext = "35100-21.htm";
                           break;
                        case 9:
                           htmltext = "35100-22.htm";
                     }
                  case 2:
                     htmltext = getAlreadyCompletedMsg(talker);
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st != null && st.isCond(6)) {
         if (npc.getId() == 27393) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.DOES_MY_MISSION_TO_BLOCK_THE_SUPPLIES_END_HERE), 2000);
            giveItems(killer, 13848, 1L);
            st.setCond(7);
         } else if (getRandom(100) < 10) {
            addSpawn(27393, npc.getLocation(), false, 100000L);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _708_PathToBecomingALordGludio(708, "_708_PathToBecomingALordGludio", "");
   }
}
