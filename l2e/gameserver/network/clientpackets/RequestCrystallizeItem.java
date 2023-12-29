package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestCrystallizeItem extends GameClientPacket {
   private int _objectId;
   private long _count;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._count = this.readQ();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else if (this._count <= 0L) {
            Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + this._objectId + " owner: " + activeChar.getName());
         } else if (activeChar.getPrivateStoreType() == 0 && !activeChar.isInCrystallize()) {
            int skillLevel = activeChar.getSkillLevel(248);
            if (skillLevel <= 0) {
               activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
               activeChar.sendActionFailed();
               if (activeChar.getRace() != Race.Dwarf && activeChar.getClassId().ordinal() != 117 && activeChar.getClassId().ordinal() != 55) {
                  _log.info("Player " + activeChar.getClient() + " used crystalize with classid: " + activeChar.getClassId().ordinal());
               }
            } else {
               PcInventory inventory = activeChar.getInventory();
               if (inventory != null) {
                  ItemInstance item = inventory.getItemByObjectId(this._objectId);
                  if (item == null) {
                     activeChar.sendActionFailed();
                     return;
                  }

                  if (item.isHeroItem()) {
                     return;
                  }

                  if (this._count > item.getCount()) {
                     this._count = activeChar.getInventory().getItemByObjectId(this._objectId).getCount();
                  }
               }

               ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(this._objectId);
               if (itemToRemove != null && !itemToRemove.isShadowItem() && !itemToRemove.isTimeLimitedItem()) {
                  if (itemToRemove.getItem().isCrystallizable()
                     && itemToRemove.getItem().getCrystalCount() > 0
                     && itemToRemove.getItem().getCrystalType() != 0) {
                     if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getId())) {
                        activeChar.sendMessage("You cannot use this item.");
                     } else {
                        boolean canCrystallize = true;
                        switch(itemToRemove.getItem().getItemGradeSPlus()) {
                           case 2:
                              if (skillLevel <= 1) {
                                 canCrystallize = false;
                              }
                              break;
                           case 3:
                              if (skillLevel <= 2) {
                                 canCrystallize = false;
                              }
                              break;
                           case 4:
                              if (skillLevel <= 3) {
                                 canCrystallize = false;
                              }
                              break;
                           case 5:
                              if (skillLevel <= 4) {
                                 canCrystallize = false;
                              }
                        }

                        if (!canCrystallize) {
                           activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
                           activeChar.sendActionFailed();
                        } else {
                           activeChar.setInCrystallize(true);
                           if (itemToRemove.isEquipped()) {
                              ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
                              InventoryUpdate iu = new InventoryUpdate();

                              for(ItemInstance item : unequiped) {
                                 iu.addModifiedItem(item);
                              }

                              activeChar.sendPacket(iu);
                              SystemMessage sm;
                              if (itemToRemove.getEnchantLevel() > 0) {
                                 sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                                 sm.addNumber(itemToRemove.getEnchantLevel());
                                 sm.addItemName(itemToRemove);
                              } else {
                                 sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                                 sm.addItemName(itemToRemove);
                              }

                              activeChar.sendPacket(sm);
                           }

                           ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", this._objectId, this._count, activeChar, null);
                           InventoryUpdate iu = new InventoryUpdate();
                           iu.addRemovedItem(removedItem);
                           activeChar.sendPacket(iu);
                           int crystalId = itemToRemove.getItem().getCrystalItemId();
                           int crystalAmount = itemToRemove.getCrystalCount();
                           ItemInstance createditem = activeChar.getInventory().addItem("Crystalize", crystalId, (long)crystalAmount, activeChar, activeChar);
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED);
                           sm.addItemName(removedItem);
                           activeChar.sendPacket(sm);
                           sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                           sm.addItemName(createditem);
                           sm.addItemNumber((long)crystalAmount);
                           activeChar.sendPacket(sm);
                           activeChar.broadcastCharInfo();
                           activeChar.setInCrystallize(false);
                        }
                     }
                  } else {
                     Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to crystallize " + itemToRemove.getItem().getId());
                  }
               }
            }
         } else {
            activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
         }
      }
   }
}
