package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class AirshipGuard1 extends Fighter {
   static final Location[] points = new Location[]{
      new Location(-149633, 254016, -180),
      new Location(-149874, 254224, -184),
      new Location(-150088, 254429, -184),
      new Location(-150229, 254603, -184),
      new Location(-150368, 254822, -184),
      new Location(-150570, 255125, -184),
      new Location(-149649, 254189, -180),
      new Location(-149819, 254291, -184),
      new Location(-150038, 254487, -184),
      new Location(-150182, 254654, -184),
      new Location(-150301, 254855, -184),
      new Location(-150438, 255133, -181)
   };
   private int current_point = -1;
   private long wait_timeout = 0L;
   private boolean wait = false;

   public AirshipGuard1(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else {
         if (this.current_point >= 0 && this.current_point < points.length) {
            Location loc = points[this.current_point];
            if (!Util.checkIfInRange(80, loc.getX(), loc.getY(), loc.getZ(), actor, false)) {
               actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, actor.getGeoIndex(), true));
               return true;
            }

            this.startMoveTask();
         } else {
            this.startMoveTask();
         }

         return false;
      }
   }

   private void startMoveTask() {
      Attackable actor = this.getActiveChar();
      if (System.currentTimeMillis() > this.wait_timeout && (this.current_point > -1 || Rnd.chance(5))) {
         if (!this.wait && (this.current_point == 0 || this.current_point == 8)) {
            this.wait_timeout = System.currentTimeMillis() + (long)Rnd.get(0, 30000);
            this.wait = true;
            return;
         }

         this.wait_timeout = 0L;
         this.wait = false;
         ++this.current_point;
         if (this.current_point >= points.length) {
            this.current_point = 0;
         }

         Location loc = points[this.current_point];
         if (loc == null) {
            this.current_point = 0;
            loc = points[this.current_point];
         }

         actor.setWalking();
         actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(actor, loc, 0, 100, true));
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }
}
