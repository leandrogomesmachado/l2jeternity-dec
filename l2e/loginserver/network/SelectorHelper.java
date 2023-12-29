package l2e.loginserver.network;

import java.nio.channels.SocketChannel;
import l2e.loginserver.IpBanManager;
import l2e.loginserver.ThreadPoolManager;
import l2e.loginserver.network.serverpackets.Init;
import org.nio.impl.IAcceptFilter;
import org.nio.impl.IClientFactory;
import org.nio.impl.IMMOExecutor;
import org.nio.impl.MMOConnection;

public class SelectorHelper implements IMMOExecutor<LoginClient>, IClientFactory<LoginClient>, IAcceptFilter {
   @Override
   public void execute(Runnable r) {
      ThreadPoolManager.getInstance().execute(r);
   }

   public LoginClient create(MMOConnection<LoginClient> con) {
      LoginClient client = new LoginClient(con);
      client.sendPacket(new Init(client));
      ThreadPoolManager.getInstance().schedule(() -> client.closeNow(false), 60000L);
      return client;
   }

   @Override
   public boolean accept(SocketChannel sc) {
      return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
   }
}
