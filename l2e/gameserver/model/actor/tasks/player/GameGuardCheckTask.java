package l2e.gameserver.model.actor.tasks.player;

import java.util.logging.Logger;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.LogOutOk;

public class GameGuardCheckTask implements Runnable {
   private static final Logger _log = Logger.getLogger(GameGuardCheckTask.class.getName());
   private final Player _player;

   public GameGuardCheckTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         GameClient client = this._player.getClient();
         if (client != null && !client.isAuthedGG() && this._player.isOnline()) {
            AdminParser.getInstance().broadcastMessageToGMs("Client " + client + " failed to reply GameGuard query and is being kicked!");
            _log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
            client.close(LogOutOk.STATIC_PACKET);
         }
      }
   }
}
