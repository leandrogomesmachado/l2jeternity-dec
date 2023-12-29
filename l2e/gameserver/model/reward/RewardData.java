package l2e.gameserver.model.reward;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.NamedNodeMap;

public class RewardData implements Cloneable {
   private final Item _item;
   private boolean _notRate = false;
   private long _mindrop;
   private long _maxdrop;
   private double _chance;

   public RewardData(int itemId) {
      this._item = ItemsParser.getInstance().getTemplate(itemId);
      if (this._item.isArrow()
         || Config.NO_RATE_EQUIPMENT && (this._item.isEquipment() || this._item.isCloak())
         || Config.NO_RATE_KEY_MATERIAL && this._item.isKeyMatherial()
         || Config.NO_RATE_RECIPES && this._item.isRecipe()
         || ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId)) {
         this._notRate = true;
      }
   }

   public RewardData(int itemId, long min, long max, double chance) {
      this(itemId);
      this._mindrop = min;
      this._maxdrop = max;
      this._chance = chance;
   }

   public boolean notRate() {
      return this._notRate;
   }

   public void setNotRate(boolean notRate) {
      this._notRate = notRate;
   }

   public int getId() {
      return this._item.getId();
   }

   public Item getItem() {
      return this._item;
   }

   public long getMinDrop() {
      return this._mindrop;
   }

   public long getMaxDrop() {
      return this._maxdrop;
   }

   public double getChance() {
      return this._chance;
   }

   public void setMinDrop(long mindrop) {
      this._mindrop = mindrop;
   }

   public void setMaxDrop(long maxdrop) {
      this._maxdrop = maxdrop;
   }

   public void setChance(double chance) {
      this._chance = chance;
   }

   @Override
   public String toString() {
      return "ItemID: " + this.getId() + " Min: " + this.getMinDrop() + " Max: " + this.getMaxDrop() + " Chance: " + this.getChance() / 10000.0 + "%";
   }

   public RewardData clone() {
      return new RewardData(this.getId(), this.getMinDrop(), this.getMaxDrop(), this.getChance());
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof RewardData) {
         RewardData drop = (RewardData)o;
         return drop.getId() == this.getId();
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return 18 * this.getId() + 184140;
   }

   protected RewardItem rollAdena(double mod, double rate, double premiumBonus) {
      if (this.notRate()) {
         mod = Math.min(mod, 1.0);
         rate = 1.0;
      }

      if (mod > 0.0 && rate > 0.0) {
         double chance = this.getChance() * mod;
         if (chance > (double)Rnd.get(1000000)) {
            RewardItem t = new RewardItem(this._item.getId());
            if (this.getMinDrop() >= this.getMaxDrop()) {
               t._count = (long)(rate * (double)this.getMinDrop() * premiumBonus);
            } else {
               t._count = (long)(rate * (double)Rnd.get((long)((double)this.getMinDrop() * premiumBonus), (long)((double)this.getMaxDrop() * premiumBonus)));
            }

            return t;
         }
      }

      return null;
   }

   protected RewardItem rollItem(Player player, double mod, double dpmod, double rate, double dropChance, double premiumBonus, boolean useModifier) {
      if (this.notRate()) {
         mod = Math.min(mod, 1.0);
         dpmod = Math.min(dpmod, 1.0);
         rate = 1.0;
      }

      if (mod > 0.0 && rate > 0.0) {
         double chance = Math.min(1000000.0, this.getChance() * mod);
         double dropGeneral = Math.min(1000000.0, this.getChance() * dropChance / 1000000.0 * dpmod) / 1000000.0;
         if (chance > 0.0) {
            boolean success = false;
            if (chance >= 1000000.0) {
               success = true;
            } else if (chance > (double)Rnd.get(1000000)) {
               success = true;
            }

            if (success) {
               RewardItem t = new RewardItem(this._item.getId());
               double maxAmount = (double)this.getMaxDrop() * rate;
               if (rate > 1.0) {
                  maxAmount = getCorrectMaxAmount(
                     this.notRate() ? 1.0 : (this.getMaxDrop() > 1L ? maxAmount * dropGeneral * premiumBonus / 2.0 : maxAmount * dropGeneral * premiumBonus)
                  );
               }

               long minCount = Math.max(
                  1L,
                  (long)(
                     this.notRate()
                        ? (double)this.getMinDrop()
                        : RewardItemRates.getMinCountModifier(player, this._item, useModifier) * (double)this.getMinDrop()
                  )
               );
               long maxCount = Math.max(
                  1L, (long)(this.notRate() ? (double)this.getMaxDrop() : RewardItemRates.getMaxCountModifier(player, this._item, useModifier) * maxAmount)
               );
               if (minCount >= maxCount) {
                  t._count = minCount;
               } else {
                  t._count = Rnd.get(minCount, maxCount);
               }

               return t;
            }
         }
      }

      return null;
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

   public static RewardData parseReward(NamedNodeMap rewardElement, RewardType type) {
      int itemId = Integer.parseInt(rewardElement.getNamedItem("itemId").getNodeValue());
      if (type == RewardType.SWEEP) {
         if (Config.NO_DROP_ITEMS_FOR_SWEEP.contains(itemId)) {
            return null;
         }
      } else if (Config.DISABLE_ITEM_DROP_LIST.contains(itemId)) {
         return null;
      }

      if (Config.ALLOW_ONLY_THESE_DROP_ITEMS_ID.size() >= 1
         && Config.ALLOW_ONLY_THESE_DROP_ITEMS_ID.get(0) != 0
         && !Config.ALLOW_ONLY_THESE_DROP_ITEMS_ID.contains(itemId)) {
         return null;
      } else {
         int min = Integer.parseInt(rewardElement.getNamedItem("min").getNodeValue());
         int max = Integer.parseInt(rewardElement.getNamedItem("max").getNodeValue());
         int chance = (int)(Double.parseDouble(rewardElement.getNamedItem("chance").getNodeValue()) * 10000.0);
         double chance_dop = (double)chance * Config.RATE_CHANCE_DROP_ITEMS;
         double chance_h = (double)chance * Config.RATE_CHANCE_DROP_HERBS;
         double chance_sp = (double)chance * Config.RATE_CHANCE_SPOIL;
         double chance_weapon = (double)chance * Config.RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
         double chance_weapon_sp = (double)chance * Config.RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
         double chance_epolet = (double)chance * Config.RATE_CHANCE_DROP_EPOLET;
         double chance_att = (double)chance * Config.RATE_CHANCE_ATTRIBUTE;
         if (chance_dop > 1000000.0) {
            chance_dop = 1000000.0;
         }

         if (chance_h > 1000000.0) {
            chance_h = 1000000.0;
         }

         if (chance_sp > 1000000.0) {
            chance_sp = 1000000.0;
         }

         if (chance_weapon > 1000000.0) {
            chance_weapon = 1000000.0;
         }

         if (chance_weapon_sp > 1000000.0) {
            chance_weapon_sp = 1000000.0;
         }

         if (chance_epolet > 1000000.0) {
            chance_epolet = 1000000.0;
         }

         if (chance_att > 1000000.0) {
            chance_att = 1000000.0;
         }

         RewardData data = new RewardData(itemId);
         if (type == RewardType.RATED_GROUPED) {
            if (data.getItem().isHerb()) {
               data.setChance(chance_h);
            } else if (data.getItem().isWeapon() || data.getItem().isArmor() || data.getItem().isAccessory()) {
               data.setChance(chance_weapon);
            } else if (data.getItem().isEpolets()) {
               data.setChance(chance_epolet);
            } else if (!data.getItem().isAttributeCrystal() && !data.getItem().isAttributeStone()) {
               data.setChance(chance_dop);
            } else {
               data.setChance(chance_att);
            }
         } else if (type == RewardType.SWEEP) {
            if (!data.getItem().isWeapon() && !data.getItem().isArmor() && !data.getItem().isAccessory()) {
               data.setChance(chance_sp);
            } else {
               data.setChance(chance_weapon_sp);
            }
         } else if (type == RewardType.NOT_RATED_GROUPED || type == RewardType.NOT_RATED_NOT_GROUPED) {
            if (data.getItem().isHerb()) {
               data.setChance(chance_h);
            } else {
               data.setChance((double)chance);
            }
         }

         data.setMinDrop((long)min);
         data.setMaxDrop((long)max);
         return data;
      }
   }
}
