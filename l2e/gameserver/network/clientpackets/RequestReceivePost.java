package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
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

public final class RequestReceivePost extends GameClientPacket {
   private int _msgId;

   @Override
   protected void readImpl() {
      this._msgId = this.readD();
   }

   @Override
   public void runImpl() {
      if (Config.ALLOW_MAIL && Config.ALLOW_ATTACHMENTS) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            if (!activeChar.getAccessLevel().allowTransaction()) {
               activeChar.sendMessage("Transactions are disabled for your Access Level");
            } else if (!activeChar.isInZonePeace()) {
               activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NOT_IN_PEACE_ZONE);
            } else if (activeChar.getActiveTradeList() != null) {
               activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE);
            } else if (activeChar.isEnchanting()) {
               activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_ENCHANT);
            } else if (activeChar.getPrivateStoreType() > 0) {
               activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_PRIVATE_STORE);
            } else {
               Message msg = MailManager.getInstance().getMessage(this._msgId);
               if (msg != null) {
                  if (msg.getReceiverId() != activeChar.getObjectId()) {
                     Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to get not own attachment!");
                  } else if (msg.hasAttachments()) {
                     ItemContainer attachments = msg.getAttachments();
                     if (attachments != null) {
                        int weight = 0;
                        int slots = 0;

                        for(ItemInstance item : attachments.getItems()) {
                           if (item != null) {
                              if (item.getOwnerId() != msg.getSenderId()) {
                                 Util.handleIllegalPlayerAction(
                                    activeChar, "" + activeChar.getName() + " tried to get wrong item (ownerId != senderId) from attachment!"
                                 );
                                 return;
                              }

                              if (item.getItemLocation() != ItemInstance.ItemLocation.MAIL) {
                                 Util.handleIllegalPlayerAction(
                                    activeChar, "" + activeChar.getName() + " tried to get wrong item (Location != MAIL) from attachment!"
                                 );
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
                           activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
                        } else if (!activeChar.getInventory().validateWeight((long)weight)) {
                           activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
                        } else {
                           long adena = msg.getReqAdena();
                           if (adena > 0L && !activeChar.reduceAdena("PayMail", adena, null, true)) {
                              activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NO_ADENA);
                           } else {
                              InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

                              for(ItemInstance item : attachments.getItems()) {
                                 if (item != null) {
                                    if (item.getOwnerId() != msg.getSenderId()) {
                                       Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to get items with owner != sender !");
                                       return;
                                    }

                                    long count = item.getCount();
                                    ItemInstance newItem = attachments.transferItem(
                                       attachments.getName(), item.getObjectId(), item.getCount(), activeChar.getInventory(), activeChar, null
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

                              if (playerIU != null) {
                                 activeChar.sendPacket(playerIU);
                              } else {
                                 activeChar.sendItemList(false);
                              }

                              msg.removeAttachments();
                              StatusUpdate su = new StatusUpdate(activeChar);
                              su.addAttribute(14, activeChar.getCurrentLoad());
                              activeChar.sendPacket(su);
                              Player sender = World.getInstance().getPlayer(msg.getSenderId());
                              if (adena > 0L) {
                                 if (sender != null) {
                                    sender.addAdena("PayMail", adena, activeChar, false);
                                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2);
                                    sm.addItemNumber(adena);
                                    sm.addCharName(activeChar);
                                    sender.sendPacket(sm);
                                 } else {
                                    ItemInstance paidAdena = ItemsParser.getInstance().createItem("PayMail", 57, adena, activeChar, null);
                                    paidAdena.setOwnerId(msg.getSenderId());
                                    paidAdena.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
                                    paidAdena.updateDatabase(true);
                                    World.getInstance().removeObject(paidAdena);
                                 }
                              } else if (sender != null) {
                                 SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ACQUIRED_ATTACHED_ITEM);
                                 sm.addCharName(activeChar);
                                 sender.sendPacket(sm);
                              }

                              activeChar.sendPacket(new ExChangePostState(true, this._msgId, 1));
                              activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RECEIVED);
                           }
                        }
                     }
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
