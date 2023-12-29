package l2e.gameserver.network.serverpackets;

import java.util.List;

public class ExSendManorList extends GameServerPacket {
   private final List<String> _manors;

   public ExSendManorList(List<String> manors) {
      this._manors = manors;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._manors.size());
      int i = 1;

      for(String manor : this._manors) {
         this.writeD(i++);
         this.writeS(manor);
      }
   }
}
