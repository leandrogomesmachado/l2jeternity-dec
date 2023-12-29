package l2e.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.network.GameClient;

public class RestoreOfflineTraders extends RunnableImpl {
   private static final Logger _log = Logger.getLogger(RestoreOfflineTraders.class.getName());

   @Override
   public void runImpl() throws Exception {
      int count = 0;
      int buffers = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         if (Config.OFFLINE_MAX_DAYS > 0) {
            int expireTimeSecs = (int)(System.currentTimeMillis() / 1000L - (long)Config.OFFLINE_MAX_DAYS * 86400L);

            try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?")) {
               statement.setLong(1, (long)expireTimeSecs);
               statement.executeUpdate();
            }

            Object rset = null;

            try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlineBuff' AND value < ?")) {
               statement.setLong(1, (long)expireTimeSecs);
               statement.executeUpdate();
            }
         }

         try (PreparedStatement statement = con.prepareStatement(
               "DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT charId FROM characters WHERE accessLevel < 0)"
            )) {
            statement.executeUpdate();
         }

         try (PreparedStatement statement = con.prepareStatement(
               "DELETE FROM character_variables WHERE name = 'offlineBuff' AND obj_id IN (SELECT charId FROM characters WHERE accessLevel < 0)"
            )) {
            statement.executeUpdate();
         }

         try (
            PreparedStatement statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
            ResultSet rset = statement.executeQuery();
         ) {
            int objectId = 0;
            Player player = null;

            while(rset.next()) {
               objectId = rset.getInt("obj_id");
               GameClient client = new GameClient(null);
               client.setDetached(true);
               player = Player.load(objectId);
               if (player != null) {
                  client.setActiveChar(player);
                  player.setOnlineStatus(true, false);
                  client.setLogin(player.getAccountNamePlayer());
                  if (!player.isDead() && player.getAccessLevel().getLevel() >= 0) {
                     World.getInstance().addToAllPlayers(player);
                     client.setState(GameClient.GameClientState.IN_GAME);
                     player.setClient(client);
                     player.spawnMe();
                     if (Config.OFFLINE_SET_NAME_COLOR) {
                        player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
                     }

                     player.setOfflineMode(true);
                     player.setOnlineStatus(true, true);
                     player.restoreEffects();
                     if (Config.OFFLINE_SET_VISUAL_EFFECT) {
                        player.startAbnormalEffect(AbnormalEffect.SLEEP);
                     }

                     player.broadcastCharInfo();
                     ++count;
                  } else {
                     player.deleteMe();
                  }
               }
            }
         }

         try (
            PreparedStatement statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offlineBuff'");
            ResultSet rset = statement.executeQuery();
         ) {
            int objectId = 0;
            Player player = null;

            while(rset.next()) {
               objectId = rset.getInt("obj_id");
               GameClient client = new GameClient(null);
               client.setDetached(true);
               player = Player.load(objectId);
               if (player != null) {
                  client.setActiveChar(player);
                  player.setOnlineStatus(true, false);
                  if (!player.isDead() && player.getAccessLevel().getLevel() >= 0) {
                     client.setLogin(player.getAccountNamePlayer());
                     client.setState(GameClient.GameClientState.IN_GAME);
                     World.getInstance().addToAllPlayers(player);
                     player.setClient(client);
                     player.spawnMe();
                     if (Config.OFFLINE_SET_NAME_COLOR) {
                        player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
                     }

                     player.setOfflineMode(true);
                     player.setIsSellingBuffs(true);
                     player.setOnlineStatus(true, true);
                     player.restoreEffects();
                     player.startAbnormalEffect(AbnormalEffect.SLEEP);
                     player.broadcastCharInfo();
                     ++buffers;
                  } else {
                     player.deleteMe();
                  }
               }
            }
         }
      } catch (SQLException var351) {
         _log.log(Level.WARNING, "Error while restoring offline traders!", (Throwable)var351);
      }

      _log.info("RestoreOfflineTraders: Restored " + count + " offline traders and " + buffers + " sellbuff traders.");
   }
}
