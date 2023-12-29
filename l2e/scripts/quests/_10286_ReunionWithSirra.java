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

public final class _10286_ReunionWithSirra extends Quest {
   public _10286_ReunionWithSirra(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32020);
      this.addTalkId(new int[]{32020, 32760, 32762, 32781});
      this.registerQuestItems(new int[]{15470});
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
               qs.startQuest();
               qs.setMemoState(1);
               htmltext = event;
               break;
            case "32020-03.htm":
            case "32760-02.htm":
            case "32760-03.htm":
            case "32760-04.htm":
               if (qs.isMemoState(1)) {
                  htmltext = event;
               }
               break;
            case "32760-05.htm":
               if (qs.isMemoState(1)) {
                  Npc sirra = addSpawn(32762, -23905, -8790, -5384, 56238, false, 0L, false, npc.getReflectionId());
                  sirra.broadcastPacket(
                     new NpcSay(sirra.getObjectId(), 22, sirra.getId(), NpcStringId.YOU_ADVANCED_BRAVELY_BUT_GOT_SUCH_A_TINY_RESULT_HOHOHO), 2000
                  );
                  qs.set("ex", 1);
                  qs.setCond(3, true);
                  htmltext = event;
               }
               break;
            case "32760-07.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 2) {
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
            case "32760-08.htm":
               if (qs.isMemoState(2)) {
                  qs.setCond(5, true);
                  player.teleToLocation(new Location(113793, -109342, -845, 0), 0, true);
                  htmltext = event;
               }
               break;
            case "32762-02.htm":
            case "32762-03.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 1) {
                  htmltext = event;
               }
               break;
            case "32762-04.htm":
               if (qs.isMemoState(1) && qs.getInt("ex") == 1) {
                  if (!hasQuestItems(player, 15470)) {
                     giveItems(player, 15470, 5L);
                  }

                  qs.set("ex", 2);
                  qs.setCond(4, true);
                  htmltext = event;
               }
               break;
            case "32781-02.htm":
            case "32781-03.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
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
               qs = player.getQuestState("_10285_MeetingSirra");
               htmltext = player.getLevel() >= 82 && qs != null && qs.isCompleted() ? "32020-01.htm" : "32020-04.htm";
            }
            break;
         case 1:
            switch(npc.getId()) {
               case 32020:
                  if (qs.isMemoState(1)) {
                     htmltext = player.getLevel() >= 82 ? "32020-06.htm" : "32020-08.htm";
                  } else if (qs.isMemoState(2)) {
                     return "32020-07.htm";
                  }

                  return htmltext;
               case 32760:
                  if (qs.isMemoState(1)) {
                     switch(qs.getInt("ex")) {
                        case 0:
                           return "32760-01.htm";
                        case 1:
                           return "32760-05.htm";
                        case 2:
                           htmltext = "32760-06.htm";
                     }
                  }

                  return htmltext;
               case 32762:
                  if (qs.isMemoState(1)) {
                     int state = qs.getInt("ex");
                     if (state == 1) {
                        htmltext = "32762-01.htm";
                     } else if (state == 2) {
                        htmltext = "32762-05.htm";
                        return htmltext;
                     }

                     return htmltext;
                  }

                  return htmltext;
               case 32781:
                  if (qs.isMemoState(10)) {
                     qs.calcExpAndSp(this.getId());
                     qs.exitQuest(false, true);
                     htmltext = "32781-01.htm";
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
      new _10286_ReunionWithSirra(10286, _10286_ReunionWithSirra.class.getSimpleName(), "");
   }
}
