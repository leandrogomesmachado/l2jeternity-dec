package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;

public final class RequestDeleteSentPost extends GameClientPacket {
   private static final int BATCH_LENGTH = 4;
   int[] _msgIds = null;

   @Override
   protected void readImpl() {
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 4 == this._buf.remaining()) {
         this._msgIds = new int[count];

         for(int i = 0; i < count; ++i) {
            this._msgIds[i] = this.readD();
         }
      }
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && this._msgIds != null && Config.ALLOW_MAIL) {
         if (!activeChar.isInZonePeace()) {
            activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
         } else {
            for(int msgId : this._msgIds) {
               Message msg = MailManager.getInstance().getMessage(msgId);
               if (msg != null) {
                  if (msg.getSenderId() != activeChar.getObjectId()) {
                     Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to delete not own post!");
                     return;
                  }

                  if (msg.hasAttachments() || msg.isDeletedBySender()) {
                     return;
                  }

                  msg.setDeletedBySender();
               }
            }

            activeChar.sendPacket(new ExChangePostState(false, this._msgIds, 0));
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
