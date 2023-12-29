package l2e.commons.util;

import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class NpcUtils {
   public static MonsterInstance spawnSingle(int npcId, int x, int y, int z) {
      return spawnSingle(npcId, new Location(x, y, z, -1));
   }

   public static Npc spawnSingleNpc(int npcId, int x, int y, int z) {
      return spawnSingleNpc(npcId, new Location(x, y, z, -1));
   }

   public static MonsterInstance spawnSingle(int npcId, int x, int y, int z, int h) {
      return spawnSingle(npcId, new Location(x, y, z, h));
   }

   public static Npc spawnSingleNpc(int npcId, int x, int y, int z, int h) {
      return spawnSingleNpc(npcId, new Location(x, y, z, h));
   }

   public static MonsterInstance spawnSingle(int npcId, Location loc) {
      return spawnSingle(npcId, loc, 0, 0L);
   }

   public static Npc spawnSingleNpc(int npcId, Location loc) {
      return spawnSingleNpc(npcId, loc, 0, 0L);
   }

   public static MonsterInstance spawnSingle(int npcId, Location loc, long despawnTime) {
      return spawnSingle(npcId, loc, 0, despawnTime);
   }

   public static Npc spawnSingleNpc(int npcId, Location loc, long despawnTime) {
      return spawnSingleNpc(npcId, loc, 0, despawnTime);
   }

   public static MonsterInstance spawnSingle(int npcId, Location loc, int reflection, long despawnTime) {
      NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
      if (template == null) {
         throw new NullPointerException("Npc template id : " + npcId + " not found!");
      } else if (loc == null) {
         return null;
      } else {
         MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
         npc.setHeading(loc.getHeading() < 0 ? Rnd.get(65535) : loc.getHeading());
         npc.setLocation(loc);
         npc.setReflectionId(reflection);
         npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
         npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
         if (despawnTime > 0L) {
            npc.scheduleDespawn(despawnTime);
         }

         return npc;
      }
   }

   public static Npc spawnSingleNpc(int npcId, Location loc, int reflection, long despawnTime) {
      NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
      if (template == null) {
         throw new NullPointerException("Npc template id : " + npcId + " not found!");
      } else if (loc == null) {
         return null;
      } else {
         Npc npc = new Npc(IdFactory.getInstance().getNextId(), template);
         npc.setHeading(loc.getHeading() < 0 ? Rnd.get(65535) : loc.getHeading());
         npc.setLocation(loc);
         npc.setReflectionId(reflection);
         npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
         npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
         if (despawnTime > 0L) {
            npc.scheduleDespawn(despawnTime);
         }

         return npc;
      }
   }
}
