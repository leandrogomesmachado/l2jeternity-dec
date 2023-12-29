package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class ElrokiTeleporters extends Quest {
   public ElrokiTeleporters(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32111);
      this.addTalkId(32111);
      this.addStartNpc(32112);
      this.addTalkId(32112);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         switch(npc.getId()) {
            case 32111:
               if (player.isInCombat()) {
                  return "32111-no.htm";
               }

               if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                  BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(4990, -1879, -3178), 1000);
                  return null;
               }

               player.teleToLocation(4990, -1879, -3178, true);
               break;
            case 32112:
               if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                  BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(7557, -5513, -3221), 1000);
                  return null;
               }

               player.teleToLocation(7557, -5513, -3221, true);
         }

         return "";
      }
   }

   public static void main(String[] args) {
      new ElrokiTeleporters(-1, ElrokiTeleporters.class.getSimpleName(), "teleports");
   }
}
