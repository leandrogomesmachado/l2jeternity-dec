package l2e.gameserver.model.entity.mods.votereward.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardRecord;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import org.napile.primitive.pair.IntIntPair;

public abstract class AbstractAutoRewardSite extends VoteRewardSite {
   public AbstractAutoRewardSite(MultiValueSet<String> parameters) {
      super(parameters);
   }

   @Override
   public final void run() {
      this.getLock().lock();

      try {
         Map<String, List<IntIntPair>> votesCache = new HashMap<>();
         this.parseVotes(votesCache);

         for(Player player : World.getInstance().getAllPlayers()) {
            if (!player.isInOfflineMode()) {
               List<IntIntPair> voteInfos = votesCache.get(this.getIdentifier(player).toLowerCase());
               if (voteInfos != null) {
                  int availableVotes = 0;
                  VoteRewardRecord record = this.getRecord(this.getIdentifier(player));

                  for(IntIntPair info : voteInfos) {
                     if (info.getKey() > record.getLastVoteTime()) {
                        availableVotes += info.getValue();
                     }
                  }

                  if (availableVotes > 0) {
                     long lastVoteTime = (long)record.getLastVoteTime() * 1000L;
                     long nextVoteTime = lastVoteTime + TimeUnit.HOURS.toMillis(12L);
                     if (System.currentTimeMillis() >= nextVoteTime) {
                        record.onReceiveReward(availableVotes, System.currentTimeMillis());
                        this.giveRewards(player, availableVotes);
                     }
                  }
               }
            }
         }
      } finally {
         this.getLock().unlock();
      }
   }

   protected String getIdentifier(Player player) {
      return player.getName();
   }

   protected abstract void parseVotes(Map<String, List<IntIntPair>> var1);
}
