package l2e.gameserver.model.entity.mods.votereward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;

public class VoteApiService {
   private static final Logger _log = Logger.getLogger(VoteApiService.class.getName());

   public static String getApiResponse(String endpoint) {
      HttpURLConnection connection = null;

      Object reader;
      try {
         StringBuilder stringBuilder = new StringBuilder();
         URL url = new URL(endpoint);
         connection = (HttpURLConnection)url.openConnection();
         connection.addRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36"
         );
         connection.setRequestMethod("GET");
         connection.setReadTimeout(5000);
         connection.connect();
         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
               while((line = reader.readLine()) != null) {
                  stringBuilder.append(line).append("\n");
               }
            }

            return stringBuilder.toString();
         }

         _log.warning("Problem in VoteApiService: getApiResponse LINK[" + endpoint + "]");
         if (Config.DEBUG) {
            _log.log(Level.SEVERE, "VoteApiService::getApiResponse returned error CODE[" + responseCode + "] LINK[" + endpoint + "]");
         }

         reader = null;
      } catch (Exception var26) {
         _log.warning("Problem in VoteApiService: getApiResponse LINK[" + endpoint + "]");
         if (Config.DEBUG) {
            _log.log(Level.SEVERE, "Something went wrong in VoteApiService::getApiResponse LINK[" + endpoint + "]", (Throwable)var26);
         }

         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }

      return (String)reader;
   }
}
