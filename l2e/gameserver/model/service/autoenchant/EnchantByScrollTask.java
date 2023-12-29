package l2e.gameserver.model.service.autoenchant;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.enchant.EnchantScroll;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class EnchantByScrollTask implements Runnable {
   private final Player _player;

   public EnchantByScrollTask(Player player) {
      this._player = player;
   }

   public static boolean isValidPlayer(Player player) {
      if (player == null) {
         return false;
      } else if (player.isActionsDisabled()) {
         return false;
      } else if (player.isProcessingTransaction() || player.isInStoreMode()) {
         player.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
         return false;
      } else {
         return player.isOnline() && !player.getClient().isDetached();
      }
   }

   @Override
   public void run() {
      if (!isValidPlayer(this._player)) {
         this._player.sendMessage(new ServerMessage("Enchant.NOT_VALID", this._player.getLang()).toString());
      } else {
         boolean isNeedEquip = false;
         boolean isNeedUpdate = false;
         int isCrystallized = 0;
         int maxEnchant = 0;
         int commonScrolls = 0;
         int scrolls = 0;
         int success = 0;
         int count = 0;
         ItemInstance item = this._player.getEnchantParams().targetItem;
         ItemInstance scroll = this._player.getEnchantParams().upgradeItem;
         PcInventory inventory = this._player.getInventory();
         if (item != null && item.isEquipped()) {
            inventory.unEquipItem(item);
            isNeedEquip = true;
         }

         try {
            for(int i = 0;
               i < this._player.getEnchantParams().upgradeItemLimit
                  && this._player.getEnchantParams().targetItem.getEnchantLevel() < this._player.getEnchantParams().maxEnchant;
               ++i
            ) {
               if (!isValidPlayer(this._player)) {
                  this._player.sendMessage(new ServerMessage("Enchant.NOT_VALID", this._player.getLang()).toString());
                  return;
               }

               if (item == null) {
                  this._player.sendMessage(new ServerMessage("Enchant.SELECT_SCROLL", this._player.getLang()).toString());
                  return;
               }

               if (item.getEnchantLevel() < 3 && this._player.getEnchantParams().isUseCommonScrollWhenSafe) {
                  scroll = EnchantUtils.getInstance().getUnsafeEnchantScroll(this._player, item);
                  if (scroll == null) {
                     this._player.sendMessage(new ServerMessage("Enchant.COND_OR_SCROLL", this._player.getLang()).toString());
                     return;
                  }

                  ++commonScrolls;
               } else {
                  scroll = this._player.getEnchantParams().upgradeItem;
                  if (scroll == null) {
                     this._player.sendMessage(new ServerMessage("Enchant.NOT_SCROLL", this._player.getLang()).toString());
                     return;
                  }

                  ++scrolls;
               }

               this._player.setActiveEnchantItemId(scroll.getObjectId());
               EnchantScroll esi = EnchantItemParser.getInstance().getEnchantScroll(scroll);
               if (esi == null) {
                  this._player.setActiveEnchantItemId(-1);
                  this._player.sendMessage(new ServerMessage("Enchant.SCROLL_NOT_VALID", this._player.getLang()).toString());
                  return;
               }

               if (!esi.isValid(item)) {
                  this._player.setActiveEnchantItemId(-1);
                  this._player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                  return;
               }

               if (inventory.destroyItem("[AutoEnchant]", scroll.getObjectId(), 1L, this._player, null) == null) {
                  this._player.setActiveEnchantItemId(-1);
                  this._player.sendMessage(new ServerMessage("Enchant.SCROLL_MISS", this._player.getLang()).toString());
                  return;
               }

               if (item.isEquipped()) {
                  inventory.unEquipItem(item);
               }

               double chance = esi.getChance(this._player, item);
               if (chance == -1.0) {
                  this._player.setActiveEnchantItemId(-1);
                  this._player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
                  return;
               }

               this._player.setActiveEnchantItemId(item.getId());
               chance = Math.min(chance + (double)this._player.getPremiumBonus().getEnchantChance(), 100.0);
               if (!Rnd.chance(chance + (double)Config.ENCHANT_SCROLL_CHANCE_CORRECT)) {
                  if (esi.isSafe()) {
                     this._player.sendPacket(SystemMessageId.SAFE_ENCHANT_FAILED);
                     return;
                  }

                  if (esi.isBlessed()) {
                     this._player.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED);
                     if (Config.SYSTEM_BLESSED_ENCHANT) {
                        item.setEnchantLevel(Config.BLESSED_ENCHANT_SAVE);
                     } else {
                        item.setEnchantLevel(0);
                        item.updateDatabase();
                     }

                     return;
                  }

                  int crystalId = item.getItem().getCrystalItemId();
                  int CryCount = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
                  if (CryCount < 1) {
                     CryCount = 1;
                  }

                  ItemInstance destroyItem = this._player.getInventory().destroyItem("[AutoEnchant]", item, this._player, null);
                  if (destroyItem != null) {
                     ItemInstance crystals = null;
                     if (crystalId != 0) {
                        crystals = this._player.getInventory().addItem("[AutoEnchant]", crystalId, (long)CryCount, this._player, destroyItem);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                        sm.addItemName(crystals);
                        sm.addItemNumber((long)CryCount);
                        this._player.sendPacket(sm);
                     }

                     if (!Config.FORCE_INVENTORY_UPDATE) {
                        InventoryUpdate iu = new InventoryUpdate();
                        if (destroyItem.getCount() == 0L) {
                           iu.addRemovedItem(destroyItem);
                        } else {
                           iu.addModifiedItem(destroyItem);
                        }

                        if (crystals != null) {
                           iu.addItem(crystals);
                        }

                        if (scroll.getCount() == 0L) {
                           iu.addRemovedItem(scroll);
                        } else {
                           iu.addModifiedItem(scroll);
                        }

                        this._player.sendPacket(iu);
                     } else {
                        this._player.sendItemList(true);
                     }

                     World.getInstance().removeObject(destroyItem);
                     return;
                  }

                  Util.handleIllegalPlayerAction(
                     this._player, "Unable to delete item on enchant failure from player " + this._player.getName() + ", possible cheater !"
                  );
                  this._player.setActiveEnchantItemId(-1);
                  return;
               }

               if (chance != 100.0) {
                  ++success;
               }

               item.setEnchantLevel(item.getEnchantLevel() + 1);
               item.updateDatabase();
               int ench = item.getEnchantLevel();
               if (ench > maxEnchant) {
                  maxEnchant = ench;
               }

               isNeedUpdate = true;
               if (ench >= 3) {
                  ++count;
               }
            }
         } finally {
            if (isNeedEquip && item != null) {
               inventory.equipItem(item);
            }

            if (isNeedUpdate) {
               if (Config.ENCHANT_CONSUME_ITEM != 0) {
                  this._player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, (long)Config.ENCHANT_CONSUME_ITEM_COUNT, "[AutoEnchant]");
                  Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                  ServerMessage msg = new ServerMessage("Enchant.SPET_ITEMS", this._player.getLang());
                  msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
                  msg.add(this._player.getItemName(template));
                  this._player.sendMessage(msg.toString());
               }

               Map<String, Integer> result = new HashMap<>();
               result.put("crystallized", 0);
               result.put("enchant", item == null ? 0 : item.getEnchantLevel());
               result.put("maxenchant", maxEnchant);
               result.put("scrolls", scrolls);
               result.put("commonscrolls", commonScrolls);
               if (count == 0) {
                  ++count;
               }

               result.put("chance", (int)((double)success / ((double)count / 100.0) * 100.0));
               result.put("success", item == null ? 0 : (item.getEnchantLevel() == this._player.getEnchantParams().maxEnchant ? 1 : 0));
               EnchantManager.getInstance().showResultPage(this._player, EnchantType.SCROLL, result);
            }

            this._player.setActiveEnchantItemId(-1);
            StatusUpdate su = new StatusUpdate(this._player);
            su.addAttribute(14, this._player.getCurrentLoad());
            this._player.sendPacket(su);
            this._player.sendItemList(false);
            this._player.broadcastCharInfo();
         }
      }
   }
}
