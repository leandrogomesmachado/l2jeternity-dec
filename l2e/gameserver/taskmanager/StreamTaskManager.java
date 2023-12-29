package l2e.gameserver.taskmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import l2e.gameserver.model.entity.mods.streaming.StreamTemplate;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class StreamTaskManager {
   private static final long MIN_CHECK_DELAY = 1000L;
   private final Map<StreamTemplate, Long> lastAFKMessages = new HashMap<>();

   public StreamTaskManager() {
      long delay = Math.max(TimeUnit.SECONDS.toMillis((long)Config.STREAM_AFK_DELAY), 1000L);
      ThreadPoolManager.getInstance().scheduleAtFixedDelay(new StreamTaskManager.CheckAFKStreamers(this), delay, delay);
   }

   private Map<StreamTemplate, Long> getLastAFKMessages() {
      return this.lastAFKMessages;
   }

   public static StreamTaskManager getInstance() {
      return StreamTaskManager.SingletonHolder.instance;
   }

   private static class CheckAFKStreamers extends RunnableImpl {
      private final StreamTaskManager _handler;

      private CheckAFKStreamers(StreamTaskManager handler) {
         this._handler = handler;
      }

      @Override
      public void runImpl() {
         if (Config.ALLOW_STREAM_AFK_SYSTEM && Config.STREAM_AFK_DELAY > 0) {
            long currentDate = System.currentTimeMillis();
            long minDateToAfk = currentDate - TimeUnit.SECONDS.toMillis((long)Config.STREAM_AFK_SECONDS);

            for(StreamTemplate stream : StreamManager.getInstance().getAllActiveStreamsCopy()) {
               Player player = stream.getStreamingPlayer();
               if (player != null && StreamManager.getInstance().isStreamActive(stream) && player.getLastNotAfkTime() < minDateToAfk) {
                  if (this._handler.getLastAFKMessages().containsKey(stream)) {
                     if (this._handler.getLastAFKMessages().get(stream) < currentDate) {
                        sendAFKMessage(player);
                        this._handler.getLastAFKMessages().put(stream, currentDate + (long)Config.STREAM_AFK_DELAY);
                     }
                  } else {
                     sendAFKMessage(player);
                     this._handler.getLastAFKMessages().put(stream, currentDate + (long)Config.STREAM_AFK_DELAY);
                  }
               }
            }
         }
      }

      private static void sendAFKMessage(Player player) {
         player.sendPacket(
            new CreatureSay(
               0,
               15,
               ServerStorage.getInstance().getString(player.getLang(), "Twitch.Stream"),
               ServerStorage.getInstance().getString(player.getLang(), "Twitch.AFKMessage")
            )
         );
      }
   }

   private static class SingletonHolder {
      private static final StreamTaskManager instance = new StreamTaskManager();
   }
}
