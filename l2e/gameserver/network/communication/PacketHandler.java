package l2e.gameserver.network.communication;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import l2e.gameserver.network.communication.loginserverpackets.AuthResponse;
import l2e.gameserver.network.communication.loginserverpackets.ChangePasswordResponse;
import l2e.gameserver.network.communication.loginserverpackets.GetAccountInfo;
import l2e.gameserver.network.communication.loginserverpackets.KickPlayer;
import l2e.gameserver.network.communication.loginserverpackets.LoginServerFail;
import l2e.gameserver.network.communication.loginserverpackets.PingRequest;
import l2e.gameserver.network.communication.loginserverpackets.PlayerAuthResponse;

public class PacketHandler {
   private static final Logger _log = Logger.getLogger(PacketHandler.class.getName());

   public static ReceivablePacket handlePacket(ByteBuffer buf) {
      ReceivablePacket packet = null;
      int id = buf.get() & 255;
      switch(id) {
         case 0:
            packet = new AuthResponse();
            break;
         case 1:
            packet = new LoginServerFail();
            break;
         case 2:
            packet = new PlayerAuthResponse();
            break;
         case 3:
            packet = new KickPlayer();
            break;
         case 4:
            packet = new GetAccountInfo();
            break;
         case 6:
            packet = new ChangePasswordResponse();
            break;
         case 255:
            packet = new PingRequest();
            break;
         default:
            _log.warning("Received unknown packet: " + Integer.toHexString(id));
      }

      return packet;
   }
}
