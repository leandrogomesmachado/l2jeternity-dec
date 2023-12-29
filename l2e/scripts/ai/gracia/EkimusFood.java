package l2e.scripts.ai.gracia;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class EkimusFood extends DefaultAI {
   private static final Location[] _route1 = new Location[]{
      new Location(-179544, 207400, -15496),
      new Location(-178856, 207464, -15496),
      new Location(-178168, 207864, -15496),
      new Location(-177512, 208728, -15496),
      new Location(-177336, 209528, -15496),
      new Location(-177448, 210328, -15496),
      new Location(-177864, 211048, -15496),
      new Location(-178584, 211608, -15496),
      new Location(-179304, 211848, -15496),
      new Location(-179512, 211864, -15496),
      new Location(-179528, 211448, -15472)
   };
   private static final Location[] _route2 = new Location[]{
      new Location(-179576, 207352, -15496),
      new Location(-180440, 207544, -15496),
      new Location(-181256, 208152, -15496),
      new Location(-181752, 209112, -15496),
      new Location(-181720, 210264, -15496),
      new Location(-181096, 211224, -15496),
      new Location(-180264, 211720, -15496),
      new Location(-179528, 211848, -15496),
      new Location(-179528, 211400, -15472)
   };
   private final Location[] _points;
   private boolean _firstThought = true;
   private int _lastPoint = 0;

   public EkimusFood(Attackable actor) {
      super(actor);
      actor.setIsEkimusFood(true);
      ((MonsterInstance)actor).setPassive(true);
      this.MAX_PURSUE_RANGE = 2147483637;
      this._points = Rnd.chance(50) ? _route1 : _route2;
   }

   @Override
   public boolean checkAggression(Creature target) {
      return false;
   }

   @Override
   protected boolean thinkActive() {
      Attackable npc = this.getActiveChar();
      if (npc.isDead()) {
         return true;
      } else if (npc.isMoving()) {
         return true;
      } else {
         if (this._firstThought) {
            this.startMoveTask();
         } else {
            Location loc = this._points[this._lastPoint];
            if (Util.checkIfInRange(80, loc.getX(), loc.getY(), loc.getZ(), npc, false)) {
               this.startMoveTask();
            } else {
               npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, npc.getGeoIndex(), true));
               if (!npc.isMoving()) {
                  ++this._lastPoint;
               }
            }
         }

         return true;
      }
   }

   private void startMoveTask() {
      Attackable npc = this.getActiveChar();
      if (this._firstThought) {
         this._lastPoint = this.getIndex(Location.findNearest(npc, this._points));
         this._firstThought = false;
      } else {
         ++this._lastPoint;
      }

      if (this._lastPoint >= this._points.length) {
         this._lastPoint = this._points.length - 1;
      }

      Location loc = this._points[this._lastPoint];
      if (loc == null) {
         this._lastPoint = this._points.length - 1;
         loc = this._points[this._lastPoint];
      }

      npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, npc.getGeoIndex(), true));
   }

   private int getIndex(Location loc) {
      for(int i = 0; i < this._points.length; ++i) {
         if (this._points[i] == loc) {
            return i;
         }
      }

      return 0;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void returnHome(boolean clearAggro, boolean teleport) {
   }

   @Override
   protected void teleportHome() {
   }
}
