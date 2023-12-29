package l2e.loginserver.accounts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.loginserver.database.DatabaseFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.utils.Net;
import org.utils.NetList;

public class Account {
   private static final Logger _log = Logger.getLogger(Account.class.getName());
   private final String _login;
   private String _passwordHash;
   private String _allowedIP;
   private String _allowedHwid;
   private final NetList _allowedIpList = new NetList();
   private int _accessLevel;
   private int _banExpire;
   private String _lastIP;
   private int _lastAccess;
   private int _lastServer;
   private final IntObjectMap<Pair<Integer, int[]>> _serversInfo = new HashIntObjectMap<>(2);

   public Account(String login) {
      this._login = login;
   }

   public String getLogin() {
      return this._login;
   }

   public String getPasswordHash() {
      return this._passwordHash;
   }

   public void setPasswordHash(String passwordHash) {
      this._passwordHash = passwordHash;
   }

   public String getAllowedIP() {
      return this._allowedIP;
   }

   public String getAllowedHwid() {
      return this._allowedHwid;
   }

   public boolean isAllowedIP(String ip) {
      return this._allowedIpList.isEmpty() || this._allowedIpList.matches(ip);
   }

   public void setAllowedIP(String allowedIP) {
      this._allowedIpList.clear();
      this._allowedIP = allowedIP;
      if (!allowedIP.isEmpty()) {
         String[] masks = allowedIP.split("[\\s,;]+");

         for(String mask : masks) {
            try {
               this._allowedIpList.add(Net.valueOf(mask));
            } catch (Exception var8) {
               _log.log(Level.WARNING, "", (Throwable)var8);
            }
         }
      }
   }

   public void setAllowedHwid(String allowedHwid) {
      this._allowedHwid = allowedHwid;
   }

   public int getAccessLevel() {
      return this._accessLevel;
   }

   public void setAccessLevel(int accessLevel) {
      this._accessLevel = accessLevel;
   }

   public int getBanExpire() {
      return this._banExpire;
   }

   public void setBanExpire(int banExpire) {
      this._banExpire = banExpire;
   }

   public void setLastIP(String lastIP) {
      this._lastIP = lastIP;
   }

   public String getLastIP() {
      return this._lastIP;
   }

   public int getLastAccess() {
      return this._lastAccess;
   }

   public void setLastAccess(int lastAccess) {
      this._lastAccess = lastAccess;
   }

   public int getLastServer() {
      return this._lastServer;
   }

   public void setLastServer(int lastServer) {
      this._lastServer = lastServer;
   }

   public void addAccountInfo(int serverId, int size, int[] deleteChars) {
      this._serversInfo.put(serverId, new ImmutablePair<>(size, deleteChars));
   }

   public Pair<Integer, int[]> getAccountInfo(int serverId) {
      return this._serversInfo.get(serverId);
   }

   @Override
   public String toString() {
      return this._login;
   }

   public void restore() {
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement(
            "SELECT password, accessLevel, banExpire, allowIp, allowHwid, lastServer, lastIP, lastAccess FROM accounts WHERE login = ?"
         );
         statement.setString(1, this._login);
         rset = statement.executeQuery();
         if (rset.next()) {
            this.setPasswordHash(rset.getString("password"));
            this.setAccessLevel(rset.getInt("accessLevel"));
            this.setBanExpire(rset.getInt("banExpire"));
            this.setAllowedIP(rset.getString("allowIp"));
            this.setAllowedHwid(rset.getString("allowHwid"));
            this.setLastServer(rset.getInt("lastServer"));
            this.setLastIP(rset.getString("lastIP"));
            this.setLastAccess(rset.getInt("lastAccess"));
         }
      } catch (Exception var8) {
         _log.log(Level.WARNING, "", (Throwable)var8);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }
   }

   public void save() {
      Connection con = null;
      PreparedStatement statement = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("INSERT INTO accounts (login, password) VALUES(?,?)");
         statement.setString(1, this.getLogin());
         statement.setString(2, this.getPasswordHash());
         statement.execute();
      } catch (Exception var7) {
         _log.log(Level.WARNING, "", (Throwable)var7);
      } finally {
         DbUtils.closeQuietly(con, statement);
      }

      _log.info("Auto created account '" + this.getLogin() + "'.");
   }

   public void update() {
      Connection con = null;
      PreparedStatement statement = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement(
            "UPDATE accounts SET password = ?, accessLevel = ?, banExpire = ?, allowIp = ?, allowHwid=?, lastServer = ?, lastIP = ?, lastAccess = ? WHERE login = ?"
         );
         statement.setString(1, this.getPasswordHash());
         statement.setInt(2, this.getAccessLevel());
         statement.setInt(3, this.getBanExpire());
         statement.setString(4, this.getAllowedIP());
         statement.setString(5, this.getAllowedHwid());
         statement.setInt(6, this.getLastServer());
         statement.setString(7, this.getLastIP());
         statement.setInt(8, this.getLastAccess());
         statement.setString(9, this.getLogin());
         statement.execute();
      } catch (Exception var7) {
         _log.log(Level.WARNING, "", (Throwable)var7);
      } finally {
         DbUtils.closeQuietly(con, statement);
      }
   }
}
