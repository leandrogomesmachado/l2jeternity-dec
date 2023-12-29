package l2e.gameserver.model.entity.mods.facebook;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public final class FacebookAutoAnnouncement extends RunnableImpl {
   private long _lastNotConnectedMsgDate = 0L;
   private long _lastConnectedMsgDate = 0L;
   private long _lastNegativePointsMsgDate = 0L;
   protected static ScheduledFuture<?> _runningThread;

   private FacebookAutoAnnouncement() {
   }

   public static void load() {
      ThreadPoolManager.getInstance().execute(new FacebookAutoAnnouncement());
   }

   @Override
   public void runImpl() {
      long currentDate = System.currentTimeMillis();
      if (Config.ALLOW_FACEBOOK_SYSTEM) {
         if (Config.ALLOW_AUTO_ANNONCE_NOT_CONNECT
            && this._lastNotConnectedMsgDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_AUTO_ANNONCE_NOT_DEDAY, TimeUnit.SECONDS) < currentDate) {
            this._lastNotConnectedMsgDate = currentDate;
            announceNotConnectedMsg();
         }

         if (Config.ALLOW_AUTO_ANNONCE_CONNECTED
            && this._lastConnectedMsgDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_AUTO_ANNONCE_DEDAY, TimeUnit.SECONDS) < currentDate) {
            this._lastConnectedMsgDate = currentDate;
            announceConnectedMsg();
         }

         if (Config.ALLOW_NEGATIVE_POINTS
            && this._lastNegativePointsMsgDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_NEGATIVE_POINTS_DELAY, TimeUnit.SECONDS) < currentDate) {
            this._lastNegativePointsMsgDate = currentDate;
            announceNegativePointsMsg();
         }
      }

      _runningThread = ThreadPoolManager.getInstance().schedule(this, this.getNextDelay(currentDate));
   }

   private static void announceNotConnectedMsg() {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.getFacebookProfile() == null) {
            ServerMessage msg = new ServerMessage("Facebook.AutoAnnounce.NotConnected", player.getLang());
            msg.add(player.getName());
            player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
         }
      }
   }

   private static void announceConnectedMsg() {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            FacebookProfile fb = player.getFacebookProfile();
            if (fb != null && !fb.hasNegativePoints() && !CompletedTasksHistory.getInstance().getAvailableActionTypes(fb).isEmpty() && !fb.hasTaskDelay()) {
               ServerMessage msg = new ServerMessage("Facebook.AutoAnnounce.NextTask", player.getLang());
               msg.add(player.getName());
               player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
            }
         }
      }
   }

   private static void announceNegativePointsMsg() {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.getFacebookProfile() != null && player.getFacebookProfile().hasNegativePoints()) {
            ServerMessage msg = new ServerMessage("Facebook.AutoAnnounce.NegativePoints", player.getLang());
            msg.add(player.getName());
            player.sendPacket(new CreatureSay(0, 10, "", msg.toString()));
         }
      }
   }

   private long getNextDelay(long currentDate) {
      if (Config.ALLOW_FACEBOOK_SYSTEM && (Config.ALLOW_AUTO_ANNONCE_NOT_CONNECT || Config.ALLOW_AUTO_ANNONCE_CONNECTED || Config.ALLOW_NEGATIVE_POINTS)) {
         long delayToNotConnected = this._lastNotConnectedMsgDate
            + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_AUTO_ANNONCE_NOT_DEDAY, TimeUnit.SECONDS)
            - currentDate;
         long delayToConnected = this._lastConnectedMsgDate
            + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_AUTO_ANNONCE_DEDAY, TimeUnit.SECONDS)
            - currentDate;
         long delayNegativePoints = this._lastNegativePointsMsgDate
            + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_NEGATIVE_POINTS_DELAY, TimeUnit.SECONDS)
            - currentDate;
         return Math.max(Math.min(delayToNotConnected, Math.min(delayToConnected, delayNegativePoints)), 1000L);
      } else {
         return 30000L;
      }
   }
}
