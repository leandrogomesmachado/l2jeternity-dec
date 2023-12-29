package l2e.loginserver.network.communication.gameserverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import l2e.loginserver.database.DatabaseFactory;
import l2e.loginserver.network.communication.ReceivablePacket;

public class ChangeAllowedIp extends ReceivablePacket {
   private String _account;
   private String _ip;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._ip = this.readS();
   }

   @Override
   protected void runImpl() {
      Connection con = null;
      PreparedStatement statement = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("UPDATE accounts SET allow_ip=? WHERE login=?");
         statement.setString(1, this._ip);
         statement.setString(2, this._account);
         statement.execute();
         statement.close();
      } catch (SQLException var12) {
         _log.log(Level.WARNING, "ChangeAllowedIP: Could not write data. Reason: " + var12);
      } finally {
         try {
            if (con != null) {
               con.close();
            }
         } catch (SQLException var11) {
            var11.printStackTrace();
         }
      }
   }
}
