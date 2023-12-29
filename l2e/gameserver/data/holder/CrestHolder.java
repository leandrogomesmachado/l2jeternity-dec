package l2e.gameserver.data.holder;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.file.filter.BMPFilter;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Crest;
import l2e.gameserver.model.CrestType;

public final class CrestHolder {
   private static final Logger _log = Logger.getLogger(CrestHolder.class.getName());
   private final Map<Integer, Crest> _crests = new ConcurrentHashMap<>();
   private final AtomicInteger _nextId = new AtomicInteger(1);

   protected CrestHolder() {
      this.load();
   }

   public synchronized void load() {
      this._crests.clear();
      Set<Integer> crestsInUse = new HashSet<>();

      for(Clan clan : ClanHolder.getInstance().getClans()) {
         if (clan.getCrestId() != 0) {
            crestsInUse.add(clan.getCrestId());
         }

         if (clan.getCrestLargeId() != 0) {
            crestsInUse.add(clan.getCrestLargeId());
         }

         if (clan.getAllyCrestId() != 0) {
            crestsInUse.add(clan.getAllyCrestId());
         }
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement statement = con.createStatement(1003, 1008);
         ResultSet rs = statement.executeQuery("SELECT `crest_id`, `data`, `type` FROM `crests` ORDER BY `crest_id` DESC");
      ) {
         while(rs.next()) {
            int id = rs.getInt("crest_id");
            if (this._nextId.get() <= id) {
               this._nextId.set(id + 1);
            }

            if (!crestsInUse.contains(id) && id != this._nextId.get() - 1) {
               rs.deleteRow();
            } else {
               byte[] data = rs.getBytes("data");
               CrestType crestType = CrestType.getById(rs.getInt("type"));
               if (crestType != null) {
                  this._crests.put(id, new Crest(id, data, crestType));
               } else {
                  _log.warning("Unknown crest type found in database. Type:" + rs.getInt("type"));
               }
            }
         }
      } catch (SQLException var62) {
         _log.log(Level.SEVERE, "There was an error while loading crests from database:", (Throwable)var62);
      }

      this.moveOldCrestsToDb(crestsInUse);
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._crests.size() + " Crests.");

      for(Clan clan : ClanHolder.getInstance().getClans()) {
         if (clan.getCrestId() != 0 && this.getCrest(clan.getCrestId()) == null) {
            _log.info("Removing non-existent crest for clan " + clan.getName() + " [" + clan.getId() + "], crestId:" + clan.getCrestId());
            clan.setCrestId(0);
            clan.changeClanCrest(0);
         }

         if (clan.getCrestLargeId() != 0 && this.getCrest(clan.getCrestLargeId()) == null) {
            _log.info("Removing non-existent large crest for clan " + clan.getName() + " [" + clan.getId() + "], crestLargeId:" + clan.getCrestLargeId());
            clan.setCrestLargeId(0);
            clan.changeLargeCrest(0);
         }

         if (clan.getAllyCrestId() != 0 && this.getCrest(clan.getAllyCrestId()) == null) {
            _log.info("Removing non-existent ally crest for clan " + clan.getName() + " [" + clan.getId() + "], allyCrestId:" + clan.getAllyCrestId());
            clan.setAllyCrestId(0);
            clan.changeAllyCrest(0, true);
         }
      }
   }

   private void moveOldCrestsToDb(Set<Integer> crestsInUse) {
      File crestDir = new File(Config.DATAPACK_ROOT, "data/crests/");
      if (crestDir.exists()) {
         File[] files = crestDir.listFiles(new BMPFilter());
         if (files == null) {
            return;
         }

         for(File file : files) {
            try {
               byte[] data = Files.readAllBytes(file.toPath());
               if (file.getName().startsWith("Crest_Large_")) {
                  int crestId = Integer.parseInt(file.getName().substring(12, file.getName().length() - 4));
                  if (crestsInUse.contains(crestId)) {
                     Crest crest = this.createCrest(data, CrestType.PLEDGE_LARGE);
                     if (crest != null) {
                        for(Clan clan : ClanHolder.getInstance().getClans()) {
                           if (clan.getCrestLargeId() == crestId) {
                              clan.setCrestLargeId(0);
                              clan.changeLargeCrest(crest.getId());
                           }
                        }
                     }
                  }
               } else if (file.getName().startsWith("Crest_")) {
                  int crestId = Integer.parseInt(file.getName().substring(6, file.getName().length() - 4));
                  if (crestsInUse.contains(crestId)) {
                     Crest crest = this.createCrest(data, CrestType.PLEDGE);
                     if (crest != null) {
                        for(Clan clan : ClanHolder.getInstance().getClans()) {
                           if (clan.getCrestId() == crestId) {
                              clan.setCrestId(0);
                              clan.changeClanCrest(crest.getId());
                           }
                        }
                     }
                  }
               } else if (file.getName().startsWith("AllyCrest_")) {
                  int crestId = Integer.parseInt(file.getName().substring(10, file.getName().length() - 4));
                  if (crestsInUse.contains(crestId)) {
                     Crest crest = this.createCrest(data, CrestType.ALLY);
                     if (crest != null) {
                        for(Clan clan : ClanHolder.getInstance().getClans()) {
                           if (clan.getAllyCrestId() == crestId) {
                              clan.setAllyCrestId(0);
                              clan.changeAllyCrest(crest.getId(), false);
                           }
                        }
                     }
                  }
               }

               file.delete();
            } catch (Exception var15) {
               _log.log(Level.SEVERE, "There was an error while moving crest file " + file.getName() + " to database:", (Throwable)var15);
            }
         }

         crestDir.delete();
      }
   }

   public Crest getCrest(int crestId) {
      return this._crests.get(crestId);
   }

   public Crest createCrest(byte[] data, CrestType crestType) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO `crests`(`crest_id`, `data`, `type`) VALUES(?, ?, ?)");
      ) {
         Crest crest = new Crest(this.getNextId(), data, crestType);
         statement.setInt(1, crest.getId());
         statement.setBytes(2, crest.getData());
         statement.setInt(3, crest.getType().getId());
         statement.executeUpdate();
         this._crests.put(crest.getId(), crest);
         return crest;
      } catch (SQLException var37) {
         _log.log(Level.SEVERE, "There was an error while saving crest in database:", (Throwable)var37);
         return null;
      }
   }

   public void removeCrest(int crestId) {
      this._crests.remove(crestId);
      if (crestId != this._nextId.get() - 1) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM `crests` WHERE `crest_id` = ?");
         ) {
            statement.setInt(1, crestId);
            statement.executeUpdate();
         } catch (SQLException var34) {
            _log.log(Level.SEVERE, "There was an error while deleting crest from database:", (Throwable)var34);
         }
      }
   }

   public int getNextId() {
      return this._nextId.getAndIncrement();
   }

   public static CrestHolder getInstance() {
      return CrestHolder.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CrestHolder _instance = new CrestHolder();
   }
}
