package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestCancelSentPost extends GameClientPacket {
   private int _msgId;

   @Override
   protected void readImpl() {
      this._msgId = this.readD();
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && Config.ALLOW_MAIL && Config.ALLOW_ATTACHMENTS) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            Message msg = MailManager.getInstance().getMessage(this._msgId);
            if (msg != null) {
               if (msg.getSenderId() != activeChar.getObjectId()) {
                  Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to cancel not own post!");
               } else if (!activeChar.isInZonePeace()) {
                  activeChar.sendPacket(SystemMessageId.CANT_CANCEL_NOT_IN_PEACE_ZONE);
               } else if (activeChar.getActiveTradeList() != null) {
                  activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_EXCHANGE);
               } else if (activeChar.isEnchanting()) {
                  activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_ENCHANT);
               } else if (activeChar.getPrivateStoreType() > 0) {
                  activeChar.sendPacket(SystemMessageId.CANT_CANCEL_PRIVATE_STORE);
               } else if (!msg.hasAttachments()) {
                  activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
               } else {
                  ItemContainer attachments = msg.getAttachments();
                  if (attachments != null && attachments.getSize() != 0) {
                     int weight = 0;
                     int slots = 0;

                     for(ItemInstance item : attachments.getItems()) {
                        if (item != null) {
                           if (item.getOwnerId() != activeChar.getObjectId()) {
                              Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to get not own item from cancelled attachment!");
                              return;
                           }

                           if (item.getItemLocation() != ItemInstance.ItemLocation.MAIL) {
                              Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to get items not from mail !");
                              return;
                           }

                           if (item.getLocationSlot() != msg.getId()) {
                              Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to get items from different attachment!");
                              return;
                           }

                           weight = (int)((long)weight + item.getCount() * (long)item.getItem().getWeight());
                           if (!item.isStackable()) {
                              slots = (int)((long)slots + item.getCount());
                           } else if (activeChar.getInventory().getItemByItemId(item.getId()) == null) {
                              ++slots;
                           }
                        }
                     }

                     if (!activeChar.getInventory().validateCapacity((long)slots)) {
                        activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
                     } else if (!activeChar.getInventory().validateWeight((long)weight)) {
                        activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
                     } else {
                        InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                        for(ItemInstance item : attachments.getItems()) {
                           if (item != null) {
                              long count = item.getCount();
                              ItemInstance newItem = attachments.transferItem(
                                 attachments.getName(), item.getObjectId(), count, activeChar.getInventory(), activeChar, null
                              );
                              if (newItem == null) {
                                 return;
                              }

                              if (playerIU != null) {
                                 if (newItem.getCount() > count) {
                                    playerIU.addModifiedItem(newItem);
                                 } else {
                                    playerIU.addNewItem(newItem);
                                 }
                              }

                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1);
                              sm.addItemName(item.getId());
                              sm.addItemNumber(count);
                              activeChar.sendPacket(sm);
                           }
                        }

                        msg.removeAttachments();
                        if (playerIU != null) {
                           activeChar.sendPacket(playerIU);
                        } else {
                           activeChar.sendItemList(false);
                        }

                        StatusUpdate su = new StatusUpdate(activeChar);
                        su.addAttribute(14, activeChar.getCurrentLoad());
                        activeChar.sendPacket(su);
                        Player receiver = World.getInstance().getPlayer(msg.getReceiverId());
                        if (receiver != null) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELLED_MAIL);
                           sm.addCharName(activeChar);
                           receiver.sendPacket(sm);
                           receiver.sendPacket(new ExChangePostState(true, this._msgId, 0));
                        }

                        MailManager.getInstance().deleteMessageInDb(this._msgId);
                        activeChar.sendPacket(new ExChangePostState(false, this._msgId, 0));
                        activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_CANCELLED);
                     }
                  } else {
                     activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
