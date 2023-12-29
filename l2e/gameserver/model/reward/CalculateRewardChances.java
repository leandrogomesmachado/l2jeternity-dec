package l2e.gameserver.model.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.stats.Stats;
import org.apache.commons.lang3.StringUtils;

public class CalculateRewardChances {
   private static final Map<Integer, Integer[]> droplistsCountCache = new HashMap<>();

   public static List<NpcTemplate> getNpcsContainingString(CharSequence name) {
      List<NpcTemplate> templates = new ArrayList<>();

      for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
         if (templateExists(template)
            && (StringUtils.containsIgnoreCase(template.getName(), name) || StringUtils.containsIgnoreCase(template.getNameRu(), name))
            && isDroppingAnything(template)) {
            templates.add(template);
         }
      }

      return templates;
   }

   public static int getDroplistsCountByItemId(int itemId, boolean drop) {
      if (droplistsCountCache.containsKey(itemId)) {
         return drop ? droplistsCountCache.get(itemId)[0] : droplistsCountCache.get(itemId)[1];
      } else {
         int dropCount = 0;
         int spoilCount = 0;

         for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
            if (templateExists(template)) {
               for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
                  for(RewardGroup group : rewardEntry.getValue()) {
                     for(RewardData data : group.getItems()) {
                        if (data.getItem().getId() == itemId) {
                           if (rewardEntry.getKey() == RewardType.SWEEP) {
                              ++spoilCount;
                           } else {
                              ++dropCount;
                           }
                        }
                     }
                  }
               }
            }
         }

         droplistsCountCache.put(itemId, new Integer[]{dropCount, spoilCount});
         return drop ? dropCount : spoilCount;
      }
   }

   private static boolean templateExists(NpcTemplate template) {
      return template != null;
   }

   public static List<Item> getDroppableItems() {
      List<Item> items = new ArrayList<>();

      for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
         if (templateExists(template)) {
            for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
               for(RewardGroup group : rewardEntry.getValue()) {
                  for(RewardData data : group.getItems()) {
                     if (!items.contains(data.getItem())) {
                        items.add(data.getItem());
                     }
                  }
               }
            }
         }
      }

      return items;
   }

   public static List<CalculateRewardChances.NpcTemplateDrops> getNpcsByDropOrSpoil(int itemId) {
      List<CalculateRewardChances.NpcTemplateDrops> templates = new ArrayList<>();

      for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
         if (template != null) {
            boolean[] dropSpoil = templateContainsItemId(template, itemId);
            if (dropSpoil[0]) {
               templates.add(new CalculateRewardChances.NpcTemplateDrops(template, true));
            }

            if (dropSpoil[1]) {
               templates.add(new CalculateRewardChances.NpcTemplateDrops(template, false));
            }
         }
      }

      return templates;
   }

   private static boolean[] templateContainsItemId(NpcTemplate template, int itemId) {
      boolean[] dropSpoil = new boolean[]{false, false};

      for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
         if (rewardListContainsItemId(rewardEntry.getValue(), itemId)) {
            if (rewardEntry.getKey() == RewardType.SWEEP) {
               dropSpoil[1] = true;
            } else {
               dropSpoil[0] = true;
            }
         }
      }

      return dropSpoil;
   }

   private static boolean rewardListContainsItemId(RewardList list, int itemId) {
      for(RewardGroup group : list) {
         for(RewardData reward : group.getItems()) {
            if (reward.getId() == itemId) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isDroppingAnything(NpcTemplate template) {
      for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
         for(RewardGroup group : rewardEntry.getValue()) {
            if (!group.getItems().isEmpty()) {
               return true;
            }
         }
      }

      return false;
   }

   public static List<RewardData> getDrops(NpcTemplate template, boolean drop, boolean spoil) {
      List<RewardData> allRewards = new ArrayList<>();
      if (template == null) {
         return allRewards;
      } else {
         for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
            if ((rewardEntry.getKey() != RewardType.SWEEP || spoil) && (rewardEntry.getKey() == RewardType.SWEEP || drop)) {
               for(RewardGroup group : rewardEntry.getValue()) {
                  for(RewardData reward : group.getItems()) {
                     allRewards.add(reward);
                  }
               }
            }
         }

         return allRewards;
      }
   }

   public static double[] getAmountAndChanceById(
      Player player, NpcTemplate template, double penaltyMod, boolean dropNoSpoil, int itemId, ChampionTemplate championTemplate
   ) {
      double[] shortInfo = new double[]{0.0, 0.0, 0.0};
      List<CalculateRewardChances.DropInfoTemplate> infoAndData = getAmountAndChance(player, template, penaltyMod, dropNoSpoil, championTemplate);
      if (infoAndData == null) {
         return shortInfo;
      } else {
         for(CalculateRewardChances.DropInfoTemplate dp : infoAndData) {
            if (dp._item.getId() == itemId) {
               shortInfo = new double[]{(double)dp._minCount, (double)dp._maxCount, dp._chance};
            }
         }

         infoAndData.clear();
         return shortInfo;
      }
   }

   public static List<CalculateRewardChances.DropInfoTemplate> getAmountAndChance(
      Player player, NpcTemplate template, double penaltyMod, boolean dropNoSpoil, ChampionTemplate championTemplate
   ) {
      List<CalculateRewardChances.DropInfoTemplate> info = new ArrayList<>();

      for(Entry<RewardType, RewardList> rewardEntry : template.getRewards().entrySet()) {
         if ((rewardEntry.getKey() != RewardType.SWEEP || !dropNoSpoil) && (rewardEntry.getKey() == RewardType.SWEEP || dropNoSpoil)) {
            for(RewardGroup group : rewardEntry.getValue()) {
               List<RewardData> items = new ArrayList<>();

               for(RewardData d : group.getItems()) {
                  if (!d.getItem().isHerb()) {
                     items.add(d);
                  }
               }

               if (!items.isEmpty()) {
                  double grate = 1.0;
                  double gpmod = penaltyMod;
                  double dpmod = penaltyMod;
                  double premiumBonus = 1.0;
                  RewardType type = rewardEntry.getKey();
                  if (type == RewardType.RATED_GROUPED) {
                     if (group.isAdena()) {
                        double adenaRate = Config.RATE_DROP_ADENA;
                        if (championTemplate != null) {
                           adenaRate *= championTemplate.adenaMultiplier;
                        }

                        premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                           ? player.getParty().getDropAdena()
                           : player.getPremiumBonus().getDropAdena();
                        grate = player.calcStat(Stats.ADENA_MULTIPLIER, adenaRate, player, null);
                     } else if (template.isEpicRaid()) {
                        double dropRate = Config.RATE_DROP_EPICBOSS;
                        premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                           ? player.getParty().getDropEpics()
                           : player.getPremiumBonus().getDropEpics();
                        grate = player.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null);
                     } else if (template.isRaid()) {
                        double dropRate = Config.RATE_DROP_RAIDBOSS;
                        premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                           ? player.getParty().getDropRaids()
                           : player.getPremiumBonus().getDropRaids();
                        grate = player.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null);
                     } else if (template.isSiegeGuard()) {
                        double dropRate = Config.RATE_DROP_SIEGE_GUARD;
                        premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                           ? player.getParty().getDropSiege()
                           : player.getPremiumBonus().getDropSiege();
                        grate = player.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null);
                     } else {
                        double dropRate = Config.RATE_DROP_ITEMS;
                        if (championTemplate != null) {
                           dropRate *= championTemplate.itemDropMultiplier;
                        }

                        premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                           ? player.getParty().getDropItems()
                           : player.getPremiumBonus().getDropItems();
                        grate = player.calcStat(Stats.REWARD_MULTIPLIER, dropRate, player, null);
                     }
                  } else if (type == RewardType.SWEEP) {
                     double sweepRate = Config.RATE_DROP_SPOIL;
                     if (championTemplate != null) {
                        sweepRate *= championTemplate.spoilDropMultiplier;
                     }

                     premiumBonus = player.isInParty() && Config.PREMIUM_PARTY_RATE
                        ? player.getParty().getDropSpoil()
                        : player.getPremiumBonus().getDropSpoil();
                     grate = player.calcStat(Stats.SPOIL_MULTIPLIER, sweepRate, player, null);
                  }

                  if (group.notRate()) {
                     gpmod = Math.min(penaltyMod, 1.0);
                     dpmod = Math.min(penaltyMod, 1.0);
                     grate = 1.0;
                  }

                  if (player.isGM() || gpmod != 0.0 && grate != 0.0) {
                     double pRate = grate * premiumBonus;
                     double groupChanceModifier = pRate >= 2.0 ? Config.GROUP_CHANCE_MODIFIER * pRate + 1.0 : 1.0;
                     double dropChanceModifier = grate >= 2.0 ? Config.GROUP_CHANCE_MODIFIER * grate + 1.0 : 1.0;
                     double groupChance = group.isAdena() ? group.getChance() * gpmod : group.getChance() * gpmod * groupChanceModifier;
                     if (groupChance > 1000000.0) {
                        gpmod = (groupChance - 1000000.0) / group.getChance() + 1.0;
                        groupChance = 1000000.0;
                     } else {
                        gpmod = 1.0;
                     }

                     double dropChance = group.isAdena() ? group.getChance() * dpmod : group.getChance() * dpmod * dropChanceModifier;
                     if (dropChance > 1000000.0) {
                        dpmod = (dropChance - 1000000.0) / group.getChance() + 1.0;
                        dropChance = 1000000.0;
                     } else {
                        dpmod = 1.0;
                     }

                     for(RewardData d : items) {
                        boolean allowModifier = true;
                        if (type == RewardType.SWEEP) {
                           allowModifier = Config.ALLOW_MODIFIER_FOR_SPOIL;
                        } else {
                           allowModifier = !template.isEpicRaid() && !template.isRaid() ? Config.ALLOW_MODIFIER_FOR_DROP : Config.ALLOW_MODIFIER_FOR_RAIDS;
                        }

                        double ipmod = d.notRate() ? Math.min(gpmod, 1.0) : gpmod;
                        double idmod = d.notRate() ? Math.min(dpmod, 1.0) : dpmod;
                        double irate = d.notRate() ? 1.0 : grate;
                        long minCount = (long)Math.max(1.0, (double)d.getMinDrop() * (group.isAdena() ? irate * premiumBonus : 1.0));
                        double maxCount = Math.max(1.0, (double)d.getMaxDrop() * (group.isAdena() ? irate * premiumBonus : irate));
                        double chanceGeneral = (
                              group.isAdena()
                                 ? Math.min(1000000.0, 1000000.0 * groupChance / d.getChance() * ipmod)
                                 : Math.min(1000000.0, d.getChance() * groupChance / 1000000.0 * ipmod)
                           )
                           / 1000000.0;
                        double dropGeneral = (
                              group.isAdena()
                                 ? Math.min(1000000.0, 1000000.0 * dropChance / d.getChance() * idmod)
                                 : Math.min(1000000.0, d.getChance() * dropChance / 1000000.0 * idmod)
                           )
                           / 1000000.0;
                        if (!group.isAdena()) {
                           if (irate > 1.0) {
                              maxCount = getCorrectMaxAmount(
                                 d.notRate()
                                    ? 1.0
                                    : (d.getMaxDrop() > 1L ? maxCount * dropGeneral * premiumBonus / 2.0 : maxCount * dropGeneral * premiumBonus)
                              );
                           }

                           minCount = Math.max(
                              1L,
                              (long)(
                                 d.notRate()
                                    ? (double)d.getMinDrop()
                                    : RewardItemRates.getMinCountModifier(player, d.getItem(), allowModifier) * (double)minCount
                              )
                           );
                           maxCount = (double)Math.max(
                              1L,
                              (long)(d.notRate() ? (double)d.getMaxDrop() : RewardItemRates.getMaxCountModifier(player, d.getItem(), allowModifier) * maxCount)
                           );
                        }

                        info.add(new CalculateRewardChances.DropInfoTemplate(d, minCount, (long)maxCount, chanceGeneral));
                     }
                  }
               }
            }
         }
      }

      return info;
   }

   private static double getCorrectMaxAmount(double modifier) {
      double finalMod = 1.0;

      for(int amounts : Config.MAX_AMOUNT_CORRECTOR.keySet()) {
         if (modifier >= (double)amounts && finalMod <= (double)amounts) {
            finalMod = Config.MAX_AMOUNT_CORRECTOR.get(amounts);
         }
      }

      finalMod *= modifier;
      if (finalMod < 1.0) {
         finalMod = 1.0;
      }

      return finalMod;
   }

   public static class DropInfoTemplate {
      public final RewardData _item;
      public final long _minCount;
      public final long _maxCount;
      public final double _chance;

      public DropInfoTemplate(RewardData item, long minCount, long maxCount, double chance) {
         this._item = item;
         this._minCount = minCount;
         this._maxCount = maxCount;
         this._chance = chance;
      }
   }

   public static class NpcTemplateDrops {
      public NpcTemplate _template;
      public boolean _dropNoSpoil;

      private NpcTemplateDrops(NpcTemplate template, boolean dropNoSpoil) {
         this._template = template;
         this._dropNoSpoil = dropNoSpoil;
      }
   }
}
