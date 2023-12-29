package l2e.loginserver.network.communication;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.loginserver.database.DatabaseFactory;
import org.HostInfo;

public class GameServer {
   private static final Logger _log = Logger.getLogger(GameServer.class.getName());
   private final TIntObjectMap<HostInfo> _hosts = new TIntObjectHashMap<>();
   private int _serverType;
   private int _ageLimit;
   private int _protocol;
   private boolean _isOnline;
   private boolean _isPvp;
   private boolean _isShowingBrackets;
   private boolean _isGmOnly;
   private int _maxPlayers;
   private GameServerConnection _conn;
   private boolean _isAuthed;
   private final Set<String> _accounts = new CopyOnWriteArraySet<>();

   public GameServer(GameServerConnection conn) {
      this._conn = conn;
   }

   public GameServer(int id, String ip, int port, String key) {
      this._conn = null;
      this.addHost(new HostInfo(id, ip, port, key));
   }

   public void addHost(HostInfo host) {
      this._hosts.put(host.getId(), host);
   }

   public HostInfo removeHost(int id) {
      return this._hosts.remove(id);
   }

   public HostInfo getHost(int id) {
      return this._hosts.get(id);
   }

   public HostInfo[] getHosts() {
      return this._hosts.values(new HostInfo[this._hosts.size()]);
   }

   public void setAuthed(boolean isAuthed) {
      this._isAuthed = isAuthed;
   }

   public boolean isAuthed() {
      return this._isAuthed;
   }

   public void setConnection(GameServerConnection conn) {
      this._conn = conn;
   }

   public GameServerConnection getConnection() {
      return this._conn;
   }

   public void setMaxPlayers(int maxPlayers) {
      this._maxPlayers = maxPlayers;
   }

   public int getMaxPlayers() {
      return this._maxPlayers;
   }

   public int getOnline() {
      return this._accounts.size();
   }

   public Set<String> getAccounts() {
      return this._accounts;
   }

   public void addAccount(String account) {
      this._accounts.add(account);
   }

   public void removeAccount(String account) {
      this._accounts.remove(account);
   }

   public void setDown() {
      this.setAuthed(false);
      this.setConnection(null);
      this.setOnline(false);
      this._accounts.clear();
   }

   public void sendPacket(SendablePacket packet) {
      GameServerConnection conn = this.getConnection();
      if (conn != null) {
         conn.sendPacket(packet);
      }
   }

   public int getServerType() {
      return this._serverType;
   }

   public boolean isOnline() {
      return this._isOnline;
   }

   public void setOnline(boolean online) {
      this._isOnline = online;
   }

   public void setServerType(int serverType) {
      this._serverType = serverType;
   }

   public boolean isPvp() {
      return this._isPvp;
   }

   public void setPvp(boolean pvp) {
      this._isPvp = pvp;
   }

   public boolean isShowingBrackets() {
      return this._isShowingBrackets;
   }

   public void setShowingBrackets(boolean showingBrackets) {
      this._isShowingBrackets = showingBrackets;
   }

   public boolean isGmOnly() {
      return this._isGmOnly;
   }

   public void setGmOnly(boolean gmOnly) {
      this._isGmOnly = gmOnly;
   }

   public int getAgeLimit() {
      return this._ageLimit;
   }

   public void setAgeLimit(int ageLimit) {
      this._ageLimit = ageLimit;
   }

   public int getProtocol() {
      return this._protocol;
   }

   public void setProtocol(int protocol) {
      this._protocol = protocol;
   }

   public boolean store() {
      Connection con = null;
      PreparedStatement statement = null;

      boolean host;
      try {
         con = DatabaseFactory.getInstance().getConnection();

         for(HostInfo host : this._hosts.valueCollection()) {
            statement = con.prepareStatement(
               "REPLACE INTO gameservers (`id`, `ip`, `port`, `age_limit`, `pvp`, `max_players`, `type`, `brackets`, `key`) VALUES(?,?,?,?,?,?,?,?,?)"
            );
            int i = 0;
            statement.setInt(++i, host.getId());
            statement.setString(++i, host.getAddress());
            statement.setShort(++i, (short)host.getPort());
            statement.setByte(++i, (byte)this.getAgeLimit());
            statement.setByte(++i, (byte)(this.isPvp() ? 1 : 0));
            statement.setShort(++i, (short)this.getMaxPlayers());
            statement.setInt(++i, this.getServerType());
            statement.setByte(++i, (byte)(this.isShowingBrackets() ? 1 : 0));
            statement.setString(++i, host.getKey());
            statement.execute();
            DbUtils.closeQuietly(statement);
         }

         return true;
      } catch (Exception var9) {
         _log.log(Level.WARNING, "Error while store gameserver: " + var9, (Throwable)var9);
         host = false;
      } finally {
         DbUtils.closeQuietly(con, statement);
      }

      return host;
   }
}
