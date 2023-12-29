package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestRejectPost extends GameClientPacket {
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
            if (activeChar.isActionsDisabled()) {
               activeChar.sendActionFailed();
            } else if (activeChar.getActiveTradeList() != null) {
               activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE);
            } else if (!activeChar.isInZonePeace()) {
               activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
            } else {
               Message msg = MailManager.getInstance().getMessage(this._msgId);
               if (msg != null) {
                  if (msg.getReceiverId() != activeChar.getObjectId()) {
                     Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " tried to reject not own attachment!");
                  } else if (msg.hasAttachments() && msg.getType().ordinal() == 0) {
                     MailManager.getInstance().sendMessage(new Message(msg));
                     activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RETURNED);
                     activeChar.sendPacket(new ExChangePostState(true, this._msgId, 2));
                     Player sender = World.getInstance().getPlayer(msg.getSenderId());
                     if (sender != null) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RETURNED_MAIL);
                        sm.addCharName(activeChar);
                        sender.sendPacket(sm);
                     }
                  }
               }
            }
         }
      }
   }
}
