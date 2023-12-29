package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.network.serverpackets.VersionCheck;
import org.strixplatform.StrixPlatform;
import org.strixplatform.managers.ClientGameSessionManager;
import org.strixplatform.managers.ClientProtocolDataManager;
import org.strixplatform.utils.StrixClientData;

public final class SendProtocolVersion extends GameClientPacket {
   protected static final Logger _log = Logger.getLogger(SendProtocolVersion.class.getName());
   private static final Logger _logAccounting = Logger.getLogger("accounting");
   private int _version;
   private byte[] _data;
   private int _dataChecksum;

   @Override
   protected void readImpl() {
      this._version = this.readD();
      if (StrixPlatform.getInstance().isPlatformEnabled()) {
         try {
            if (this._buf.remaining() >= StrixPlatform.getInstance().getProtocolVersionDataSize()) {
               this._data = new byte[StrixPlatform.getInstance().getClientDataSize()];
               this.readB(this._data);
               this._dataChecksum = this.readD();
            }
         } catch (Exception var2) {
            this.getClient().close(new VersionCheck(null));
            _log.warning("Client [IP=" + this.toString() + "] used unprotected client. Disconnect...");
            return;
         }
      }
   }

   @Override
   protected void runImpl() {
      if (this._version == -2) {
         this.getClient().closeNow(false);
      } else if (!Config.PROTOCOL_LIST.contains(this._version)) {
         LogRecord record = new LogRecord(Level.WARNING, "Wrong protocol");
         record.setParameters(new Object[]{this._version, this.getClient()});
         _logAccounting.log(record);
         this.getClient().close(new VersionCheck(null));
      }

      if (!StrixPlatform.getInstance().isPlatformEnabled()) {
         this.getClient().setRevision(this._version);
         this.getClient().sendPacket(new VersionCheck(this._client.enableCrypt()));
      } else if (this._data == null) {
         _log.warning(
            "SendProtocolVersion: Client [IP=" + this.getClient().getConnectionAddress().getHostAddress() + "] used unprotected client. Disconnect..."
         );
         this.getClient().close(new VersionCheck(null));
      } else {
         StrixClientData clientData = ClientProtocolDataManager.getInstance().getDecodedData(this._data, this._dataChecksum);
         if (clientData != null) {
            if (!ClientGameSessionManager.getInstance().checkServerResponse(clientData)) {
               this.getClient().close(new VersionCheck(null, clientData));
            } else {
               this.getClient().setStrixClientData(clientData);
               this.getClient().setRevision(this._version);
               this.sendPacket(new VersionCheck(this._client.enableCrypt()));
            }
         } else {
            _log.warning(
               "SendProtocolVersion: Decode client data failed. See Strix-Platform log file. Disconected client "
                  + this.getClient().getConnectionAddress().getHostAddress()
            );
            this.getClient().close(new VersionCheck(null));
         }
      }
   }
}
