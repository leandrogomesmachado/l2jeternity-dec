package l2e.gameserver;

import l2e.gameserver.model.World;
import l2e.gameserver.model.strings.server.ServerMessage;

public class OnlinePlayers {
   private static OnlinePlayers _instance;

   public static OnlinePlayers getInstance() {
      if (_instance == null) {
         _instance = new OnlinePlayers();
      }

      return _instance;
   }

   private OnlinePlayers() {
      ThreadPoolManager.getInstance().schedule(new OnlinePlayers.AnnounceOnline(), (long)Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
   }

   class AnnounceOnline implements Runnable {
      @Override
      public void run() {
         if (Config.ONLINE_PLAYERS_AT_STARTUP) {
            ServerMessage msg = new ServerMessage("OnlinePlayers.ONLINE_ANNOUNCE", true);
            msg.add((double)World.getInstance().getAllPlayers().size() * Config.FAKE_ONLINE);
            Announcements.getInstance().announceToAll(msg);
            ThreadPoolManager.getInstance().schedule(OnlinePlayers.this.new AnnounceOnline(), (long)Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
         }
      }
   }
}
