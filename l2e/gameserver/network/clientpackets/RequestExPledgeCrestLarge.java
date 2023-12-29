package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.serverpackets.ExPledgeEmblem;

public final class RequestExPledgeCrestLarge extends GameClientPacket {
   private int _crestId;

   @Override
   protected void readImpl() {
      this._crestId = this.readD();
   }

   @Override
   protected void runImpl() {
      this.sendPacket(new ExPledgeEmblem(this._crestId));
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
