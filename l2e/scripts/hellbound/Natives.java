package l2e.scripts.hellbound;

import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Natives extends Quest {
   private static final int NATIVE = 32362;
   private static final int INSURGENT = 32363;
   private static final int TRAITOR = 32364;
   private static final int INCASTLE = 32357;
   private static final int MARK_OF_BETRAYAL = 9676;
   private static final int BADGES = 9674;
   private static final int[] doors = new int[]{19250003, 19250004};

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      String htmltext = "";
      int hellboundLevel = HellboundManager.getInstance().getLevel();
      int npcId = npc.getId();
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      switch(npcId) {
         case 32357:
            if (hellboundLevel < 9) {
               htmltext = "32357-01a.htm";
            } else if (hellboundLevel == 9) {
               htmltext = npc.isBusy() ? "32357-02.htm" : "32357-01.htm";
            } else {
               htmltext = "32357-01b.htm";
            }
            break;
         case 32362:
            htmltext = hellboundLevel > 5 ? "32362-01.htm" : "32362.htm";
            break;
         case 32363:
            htmltext = hellboundLevel > 5 ? "32363-01.htm" : "32363.htm";
      }

      return htmltext;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      if (npc.getId() == 32364) {
         if (event.equalsIgnoreCase("open_door")) {
            QuestState qs = player.getQuestState(this.getName());
            if (qs.getQuestItemsCount(9676) >= 10L) {
               qs.takeItems(9676, 10L);
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.ALRIGHT_NOW_LEODAS_IS_YOURS), 2000);
               HellboundManager.getInstance().updateTrust(-50, true);

               for(int doorId : doors) {
                  DoorInstance door = DoorParser.getInstance().getDoor(doorId);
                  if (door != null) {
                     door.openMe();
                  }
               }

               this.cancelQuestTimers("close_doors");
               this.startQuestTimer("close_doors", 1800000L, npc, player);
            } else if (qs.hasQuestItems(9676)) {
               htmltext = "32364-01.htm";
            } else {
               htmltext = "32364-02.htm";
            }
         } else if (event.equalsIgnoreCase("close_doors")) {
            for(int doorId : doors) {
               DoorInstance door = DoorParser.getInstance().getDoor(doorId);
               if (door != null) {
                  door.closeMe();
               }
            }
         }
      } else if (npc.getId() != 32362 || !event.equalsIgnoreCase("hungry_death")) {
         if (npc.getId() == 32357) {
            if (event.equalsIgnoreCase("FreeSlaves")) {
               QuestState qs = player.getQuestState(this.getName());
               if (qs.getQuestItemsCount(9674) >= 5L) {
                  qs.takeItems(9674, 5L);
                  npc.setBusy(true);
                  HellboundManager.getInstance().updateTrust(100, true);
                  htmltext = "32357-02.htm";
                  this.startQuestTimer("delete_me", 3000L, npc, null);
               } else {
                  htmltext = "32357-02a.htm";
               }
            } else if (event.equalsIgnoreCase("delete_me")) {
               npc.setBusy(false);
               npc.deleteMe();
               npc.getSpawn().decreaseCount(npc);
            }
         }
      } else if (npc != null) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.HUN_HUNGRY), 2000);
         npc.doDie(null);
      }

      return htmltext;
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 32362 && HellboundManager.getInstance().getLevel() < 6) {
         this.startQuestTimer("hungry_death", 600000L, npc, null);
      }

      return super.onSpawn(npc);
   }

   public Natives(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32362);
      this.addFirstTalkId(32363);
      this.addFirstTalkId(32357);
      this.addStartNpc(32364);
      this.addStartNpc(32357);
      this.addTalkId(32364);
      this.addTalkId(32357);
      this.addSpawnId(new int[]{32362});
   }

   public static void main(String[] args) {
      new Natives(-1, "Natives", "hellbound");
   }
}
