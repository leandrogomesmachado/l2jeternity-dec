package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.service.BotFunctions;

public class TemporaryTeleporter extends Quest {
   public TemporaryTeleporter(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(32602);
      this.addFirstTalkId(32602);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getId() == 32602) {
         if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
            BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(-248717, 250260, 4337), 1000);
            return null;
         }

         player.teleToLocation(-248717, 250260, 4337, true);
      }

      player.sendActionFailed();
      return null;
   }

   public static void main(String[] args) {
      new TemporaryTeleporter(-1, TemporaryTeleporter.class.getSimpleName(), "teleports");
   }
}
