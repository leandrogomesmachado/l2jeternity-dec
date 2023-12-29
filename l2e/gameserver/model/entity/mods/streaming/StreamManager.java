package l2e.gameserver.model.entity.mods.streaming;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.StreamingDAO;
import l2e.gameserver.model.actor.Player;

public class StreamManager {
   private static Logger _logTwitch = Logger.getLogger("twitch");
   private final List<StreamTemplate> _allActiveStreams = new ArrayList<>();
   private final Object _streamsListLock = new Object();
   private long _nextTotalRewardTimeClearDate = -1L;
   private int _minRequiredViewers = -1;

   private StreamManager() {
      this.calculateMinRequiredViewers();
   }

   public void onStreamActive(String channelName, String gameName, String streamTitle, int viewersCount, long currentDate, long delayBetweenChecks) {
      this.checkClearTotalRewardedTime();
      synchronized(this._streamsListLock) {
         for(StreamTemplate stream : this._allActiveStreams) {
            if (stream.getChannelName().equals(channelName)) {
               if (!stream.getStreamGameName().equals(gameName)) {
                  stream.setStreamGameName(gameName, isGameNameCorrect(gameName));
               }

               if (!stream.getStreamTitle().equals(streamTitle)) {
                  stream.setStreamTitle(streamTitle, isTitleCorrect(streamTitle));
               }

               stream.setViewersCount(viewersCount);
               String logMsg = "";
               if (stream.isNowPunished(currentDate)) {
                  logMsg = "Unable to increase reward for Stream: " + stream + "! Punished until date: " + stream.getPunishedUntilDate();
               } else if (this.isStreamActive(stream)) {
                  stream.setLastActiveDate(currentDate);
                  Player activeStreamPlayer = stream.getStreamingPlayer();
                  if (activeStreamPlayer != null && !isPlayerActive(activeStreamPlayer, currentDate)) {
                     logMsg = "Streaming: " + activeStreamPlayer.toString() + " is AFK Streaming: " + stream;
                  } else if ((activeStreamPlayer == null || !activeStreamPlayer.isOnline()) && !Config.ALLOW_INCREASE_REWARD) {
                     logMsg = "Unable to increase reward for Stream: " + stream + "! Player is offline/afk!";
                  } else {
                     long actualRewardedSeconds = stream.incNotRewardedSeconds(TimeUnit.MILLISECONDS.toSeconds(delayBetweenChecks), true, true);
                     if (actualRewardedSeconds > 0L) {
                        logMsg = "Increasing reward for Stream: " + stream + " by " + actualRewardedSeconds + " seconds!";
                     } else {
                        logMsg = "Unable to increase reward for Stream: " + stream + "! Streamer reached MAX time!";
                     }
                  }
               } else {
                  logMsg = "Unable to increase reward for Stream: " + stream + "! It isn't active!";
               }

               if (Config.ALLOW_STREAM_LOGS) {
                  LogRecord record = new LogRecord(Level.INFO, logMsg);
                  record.setLoggerName("twitch");
                  _logTwitch.log(record);
               }

               return;
            }
         }

         StreamTemplate stream2 = new StreamTemplate(
            channelName, gameName, isGameNameCorrect(gameName), streamTitle, isTitleCorrect(streamTitle), viewersCount, currentDate
         );
         if (Config.ALLOW_STREAM_SAVE_DB) {
            StreamingDAO.onStreamCreated(stream2);
         }

         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(Level.INFO, "New Stream has been found: " + stream2 + "!");
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }

         this._allActiveStreams.add(stream2);
      }
   }

   public List<StreamTemplate> getAllActiveStreamsCopy() {
      synchronized(this._streamsListLock) {
         return new ArrayList<>(this._allActiveStreams);
      }
   }

   @Nullable
   public StreamTemplate getMyStream(Player player) {
      int playerObjectId = player.getObjectId();
      synchronized(this._streamsListLock) {
         for(StreamTemplate stream : this._allActiveStreams) {
            if (stream.getAttachedPlayerId() == playerObjectId) {
               return stream;
            }
         }

         return null;
      }
   }

   public boolean isAwaitingForApproval(Player player) {
      int playerObjectId = player.getObjectId();
      synchronized(this._streamsListLock) {
         for(StreamTemplate stream : this._allActiveStreams) {
            if (stream.getIdsToApprove().contains(playerObjectId)) {
               return true;
            }
         }

         return false;
      }
   }

   public StreamTemplate getStreamByChannelName(String channelName) {
      synchronized(this._streamsListLock) {
         for(StreamTemplate stream : this._allActiveStreams) {
            if (stream.getChannelName().equalsIgnoreCase(channelName)) {
               return stream;
            }
         }

         return null;
      }
   }

   protected void checkClearTotalRewardedTime() {
      long currentDate = System.currentTimeMillis();
      if (this._nextTotalRewardTimeClearDate <= 0L) {
         this.setupTotalRewardClearTimer(currentDate);
      } else if (this._nextTotalRewardTimeClearDate < currentDate) {
         this.clearTotalRewardedTimes();
         this.setupTotalRewardClearTimer(currentDate);
      }
   }

   private void clearTotalRewardedTimes() {
      for(StreamTemplate stream : this._allActiveStreams) {
         stream.setTotalRewardedSecondsToday(0L);
      }

      StreamingDAO.resetTotalRewardedTimes();
   }

   public static boolean isPlayerActive(Player player) {
      return isPlayerActive(player, System.currentTimeMillis());
   }

   public static boolean isPlayerActive(Player player, long currentTime) {
      return Config.ALLOW_STREAM_AFK_SYSTEM && player.getLastNotAfkTime() + TimeUnit.SECONDS.toMillis((long)Config.STREAM_AFK_SECONDS) >= currentTime;
   }

   public boolean isStreamActive(StreamTemplate stream) {
      return stream.isStreamGameNameCorrect() && stream.isTitleCorrect() && stream.getViewersCount() >= this._minRequiredViewers;
   }

   public static boolean isGameNameCorrect(String gameName) {
      String correctGameName = Config.TWITCH_CORRECT_STREAM_GAME;
      return correctGameName.isEmpty() || gameName.equals(correctGameName);
   }

   public static boolean isTitleCorrect(CharSequence streamTitle) {
      String title = Config.TWITCH_CORRECT_STREAM_TITLE;
      if (title != null && !title.isEmpty()) {
         String[] possibilities = title.split(";");
         return possibilities.length == 0 ? true : true;
      } else {
         return true;
      }
   }

   private void setupTotalRewardClearTimer(long currentDate) {
      Calendar c = Calendar.getInstance();
      c.set(11, Config.TOTAL_REWARD_STREAMERS);
      c.set(12, 0);
      c.set(13, 0);

      while(c.getTimeInMillis() < currentDate) {
         c.add(6, 1);
         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(Level.INFO, "Adding 1 Day to Total Reward Clear Time!");
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }
      }

      this._nextTotalRewardTimeClearDate = c.getTimeInMillis();
   }

   public int getMinRequiredViewers() {
      return this._minRequiredViewers;
   }

   private void calculateMinRequiredViewers() {
      int lowestValue = Integer.MAX_VALUE;
      String[] propertySplit = Config.STREAMING_REWARDS.split(";");

      for(String rewards : propertySplit) {
         String[] reward = rewards.split(",");
         if (reward.length == 3 && Integer.parseInt(reward[0]) < lowestValue) {
            lowestValue = Integer.parseInt(reward[0]);
         }
      }

      this._minRequiredViewers = lowestValue;
   }

   public static StreamManager getInstance() {
      return StreamManager.SingletonHolder.instance;
   }

   private static final class SingletonHolder {
      private static final StreamManager instance = new StreamManager();
   }
}
