package l2e.loginserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.loginserver.database.DatabaseFactory;
import l2e.loginserver.network.ProxyServer;
import l2e.loginserver.network.communication.GameServer;
import org.HostInfo;
import org.apache.commons.lang3.StringUtils;

public class GameServerManager {
   public static final int SUCCESS_GS_REGISTER = 0;
   public static final int FAIL_GS_REGISTER_DIFF_KEYS = 1;
   public static final int FAIL_GS_REGISTER_ID_ALREADY_USE = 2;
   public static final int FAIL_GS_REGISTER_ERROR = 3;
   private static final Logger _log = Logger.getLogger(GameServerManager.class.getName());
   private static final GameServerManager _instance = new GameServerManager();
   private final Map<Integer, GameServer> _gameServers = new TreeMap<>();
   private final Map<Integer, List<ProxyServer>> _gameServerProxys = new TreeMap<>();
   private final Map<Integer, ProxyServer> _proxyServers = new TreeMap<>();
   private final ReadWriteLock _lock = new ReentrantReadWriteLock();
   private final Lock _readLock = this._lock.readLock();
   private final Lock _writeLock = this._lock.writeLock();

   public static final GameServerManager getInstance() {
      return _instance;
   }

   public GameServerManager() {
      this.load();
   }

   private void load() {
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT `id`, `ip`, `port`, `age_limit`, `pvp`, `max_players`, `type`, `brackets`, `key` FROM gameservers");
         rset = statement.executeQuery();

         while(rset.next()) {
            int id = rset.getInt("id");
            GameServer gs = new GameServer(id, rset.getString("ip"), rset.getInt("port"), rset.getString("key"));
            gs.setAgeLimit(rset.getInt("age_limit"));
            gs.setPvp(rset.getInt("pvp") > 0);
            gs.setMaxPlayers(rset.getInt("max_players"));
            gs.setServerType(rset.getInt("type"));
            gs.setShowingBrackets(rset.getInt("brackets") > 0);
            this._gameServers.put(id, gs);
         }
      } catch (Exception var9) {
         _log.log(Level.WARNING, "", (Throwable)var9);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._gameServers.size() + " registered GameServer(s).");
      this.loadProxyServers();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._proxyServers.size() + " proxy server(s).");
   }

   public GameServer[] getGameServers() {
      this._readLock.lock();

      GameServer[] var2;
      try {
         Set<GameServer> gameservers = new HashSet<>(this._gameServers.values());
         var2 = gameservers.toArray(new GameServer[gameservers.size()]);
      } finally {
         this._readLock.unlock();
      }

      return var2;
   }

   public GameServer getGameServerById(int id) {
      this._readLock.lock();

      GameServer var2;
      try {
         var2 = this._gameServers.get(id);
      } finally {
         this._readLock.unlock();
      }

      return var2;
   }

   public int registerGameServer(HostInfo host, GameServer gs) {
      this._writeLock.lock();

      try {
         GameServer pgs = this._gameServers.get(host.getId());
         if (pgs != null) {
            HostInfo phost = pgs.getHost(host.getId());
            if (phost == null || !StringUtils.equals(host.getKey(), phost.getKey())) {
               return 1;
            }
         } else if (!Config.ACCEPT_NEW_GAMESERVER) {
            return 2;
         }

         if (pgs != null && pgs.isAuthed()) {
            return 3;
         } else {
            if (pgs != null) {
               pgs.removeHost(host.getId());
            }

            this._gameServers.put(host.getId(), gs);
            return 0;
         }
      } finally {
         this._writeLock.unlock();
      }
   }

   private void loadProxyServers() {
      for(Config.ProxyServerConfig psc : Config.PROXY_SERVERS_CONFIGS) {
         if (psc != null) {
            if (this._gameServers.containsKey(psc.getProxyId())) {
               _log.warning("Won't load collided proxy with id " + psc.getProxyId() + ".");
            } else {
               ProxyServer ps = new ProxyServer(psc.getOrigServerId(), psc.getProxyId());

               try {
                  InetAddress inetAddress = InetAddress.getByName(psc.getPorxyHost());
                  ps.setProxyAddr(inetAddress);
               } catch (UnknownHostException var7) {
                  _log.log(Level.WARNING, "Can't load proxy", (Throwable)var7);
                  continue;
               }

               ps.setProxyPort(psc.getProxyPort());
               List<ProxyServer> proxyList = this._gameServerProxys.get(ps.getOrigServerId());
               if (proxyList == null) {
                  this._gameServerProxys.put(ps.getOrigServerId(), proxyList = new LinkedList<>());
               }

               proxyList.add(ps);
               this._proxyServers.put(ps.getProxyServerId(), ps);
            }
         }
      }
   }

   public List<ProxyServer> getProxyServersList(int gameServerId) {
      List<ProxyServer> result = this._gameServerProxys.get(gameServerId);
      return result != null ? result : Collections.emptyList();
   }

   public ProxyServer getProxyServerById(int proxyServerId) {
      return this._proxyServers.get(proxyServerId);
   }
}
