package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;

public class SecretArea extends AbstractReflection {
   public SecretArea() {
      super(SecretArea.class.getSimpleName(), "instances");
      this.addStartNpc(new int[]{32566, 32567});
      this.addTalkId(new int[]{32566, 32567});
   }

   private final synchronized void enterInstance(Player player, Npc npc, int templateId) {
      this.enterInstance(player, npc, new SecretArea.SAWorld(), templateId);
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

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("enter_117")) {
         this.enterInstance(player, npc, 117);
      } else if (event.equalsIgnoreCase("enter_118")) {
         this.enterInstance(player, npc, 118);
      } else if (event.equalsIgnoreCase("exit")) {
         this.teleportPlayer(player, new Location(-185057, 242821, 1576), 0);
      }

      return super.onAdvEvent(event, npc, player);
   }

   public static void main(String[] args) {
      new SecretArea();
   }

   private class SAWorld extends ReflectionWorld {
      public SAWorld() {
      }
   }
}
