package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;
import l2e.gameserver.network.serverpackets.ExReplyReceivedPost;

public final class RequestReceivedPost extends GameClientPacket {
   private int _msgId;

   @Override
   protected void readImpl() {
      this._msgId = this.readD();
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && Config.ALLOW_MAIL) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            Message msg = MailManager.getInstance().getMessage(this._msgId);
            if (msg != null) {
               if (!activeChar.isInZonePeace() && msg.hasAttachments()) {
                  activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
               } else if (msg.getReceiverId() != activeChar.getObjectId()) {
                  Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to receive not own post!");
               } else if (!msg.isDeletedByReceiver()) {
                  activeChar.sendPacket(new ExReplyReceivedPost(msg));
                  activeChar.sendPacket(new ExChangePostState(true, this._msgId, 1));
                  msg.markAsRead();
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
