package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;

public class ExMultiPartyCommandChannelInfo extends GameServerPacket {
   private final CommandChannel _channel;

   public ExMultiPartyCommandChannelInfo(CommandChannel channel) {
      this._channel = channel;
   }

   @Override
   protected void writeImpl() {
      if (this._channel != null) {
         this.writeS(this._channel.getLeader().getName());
         this.writeD(0);
         this.writeD(this._channel.getMemberCount());
         this.writeD(this._channel.getPartys().size());

         for(Party p : this._channel.getPartys()) {
            this.writeS(p.getLeader().getName());
            this.writeD(p.getLeaderObjectId());
            this.writeD(p.getMemberCount());
         }
      }
   }
}
