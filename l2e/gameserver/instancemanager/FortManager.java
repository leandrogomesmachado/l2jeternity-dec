package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.InstanceListManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.entity.Fort;

public class FortManager implements InstanceListManager {
   protected static final Logger _log = Logger.getLogger(FortManager.class.getName());
   private final List<Fort> _forts = new ArrayList<>();

   public static final FortManager getInstance() {
      return FortManager.SingletonHolder._instance;
   }

   protected FortManager() {
   }

   public final int findNearestFortIndex(GameObject obj) {
      return this.findNearestFortIndex(obj, Long.MAX_VALUE);
   }

   public final int findNearestFortIndex(GameObject obj, long maxDistance) {
      int index = this.getFortIndex(obj);
      if (index < 0) {
         for(int i = 0; i < this.getForts().size(); ++i) {
            Fort fort = this.getForts().get(i);
            if (fort != null) {
               double distance = fort.getDistance(obj);
               if ((double)maxDistance > distance) {
                  maxDistance = (long)distance;
                  index = i;
               }
            }
         }
      }

      return index;
   }

   public final Fort getFortById(int fortId) {
      for(Fort f : this.getForts()) {
         if (f.getId() == fortId) {
            return f;
         }
      }

      return null;
   }

   public final Fort getFortByOwner(Clan clan) {
      for(Fort f : this.getForts()) {
         if (f.getOwnerClan() == clan) {
            return f;
         }
      }

      return null;
   }

   public final Fort getFort(String name) {
      for(Fort f : this.getForts()) {
         if (f.getName().equalsIgnoreCase(name.trim())) {
            return f;
         }
      }

      return null;
   }

   public final Fort getFort(int x, int y, int z) {
      for(Fort f : this.getForts()) {
         if (f.checkIfInZone(x, y, z)) {
            return f;
         }
      }

      return null;
   }

   public final Fort getFort(GameObject activeObject) {
      return this.getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final int getFortIndex(int fortId) {
      for(int i = 0; i < this.getForts().size(); ++i) {
         Fort fort = this.getForts().get(i);
         if (fort != null && fort.getId() == fortId) {
            return i;
         }
      }

      return -1;
   }

   public final int getFortIndex(GameObject activeObject) {
      return this.getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final int getFortIndex(int x, int y, int z) {
      for(int i = 0; i < this.getForts().size(); ++i) {
         Fort fort = this.getForts().get(i);
         if (fort != null && fort.checkIfInZone(x, y, z)) {
            return i;
         }
      }

      return -1;
   }

   public final List<Fort> getForts() {
      return this._forts;
   }

   @Override
   public void loadInstances() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT id FROM fort ORDER BY id");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.getForts().add(new Fort(rs.getInt("id")));
         }

         rs.close();
         statement.close();
         _log.info(this.getClass().getSimpleName() + ": Loaded: " + this.getForts().size() + " fortress");

         for(Fort fort : this.getForts()) {
            fort.getSiege().getSiegeGuardManager().loadSiegeGuard();
         }
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Exception: loadFortData(): " + var17.getMessage(), (Throwable)var17);
      }
   }

   @Override
   public void updateReferences() {
   }

   @Override
   public void activateInstances() {
      for(Fort fort : this._forts) {
         fort.activateInstance();
      }
   }

   private static class SingletonHolder {
      protected static final FortManager _instance = new FortManager();
   }
}
