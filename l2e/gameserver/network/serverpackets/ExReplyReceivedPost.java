package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;

public class ExReplyReceivedPost extends GameServerPacket {
   private final Message _msg;
   private ItemInstance[] _items = null;

   public ExReplyReceivedPost(Message msg) {
      this._msg = msg;
      if (msg.hasAttachments()) {
         ItemContainer attachments = msg.getAttachments();
         if (attachments != null && attachments.getSize() > 0) {
            this._items = attachments.getItems();
         } else {
            _log.warning("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._msg.getId());
      this.writeD(this._msg.isLocked() ? 1 : 0);
      this.writeD(0);
      this.writeS(this._msg.getSenderName());
      this.writeS(this._msg.getSubject());
      this.writeS(this._msg.getContent());
      if (this._items != null && this._items.length > 0) {
         this.writeD(this._items.length);

         for(ItemInstance item : this._items) {
            this.writeD(0);
            this.writeD(item.getDisplayId());
            this.writeD(item.getLocationSlot());
            this.writeQ(item.getCount());
            this.writeH(item.getItem().getType2());
            this.writeH(item.getCustomType1());
            this.writeH(item.isEquipped() ? 1 : 0);
            this.writeD(item.getItem().getBodyPart());
            this.writeH(item.getEnchantLevel());
            this.writeH(item.getCustomType2());
            if (item.isAugmented()) {
               this.writeD(item.getAugmentation().getAugmentationId());
            } else {
               this.writeD(0);
            }

            this.writeD(item.getMana());
            this.writeD(item.isTimeLimitedItem() ? (int)(item.getRemainingTime() / 1000L) : -9999);
            this.writeH(item.getAttackElementType());
            this.writeH(item.getAttackElementPower());

            for(byte i = 0; i < 6; ++i) {
               this.writeH(item.getElementDefAttr(i));
            }

            for(int op : item.getEnchantOptions()) {
               this.writeH(op);
            }

            this.writeD(item.getObjectId());
         }
      } else {
         this.writeD(0);
      }

      this.writeQ(this._msg.getReqAdena());
      this.writeD(this._msg.hasAttachments() ? 1 : 0);
      this.writeD(this._msg.getType().ordinal());
   }
}
