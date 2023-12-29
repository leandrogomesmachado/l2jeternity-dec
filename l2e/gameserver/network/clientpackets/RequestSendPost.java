package l2e.gameserver.network.clientpackets;

import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.AccessLevel;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Mail;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExNoticePostSent;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.apache.commons.lang3.ArrayUtils;

public final class RequestSendPost extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private static final int MAX_RECV_LENGTH = 16;
   private static final int MAX_SUBJ_LENGTH = 128;
   private static final int MAX_TEXT_LENGTH = 512;
   private static final int MAX_ATTACHMENTS = 8;
   private static final int INBOX_SIZE = 240;
   private static final int OUTBOX_SIZE = 240;
   private String _receiver;
   private boolean _isCod;
   private String _subject;
   private String _text;
   private int _count;
   private int[] _items;
   private long[] _itemQ;
   private long _reqAdena;

   @Override
   protected void readImpl() {
      this._receiver = this.readS();
      this._isCod = this.readD() != 0;
      this._subject = this.readS();
      this._text = this.readS();
      this._count = this.readD();
      if (this._count >= 1 && this._count <= Config.MAX_ITEM_IN_PACKET && this._count * 12 + 8 == this._buf.remaining()) {
         this._items = new int[this._count];
         this._itemQ = new long[this._count];

         for(int i = 0; i < this._count; ++i) {
            this._items[i] = this.readD();
            this._itemQ[i] = this.readQ();
            if (this._itemQ[i] < 1L || ArrayUtils.indexOf(this._items, this._items[i]) < i) {
               this._count = 0;
               return;
            }
         }

         this._reqAdena = this.readQ();
         if (this._reqAdena < 0L) {
            this._count = 0;
            this._reqAdena = 0L;
         }
      } else {
         this._count = 0;
      }
   }

   @Override
   public void runImpl() {
      if (Config.ALLOW_MAIL) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            if (!Config.ALLOW_ATTACHMENTS) {
               this._items = null;
               this._isCod = false;
               this._reqAdena = 0L;
            }

            if (!activeChar.getAccessLevel().allowTransaction()) {
               activeChar.sendMessage("Transactions are disabled for your Access Level.");
            } else if (activeChar.isActionsDisabled()) {
               activeChar.sendActionFailed();
            } else if (!activeChar.isInZonePeace() && this._items != null) {
               activeChar.sendPacket(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE);
            } else if (activeChar.getActiveTradeList() != null) {
               activeChar.sendPacket(SystemMessageId.CANT_FORWARD_DURING_EXCHANGE);
            } else if (activeChar.isEnchanting()) {
               activeChar.sendPacket(SystemMessageId.CANT_FORWARD_DURING_ENCHANT);
            } else if (activeChar.getPrivateStoreType() > 0) {
               activeChar.sendPacket(SystemMessageId.CANT_FORWARD_PRIVATE_STORE);
            } else if (this._receiver.length() > 16) {
               activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED);
            } else if (this._subject.length() > 128) {
               activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED);
            } else if (this._text.length() > 512) {
               activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED);
            } else if (this._items != null && this._items.length > 8) {
               activeChar.sendPacket(SystemMessageId.ITEM_SELECTION_POSSIBLE_UP_TO_8);
            } else if (this._reqAdena >= 0L && this._reqAdena <= PcInventory.MAX_ADENA) {
               if (this._isCod) {
                  if (this._reqAdena == 0L) {
                     activeChar.sendPacket(SystemMessageId.PAYMENT_AMOUNT_NOT_ENTERED);
                     return;
                  }

                  if (this._items == null || this._items.length == 0) {
                     activeChar.sendPacket(SystemMessageId.PAYMENT_REQUEST_NO_ITEM);
                     return;
                  }
               }

               int receiverId = CharNameHolder.getInstance().getIdByName(this._receiver);
               if (receiverId <= 0) {
                  activeChar.sendPacket(SystemMessageId.RECIPIENT_NOT_EXIST);
               } else if (receiverId == activeChar.getObjectId()) {
                  activeChar.sendPacket(SystemMessageId.YOU_CANT_SEND_MAIL_TO_YOURSELF);
               } else {
                  int level = CharNameHolder.getInstance().getAccessLevelById(receiverId);
                  AccessLevel accessLevel = AdminParser.getInstance().getAccessLevel(level);
                  if (accessLevel.isGm() && !activeChar.getAccessLevel().isGm()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_C1);
                     sm.addString(this._receiver);
                     activeChar.sendPacket(sm);
                  } else if (activeChar.isJailed() && (this._items != null || Config.JAIL_DISABLE_CHAT)) {
                     activeChar.sendPacket(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE);
                  } else if (BlockedList.isInBlockList(receiverId, activeChar.getObjectId())) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_BLOCKED_YOU_CANNOT_MAIL);
                     sm.addString(this._receiver);
                     activeChar.sendPacket(sm);
                  } else if (MailManager.getInstance().getOutboxSize(activeChar.getObjectId()) >= 240) {
                     activeChar.sendPacket(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED);
                  } else if (MailManager.getInstance().getInboxSize(receiverId) >= 240) {
                     activeChar.sendPacket(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED);
                  } else {
                     Message msg = new Message(activeChar.getObjectId(), receiverId, this._isCod, this._subject, this._text, this._reqAdena);
                     if (this.removeItems(activeChar, msg)) {
                        MailManager.getInstance().sendMessage(msg);
                        activeChar.sendPacket(ExNoticePostSent.valueOf(true));
                        activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_SENT);
                     }
                  }
               }
            }
         }
      }
   }

   private final boolean removeItems(Player player, Message msg) {
      long serviceCost = (long)(100 + this._count * 1000);
      if (this._count > 0) {
         for(int i = 0; i < this._count; ++i) {
            ItemInstance item = player.getInventory().getItemByObjectId(this._items[i]);
            if (item == null
               || item.getCount() < this._itemQ[i]
               || item.getId() == 57 && item.getCount() < this._itemQ[i] + serviceCost
               || !item.isTradeable()
               || item.isEquipped()) {
               player.sendPacket(SystemMessageId.CANT_FORWARD_BAD_ITEM);
               return false;
            }
         }
      }

      if (!player.reduceAdena("MailFee", serviceCost, null, false)) {
         player.sendPacket(SystemMessageId.CANT_FORWARD_NO_ADENA);
         return false;
      } else if (this._count == 0) {
         return true;
      } else {
         Mail attachments = msg.createAttachments();
         if (attachments == null) {
            return false;
         } else {
            StringBuilder recv = new StringBuilder(32);
            StringUtil.append(recv, msg.getReceiverName(), "[", String.valueOf(msg.getReceiverId()), "]");
            String receiver = recv.toString();
            InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

            for(int i = 0; i < this._count; ++i) {
               ItemInstance oldItem = player.checkItemManipulation(this._items[i], this._itemQ[i], "attach");
               if (oldItem == null || !oldItem.isTradeable() || oldItem.isEquipped()) {
                  _log.warning("Error adding attachment for char " + player.getName() + " (olditem == null)");
                  return false;
               }

               ItemInstance newItem = player.getInventory().transferItem("SendMail", this._items[i], this._itemQ[i], attachments, player, receiver);
               if (newItem == null) {
                  _log.warning("Error adding attachment for char " + player.getName() + " (newitem == null)");
               } else {
                  newItem.setItemLocation(newItem.getItemLocation(), msg.getId());
                  if (playerIU != null) {
                     if (oldItem.getCount() > 0L && oldItem != newItem) {
                        playerIU.addModifiedItem(oldItem);
                     } else {
                        playerIU.addRemovedItem(oldItem);
                     }
                  }
               }
            }

            if (playerIU != null) {
               player.sendPacket(playerIU);
            } else {
               player.sendItemList(false);
            }

            StatusUpdate su = new StatusUpdate(player);
            su.addAttribute(14, player.getCurrentLoad());
            player.sendPacket(su);
            return true;
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
