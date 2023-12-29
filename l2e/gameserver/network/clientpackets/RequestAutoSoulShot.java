package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAutoSoulShot;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends GameClientPacket {
   private int _itemId;
   private int _type;

   @Override
   protected void readImpl() {
      this._itemId = this.readD();
      this._type = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead()) {
            ItemInstance item = activeChar.getInventory().getItemByItemId(this._itemId);
            if (item == null) {
               return;
            }

            if (this._type == 1) {
               if (!activeChar.getInventory().canManipulateWithItemId(item.getId())) {
                  activeChar.sendMessage("Cannot use this item.");
                  return;
               }

               if (this._itemId >= 6535 && this._itemId <= 6540 && !Config.ALLOW_AUTO_FISH_SHOTS) {
                  return;
               }

               activeChar.addAutoSoulShot(this._itemId);
               activeChar.sendPacket(new ExAutoSoulShot(this._itemId, this._type));
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
               sm.addItemName(item);
               activeChar.sendPacket(sm);
               activeChar.rechargeShots(true, true);
               if ((
                     this._itemId == 6645
                        || this._itemId == 6646
                        || this._itemId == 6647
                        || this._itemId == 20332
                        || this._itemId == 20333
                        || this._itemId == 20334
                  )
                  && activeChar.hasSummon()) {
                  activeChar.getSummon().rechargeShots(true, true);
               }
            } else if (this._type == 0) {
               activeChar.removeAutoSoulShot(this._itemId);
               activeChar.sendPacket(new ExAutoSoulShot(this._itemId, this._type));
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
               sm.addItemName(item);
               activeChar.sendPacket(sm);
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
