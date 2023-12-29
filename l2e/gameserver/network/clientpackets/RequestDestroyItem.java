package l2e.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.ItemRecoveryManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestDestroyItem extends GameClientPacket {
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
         if (this._count <= 0L) {
            if (this._count < 0L) {
               Util.handleIllegalPlayerAction(
                  activeChar,
                  ""
                     + activeChar.getName()
                     + " of account "
                     + activeChar.getAccountName()
                     + " tried to destroy item with oid "
                     + this._objectId
                     + " but has count < 0!"
               );
            }
         } else if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            long count = this._count;
            if (!activeChar.isProcessingTransaction() && activeChar.getPrivateStoreType() == 0) {
               ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(this._objectId);
               if (itemToRemove == null) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
               } else if (activeChar.isCastingNow()
                  && activeChar.getCurrentSkill() != null
                  && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getId()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
               } else if (activeChar.isCastingSimultaneouslyNow()
                  && activeChar.getCastingSkill() != null
                  && activeChar.getCastingSkill().getItemConsumeId() == itemToRemove.getId()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
               } else {
                  int itemId = itemToRemove.getId();
                  if ((activeChar.canOverrideCond(PcCondOverride.DESTROY_ALL_ITEMS) || itemToRemove.isDestroyable())
                     && !CursedWeaponsManager.getInstance().isCursed(itemId)) {
                     if (!itemToRemove.isStackable() && count > 1L) {
                        Util.handleIllegalPlayerAction(
                           activeChar,
                           ""
                              + activeChar.getName()
                              + " of account "
                              + activeChar.getAccountName()
                              + " tried to destroy a non-stackable item with oid "
                              + this._objectId
                              + " but has count > 1!"
                        );
                     } else if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getId())) {
                        activeChar.sendMessage("You cannot use this item.");
                     } else {
                        if (this._count > itemToRemove.getCount()) {
                           count = itemToRemove.getCount();
                        }

                        if (itemToRemove.getItem().isPetItem()) {
                           if (activeChar.hasSummon() && activeChar.getSummon().getControlObjectId() == this._objectId) {
                              activeChar.getSummon().unSummon(activeChar);
                           }

                           try (
                              Connection con = DatabaseFactory.getInstance().getConnection();
                              PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
                           ) {
                              statement.setInt(1, this._objectId);
                              statement.execute();
                           } catch (Exception var38) {
                              _log.log(Level.WARNING, "could not delete pet objectid: ", (Throwable)var38);
                           }
                        }

                        if (itemToRemove.isTimeLimitedItem()) {
                           itemToRemove.endOfLife();
                        }

                        if (itemToRemove.isEquipped()) {
                           if (itemToRemove.getEnchantLevel() > 0) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                              sm.addNumber(itemToRemove.getEnchantLevel());
                              sm.addItemName(itemToRemove);
                              activeChar.sendPacket(sm);
                           } else {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                              sm.addItemName(itemToRemove);
                              activeChar.sendPacket(sm);
                           }

                           ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
                           InventoryUpdate iu = new InventoryUpdate();

                           for(ItemInstance itm : unequiped) {
                              iu.addModifiedItem(itm);
                           }

                           activeChar.sendPacket(iu);
                        }

                        ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", itemToRemove, count, activeChar, null);
                        if (removedItem != null) {
                           if (Config.ALLOW_RECOVERY_ITEMS) {
                              ItemRecoveryManager.getInstance().saveToRecoveryItem(activeChar, removedItem, count);
                           }

                           if (!Config.FORCE_INVENTORY_UPDATE) {
                              InventoryUpdate iu = new InventoryUpdate();
                              if (removedItem.getCount() == 0L) {
                                 iu.addRemovedItem(removedItem);
                              } else {
                                 iu.addModifiedItem(removedItem);
                              }

                              activeChar.sendPacket(iu);
                           } else {
                              activeChar.sendItemList(true);
                           }

                           StatusUpdate su = new StatusUpdate(activeChar);
                           su.addAttribute(14, activeChar.getCurrentLoad());
                           activeChar.sendPacket(su);
                        }
                     }
                  } else {
                     if (itemToRemove.isHeroItem()) {
                        activeChar.sendPacket(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
                     } else {
                        activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
                     }
                  }
               }
            } else {
               activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
            }
         }
      }
   }
}
