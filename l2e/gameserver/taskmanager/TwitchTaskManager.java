package l2e.gameserver.taskmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitchTaskManager {
   protected static final Logger _log = Logger.getLogger(TwitchTaskManager.class.getName());
   private static Logger _logTwitch = Logger.getLogger("twitch");

   public TwitchTaskManager() {
      long delay = (long)Config.TWITCH_CHECK_DELAY;
      if (delay > 0L) {
         ThreadPoolManager.getInstance().scheduleAtFixedDelay(new TwitchTaskManager.CheckTwitch(delay), delay, delay);
      }
   }

   public static TwitchTaskManager getInstance() {
      return TwitchTaskManager.SingletonHolder.instance;
   }

   private static class CheckTwitch extends RunnableImpl {
      private final long _delayBetweenChecks;

      private CheckTwitch(long delayBetweenChecks) {
         this._delayBetweenChecks = delayBetweenChecks;
      }

      @Override
      public void runImpl() {
         if (Config.ALLOW_STREAM_SYSTEM) {
            this.parseTwitch();
         }
      }

      private void parseTwitch() {
         try {
            JSONObject data = getAllActiveChannels();
            if (data == null) {
               return;
            }

            JSONArray streams = data.getJSONArray("streams");
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "Starting to parse " + data.getInt("_total") + " active streams!");
               record.setLoggerName("twitch");
               TwitchTaskManager._logTwitch.log(record);
            }

            long currentDate = System.currentTimeMillis();

            for(int streamIndex = 0; streamIndex < streams.length(); ++streamIndex) {
               JSONObject stream = streams.getJSONObject(streamIndex);
               if (stream.get("game") != null && !stream.isNull("game")) {
                  try {
                     String gameName = stream.getString("game");
                     int viewersCount = stream.getInt("viewers");
                     JSONObject channel = stream.getJSONObject("channel");
                     if (channel.get("status") != null && !channel.isNull("status")) {
                        String channelName = channel.getString("display_name");
                        String streamTitle = channel.getString("status");
                        StreamManager.getInstance().onStreamActive(channelName, gameName, streamTitle, viewersCount, currentDate, this._delayBetweenChecks);
                     }
                  } catch (JSONException var12) {
                     TwitchTaskManager._log.log(Level.WARNING, "JSON Exception! Stream: " + stream, (Throwable)var12);
                  }
               }
            }
         } catch (MalformedURLException var13) {
            TwitchTaskManager._log.log(Level.WARNING, "Config \"TwitchActiveStreamsURL\" has wrong Value!", (Throwable)var13);
         } catch (IOException var14) {
            TwitchTaskManager._log.log(Level.WARNING, "Error while connecting to Twitch!", (Throwable)var14);
         }
      }

      private static JSONObject getAllActiveChannels() throws MalformedURLException, IOException {
         HttpURLConnection urlConnection = null;

         try {
            StringBuilder stringBuilder = new StringBuilder();
            URL url = new URL(String.format(Config.TWITCH_ACTIVE_STREAMS_URL, Config.TWITCH_CLIENT_ID));
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.addRequestProperty(
               "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36"
            );
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(5000);
            urlConnection.connect();

            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
               while((line = reader.readLine()) != null) {
                  stringBuilder.append(line);
               }
            }

            return new JSONObject(stringBuilder.toString());
         } catch (Exception var24) {
            TwitchTaskManager._log.warning("Problem while connecting to Twitch with client_id - " + Config.TWITCH_CLIENT_ID);
         } finally {
            if (urlConnection != null) {
               urlConnection.disconnect();
            }
         }

         return null;
      }
   }

   private static class SingletonHolder {
      private static final TwitchTaskManager instance = new TwitchTaskManager();
   }
}
