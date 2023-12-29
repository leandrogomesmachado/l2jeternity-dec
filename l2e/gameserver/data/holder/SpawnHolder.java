package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassMasterParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;

public class SpawnHolder {
   private static Logger _log = Logger.getLogger(SpawnHolder.class.getName());
   private final Set<Spawner> _spawntable = ConcurrentHashMap.newKeySet();

   protected SpawnHolder() {
      this._spawntable.clear();
      if (!Config.ALT_DEV_NO_SPAWNS) {
         this.fillSpawnTable();
      }
   }

   public Set<Spawner> getSpawnTable() {
      return this._spawntable;
   }

   private void fillSpawnTable() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM spawnlist"
         );
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            NpcTemplate template1 = NpcsParser.getInstance().getTemplate(rset.getInt("npc_templateid"));
            if (template1 != null) {
               if (!template1.isType("SiegeGuard")
                  && !template1.isType("RaidBoss")
                  && (ClassMasterParser.getInstance().isAllowClassMaster() || !template1.isType("ClassMaster"))
                  && (!Config.ALT_CHEST_NO_SPAWNS || !template1.isType("TreasureChest"))) {
                  Spawner spawnDat = new Spawner(template1);
                  spawnDat.setAmount(rset.getInt("count"));
                  spawnDat.setX(rset.getInt("locx"));
                  spawnDat.setY(rset.getInt("locy"));
                  spawnDat.setZ(rset.getInt("locz"));
                  spawnDat.setHeading(rset.getInt("heading"));
                  spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_random"));
                  int loc_id = rset.getInt("loc_id");
                  spawnDat.setLocationId(loc_id);
                  switch(rset.getInt("periodOfDay")) {
                     case 0:
                        spawnDat.init();
                        break;
                     case 1:
                        DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                        break;
                     case 2:
                        DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                  }

                  this._spawntable.add(spawnDat);
               }
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Spawn could not be initialized: " + var18.getMessage(), (Throwable)var18);
      }

      if (this._spawntable.size() > 0) {
         _log.info(this.getClass().getSimpleName() + ": Loaded " + this._spawntable.size() + " npc spawns from database.");
      }
   }

   public void addNewSpawn(Spawner spawn, boolean storeInDb) {
      this._spawntable.add(spawn);
      if (storeInDb) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement insert = con.prepareStatement(
               "INSERT INTO spawnlist (count,npc_templateid,locx,locy,locz,heading,respawn_delay,respawn_random,loc_id) values(?,?,?,?,?,?,?,?,?)"
            );
         ) {
            insert.setInt(1, spawn.getAmount());
            insert.setInt(2, spawn.getId());
            insert.setInt(3, spawn.getX());
            insert.setInt(4, spawn.getY());
            insert.setInt(5, spawn.getZ());
            insert.setInt(6, spawn.getHeading());
            insert.setInt(7, spawn.getRespawnDelay() / 1000);
            insert.setInt(8, spawn.getRespawnMaxDelay() - spawn.getRespawnMinDelay());
            insert.setInt(9, spawn.getLocationId());
            insert.execute();
         } catch (Exception var35) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not store spawn in the DB:" + var35.getMessage(), (Throwable)var35);
         }
      }
   }

   public void deleteSpawn(Spawner spawn, boolean updateDb) {
      if (this._spawntable.remove(spawn)) {
         if (spawn.getLocation() != null && updateDb) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement delete = con.prepareStatement("DELETE FROM spawnlist WHERE locx=? AND locy=? AND heading=? AND npc_templateid=?");
            ) {
               delete.setInt(1, spawn.getLocation().getX());
               delete.setInt(2, spawn.getLocation().getY());
               delete.setInt(3, spawn.getLocation().getHeading());
               delete.setInt(4, spawn.getId());
               delete.execute();
               _log.info(this.getClass().getSimpleName() + ": Deleted npcId - " + spawn.getId() + " from spawnlist.");
            } catch (Exception var35) {
               _log.log(
                  Level.WARNING,
                  this.getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from DB: " + var35.getMessage(),
                  (Throwable)var35
               );
            }
         }
      }
   }

   public void reloadAll() {
      this.fillSpawnTable();
   }

   public Collection<Spawner> getAllSpawns() {
      return this._spawntable;
   }

   public void findNPCInstances(Player activeChar, int npcId, int teleportIndex, boolean showposition) {
      int index = 0;

      for(Spawner spawn : this._spawntable) {
         if (npcId == spawn.getId()) {
            ++index;
            Npc _npc = spawn.getLastSpawn();
            if (teleportIndex > -1) {
               if (teleportIndex == index) {
                  if (showposition && _npc != null) {
                     activeChar.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true);
                  } else {
                     activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true);
                  }
               }
            } else if (showposition && _npc != null) {
               activeChar.sendMessage(
                  index + " - " + spawn.getTemplate().getName() + " (" + spawn + "): " + _npc.getX() + " " + _npc.getY() + " " + _npc.getZ()
               );
            } else {
               activeChar.sendMessage(
                  index + " - " + spawn.getTemplate().getName() + " (" + spawn + "): " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ()
               );
            }
         }
      }

      if (index == 0) {
         activeChar.sendMessage(this.getClass().getSimpleName() + ": No current spawns found.");
      }
   }

   public static SpawnHolder getInstance() {
      return SpawnHolder.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SpawnHolder _instance = new SpawnHolder();
   }
}
