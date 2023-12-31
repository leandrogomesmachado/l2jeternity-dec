package l2e.scripts.instances;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.zone.ZoneType;

public class PailakaInjuredDragon extends AbstractReflection {
   private static final Map<Integer, int[]> NOEXIT_ZONES = new ConcurrentHashMap<>();

   public PailakaInjuredDragon(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32499);
      this.addTalkId(32499);

      for(int zoneid : NOEXIT_ZONES.keySet()) {
         this.addEnterZoneId(new int[]{zoneid});
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new PailakaInjuredDragon.PIDWorld(), 45)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Npc latana = addSpawn(18660, 105732, -41787, -1775, 35742, false, 0L, false, world.getReflectionId());
         if (latana != null) {
            latana.setIsImmobilized(true);
         }
      }
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("enter")) {
         this.enterInstance(player, npc);
         return null;
      } else {
         return super.onAdvEvent(event, npc, player);
      }
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character instanceof Player && !character.isDead() && !character.isTeleporting() && character.getActingPlayer().isOnline()) {
         ReflectionWorld world = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (world != null && world.getTemplateId() == 45) {
            int[] zoneTeleport = (int[])NOEXIT_ZONES.get(zone.getId());
            if (zoneTeleport != null) {
               for(Npc npcs : World.getInstance().getAroundNpc(character, 1200, 200)) {
                  if (!npcs.isDead()) {
                     character.getAI().setIntention(CtrlIntention.IDLE);
                     character.setReflectionId(world.getReflectionId());
                     character.teleToLocation(zoneTeleport[0], zoneTeleport[1], zoneTeleport[2], true);
                     break;
                  }
               }
            }
         }
      }

      return super.onEnterZone(character, zone);
   }

   public static void main(String[] args) {
      new PailakaInjuredDragon(PailakaInjuredDragon.class.getSimpleName(), "instances");
   }

   static {
      NOEXIT_ZONES.put(200001, new int[]{123167, -45743, -3023});
      NOEXIT_ZONES.put(200002, new int[]{117783, -46398, -2560});
      NOEXIT_ZONES.put(200003, new int[]{116791, -51556, -2584});
      NOEXIT_ZONES.put(200004, new int[]{117993, -52505, -2480});
      NOEXIT_ZONES.put(200005, new int[]{113226, -44080, -2776});
      NOEXIT_ZONES.put(200006, new int[]{107916, -46716, -2008});
      NOEXIT_ZONES.put(200007, new int[]{118341, -55951, -2280});
      NOEXIT_ZONES.put(200008, new int[]{110127, -41562, -2332});
   }

   private class PIDWorld extends ReflectionWorld {
      public PIDWorld() {
      }
   }
}
