package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Couple;

public final class CoupleManager {
   private static final Logger _log = Logger.getLogger(CoupleManager.class.getName());
   private final List<Couple> _couples = new CopyOnWriteArrayList<>();

   protected CoupleManager() {
      this.load();
   }

   public static final CoupleManager getInstance() {
      return CoupleManager.SingletonHolder._instance;
   }

   public void reload() {
      this._couples.clear();
      this.load();
   }

   private final void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT id FROM mods_wedding ORDER BY id");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.getCouples().add(new Couple(rs.getInt("id")));
         }

         rs.close();
         statement.close();
         _log.info(this.getClass().getSimpleName() + ": Loaded: " + this.getCouples().size() + " couples(s)");
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Exception: CoupleManager.load(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   public final Couple getCouple(int coupleId) {
      int index = this.getCoupleIndex(coupleId);
      return index >= 0 ? this.getCouples().get(index) : null;
   }

   public void createCouple(Player player1, Player player2) {
      if (player1 != null && player2 != null && player1.getPartnerId() == 0 && player2.getPartnerId() == 0) {
         int _player1id = player1.getObjectId();
         int _player2id = player2.getObjectId();
         Couple _new = new Couple(player1, player2);
         this.getCouples().add(_new);
         player1.setPartnerId(_player2id);
         player2.setPartnerId(_player1id);
         player1.setCoupleId(_new.getId());
         player2.setCoupleId(_new.getId());
      }
   }

   public void deleteCouple(int coupleId) {
      int index = this.getCoupleIndex(coupleId);
      Couple couple = this.getCouples().get(index);
      if (couple != null) {
         Player player1 = World.getInstance().getPlayer(couple.getPlayer1Id());
         Player player2 = World.getInstance().getPlayer(couple.getPlayer2Id());
         if (player1 != null) {
            player1.setPartnerId(0);
            player1.setMarried(false);
            player1.setCoupleId(0);
         }

         if (player2 != null) {
            player2.setPartnerId(0);
            player2.setMarried(false);
            player2.setCoupleId(0);
         }

         couple.divorce();
         this.getCouples().remove(index);
      }
   }

   public final int getCoupleIndex(int coupleId) {
      int i = 0;

      for(Couple temp : this.getCouples()) {
         if (temp != null && temp.getId() == coupleId) {
            return i;
         }

         ++i;
      }

      return -1;
   }

   public final List<Couple> getCouples() {
      return this._couples;
   }

   private static class SingletonHolder {
      protected static final CoupleManager _instance = new CoupleManager();
   }
}
