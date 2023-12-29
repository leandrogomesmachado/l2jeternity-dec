package l2e.loginserver.network.communication.gameserverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import l2e.commons.dbutils.DbUtils;
import l2e.loginserver.Config;
import l2e.loginserver.database.DatabaseFactory;
import l2e.loginserver.network.communication.ReceivablePacket;
import l2e.loginserver.network.communication.loginserverpackets.ChangePasswordResponse;

public class ChangePassword extends ReceivablePacket {
   public String _accname;
   public String _oldPass;
   public String _newPass;
   public String _hwid;

   @Override
   protected void readImpl() {
      this._accname = this.readS();
      this._oldPass = this.readS();
      this._newPass = this.readS();
      this._hwid = this.readS();
   }

   @Override
   protected void runImpl() {
      String dbPassword = null;
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rs = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();

         try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, this._accname);
            rs = statement.executeQuery();
            if (rs.next()) {
               dbPassword = rs.getString("password");
            }
         } catch (Exception var27) {
            _log.log(Level.WARNING, "Can't recive old password for account " + this._accname + ", exciption :" + var27);
         } finally {
            DbUtils.closeQuietly(statement, rs);
         }

         try {
            if ((Config.DEFAULT_CRYPT.compare(this._oldPass, dbPassword) || !Config.ALLOW_ENCODE_PASSWORD)
               && (Config.DEFAULT_CRYPT.compare(this._oldPass, dbPassword) || dbPassword.equals(this._oldPass) || Config.ALLOW_ENCODE_PASSWORD)) {
               statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?");
               statement.setString(1, Config.ALLOW_ENCODE_PASSWORD ? Config.DEFAULT_CRYPT.encrypt(this._newPass) : this._newPass);
               statement.setString(2, this._accname);
               int result = statement.executeUpdate();
               this.sendPacket(new ChangePasswordResponse(this._accname, result != 0));
            } else {
               this.sendPacket(new ChangePasswordResponse(this._accname, false));
            }
         } catch (Exception var29) {
            var29.printStackTrace();
         } finally {
            ;
         }
      } catch (Exception var31) {
         var31.printStackTrace();
      } finally {
         DbUtils.closeQuietly(con);
      }
   }
}
