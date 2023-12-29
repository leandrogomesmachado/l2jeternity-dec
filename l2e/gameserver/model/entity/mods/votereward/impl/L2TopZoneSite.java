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

public class L2TopZoneSite extends VoteRewardSite {
   private final String _apiKey;
   private final int _voteDelay;

   public L2TopZoneSite(MultiValueSet<String> parameters) {
      super(parameters);
      this._apiKey = parameters.getString("api_key");
      this._voteDelay = parameters.getInteger("vote_delay", (int)TimeUnit.HOURS.toMillis(12L));
   }

   @Override
   public boolean isEnabled() {
      return StringUtils.isEmpty(this._apiKey) ? false : super.isEnabled();
   }

   @Override
   public boolean tryGiveRewards(Player player) {
      String serverResponse = VoteApiService.getApiResponse(
         String.format("https://api.l2topzone.com/v1/vote?token=%s&ip=%s", this._apiKey, player.getIPAddress())
      );
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

                  long nextVoteTime = lastVoteTime + (long)this._voteDelay;
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
