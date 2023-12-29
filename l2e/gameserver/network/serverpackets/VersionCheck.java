package l2e.gameserver.network.serverpackets;

import org.strixplatform.StrixPlatform;
import org.strixplatform.utils.StrixClientData;

public final class VersionCheck extends GameServerPacket {
   private final byte[] _key;
   private final StrixClientData _clientData;

   public VersionCheck(byte[] key) {
      this._key = key;
      this._clientData = null;
   }

   public VersionCheck(byte[] key, StrixClientData clientData) {
      this._key = key;
      this._clientData = clientData;
   }

   @Override
   public void writeImpl() {
      if (this._key != null && this._key.length != 0) {
         this.writeC(1);
         this.writeB(this._key);
         this.writeD(1);
         this.writeD(0);
         this.writeC(0);
         this.writeD(0);
      } else {
         if (StrixPlatform.getInstance().isBackNotificationEnabled() && this._clientData != null) {
            this.writeC(this._clientData.getServerResponse().ordinal() + 1);
         } else {
            this.writeC(0);
         }
      }
   }
}
