package l2e.gameserver.model.entity.mods.votereward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.log.LoggerObject;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.VoteRewardHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.reward.RewardItem;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.strings.server.ServerMessage;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.impl.HashIntLongMap;
import org.napile.primitive.pair.IntLongPair;

public abstract class VoteRewardSite extends LoggerObject implements Runnable {
   private final String name;
   private final boolean enabled;
   private final int runDelay;
   private final List<RewardList> rewardLists = new ArrayList<>();
   private final Map<String, VoteRewardRecord> records = new ConcurrentHashMap<>();
   private final Lock lock = new ReentrantLock();

   public VoteRewardSite(MultiValueSet<String> parameters) {
      this.name = parameters.getString("name");
      this.enabled = parameters.getBool("enabled");
      this.runDelay = parameters.getInteger("run_delay", 0);
   }

   @Override
   public void run() {
      throw new UnsupportedOperationException(this.getClass().getName() + " not implemented run");
   }

   public final String getName() {
      return this.name;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public final void addRewardList(RewardList rewardList) {
      this.rewardLists.add(rewardList);
   }

   public final VoteRewardRecord getRecord(String identifier) {
      VoteRewardRecord record = this.records.get(identifier);
      if (record == null) {
         record = new VoteRewardRecord(this.getName(), identifier, 0, -1);
         record.save();
         this.records.put(record.getIdentifier(), record);
      }

      return record;
   }

   public final Lock getLock() {
      return this.lock;
   }

   public void init() {
      VoteRewardHolder.getInstance().restore(this.records, this.getName());
      if (this.runDelay > 0 && this.isEnabled()) {
         ThreadPoolManager.getInstance().scheduleAtFixedRate(this, (long)this.runDelay, (long)this.runDelay * 1000L);
      }
   }

   public boolean tryGiveRewards(Player player) {
      return false;
   }

   protected void giveRewards(Player player, int count) {
      List<RewardItem> rolledItems = new ArrayList<>();

      for(RewardList rewardList : this.rewardLists) {
         for(int i = 0; i < count; ++i) {
            rolledItems.addAll(rewardList.roll(player));
         }
      }

      if (rolledItems.isEmpty()) {
         player.sendMessage(new ServerMessage("VoteReward.REWARD_NOT_RECEIVED." + this.getName(), player.getLang()).toString());
      } else {
         player.sendMessage(new ServerMessage("VoteReward.REWARD_RECEIVED." + this.getName(), player.getLang()).toString());
         IntLongMap rewards = new HashIntLongMap();

         for(RewardItem rewardItem : rolledItems) {
            rewards.put(rewardItem._itemId, rewards.get(rewardItem._itemId) + rewardItem._count);
         }

         for(IntLongPair pair : rewards.entrySet()) {
            player.addItem("VoteReward", pair.getKey(), pair.getValue(), player, true);
         }
      }
   }
}
