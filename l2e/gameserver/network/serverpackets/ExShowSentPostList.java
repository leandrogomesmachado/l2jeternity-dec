package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.entity.Message;

public class ExShowSentPostList extends GameServerPacket {
   private final List<Message> _outbox;

   public ExShowSentPostList(int objectId) {
      this._outbox = MailManager.getInstance().getOutbox(objectId);
   }

   @Override
   protected void writeImpl() {
      this.writeD((int)(System.currentTimeMillis() / 1000L));
      if (this._outbox != null && this._outbox.size() > 0) {
         this.writeD(this._outbox.size());

         for(Message msg : this._outbox) {
            this.writeD(msg.getId());
            this.writeS(msg.getSubject());
            this.writeS(msg.getReceiverName());
            this.writeD(msg.isLocked() ? 1 : 0);
            this.writeD(msg.getExpirationSeconds());
            this.writeD(msg.isUnread() ? 1 : 0);
            this.writeD(1);
            this.writeD(msg.hasAttachments() ? 1 : 0);
         }
      } else {
         this.writeD(0);
      }
   }
}
