package l2e.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Stats;

public class RewardGroup implements Cloneable {
   private double _chance;
   private boolean _isAdena = false;
   private boolean _notRate = false;
   private boolean _notUseMode = false;
   private final List<RewardData> _items = new ArrayList<>();

   public RewardGroup(double chance) {
      this.setChance(chance);
   }

   public boolean isNotUseMode() {
      return this._notUseMode;
   }

   public void setIsNotUseMode(boolean notUseMode) {
      this._notUseMode = notUseMode;
   }

   public boolean notRate() {
      return this._notRate;
   }

   public void setNotRate(boolean notRate) {
      this._notRate = notRate;
   }

   public double getChance() {
      return this._chance;
   }

   public void setChance(double chance) {
      this._chance = chance;
   }

   public boolean isAdena() {
      return this._isAdena;
   }

   public void setIsAdena(boolean isAdena) {
      this._isAdena = isAdena;
   }

   public void addData(RewardData item) {
      if (item.getItem().isAdena()) {
         this._isAdena = true;
      }

      if (item.getItem().getId() == 57 && Config.ADENA_FIXED_CHANCE > 0.0) {
         this.setChance(Config.ADENA_FIXED_CHANCE * 10000.0);
      }

      this._items.add(item);
   }

   public List<RewardData> getItems() {
      return this._items;
   }

   public RewardGroup clone() {
      RewardGroup ret = new RewardGroup(this._chance);

      for(RewardData i : this._items) {
         ret.addData(i.clone());
      }

      return ret;
   }

   public List<RewardItem> roll(RewardType type, Player player, double penaltyMod, double rateMod, Attackable npc) {
      switch(type) {
         case NOT_RATED_GROUPED:
         case NOT_RATED_NOT_GROUPED:
            return this.rollItems(player, penaltyMod, penaltyMod, 1.0, 1.0, false, 1);
         case SWEEP:
            double sweepRate = Config.RATE_DROP_SPOIL * rateMod;
            double premiumSweepRate = player.isInParty() && Config.PREMIUM_PARTY_RATE
               ? player.getParty().getDropSpoil()
               : player.getPremiumBonus().getDropSpoil();
            if (npc != null && npc.getChampionTemplate() != null) {
               sweepRate *= npc.getChampionTemplate().spoilDropMultiplier;
            }

            return this.rollItems(
               player,
               penaltyMod,
               penaltyMod,
               npc != null ? npc.calcStat(Stats.SPOIL_MULTIPLIER, sweepRate, player, null) : sweepRate,
               premiumSweepRate,
               Config.ALLOW_MODIFIER_FOR_SPOIL,
               player.getPremiumBonus().getMaxSpoilItemsFromOneGroup()
            );
         case RATED_GROUPED:
            if (this._isAdena) {
               double adenaRate = Config.RATE_DROP_ADENA * rateMod;
               double premiumAdenaRate = player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropAdena()
                  : player.getPremiumBonus().getDropAdena();
               if (npc != null && npc.getChampionTemplate() != null) {
                  adenaRate *= npc.getChampionTemplate().adenaMultiplier;
               }

               return this.rollAdena(penaltyMod, npc != null ? npc.calcStat(Stats.ADENA_MULTIPLIER, adenaRate, player, null) : adenaRate, premiumAdenaRate);
            } else if (npc != null && npc.isRaid()) {
               double dropRate = (npc.isEpicRaid() ? Config.RATE_DROP_EPICBOSS : Config.RATE_DROP_RAIDBOSS) * rateMod;
               double premiumRaidRate = npc.isEpicRaid()
                  ? (player.isInParty() && Config.PREMIUM_PARTY_RATE ? player.getParty().getDropEpics() : player.getPremiumBonus().getDropEpics())
                  : (player.isInParty() && Config.PREMIUM_PARTY_RATE ? player.getParty().getDropRaids() : player.getPremiumBonus().getDropRaids());
               return this.rollItems(
                  player,
                  penaltyMod,
                  penaltyMod,
                  npc != null ? npc.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null) : dropRate,
                  premiumRaidRate,
                  Config.ALLOW_MODIFIER_FOR_RAIDS,
                  player.getPremiumBonus().getMaxRaidDropItemsFromOneGroup()
               );
            } else {
               if (npc != null && npc.isSiegeGuard()) {
                  double dropRate = Config.RATE_DROP_SIEGE_GUARD * rateMod;
                  double premiumGRate = player.isInParty() && Config.PREMIUM_PARTY_RATE
                     ? player.getParty().getDropSiege()
                     : player.getPremiumBonus().getDropSiege();
                  return this.rollItems(
                     player,
                     penaltyMod,
                     penaltyMod,
                     npc != null ? npc.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null) : dropRate,
                     premiumGRate,
                     Config.ALLOW_MODIFIER_FOR_DROP,
                     player.getPremiumBonus().getMaxDropItemsFromOneGroup()
                  );
               }

               double dropRate = Config.RATE_DROP_ITEMS * rateMod;
               if (npc != null && npc.getChampionTemplate() != null) {
                  dropRate *= npc.getChampionTemplate().itemDropMultiplier;
               }

               double premiumDRate = player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropItems()
                  : player.getPremiumBonus().getDropItems();
               return this.rollItems(
                  player,
                  penaltyMod,
                  penaltyMod,
                  npc != null ? npc.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null) : dropRate,
                  premiumDRate,
                  Config.ALLOW_MODIFIER_FOR_DROP,
                  player.getPremiumBonus().getMaxDropItemsFromOneGroup()
               );
            }
         default:
            return Collections.emptyList();
      }
   }

   private List<RewardItem> rollAdena(double mod, double rate, double premiumBonus) {
      if (this.notRate()) {
         mod = Math.min(mod, 1.0);
         rate = 1.0;
      }

      if (mod > 0.0 && rate > 0.0 && this.getChance() > (double)Rnd.get(1000000)) {
         List<RewardItem> rolledItems = new ArrayList<>();

         for(RewardData data : this.getItems()) {
            RewardItem item = data.rollAdena(mod, rate, premiumBonus);
            if (item != null) {
               rolledItems.add(item);
            }
         }

         if (rolledItems.isEmpty()) {
            return Collections.emptyList();
         } else {
            List<RewardItem> result = new ArrayList<>();

            for(int i = 0; i < Config.MAX_DROP_ITEMS_FROM_ONE_GROUP; ++i) {
               RewardItem rolledItem = Rnd.get(rolledItems);
               if (rolledItems.remove(rolledItem)) {
                  result.add(rolledItem);
               }

               if (rolledItems.isEmpty()) {
                  break;
               }
            }

            return result;
         }
      } else {
         return Collections.emptyList();
      }
   }

   private List<RewardItem> rollItems(Player player, double mod, double dpmod, double rate, double premiumBonus, boolean useModifier, int perGroup) {
      if (this.notRate()) {
         mod = Math.min(mod, 1.0);
         dpmod = Math.min(dpmod, 1.0);
         rate = 1.0;
      }

      if (mod > 0.0 && rate > 0.0) {
         perGroup = this.isNotUseMode() ? 1 : perGroup;
         double pRate = rate * premiumBonus;
         double groupChanceModifier = pRate >= 2.0 ? Config.GROUP_CHANCE_MODIFIER * pRate + 1.0 : 1.0;
         double dropChanceModifier = rate >= 2.0 ? Config.GROUP_CHANCE_MODIFIER * rate + 1.0 : 1.0;
         double groupChance = this.getChance() * mod * groupChanceModifier;
         if (groupChance > 1000000.0) {
            mod = (groupChance - 1000000.0) / this.getChance() + 1.0;
            groupChance = 1000000.0;
         } else {
            mod = 1.0;
         }

         double dropChance = this.getChance() * dpmod * dropChanceModifier;
         if (dropChance > 1000000.0) {
            dpmod = (dropChance - 1000000.0) / this.getChance() + 1.0;
            dropChance = 1000000.0;
         } else {
            dpmod = 1.0;
         }

         if (groupChance > 0.0) {
            boolean success = false;
            if (groupChance >= 1000000.0) {
               success = true;
            } else if (groupChance > (double)Rnd.get(1000000)) {
               success = true;
            }

            if (success) {
               List<RewardItem> rolledItems = new ArrayList<>();

               for(RewardData data : this.getItems()) {
                  RewardItem item = data.rollItem(player, mod, dpmod, rate, dropChance, premiumBonus, useModifier);
                  if (item != null) {
                     rolledItems.add(item);
                  }
               }

               if (rolledItems.isEmpty()) {
                  return Collections.emptyList();
               }

               List<RewardItem> result = new ArrayList<>();

               for(int i = 0; i < perGroup; ++i) {
                  RewardItem rolledItem = Rnd.get(rolledItems);
                  if (rolledItems.remove(rolledItem)) {
                     result.add(rolledItem);
                  }

                  if (rolledItems.isEmpty()) {
                     break;
                  }
               }

               return result;
            }
         }
      }

      return Collections.emptyList();
   }
}
