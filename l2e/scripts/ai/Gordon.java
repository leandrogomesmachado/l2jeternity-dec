package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class Gordon extends Fighter {
   private static final Location[] points = new Location[]{
      new Location(146268, -64651, -3412),
      new Location(143678, -64045, -3434),
      new Location(141620, -62316, -3210),
      new Location(139466, -60839, -2994),
      new Location(138429, -57679, -3548),
      new Location(139402, -55879, -3334),
      new Location(139660, -52780, -2908),
      new Location(139516, -50343, -2591),
      new Location(140059, -48657, -2271),
      new Location(140319, -46063, -2408),
      new Location(142462, -45540, -2432),
      new Location(144290, -43543, -2380),
      new Location(146494, -43234, -2325),
      new Location(148416, -43186, -2329),
      new Location(151135, -44084, -2746),
      new Location(153040, -42240, -2920),
      new Location(154871, -39193, -3294),
      new Location(156725, -41827, -3569),
      new Location(157788, -45071, -3598),
      new Location(159433, -45943, -3547),
      new Location(160327, -47404, -3681),
      new Location(159106, -48215, -3691),
      new Location(159541, -50908, -3563),
      new Location(159576, -53782, -3226),
      new Location(160918, -56899, -2790),
      new Location(160785, -59505, -2662),
      new Location(158252, -60098, -2680),
      new Location(155962, -59751, -2656),
      new Location(154649, -60214, -2701),
      new Location(153121, -63319, -2969),
      new Location(151511, -64366, -3174),
      new Location(149161, -64576, -3316),
      new Location(147316, -64797, -3440)
   };
   private int current_point = -1;
   private long wait_timeout = 0L;
   private boolean wait = false;

   public Gordon(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().setCanReturnToSpawnPoint(false);
      this.current_point = -1;
      this.wait_timeout = 0L;
      this.wait = false;
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target.isPlayer() && !((Player)target).isCursedWeaponEquipped()) {
         return false;
      } else {
         if (this.getIntention() == CtrlIntention.ATTACK && this.getActiveChar().isScriptValue(0) && this.current_point > -1) {
            this.getActiveChar().setScriptValue(1);
            --this.current_point;
         }

         return super.checkAggression(target);
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor.isScriptValue(0) && this.current_point > -1) {
         actor.setScriptValue(1);
         --this.current_point;
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else if (super.thinkActive()) {
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
         if (!this.wait && this.current_point == 31) {
            this.wait_timeout = System.currentTimeMillis() + 60000L;
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

         if (actor.getScriptValue() > 0) {
            actor.setScriptValue(0);
         }

         actor.setWalking();
         actor.getAI().setIntention(CtrlIntention.MOVING, loc);
      }
   }

   @Override
   protected void teleportHome() {
   }

   @Override
   protected void returnHome(boolean clearAggro, boolean teleport) {
   }
}
