package l2e.gameserver.network.communication.gameserverpackets;

import java.util.List;
import l2e.gameserver.network.communication.SendablePacket;

public class SetAccountInfo extends SendablePacket {
   private final String _account;
   private final int _size;
   private final List<Long> _deleteChars;

   public SetAccountInfo(String account, int size, List<Long> deleteChars) {
      this._account = account;
      this._size = size;
      this._deleteChars = deleteChars;
   }

   @Override
   protected void writeImpl() {
      this.writeC(5);
      this.writeS(this._account);
      this.writeC(this._size);
      this.writeD(this._deleteChars.size());

      for(long time : this._deleteChars) {
         this.writeQ(time);
      }
   }
}
