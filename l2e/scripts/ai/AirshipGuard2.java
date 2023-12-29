package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class AirshipGuard2 extends Fighter {
   static final Location[] points = new Location[]{
      new Location(-148162, 255173, -180),
      new Location(-148242, 254842, -184),
      new Location(-148395, 254647, -184),
      new Location(-148607, 254347, -184),
      new Location(-148781, 254206, -184),
      new Location(-149090, 254012, -180),
      new Location(-148309, 255135, -181),
      new Location(-148357, 254894, -183),
      new Location(-148461, 254688, -183),
      new Location(-148643, 254495, -183),
      new Location(-148828, 254275, -183),
      new Location(-149093, 254183, -180)
   };
   private int current_point = -1;
   private long wait_timeout = 0L;
   private boolean wait = false;

   public AirshipGuard2(Attackable actor) {
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
