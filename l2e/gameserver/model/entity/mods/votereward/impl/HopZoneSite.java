package l2e.gameserver.model.entity.mods.votereward.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteApiService;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardRecord;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import org.apache.commons.lang3.StringUtils;

public class HopZoneSite extends VoteRewardSite {
   private static final DateFormat HOP_ZONE_SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private final String apiKey;
   private final int voteDelay;

   public HopZoneSite(MultiValueSet<String> parameters) {
      super(parameters);
      this.apiKey = parameters.getString("api_key");
      this.voteDelay = parameters.getInteger("vote_delay", (int)TimeUnit.HOURS.toMillis(12L));
   }

   @Override
   public boolean isEnabled() {
      return StringUtils.isEmpty(this.apiKey) ? false : super.isEnabled();
   }

   @Override
   public boolean tryGiveRewards(Player player) {
      String serverResponse = VoteApiService.getApiResponse(
         String.format("https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s", this.apiKey, player.getIPAddress())
      );
      if (serverResponse == null) {
         return false;
      } else {
         JsonElement jelement = new JsonParser().parse(serverResponse);
         JsonObject topObject = jelement.getAsJsonObject();
         JsonPrimitive statusCodePrimitive = topObject.getAsJsonPrimitive("status_code");
         if (statusCodePrimitive.getAsInt() != 200) {
            return false;
         } else {
            JsonPrimitive votedPrimitive = topObject.getAsJsonPrimitive("voted");
            if (!votedPrimitive.getAsBoolean()) {
               return false;
            } else {
               JsonPrimitive voteTimePrimitive = topObject.getAsJsonPrimitive("voteTime");

               long voteTime;
               try {
                  Date voteDate = HOP_ZONE_SERVER_DATE_FORMAT.parse(voteTimePrimitive.getAsString());
                  voteTime = voteDate.getTime();
               } catch (ParseException var19) {
                  this.error("Cannot parse voteDate from HopZone.net!", var19);
                  return false;
               }

               this.getLock().lock();

               boolean var15;
               try {
                  VoteRewardRecord record = this.getRecord(player.getIPAddress());
                  long lastVoteTime = (long)record.getLastVoteTime() * 1000L;
                  if (lastVoteTime >= voteTime) {
                     return false;
                  }

                  long nextVoteTime = lastVoteTime + (long)this.voteDelay;
                  if (System.currentTimeMillis() >= nextVoteTime) {
                     record.onReceiveReward(1, voteTime);
                     this.giveRewards(player, 1);
                     return true;
                  }

                  var15 = false;
               } finally {
                  this.getLock().unlock();
               }

               return var15;
            }
         }
      }
   }

   static {
      HOP_ZONE_SERVER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
   }
}
