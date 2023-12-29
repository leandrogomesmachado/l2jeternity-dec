package l2e.loginserver.network;

import java.nio.ByteBuffer;
import l2e.loginserver.network.clientpackets.AuthGameGuard;
import l2e.loginserver.network.clientpackets.RequestAuthLogin;
import l2e.loginserver.network.clientpackets.RequestServerList;
import l2e.loginserver.network.clientpackets.RequestServerLogin;
import org.nio.impl.IPacketHandler;
import org.nio.impl.ReceivablePacket;

public final class LoginPacketHandler implements IPacketHandler<LoginClient> {
   public ReceivablePacket<LoginClient> handlePacket(ByteBuffer buf, LoginClient client) {
      int opcode = buf.get() & 255;
      ReceivablePacket<LoginClient> packet = null;
      LoginClient.LoginClientState state = client.getState();
      switch(state) {
         case CONNECTED:
            if (opcode == 7) {
               packet = new AuthGameGuard();
            }
            break;
         case AUTHED_GG:
            if (opcode == 0) {
               packet = new RequestAuthLogin();
            }
            break;
         case AUTHED:
            if (opcode == 5) {
               packet = new RequestServerList();
            } else if (opcode == 2) {
               packet = new RequestServerLogin();
            }
      }

      return packet;
   }
}
