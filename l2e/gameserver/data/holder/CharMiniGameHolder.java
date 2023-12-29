package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.games.MiniGameScoreManager;

public class CharMiniGameHolder {
   private static final Logger _log = Logger.getLogger(CharMiniGameHolder.class.getName());
   private static final CharMiniGameHolder _instance = new CharMiniGameHolder();

   public static CharMiniGameHolder getInstance() {
      return _instance;
   }

   public void select() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT characters.char_name AS name, character_minigame_score.score AS score, character_minigame_score.charId AS charId FROM characters, character_minigame_score WHERE characters.charId=character_minigame_score.charId"
         );
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            String name = rset.getString("name");
            int score = rset.getInt("score");
            int objectId = rset.getInt("charId");
            MiniGameScoreManager.getInstance().addScore(objectId, score, name);
         }

         rset.close();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, "Exception: " + var18, (Throwable)var18);
      }
   }

   public void replace(int objectId, int score) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("REPLACE INTO character_minigame_score(charId, score) VALUES (?, ?)");
         statement.setInt(1, objectId);
         statement.setInt(2, score);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Exception: " + var16, (Throwable)var16);
      }
   }
}
