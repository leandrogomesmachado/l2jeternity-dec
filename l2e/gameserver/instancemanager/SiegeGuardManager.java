package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.spawn.Spawner;

public class SiegeGuardManager {
   private static Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());
   private final Castle _castle;
   private final List<Spawner> _siegeGuardSpawn = new ArrayList<>();

   public SiegeGuardManager(Castle castle) {
      this._castle = castle;
   }

   public void addSiegeGuard(Player activeChar, int npcId) {
      if (activeChar != null) {
         this.addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
      }
   }

   public void addSiegeGuard(int x, int y, int z, int heading, int npcId) {
      this.saveSiegeGuard(x, y, z, heading, npcId, 0);
   }

   public void hireMerc(Player activeChar, int npcId) {
      if (activeChar != null) {
         this.hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
      }
   }

   public void hireMerc(int x, int y, int z, int heading, int npcId) {
      this.saveSiegeGuard(x, y, z, heading, npcId, 1);
   }

   public void removeMerc(int npcId, int x, int y, int z) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
         statement.setInt(1, npcId);
         statement.setInt(2, x);
         statement.setInt(3, y);
         statement.setInt(4, z);
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, "Error deleting hired siege guard at " + x + ',' + y + ',' + z + ": " + var18.getMessage(), (Throwable)var18);
      }
   }

   public void removeMercs() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1");
         statement.setInt(1, this.getCastle().getId());
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Error deleting hired siege guard for castle " + this.getCastle().getName() + ": " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void spawnSiegeGuard() {
      try {
         int hiredCount = 0;
         int hiredMax = MercTicketManager.getInstance().getMaxAllowedMerc(this._castle.getId());
         boolean isHired = this.getCastle().getOwnerId() > 0;
         this.loadSiegeGuard();

         for(Spawner spawn : this.getSiegeGuardSpawn()) {
            if (spawn != null) {
               spawn.init();
               if (isHired) {
                  spawn.stopRespawn();
                  if (++hiredCount > hiredMax) {
                     return;
                  }
               }
            }
         }
      } catch (Exception var6) {
         _log.log(Level.SEVERE, "Error spawning siege guards for castle " + this.getCastle().getName(), (Throwable)var6);
      }
   }

   public void unspawnSiegeGuard() {
      for(Spawner spawn : this.getSiegeGuardSpawn()) {
         if (spawn != null && spawn.getLastSpawn() != null) {
            spawn.stopRespawn();
            spawn.getLastSpawn().doDie(spawn.getLastSpawn());
         }
      }

      this.getSiegeGuardSpawn().clear();
   }

   private void loadSiegeGuard() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?");
         statement.setInt(1, this.getCastle().getId());
         if (this.getCastle().getOwnerId() > 0) {
            statement.setInt(2, 1);
         } else {
            statement.setInt(2, 0);
         }

         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rs.getInt("npcId"));
            if (template1 != null) {
               Spawner spawn1 = new Spawner(template1);
               spawn1.setAmount(1);
               spawn1.setX(rs.getInt("x"));
               spawn1.setY(rs.getInt("y"));
               spawn1.setZ(rs.getInt("z"));
               spawn1.setHeading(rs.getInt("heading"));
               spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
               spawn1.setLocationId(0);
               this._siegeGuardSpawn.add(spawn1);
            } else {
               _log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Error loading siege guard for castle " + this.getCastle().getName() + ": " + var17.getMessage(), (Throwable)var17);
      }
   }

   private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)"
         );
         statement.setInt(1, this.getCastle().getId());
         statement.setInt(2, npcId);
         statement.setInt(3, x);
         statement.setInt(4, y);
         statement.setInt(5, z);
         statement.setInt(6, heading);
         statement.setInt(7, isHire == 1 ? 0 : 600);
         statement.setInt(8, isHire);
         statement.execute();
         statement.close();
      } catch (Exception var20) {
         _log.log(Level.WARNING, "Error adding siege guard for castle " + this.getCastle().getName() + ": " + var20.getMessage(), (Throwable)var20);
      }
   }

   public final Castle getCastle() {
      return this._castle;
   }

   public final List<Spawner> getSiegeGuardSpawn() {
      return this._siegeGuardSpawn;
   }
}
