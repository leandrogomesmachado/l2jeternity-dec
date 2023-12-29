package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;

public class TowerofInfinitum5Floor extends AbstractReflection {
   public TowerofInfinitum5Floor(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32375, 32748});
      this.addTalkId(new int[]{32375, 32748});
      this.addKillId(25540);
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
      this.enterInstance(player, npc, new TowerofInfinitum5Floor.TOI5World(), 142);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      switch(npc.getId()) {
         case 32375:
            ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (world != null && world instanceof TowerofInfinitum5Floor.TOI5World) {
               world.removeAllowed(player.getObjectId());
               this.teleportPlayer(player, new Location(-19024, 277122, -8256), 0);
            }
            break;
         case 32748:
            this.enterInstance(player, npc);
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (world != null && world instanceof TowerofInfinitum5Floor.TOI5World) {
         Reflection inst = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
         if (inst != null) {
            inst.setReturnLoc(new Location(-19024, 277122, -8256));
         }

         this.finishInstance(world, true);
         addSpawn(32375, -22144, 278744, -8256, 0, false, 0L, false, npc.getReflectionId());
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new TowerofInfinitum5Floor(TowerofInfinitum5Floor.class.getSimpleName(), "instances");
   }

   protected class TOI5World extends ReflectionWorld {
      public TOI5World() {
      }
   }
}
