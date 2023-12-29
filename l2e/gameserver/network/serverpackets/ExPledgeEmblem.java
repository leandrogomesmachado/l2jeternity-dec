package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.model.Crest;

public class ExPledgeEmblem extends GameServerPacket {
   private final int _crestId;
   private final byte[] _data;

   public ExPledgeEmblem(int crestId, byte[] data) {
      this._crestId = crestId;
      this._data = data;
   }

   public ExPledgeEmblem(int crestId) {
      this._crestId = crestId;
      Crest crest = CrestHolder.getInstance().getCrest(crestId);
      this._data = crest != null ? crest.getData() : null;
   }

   @Override
   protected void writeImpl() {
      this.writeD(0);
      this.writeD(this._crestId);
      if (this._data != null) {
         this.writeD(this._data.length);
         this.writeB(this._data);
      } else {
         this.writeD(0);
      }
   }
}
