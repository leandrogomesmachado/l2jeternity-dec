package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.entity.Message;

public class ExShowReceivedPostList extends GameServerPacket {
   private final List<Message> _inbox;

   public ExShowReceivedPostList(int objectId) {
      this._inbox = MailManager.getInstance().getInbox(objectId);
   }

   @Override
   protected void writeImpl() {
      this.writeD((int)(System.currentTimeMillis() / 1000L));
      if (this._inbox != null && this._inbox.size() > 0) {
         this.writeD(this._inbox.size());

         for(Message msg : this._inbox) {
            this.writeD(msg.getId());
            this.writeS(msg.getSubject());
            this.writeS(msg.getSenderName());
            this.writeD(msg.isLocked() ? 1 : 0);
            this.writeD(msg.getExpirationSeconds());
            this.writeD(msg.isUnread() ? 1 : 0);
            this.writeD(msg.getType() == Message.SenderType.NORMAL ? 0 : 1);
            this.writeD(msg.hasAttachments() ? 1 : 0);
            this.writeD(msg.isReturned() ? 1 : 0);
            this.writeD(msg.getType().ordinal());
            this.writeD(0);
         }
      } else {
         this.writeD(0);
      }
   }
}
