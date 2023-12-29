package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import org.apache.commons.lang3.ArrayUtils;

public class Patrollers extends Fighter {
   protected Location[] _points = null;
   private final int[] _recycles = new int[]{22833, 22834, 22835};
   private int _lastPoint = 0;
   private boolean _firstThought = true;
   private boolean _isRecycle = false;

   public Patrollers(Attackable actor) {
      super(actor);
      this.MAX_PURSUE_RANGE = 2147483637;
      actor.setIsRunner(true);
      actor.setCanReturnToSpawnPoint(false);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this._lastPoint = 0;
      this._firstThought = true;
      this._isRecycle = false;
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable npc = this.getActiveChar();
      if (npc.isDead()) {
         return true;
      } else if (!super.thinkActive() && this._points != null) {
         if (!this._firstThought && this._lastPoint < this._points.length && this._lastPoint >= 0) {
            Location loc = this._points[this._lastPoint];
            if (Util.checkIfInRange(80, loc.getX(), loc.getY(), loc.getZ(), npc, false)) {
               this.startMoveTask();
            } else {
               if (!npc.isRunning()) {
                  npc.setRunning();
               }

               npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, npc.getGeoIndex(), true));
               if (!npc.isMoving()) {
                  ++this._lastPoint;
               }
            }
         } else {
            this.startMoveTask();
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor.isScriptValue(0) && this._lastPoint > 0) {
         actor.setScriptValue(1);
         if (this._isRecycle) {
            ++this._lastPoint;
         } else {
            --this._lastPoint;
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   private void startMoveTask() {
      Attackable npc = this.getActiveChar();
      if (this._firstThought) {
         this._lastPoint = this.getIndex(Location.findNearest(npc, this._points));
         this._firstThought = false;
      } else if (this._isRecycle) {
         --this._lastPoint;
      } else {
         ++this._lastPoint;
      }

      if (this._isRecycle && this._lastPoint <= 0) {
         this._lastPoint = 0;
         this._isRecycle = false;
      }

      if (this._lastPoint >= this._points.length && !this._isRecycle) {
         if (ArrayUtils.contains(this._recycles, npc.getId())) {
            this._isRecycle = true;
            --this._lastPoint;
         } else {
            this._lastPoint = 0;
         }
      }

      if (npc.getScriptValue() > 0) {
         npc.setScriptValue(0);
      }

      npc.setRunning();
      if (Rnd.chance(5)) {
         npc.makeTriggerCast(SkillsParser.getInstance().getInfo(6757, 1), npc);
      }

      Location loc = null;

      try {
         loc = this._points[this._lastPoint];
      } catch (Exception var4) {
      }

      if (loc == null) {
         if (this._isRecycle) {
            this._lastPoint = this._points.length - 1;
         } else {
            this._lastPoint = 0;
         }

         loc = this._points[this._lastPoint];
      }

      if (loc != null) {
         npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, npc.getGeoIndex(), true));
      }
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
   protected void teleportHome() {
   }

   @Override
   protected void returnHome(boolean clearAggro, boolean teleport) {
      super.returnHome(clearAggro, teleport);
      this._firstThought = true;
      this.startMoveTask();
   }
}
