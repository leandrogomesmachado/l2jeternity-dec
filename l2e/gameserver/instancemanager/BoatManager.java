package l2e.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class BoatManager {
   private static final Logger _log = Logger.getLogger(BoatManager.class.getName());
   private final Map<Integer, BoatInstance> _boats = new ConcurrentHashMap<>();
   private final boolean[] _docksBusy = new boolean[3];
   public static final int TALKING_ISLAND = 1;
   public static final int GLUDIN_HARBOR = 2;
   public static final int RUNE_HARBOR = 3;

   public static final BoatManager getInstance() {
      return BoatManager.SingletonHolder._instance;
   }

   protected BoatManager() {
      for(int i = 0; i < this._docksBusy.length; ++i) {
         this._docksBusy[i] = false;
      }

      if (Config.ALLOW_BOAT) {
         _log.info(this.getClass().getSimpleName() + ": Loaded all ship functions.");
      }
   }

   public BoatInstance getNewBoat(int boatId, int x, int y, int z, int heading) {
      if (!Config.ALLOW_BOAT) {
         return null;
      } else {
         StatsSet npcDat = new StatsSet();
         npcDat.set("npcId", boatId);
         npcDat.set("level", 0);
         npcDat.set("jClass", "boat");
         npcDat.set("baseSTR", 0);
         npcDat.set("baseCON", 0);
         npcDat.set("baseDEX", 0);
         npcDat.set("baseINT", 0);
         npcDat.set("baseWIT", 0);
         npcDat.set("baseMEN", 0);
         npcDat.set("baseShldDef", 0);
         npcDat.set("baseShldRate", 0);
         npcDat.set("baseAccCombat", 38);
         npcDat.set("baseEvasRate", 38);
         npcDat.set("baseCritRate", 38);
         npcDat.set("collision_radius", 0);
         npcDat.set("collision_height", 0);
         npcDat.set("sex", "male");
         npcDat.set("type", "");
         npcDat.set("baseAtkRange", 0);
         npcDat.set("baseMpMax", 0);
         npcDat.set("baseCpMax", 0);
         npcDat.set("rewardExp", 0);
         npcDat.set("rewardSp", 0);
         npcDat.set("basePAtk", 0);
         npcDat.set("baseMAtk", 0);
         npcDat.set("basePAtkSpd", 0);
         npcDat.set("aggroRange", 0);
         npcDat.set("baseMAtkSpd", 0);
         npcDat.set("rhand", 0);
         npcDat.set("lhand", 0);
         npcDat.set("armor", 0);
         npcDat.set("baseWalkSpd", 0);
         npcDat.set("baseRunSpd", 0);
         npcDat.set("baseHpMax", 50000);
         npcDat.set("baseHpReg", 3.0);
         npcDat.set("baseMpReg", 3.0);
         npcDat.set("basePDef", 100);
         npcDat.set("baseMDef", 100);
         CharTemplate template = new CharTemplate(npcDat);
         BoatInstance boat = new BoatInstance(IdFactory.getInstance().getNextId(), template);
         this._boats.put(boat.getObjectId(), boat);
         boat.setHeading(heading);
         boat.setXYZInvisible(x, y, z);
         boat.spawnMe();
         return boat;
      }
   }

   public BoatInstance getBoat(int boatId) {
      return this._boats.get(boatId);
   }

   public void dockShip(int h, boolean value) {
      try {
         this._docksBusy[h] = value;
      } catch (ArrayIndexOutOfBoundsException var4) {
      }
   }

   public boolean dockBusy(int h) {
      try {
         return this._docksBusy[h];
      } catch (ArrayIndexOutOfBoundsException var3) {
         return false;
      }
   }

   public void broadcastPacket(VehicleTemplate point1, VehicleTemplate point2, GameServerPacket packet) {
      this.broadcastPacketsToPlayers(point1, point2, packet);
   }

   public void broadcastPackets(VehicleTemplate point1, VehicleTemplate point2, GameServerPacket... packets) {
      this.broadcastPacketsToPlayers(point1, point2, packets);
   }

   private void broadcastPacketsToPlayers(VehicleTemplate point1, VehicleTemplate point2, GameServerPacket... packets) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            double dx = (double)player.getX() - (double)point1.getX();
            double dy = (double)player.getY() - (double)point1.getY();
            if (Math.sqrt(dx * dx + dy * dy) < (double)Config.BOAT_BROADCAST_RADIUS) {
               for(GameServerPacket p : packets) {
                  player.sendPacket(p);
               }
            } else {
               dx = (double)player.getX() - (double)point2.getX();
               dy = (double)player.getY() - (double)point2.getY();
               if (Math.sqrt(dx * dx + dy * dy) < (double)Config.BOAT_BROADCAST_RADIUS) {
                  for(GameServerPacket p : packets) {
                     player.sendPacket(p);
                  }
               }
            }
         }
      }
   }

   private static class SingletonHolder {
      protected static final BoatManager _instance = new BoatManager();
   }
}
