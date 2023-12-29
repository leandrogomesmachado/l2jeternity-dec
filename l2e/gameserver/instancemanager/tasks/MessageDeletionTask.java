package l2e.gameserver.instancemanager.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class MessageDeletionTask implements Runnable {
   private static final Logger _log = Logger.getLogger(MessageDeletionTask.class.getName());
   final int _msgId;

   public MessageDeletionTask(int msgId) {
      this._msgId = msgId;
   }

   @Override
   public void run() {
      Message msg = MailManager.getInstance().getMessage(this._msgId);
      if (msg != null) {
         if (msg.hasAttachments()) {
            try {
               Player sender = World.getInstance().getPlayer(msg.getSenderId());
               if (sender != null) {
                  sender.sendPacket(SystemMessageId.MAIL_RETURNED);
                  InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                  for(ItemInstance item : msg.getAttachments().getItems()) {
                     if (item != null) {
                        long count = item.getCount();
                        ItemInstance newItem = msg.getAttachments()
                           .transferItem(msg.getAttachments().getName(), item.getObjectId(), item.getCount(), sender.getInventory(), sender, null);
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
                        sender.sendPacket(sm);
                     }
                  }

                  if (playerIU != null) {
                     sender.sendPacket(playerIU);
                  } else {
                     sender.sendItemList(false);
                  }
               } else {
                  for(ItemInstance item : msg.getAttachments().getItems()) {
                     if (item != null) {
                        ItemInstance returnItem = ItemsParser.getInstance().createItem("PayMail", item.getId(), item.getCount(), null, null);
                        if (returnItem.isWeapon() || returnItem.isArmor()) {
                           returnItem.setEnchantLevel(item.getEnchantLevel());
                           returnItem.setAugmentation(item.getAugmentation());
                           if (item.getElementals() != null) {
                              for(Elementals elm : item.getElementals()) {
                                 if (elm.getElement() != -1 && elm.getValue() != -1) {
                                    returnItem.setElementAttr(elm.getElement(), elm.getValue());
                                 }
                              }
                           }
                        }

                        returnItem.setOwnerId(msg.getSenderId());
                        returnItem.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
                        returnItem.updateDatabase(true);
                        World.getInstance().removeObject(returnItem);
                     }
                  }
               }

               msg.getAttachments().deleteMe();
               msg.removeAttachments();
               Player receiver = World.getInstance().getPlayer(msg.getReceiverId());
               if (receiver != null) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.MAIL_RETURNED);
                  receiver.sendPacket(sm);
               }
            } catch (Exception var12) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error returning items:" + var12.getMessage(), (Throwable)var12);
            }
         }

         MailManager.getInstance().deleteMessageInDb(msg.getId());
      }
   }
}
