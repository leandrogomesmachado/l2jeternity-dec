package l2e.loginserver.network.communication.gameserverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import l2e.loginserver.database.DatabaseFactory;
import l2e.loginserver.network.communication.ReceivablePacket;

public class ChangeAllowedHwid extends ReceivablePacket {
   private String _account;
   private String _hwid;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._hwid = this.readS();
   }

   @Override
   protected void runImpl() {
      Connection con = null;
      PreparedStatement statement = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("UPDATE accounts SET allow_hwid=? WHERE login=?");
         statement.setString(1, this._hwid);
         statement.setString(2, this._account);
         statement.execute();
         statement.close();
      } catch (SQLException var12) {
         _log.warning("ChangeAllowedIP: Could not write data. Reason: " + var12);
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
