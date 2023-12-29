package l2e.gameserver.data.holder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Base64;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.gameserverpackets.ChangeAccessLevel;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordVerify;

public class SecPasswordHolder {
   private final Logger _log = Logger.getLogger(SecPasswordHolder.class.getName());
   private final GameClient _activeClient;
   private String _password;
   private int _wrongAttempts;
   private boolean _authed;
   private static final String VAR_PWD = "secauth_pwd";
   private static final String VAR_WTE = "secauth_wte";
   private static final String SELECT_PASSWORD = "SELECT var, value FROM character_secondary_password WHERE account_name=? AND var LIKE 'secauth_%'";
   private static final String INSERT_PASSWORD = "INSERT INTO character_secondary_password VALUES (?, ?, ?)";
   private static final String UPDATE_PASSWORD = "UPDATE character_secondary_password SET value=? WHERE account_name=? AND var=?";
   private static final String INSERT_ATTEMPT = "INSERT INTO character_secondary_password VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?";

   public SecPasswordHolder(GameClient activeClient) {
      this._activeClient = activeClient;
      this._password = null;
      this._wrongAttempts = 0;
      this._authed = false;
      this.loadPassword();
   }

   private void loadPassword() {
      String value = null;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT var, value FROM character_secondary_password WHERE account_name=? AND var LIKE 'secauth_%'"
         );
      ) {
         statement.setString(1, this._activeClient.getLogin());

         try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
               String var = rs.getString("var");
               value = rs.getString("value");
               if (var.equals("secauth_pwd")) {
                  this._password = value;
               } else if (var.equals("secauth_wte")) {
                  this._wrongAttempts = Integer.parseInt(value);
               }
            }
         }
      } catch (Exception var61) {
         this._log.log(Level.SEVERE, "Error while reading password.", (Throwable)var61);
      }
   }

   public boolean savePassword(String password) {
      if (this.passwordExist()) {
         this._log.warning("[SecondaryPasswordAuth]" + this._activeClient.getLogin() + " forced savePassword");
         this._activeClient.closeNow();
         return false;
      } else if (!this.validatePassword(password)) {
         this._activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
         return false;
      } else {
         password = this.cryptPassword(password);

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_secondary_password VALUES (?, ?, ?)");
         ) {
            statement.setString(1, this._activeClient.getLogin());
            statement.setString(2, "secauth_pwd");
            statement.setString(3, password);
            statement.execute();
         } catch (Exception var34) {
            this._log.log(Level.SEVERE, "Error while writing password.", (Throwable)var34);
            return false;
         }

         this._password = password;
         return true;
      }
   }

   public boolean insertWrongAttempt(int attempts) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_secondary_password VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
      ) {
         statement.setString(1, this._activeClient.getLogin());
         statement.setString(2, "secauth_wte");
         statement.setString(3, Integer.toString(attempts));
         statement.setString(4, Integer.toString(attempts));
         statement.execute();
         return true;
      } catch (Exception var34) {
         this._log.log(Level.SEVERE, "Error while writing wrong attempts.", (Throwable)var34);
         return false;
      }
   }

   public boolean changePassword(String oldPassword, String newPassword) {
      if (!this.passwordExist()) {
         this._log.warning("[SecondaryPasswordAuth]" + this._activeClient.getLogin() + " forced changePassword");
         this._activeClient.closeNow();
         return false;
      } else if (!this.checkPassword(oldPassword, true)) {
         return false;
      } else if (!this.validatePassword(newPassword)) {
         this._activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
         return false;
      } else {
         newPassword = this.cryptPassword(newPassword);

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE character_secondary_password SET value=? WHERE account_name=? AND var=?");
         ) {
            statement.setString(1, newPassword);
            statement.setString(2, this._activeClient.getLogin());
            statement.setString(3, "secauth_pwd");
            statement.execute();
         } catch (Exception var35) {
            this._log.log(Level.SEVERE, "Error while reading password.", (Throwable)var35);
            return false;
         }

         this._password = newPassword;
         this._authed = false;
         return true;
      }
   }

   public boolean checkPassword(String password, boolean skipAuth) {
      password = this.cryptPassword(password);
      if (!password.equals(this._password)) {
         ++this._wrongAttempts;
         if (this._wrongAttempts < Config.SECOND_AUTH_MAX_ATTEMPTS) {
            this._activeClient.sendPacket(new Ex2ndPasswordVerify(1, this._wrongAttempts));
            this.insertWrongAttempt(this._wrongAttempts);
         } else {
            int banExpire = (int)(System.currentTimeMillis() / 1000L + Config.SECOND_AUTH_BAN_TIME * 60L);
            int accessLvl = Config.SECOND_AUTH_BAN_TIME > 0L ? 0 : -1;
            AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(this._activeClient.getLogin(), accessLvl, banExpire));
            this._log
               .warning(
                  this._activeClient.getLogin()
                     + " - ("
                     + this._activeClient.getConnectionAddress().getHostAddress()
                     + ") has inputted the wrong password "
                     + this._wrongAttempts
                     + " times in row."
               );
            this.insertWrongAttempt(0);
            this._activeClient.close(new Ex2ndPasswordVerify(2, Config.SECOND_AUTH_MAX_ATTEMPTS));
         }

         return false;
      } else {
         if (!skipAuth) {
            this._authed = true;
            this._activeClient.sendPacket(new Ex2ndPasswordVerify(0, this._wrongAttempts));
         }

         this.insertWrongAttempt(0);
         return true;
      }
   }

   public boolean passwordExist() {
      return this._password != null;
   }

   public void openDialog() {
      if (this.passwordExist()) {
         this._activeClient.sendPacket(new Ex2ndPasswordCheck(1));
      } else {
         this._activeClient.sendPacket(new Ex2ndPasswordCheck(0));
      }
   }

   public boolean isAuthed() {
      return this._authed;
   }

   private String cryptPassword(String password) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA");
         byte[] raw = password.getBytes("UTF-8");
         byte[] hash = md.digest(raw);
         return Base64.encodeBytes(hash);
      } catch (NoSuchAlgorithmException var5) {
         this._log.severe("[SecondaryPasswordAuth]Unsupported Algorythm");
      } catch (UnsupportedEncodingException var6) {
         this._log.severe("[SecondaryPasswordAuth]Unsupported Encoding");
      }

      return null;
   }

   private boolean validatePassword(String password) {
      if (!Util.isDigit(password)) {
         return false;
      } else if (password.length() >= 6 && password.length() <= 8) {
         if (Config.SECOND_AUTH_STRONG_PASS) {
            for(int i = 0; i < password.length() - 1; ++i) {
               char curCh = password.charAt(i);
               char nxtCh = password.charAt(i + 1);
               if (curCh + 1 == nxtCh) {
                  return false;
               }

               if (curCh - 1 == nxtCh) {
                  return false;
               }

               if (curCh == nxtCh) {
                  return false;
               }
            }

            for(int i = 0; i < password.length() - 2; ++i) {
               String toChk = password.substring(i + 1);
               StringBuffer chkEr = new StringBuffer(password.substring(i, i + 2));
               if (toChk.contains(chkEr)) {
                  return false;
               }

               if (toChk.contains(chkEr.reverse())) {
                  return false;
               }
            }
         }

         this._wrongAttempts = 0;
         return true;
      } else {
         return false;
      }
   }
}
