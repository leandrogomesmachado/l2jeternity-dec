package l2e.gameserver.model.entity.mods.votereward.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardRecord;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import org.apache.commons.lang3.StringUtils;

public class L2NetworkSite extends VoteRewardSite {
   private final String _serverName;
   private final int _voteDelay;

   public L2NetworkSite(MultiValueSet<String> parameters) {
      super(parameters);
      this._serverName = parameters.getString("server_name");
      this._voteDelay = parameters.getInteger("vote_delay", (int)TimeUnit.HOURS.toMillis(12L));
   }

   @Override
   public boolean isEnabled() {
      return StringUtils.isEmpty(this._serverName) ? false : super.isEnabled();
   }

   @Override
   public boolean tryGiveRewards(Player player) {
      if (!this.getApiResponse(player)) {
         return false;
      } else {
         this.getLock().lock();

         boolean var7;
         try {
            VoteRewardRecord record = this.getRecord(player.getIPAddress());
            long lastVoteTime = (long)record.getLastVoteTime() * 1000L;
            long nextVoteTime = lastVoteTime + (long)this._voteDelay;
            if (System.currentTimeMillis() >= nextVoteTime) {
               record.onReceiveReward(1, System.currentTimeMillis());
               this.giveRewards(player, 1);
               return true;
            }

            var7 = false;
         } finally {
            this.getLock().unlock();
         }

         return var7;
      }
   }

   protected boolean getApiResponse(Player player) {
      try {
         URL obj = new URL(String.format("https://l2network.eu/index.php?a=in&u=%s&ipc=%s", this._serverName, player.getIPAddress()));
         HttpURLConnection con = (HttpURLConnection)obj.openConnection();
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         con.setRequestProperty("charset", "utf-8");
         con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
         con.setDoOutput(true);
         int responseCode = con.getResponseCode();
         if (responseCode == 200) {
            StringBuilder sb = new StringBuilder();

            String inputLine;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
               while((inputLine = in.readLine()) != null) {
                  sb.append(inputLine);
               }
            }

            return sb.toString().equals("1");
         }
      } catch (Exception var19) {
         System.out.println("Error getApiResponse L2Network API: " + var19);
      }

      return false;
   }
}
