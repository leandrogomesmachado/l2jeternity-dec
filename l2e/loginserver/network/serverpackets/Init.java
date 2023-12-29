package l2e.loginserver.network.serverpackets;

import l2e.loginserver.network.LoginClient;

public final class Init extends LoginServerPacket {
   private final int _sessionId;
   private final byte[] _publicKey;
   private final byte[] _blowfishKey;
   private final int _protocol;

   public Init(LoginClient client) {
      this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId(), client.getProtocol());
   }

   public Init(byte[] publickey, byte[] blowfishkey, int sessionId, int protocol) {
      this._sessionId = sessionId;
      this._publicKey = publickey;
      this._blowfishKey = blowfishkey;
      this._protocol = protocol;
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(this._sessionId);
      this.writeD(this._protocol);
      this.writeB(this._publicKey);
      this.writeB(new byte[16]);
      this.writeB(this._blowfishKey);
      this.writeD(0);
   }
}
