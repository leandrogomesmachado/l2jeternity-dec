package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExReplyPostItemList;

public final class RequestPostItemList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   public void runImpl() {
      if (Config.ALLOW_MAIL && Config.ALLOW_ATTACHMENTS) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            if (activeChar.isActionsDisabled()) {
               activeChar.sendActionFailed();
            } else if (!activeChar.isInZonePeace()) {
               activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
            } else {
               activeChar.sendPacket(new ExReplyPostItemList(activeChar));
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
