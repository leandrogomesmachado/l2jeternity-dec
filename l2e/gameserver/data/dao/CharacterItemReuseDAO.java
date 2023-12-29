package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.ExUseSharedGroupItem;

public class CharacterItemReuseDAO {
   private static final Logger _log = Logger.getLogger(CharacterItemReuseDAO.class.getName());
   private static final String ADD_ITEM_REUSE_SAVE = "INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)";
   private static final String RESTORE_ITEM_REUSE_SAVE = "SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?";
   private static final String DELETE_ITEM_REUSE_SAVE = "DELETE FROM character_item_reuse_save WHERE charId=?";
   private static CharacterItemReuseDAO _instance = new CharacterItemReuseDAO();

   public void restore(Player player) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?");
         statement.setInt(1, player.getObjectId());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int itemId = rset.getInt("itemId");
            rset.getInt("itemObjId");
            long reuseDelay = rset.getLong("reuseDelay");
            long systime = rset.getLong("systime");
            boolean isInInventory = true;
            ItemInstance item = player.getInventory().getItemByItemId(itemId);
            if (item == null) {
               item = player.getWarehouse().getItemByItemId(itemId);
               isInInventory = false;
            }

            if (item != null && item.getId() == itemId && item.getReuseDelay() > 0) {
               long remainingTime = systime - System.currentTimeMillis();
               if (remainingTime > 10L) {
                  player.addTimeStampItem(item, reuseDelay, systime);
                  if (isInInventory && item.isEtcItem()) {
                     int group = item.getSharedReuseGroup();
                     if (group > 0) {
                        player.sendPacket(new ExUseSharedGroupItem(itemId, group, (long)((int)remainingTime), (int)reuseDelay));
                     }
                  }
               }
            }
         }

         rset.close();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_item_reuse_save WHERE charId=?");
         statement.setInt(1, player.getObjectId());
         statement.executeUpdate();
         statement.close();
      } catch (Exception var26) {
         _log.log(Level.WARNING, "Could not restore " + this + " Item Reuse data: " + var26.getMessage(), (Throwable)var26);
      }
   }

   public void store(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("DELETE FROM character_item_reuse_save WHERE charId=?");
         PreparedStatement ps2 = con.prepareStatement("INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)");
      ) {
         ps1.setInt(1, player.getObjectId());
         ps1.execute();

         for(TimeStamp ts : player.getItemRemainingReuseTime().values()) {
            if (ts != null && ts.hasNotPassed()) {
               ps2.setInt(1, player.getObjectId());
               ps2.setInt(2, ts.getItemId());
               ps2.setInt(3, ts.getItemObjectId());
               ps2.setLong(4, ts.getReuse());
               ps2.setDouble(5, (double)ts.getStamp());
               ps2.execute();
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Could not store char item reuse data: ", (Throwable)var61);
      }
   }

   public static CharacterItemReuseDAO getInstance() {
      return _instance;
   }
}
