package l2e.loginserver.network.communication;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import l2e.loginserver.network.communication.gameserverpackets.AuthRequest;
import l2e.loginserver.network.communication.gameserverpackets.ChangeAccessLevel;
import l2e.loginserver.network.communication.gameserverpackets.ChangeAllowedHwid;
import l2e.loginserver.network.communication.gameserverpackets.ChangeAllowedIp;
import l2e.loginserver.network.communication.gameserverpackets.ChangePassword;
import l2e.loginserver.network.communication.gameserverpackets.OnlineStatus;
import l2e.loginserver.network.communication.gameserverpackets.PingResponse;
import l2e.loginserver.network.communication.gameserverpackets.PlayerAuthRequest;
import l2e.loginserver.network.communication.gameserverpackets.PlayerInGame;
import l2e.loginserver.network.communication.gameserverpackets.PlayerLogout;
import l2e.loginserver.network.communication.gameserverpackets.SetAccountInfo;

public class PacketHandler {
   private static Logger _log = Logger.getLogger(PacketHandler.class.getName());

   public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf) {
      ReceivablePacket packet = null;
      int id = buf.get() & 255;
      if (!gs.isAuthed()) {
         switch(id) {
            case 0:
               packet = new AuthRequest();
               break;
            default:
               _log.warning("Received unknown packet: " + Integer.toHexString(id));
         }
      } else {
         switch(id) {
            case 1:
               packet = new OnlineStatus();
               break;
            case 2:
               packet = new PlayerAuthRequest();
               break;
            case 3:
               packet = new PlayerInGame();
               break;
            case 4:
               packet = new PlayerLogout();
               break;
            case 5:
               packet = new SetAccountInfo();
               break;
            case 7:
               packet = new ChangeAllowedIp();
               break;
            case 8:
               packet = new ChangePassword();
               break;
            case 9:
               packet = new ChangeAllowedHwid();
               break;
            case 17:
               packet = new ChangeAccessLevel();
               break;
            case 255:
               packet = new PingResponse();
               break;
            default:
               _log.warning("Received unknown packet: " + Integer.toHexString(id));
         }
      }

      return packet;
   }
}
