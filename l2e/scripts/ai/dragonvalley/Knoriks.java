package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Knoriks extends Fighter {
   private Location[] _points = null;
   private final Location[] _points1 = new Location[]{
      new Location(141848, 121592, -3912),
      new Location(140440, 120264, -3912),
      new Location(140664, 118328, -3912),
      new Location(142104, 117400, -3912),
      new Location(142968, 117816, -3912),
      new Location(142648, 119672, -3912),
      new Location(143864, 121016, -3896),
      new Location(144504, 119320, -3896),
      new Location(145448, 117624, -3912),
      new Location(146824, 118328, -3984),
      new Location(147080, 119320, -4288),
      new Location(147432, 121224, -4768),
      new Location(148568, 120936, -4864),
      new Location(149640, 119480, -4864),
      new Location(150616, 118312, -4936),
      new Location(152936, 116664, -5256),
      new Location(153208, 115224, -5256),
      new Location(151656, 115080, -5472),
      new Location(148824, 114888, -5472),
      new Location(151128, 114520, -5464),
      new Location(152072, 114152, -5520),
      new Location(153320, 112728, -5520),
      new Location(153096, 111800, -5520),
      new Location(150504, 111256, -5520),
      new Location(149512, 111080, -5488),
      new Location(149304, 109672, -5216),
      new Location(151864, 109368, -5152),
      new Location(153320, 109032, -5152),
      new Location(153048, 108040, -5152),
      new Location(150888, 107320, -4800),
      new Location(149320, 108456, -4424),
      new Location(147704, 107256, -4048),
      new Location(146648, 108376, -3664),
      new Location(146408, 110200, -3472),
      new Location(146568, 111784, -3552),
      new Location(147896, 112584, -3720),
      new Location(148904, 113208, -3720),
      new Location(149256, 114824, -3720),
      new Location(149688, 116344, -3704),
      new Location(150680, 117880, -3688),
      new Location(152056, 118968, -3808),
      new Location(152696, 120040, -3808),
      new Location(151928, 121352, -3808),
      new Location(152856, 121752, -3808),
      new Location(154440, 121208, -3808)
   };
   private final Location[] _points2 = new Location[]{
      new Location(145452, 115969, -3760),
      new Location(144630, 115316, -3760),
      new Location(145136, 114851, -3760),
      new Location(146549, 116126, -3760),
      new Location(146421, 116429, -3760)
   };
   private final Location[] _points3 = new Location[]{
      new Location(140456, 117832, -3942),
      new Location(142632, 117336, -3942),
      new Location(142680, 118680, -3942),
      new Location(141864, 119240, -3942),
      new Location(140856, 118904, -3942)
   };
   private final Location[] _points4 = new Location[]{
      new Location(140904, 108856, -3764), new Location(140648, 112360, -3750), new Location(142856, 111768, -3974), new Location(142216, 109432, -3966)
   };
   private final Location[] _points5 = new Location[]{
      new Location(147960, 110216, -3974),
      new Location(146072, 109400, -3974),
      new Location(145576, 110856, -3974),
      new Location(144504, 107768, -3974),
      new Location(145864, 109224, -3974)
   };
   private final Location[] _points6 = new Location[]{
      new Location(154040, 118696, -3834),
      new Location(152600, 119992, -3834),
      new Location(151816, 121480, -3834),
      new Location(152808, 121960, -3834),
      new Location(153768, 121480, -3834),
      new Location(152136, 121672, -3834),
      new Location(152248, 120200, -3834)
   };
   private int _lastPoint = 0;
   private boolean _firstThought = true;
   private boolean _isRecycle = false;

   public Knoriks(Attackable actor) {
      super(actor);
      this.MAX_PURSUE_RANGE = 2147483637;
      actor.setIsRunner(true);
      actor.setCanReturnToSpawnPoint(false);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable npc = this.getActiveChar();
      if (npc.getSpawnedLoc().getX() == 141848 && npc.getSpawnedLoc().getY() == 121592) {
         this._points = this._points1;
      } else if (npc.getSpawnedLoc().getX() == 145452 && npc.getSpawnedLoc().getY() == 115969) {
         this._points = this._points2;
      } else if (npc.getSpawnedLoc().getX() == 140456 && npc.getSpawnedLoc().getY() == 117832) {
         this._points = this._points3;
      } else if (npc.getSpawnedLoc().getX() == 140904 && npc.getSpawnedLoc().getY() == 108856) {
         this._points = this._points4;
      } else if (npc.getSpawnedLoc().getX() == 147960 && npc.getSpawnedLoc().getY() == 110216) {
         this._points = this._points5;
      } else if (npc.getSpawnedLoc().getX() == 154040 && npc.getSpawnedLoc().getY() == 118696) {
         this._points = this._points6;
      }

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
               npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, npc.getGeoIndex(), true));
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
      if (!actor.isDead()) {
         if (actor.isScriptValue(0) && this._lastPoint > 0) {
            actor.setScriptValue(1);
            if (this._isRecycle) {
               ++this._lastPoint;
            } else {
               --this._lastPoint;
            }
         }

         if (Rnd.chance(5) && attacker != null && !actor.isOutOfControl() && !actor.isActionsDisabled()) {
            actor.setTarget(actor);
            actor.doCast(SkillsParser.getInstance().getInfo(6744, 1));
         } else {
            super.onEvtAttacked(attacker, damage);
         }
      }
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
         this._isRecycle = true;
         --this._lastPoint;
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
   }
}
