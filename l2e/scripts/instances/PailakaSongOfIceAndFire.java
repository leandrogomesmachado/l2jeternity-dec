package l2e.scripts.instances;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.zone.ZoneType;

public class PailakaSongOfIceAndFire extends AbstractReflection {
   public PailakaSongOfIceAndFire(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32497);
      this.addTalkId(32497);
      this.addExitZoneId(20108);
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
      this.enterInstance(player, npc, new PailakaSongOfIceAndFire.PSFWorld(), 43);
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
   public String onExitZone(Creature character, ZoneType zone) {
      if (character.isPlayer() && !character.isDead() && !character.isTeleporting() && ((Player)character).isOnline()) {
         ReflectionWorld world = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (world != null && world.getTemplateId() == 43) {
            ThreadPoolManager.getInstance().schedule(new PailakaSongOfIceAndFire.Teleport(character.getActingPlayer(), world.getReflectionId()), 1000L);
         }
      }

      return super.onExitZone(character, zone);
   }

   public static void main(String[] args) {
      new PailakaSongOfIceAndFire(PailakaSongOfIceAndFire.class.getSimpleName(), "instances");
   }

   private class PSFWorld extends ReflectionWorld {
      public PSFWorld() {
      }
   }

   private static final class Teleport implements Runnable {
      private final Player _player;
      private final int _instanceId;

      public Teleport(Player c, int id) {
         this._player = c;
         this._instanceId = id;
      }

      @Override
      public void run() {
         if (this._player != null) {
            this._player.getAI().setIntention(CtrlIntention.IDLE);
            this._player.setReflectionId(this._instanceId);
            this._player.teleToLocation(-52875, 188232, -4696, true);
         }
      }
   }
}
