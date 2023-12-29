package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;

public class TowerofInfinitum10Floor extends AbstractReflection {
   public TowerofInfinitum10Floor(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32374, 32752});
      this.addTalkId(new int[]{32374, 32752});
      this.addKillId(25542);
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
      this.enterInstance(player, npc, new TowerofInfinitum10Floor.TOI10World(), 143);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      switch(npc.getId()) {
         case 32374:
            ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (world != null && world instanceof TowerofInfinitum10Floor.TOI10World) {
               world.removeAllowed(player.getObjectId());
               this.teleportPlayer(player, new Location(-19008, 277122, -13376), 0);
            }
            break;
         case 32752:
            this.enterInstance(player, npc);
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (world != null && world instanceof TowerofInfinitum10Floor.TOI10World) {
         Reflection inst = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
         if (inst != null) {
            inst.setReturnLoc(new Location(-19008, 277122, -13376));
         }

         this.finishInstance(world, true);
         addSpawn(32374, -19056, 278732, -15040, 0, false, 0L, false, npc.getReflectionId());
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new TowerofInfinitum10Floor(TowerofInfinitum10Floor.class.getSimpleName(), "instances");
   }

   protected class TOI10World extends ReflectionWorld {
      public TOI10World() {
      }
   }
}
