package l2e.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.loginserver.GameServerManager;
import l2e.loginserver.accounts.Account;
import l2e.loginserver.network.ProxyServer;
import l2e.loginserver.network.communication.GameServer;
import org.HostInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class ServerList extends LoginServerPacket {
   private static final Logger _log = Logger.getLogger(ServerList.class.getName());
   private final List<ServerList.ServerData> _servers = new ArrayList<>();
   private final int _lastServer;
   private int _paddedBytes;

   public ServerList(Account account) {
      this._lastServer = account.getLastServer();
      this._paddedBytes = 1;

      for(GameServer gs : GameServerManager.getInstance().getGameServers()) {
         for(HostInfo host : gs.getHosts()) {
            InetAddress adress;
            try {
               String adrStr = host.checkAddress(account.getLastIP());
               if (adrStr == null) {
                  continue;
               }

               if (adrStr.equals("*")) {
                  adrStr = gs.getConnection().getIpAddress();
               }

               adress = InetAddress.getByName(adrStr);
            } catch (UnknownHostException var17) {
               _log.log(Level.WARNING, "Error with gameserver host adress: " + var17, (Throwable)var17);
               continue;
            }

            Pair<Integer, int[]> entry = account.getAccountInfo(host.getId());
            int[] deleteChars;
            int playerSize;
            if (entry != null) {
               playerSize = entry.getKey();
               deleteChars = (int[])entry.getValue();
            } else {
               playerSize = 0;
               deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
            }

            this._paddedBytes += 3 + 4 * deleteChars.length;
            this._servers
               .add(
                  new ServerList.ServerData(
                     host.getId(),
                     adress,
                     host.getPort(),
                     gs.isPvp(),
                     gs.isShowingBrackets(),
                     gs.getServerType(),
                     gs.getOnline(),
                     gs.getMaxPlayers(),
                     gs.isOnline(),
                     playerSize,
                     gs.getAgeLimit(),
                     deleteChars
                  )
               );

            for(ProxyServer ps : GameServerManager.getInstance().getProxyServersList(host.getId())) {
               this._servers
                  .add(
                     new ServerList.ServerData(
                        ps.getProxyServerId(),
                        ps.getProxyAddr(),
                        ps.getProxyPort(),
                        gs.isPvp(),
                        gs.isShowingBrackets(),
                        gs.getServerType(),
                        gs.getOnline(),
                        gs.getMaxPlayers(),
                        gs.isOnline(),
                        playerSize,
                        gs.getAgeLimit(),
                        deleteChars
                     )
                  );
            }
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeC(4);
      this.writeC(this._servers.size());
      this.writeC(this._lastServer);

      for(ServerList.ServerData server : this._servers) {
         this.writeC(server._serverId);
         byte[] raw = server._adress.getAddress();
         this.writeC(raw[0] & 255);
         this.writeC(raw[1] & 255);
         this.writeC(raw[2] & 255);
         this.writeC(raw[3] & 255);
         this.writeD(server._port);
         this.writeC(server._ageLimit);
         this.writeC(server._pvp ? 1 : 0);
         this.writeH(server._online);
         this.writeH(server._maxPlayers);
         this.writeC(server._status ? 1 : 0);
         this.writeD(server._type);
         this.writeC(server._brackets ? 1 : 0);
      }

      this.writeH(this._paddedBytes);
      this.writeC(this._servers.size());

      for(ServerList.ServerData server : this._servers) {
         this.writeC(server._serverId);
         this.writeC(server._playerSize);
         this.writeC(server._deleteChars.length);

         for(int t : server._deleteChars) {
            this.writeD((int)((long)t - System.currentTimeMillis() / 1000L));
         }
      }
   }

   private static class ServerData {
      int _serverId;
      InetAddress _adress;
      int _port;
      int _online;
      int _maxPlayers;
      boolean _status;
      boolean _pvp;
      boolean _brackets;
      int _type;
      int _ageLimit;
      int _playerSize;
      int[] _deleteChars;

      ServerData(
         int serverId,
         InetAddress adress,
         int port,
         boolean pvp,
         boolean brackets,
         int type,
         int online,
         int maxPlayers,
         boolean status,
         int size,
         int ageLimit,
         int[] d
      ) {
         this._serverId = serverId;
         this._adress = adress;
         this._port = port;
         this._pvp = pvp;
         this._brackets = brackets;
         this._type = type;
         this._online = online;
         this._maxPlayers = maxPlayers;
         this._status = status;
         this._playerSize = size;
         this._ageLimit = ageLimit;
         this._deleteChars = d;
      }
   }
}
