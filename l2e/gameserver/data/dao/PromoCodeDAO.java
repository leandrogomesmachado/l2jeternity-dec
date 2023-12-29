package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.PromoCodeParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.promocode.PromoCodeTemplate;

public class PromoCodeDAO {
   private static final Logger _log = Logger.getLogger(PromoCodeDAO.class.getName());
   private final String ADD_PROMOCODE = "REPLACE INTO promocodes (name,value) VALUES (?,?)";
   private final String ADD_CHARACTER = "INSERT INTO character_promocodes (charId,name) VALUES (?,?)";
   private final String ADD_CHARACTER_ACC = "INSERT INTO character_promocodes_account (account,name) VALUES (?,?)";
   private final String ADD_CHARACTER_HWID = "INSERT INTO character_promocodes_hwid (hwid,name) VALUES (?,?)";
   private static PromoCodeDAO _instance = new PromoCodeDAO();

   public void insert(Player player, PromoCodeTemplate tpl) {
      PromoCodeParser.getInstance().addToCharList(tpl.getName(), player.getObjectId());

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_promocodes (charId,name) VALUES (?,?)");
         statement.setInt(1, player.getObjectId());
         statement.setString(2, tpl.getName());
         statement.executeUpdate();
         statement.close();
      } catch (Exception var103) {
         _log.log(Level.WARNING, "Could not insert character_promocodes data: " + var103);
      }

      if (tpl.getLimit() > 0) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("REPLACE INTO promocodes (name,value) VALUES (?,?)");
            statement.setString(1, tpl.getName());
            statement.setInt(2, tpl.getCurLimit());
            statement.executeUpdate();
            statement.close();
         } catch (Exception var101) {
            _log.log(Level.WARNING, "Could not insert promocodes data: " + var101);
         }
      }

      if (tpl.isLimitByAccount()) {
         PromoCodeParser.getInstance().addToAccountList(tpl.getName(), player.getAccountName());

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_promocodes_account (account,name) VALUES (?,?)");
            statement.setString(1, player.getAccountName());
            statement.setString(2, tpl.getName());
            statement.executeUpdate();
            statement.close();
         } catch (Exception var99) {
            _log.log(Level.WARNING, "Could not insert character_promocodes_account data: " + var99);
         }
      }

      if (tpl.isLimitHWID()) {
         PromoCodeParser.getInstance().addToHwidList(tpl.getName(), player.getHWID());

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_promocodes_hwid (hwid,name) VALUES (?,?)");
            statement.setString(1, player.getHWID());
            statement.setString(2, tpl.getName());
            statement.executeUpdate();
            statement.close();
         } catch (Exception var97) {
            _log.log(Level.WARNING, "Could not insert character_promocodes_hwid data: " + var97);
         }
      }
   }

   public static PromoCodeDAO getInstance() {
      return _instance;
   }
}
