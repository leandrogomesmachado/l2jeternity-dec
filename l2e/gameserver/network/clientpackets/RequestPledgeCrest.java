package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.model.Crest;
import l2e.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends GameClientPacket {
   private int _crestId;

   @Override
   protected void readImpl() {
      this._crestId = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._crestId != 0) {
         Crest crest = CrestHolder.getInstance().getCrest(this._crestId);
         byte[] data = crest != null ? crest.getData() : null;
         if (data != null) {
            PledgeCrest pc = new PledgeCrest(this._crestId, data);
            this.sendPacket(pc);
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
