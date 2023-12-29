package l2e.gameserver.network.clientpackets;

public final class RequestPCCafeCouponUse extends GameClientPacket {
   private String _str;

   @Override
   protected void readImpl() {
      this._str = this.readS();
   }

   @Override
   protected void runImpl() {
      _log.info("C5: RequestPCCafeCouponUse: S: " + this._str);
   }
}
