package l2e.gameserver.network.communication.loginserverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.dbutils.DbUtils;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;
import l2e.gameserver.network.communication.gameserverpackets.SetAccountInfo;

public class GetAccountInfo extends ReceivablePacket {
   private String _account;

   @Override
   protected void readImpl() {
      this._account = this.readS();
   }

   @Override
   protected void runImpl() {
      int playerSize = 0;
      List<Long> deleteChars = new ArrayList<>();
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?");
         statement.setString(1, this._account);
         rset = statement.executeQuery();

         while(rset.next()) {
            ++playerSize;
            long d = rset.getLong("deletetime");
            if (d > 0L) {
               deleteChars.add(d);
            }
         }
      } catch (Exception var11) {
         _log.log(Level.WARNING, "GetAccountInfo:runImpl():" + var11, (Throwable)var11);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      AuthServerCommunication.getInstance().sendPacket(new SetAccountInfo(this._account, playerSize, deleteChars));
   }
}
