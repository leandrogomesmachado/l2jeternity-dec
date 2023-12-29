package l2e.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;

public class PetitionVote extends GameClientPacket {
   private static final String INSERT_FEEDBACK = "INSERT INTO petition_feedback VALUES (?,?,?,?,?)";
   private int _rate;
   private String _message;

   @Override
   protected void readImpl() {
      this.readD();
      this._rate = this.readD();
      this._message = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null && player.getLastPetitionGmName() != null) {
         if (this._rate <= 4 && this._rate >= 0) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("INSERT INTO petition_feedback VALUES (?,?,?,?,?)");
            ) {
               statement.setString(1, player.getName());
               statement.setString(2, player.getLastPetitionGmName());
               statement.setInt(3, this._rate);
               statement.setString(4, this._message);
               statement.setLong(5, System.currentTimeMillis());
               statement.execute();
            } catch (SQLException var34) {
               _log.log(Level.SEVERE, "Error while saving petition feedback");
            }
         }
      }
   }
}
