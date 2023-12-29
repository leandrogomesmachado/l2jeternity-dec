package l2e.loginserver.network.serverpackets;

public final class GGAuth extends LoginServerPacket {
   private final int _response;

   public GGAuth(int response) {
      this._response = response;
   }

   @Override
   protected void writeImpl() {
      this.writeC(11);
      this.writeD(this._response);
      this.writeB(new byte[16]);
   }
}
