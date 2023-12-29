package l2e.gameserver.model.entity.mods.votereward.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.concurrent.TimeUnit;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteApiService;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardRecord;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import org.apache.commons.lang3.StringUtils;

public class PlayL2Site extends VoteRewardSite {
   private final String apiKey;
   private final int voteDelay;

   public PlayL2Site(MultiValueSet<String> parameters) {
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
      String serverResponse = VoteApiService.getApiResponse(String.format("https://playl2.net/%s/get-ind-vote/%s", this.apiKey, player.getIPAddress()));
      if (serverResponse == null) {
         return false;
      } else {
         JsonElement jelement = new JsonParser().parse(serverResponse);
         JsonObject topObject = jelement.getAsJsonObject();
         JsonPrimitive isOkPrimitive = topObject.getAsJsonPrimitive("ok");
         if (!isOkPrimitive.getAsBoolean()) {
            return false;
         } else {
            JsonObject resultObject = topObject.getAsJsonObject("result");
            JsonPrimitive isVotedObject = resultObject.getAsJsonPrimitive("isVoted");
            if (!isVotedObject.getAsBoolean()) {
               return false;
            } else {
               JsonPrimitive voteTimePrimitive = resultObject.getAsJsonPrimitive("voteTime");
               long voteTime = (long)voteTimePrimitive.getAsInt() * 1000L;
               this.getLock().lock();

               boolean var16;
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

                  var16 = false;
               } finally {
                  this.getLock().unlock();
               }

               return var16;
            }
         }
      }
   }
}
