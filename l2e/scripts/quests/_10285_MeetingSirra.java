package l2e.scripts.quests;

import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public final class _10285_MeetingSirra extends Quest {
   public _10285_MeetingSirra(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32020);
      this.addTalkId(new int[]{32020, 32760, 32761, 32762, 32781, 32029});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "32020-02.htm":
               htmltext = event;
               break;
            case "32020-03.htm":
               qs.startQuest();
               qs.setMemoState(1);
               htmltext = event;
               break;
            case "32760-02.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 0) {
                  qs.set("ex", 1);
                  qs.setCond(3, true);
                  htmltext = event;
               }
               break;
            case "32760-05.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 2) {
                  htmltext = event;
               }
               break;
            case "32760-06.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 2) {
                  Npc sirra = addSpawn(32762, -23905, -8790, -5384, 56238, false, 0L, false, npc.getReflectionId());
                  sirra.broadcastPacket(
                     new NpcSay(sirra.getObjectId(), 22, sirra.getId(), NpcStringId.THERES_NOTHING_YOU_CANT_SAY_I_CANT_LISTEN_TO_YOU_ANYMORE), 2000
                  );
                  qs.set("ex", 3);
                  qs.setCond(5, true);
                  htmltext = event;
               }
               break;
            case "32760-09.htm":
            case "32760-10.htm":
            case "32760-11.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 4) {
                  htmltext = event;
               }
               break;
            case "32760-12.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 4) {
                  qs.set("ex", 5);
                  qs.setCond(7, true);
                  htmltext = event;
               }
               break;
            case "32760-13.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 5) {
                  qs.unset("ex");
                  qs.setMemoState(2);
                  ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
                  if (world != null) {
                     world.removeAllowed(player.getObjectId());
                  }

                  player.setReflectionId(0);
                  htmltext = event;
               }
               break;
            case "32760-14.htm":
               if (qs.isMemoState(2)) {
                  player.teleToLocation(new Location(113793, -109342, -845, 0), 0, true);
                  htmltext = event;
               }
               break;
            case "32761-02.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 1) {
                  qs.set("ex", 2);
                  qs.setCond(4, true);
                  htmltext = event;
               }
               break;
            case "32762-02.htm":
            case "32762-03.htm":
            case "32762-04.htm":
            case "32762-05.htm":
            case "32762-06.htm":
            case "32762-07.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 3) {
                  htmltext = event;
               }
               break;
            case "32762-08.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 3) {
                  qs.set("ex", 4);
                  qs.setCond(6, true);
                  htmltext = event;
                  npc.deleteMe();
               }
               break;
            case "32781-02.htm":
            case "32781-03.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
               }
               break;
            case "TELEPORT":
               if (player.getLevel() >= 82) {
                  player.teleToLocation(new Location(103045, -124361, -2768, 0), 0, true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      switch(qs.getState()) {
         case 0:
            if (npc.getId() == 32020) {
               qs = player.getQuestState("_10284_AcquisitionOfDivineSword");
               htmltext = player.getLevel() >= 82 && qs != null && qs.isCompleted() ? "32020-01.htm" : "32020-04.htm";
            }
            break;
         case 1:
            switch(npc.getId()) {
               case 32020:
                  switch(qs.getMemoState()) {
                     case 1:
                        return player.getLevel() >= 82 ? "32020-06.htm" : "32020-09.htm";
                     case 2:
                        return "32020-07.htm";
                     case 3:
                        qs.calcExpAndSp(this.getId());
                        qs.calcReward(this.getId());
                        qs.exitQuest(false, true);
                        return "32020-08.htm";
                     default:
                        return htmltext;
                  }
               case 32029:
                  if (qs.isMemoState(2)) {
                     htmltext = "32029-01.htm";
                     qs.setCond(8, true);
                  }

                  return htmltext;
               case 32760:
                  if (qs.isMemoState(1)) {
                     switch(qs.getInt("ex")) {
                        case 0:
                           return "32760-01.htm";
                        case 1:
                           return "32760-03.htm";
                        case 2:
                           return "32760-04.htm";
                        case 3:
                           return "32760-07.htm";
                        case 4:
                           return "32760-08.htm";
                        case 5:
                           htmltext = "32760-15.htm";
                     }
                  }

                  return htmltext;
               case 32761:
                  if (qs.isMemoState(1)) {
                     switch(qs.getInt("ex")) {
                        case 1:
                           return "32761-01.htm";
                        case 2:
                           return "32761-03.htm";
                        case 3:
                           htmltext = "32761-04.htm";
                     }
                  }

                  return htmltext;
               case 32762:
                  if (qs.isMemoState(1)) {
                     int state = qs.getInt("ex");
                     if (state == 3) {
                        htmltext = "32762-01.htm";
                     } else if (state == 4) {
                        htmltext = "32762-09.htm";
                        return htmltext;
                     }

                     return htmltext;
                  }

                  return htmltext;
               case 32781:
                  if (qs.isMemoState(2)) {
                     htmltext = "32781-01.htm";
                  } else if (qs.isMemoState(3)) {
                     htmltext = "32781-04.htm";
                     return htmltext;
                  }

                  return htmltext;
               default:
                  return htmltext;
            }
         case 2:
            if (npc.getId() == 32020) {
               htmltext = "32020-05.htm";
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _10285_MeetingSirra(10285, _10285_MeetingSirra.class.getSimpleName(), "");
   }
}
