package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.instance.FortBallistaInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.spawn.Spawner;

public final class FortSiegeGuardManager {
   private static final Logger _log = Logger.getLogger(FortSiegeGuardManager.class.getName());
   private final Fort _fort;
   private final Map<Integer, List<Spawner>> _siegeGuards = new HashMap<>();

   public FortSiegeGuardManager(Fort fort) {
      this._fort = fort;
   }

   public void spawnSiegeGuard() {
      try {
         List<Spawner> monsterList = this._siegeGuards.get(this.getFort().getId());
         if (monsterList != null) {
            for(Spawner spawnDat : monsterList) {
               spawnDat.doSpawn();
               if (spawnDat.getLastSpawn() instanceof FortBallistaInstance) {
                  spawnDat.stopRespawn();
               } else {
                  spawnDat.startRespawn();
               }
            }
         }
      } catch (Exception var4) {
         _log.log(Level.WARNING, "Error spawning siege guards for fort " + this.getFort().getName() + ":" + var4.getMessage(), (Throwable)var4);
      }
   }

   public void unspawnSiegeGuard() {
      try {
         List<Spawner> monsterList = this._siegeGuards.get(this.getFort().getId());
         if (monsterList != null) {
            for(Spawner spawnDat : monsterList) {
               spawnDat.stopRespawn();
               if (spawnDat.getLastSpawn() != null) {
                  spawnDat.getLastSpawn().doDie(spawnDat.getLastSpawn());
               }
            }
         }
      } catch (Exception var4) {
         _log.log(Level.WARNING, "Error unspawning siege guards for fort " + this.getFort().getName() + ":" + var4.getMessage(), (Throwable)var4);
      }
   }

   void loadSiegeGuard() {
      this._siegeGuards.clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT npcId, x, y, z, heading, respawnDelay FROM fort_siege_guards WHERE fortId = ?");
      ) {
         int fortId = this.getFort().getId();
         ps.setInt(1, fortId);

         try (ResultSet rs = ps.executeQuery()) {
            List<Spawner> siegeGuardSpawns = new ArrayList<>();

            while(rs.next()) {
               NpcTemplate template = NpcsParser.getInstance().getTemplate(rs.getInt("npcId"));
               if (template != null) {
                  Spawner spawn = new Spawner(template);
                  spawn.setAmount(1);
                  spawn.setX(rs.getInt("x"));
                  spawn.setY(rs.getInt("y"));
                  spawn.setZ(rs.getInt("z"));
                  spawn.setHeading(rs.getInt("heading"));
                  spawn.setRespawnDelay(rs.getInt("respawnDelay"));
                  spawn.setLocationId(0);
                  siegeGuardSpawns.add(spawn);
               }
            }

            this._siegeGuards.put(fortId, siegeGuardSpawns);
         }
      } catch (Exception var62) {
         _log.log(Level.WARNING, "Error loading siege guard for fort " + this.getFort().getName() + ": " + var62.getMessage(), (Throwable)var62);
      }
   }

   public final Fort getFort() {
      return this._fort;
   }

   public final Map<Integer, List<Spawner>> getSiegeGuardSpawn() {
      return this._siegeGuards;
   }
}
