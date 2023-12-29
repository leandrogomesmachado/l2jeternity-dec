package l2e.gameserver.network.serverpackets;

import java.util.List;

public class BlockList extends GameServerPacket {
   private final List<String> _blockedCharNames;

   public BlockList(List<String> blockedCharNames) {
      this._blockedCharNames = blockedCharNames;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._blockedCharNames.size());
      int i = 0;

      for(int j = this._blockedCharNames.size(); i < j; ++i) {
         this.writeS(this._blockedCharNames.get(i));
      }
   }
}
