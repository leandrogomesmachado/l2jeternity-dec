package l2e.loginserver.network.serverpackets;

import l2e.loginserver.network.SessionKey;

public final class PlayOk extends LoginServerPacket {
   private final int _playOk1;
   private final int _playOk2;
   private final int _serverId;

   public PlayOk(SessionKey sessionKey, int serverId) {
      this._playOk1 = sessionKey._playOkID1;
      this._playOk2 = sessionKey._playOkID2;
      this._serverId = serverId;
   }

   @Override
   protected void writeImpl() {
      this.writeC(7);
      this.writeD(this._playOk1);
      this.writeD(this._playOk2);
      this.writeC(this._serverId);
   }
}
