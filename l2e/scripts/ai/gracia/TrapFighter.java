package l2e.scripts.ai.gracia;

import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;

public class TrapFighter extends Fighter {
   private static final Location[] points1 = new Location[]{
      new Location(-251368, 217864, -12332),
      new Location(-251400, 216216, -12253),
      new Location(-251432, 214536, -21087),
      new Location(-251448, 211672, -11986),
      new Location(-250408, 211656, -11859),
      new Location(-250408, 210008, -11956),
      new Location(-250408, 208632, -11956),
      new Location(-250408, 207480, -11952)
   };
   private static final Location[] points2 = new Location[]{
      new Location(-250408, 210008, -11956), new Location(-250408, 208632, -11956), new Location(-250408, 207480, -11952)
   };
   private Location[] _points = null;
   private int current_point = -1;

   public TrapFighter(Attackable actor) {
      super(actor);
      this.MAX_PURSUE_RANGE = 2147483637;
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      if (this.getActiveChar().getReflectionId() > 0) {
         ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
         if (instance != null && instance.getAllowed() != null && instance.getStatus() > 8) {
            this.getActiveChar().setWatchDistance(4000);
            this.aggroPlayers();
         }
      } else {
         this.getActiveChar().setCanReturnToSpawnPoint(false);
         int stage = SoDDefenceStage.getDefenceStage();
         if (stage != 0) {
            if (stage < 3) {
               this._points = points1;
            } else {
               this._points = points2;
            }
         }
      }

      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else {
         if (this.getActiveChar().getReflectionId() > 0) {
            ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
            if (instance != null && instance.getAllowed() != null && instance.getStatus() > 8) {
               this.getActiveChar().setWatchDistance(4000);
               this.aggroPlayers();
               return true;
            }
         } else {
            int stage = SoDDefenceStage.getDefenceStage();
            if (stage == 0) {
               this.getActiveChar().deleteMe();
               return true;
            }

            if (this.aggroToController()) {
               return true;
            }

            if (this.current_point >= 0 && this.current_point < this._points.length) {
               Location loc = this._points[this.current_point];
               if (!Util.checkIfInRange(60, loc.getX(), loc.getY(), loc.getZ(), actor, false)) {
                  actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(loc, 40, actor.getGeoIndex(), true));
                  return true;
               }

               this.startMoveTask();
            } else {
               this.startMoveTask();
            }
         }

         return super.thinkActive();
      }
   }

   private void startMoveTask() {
      Attackable actor = this.getActiveChar();
      if (this._points != null) {
         ++this.current_point;
         if (this.current_point < this._points.length || !this.aggroToController()) {
            Location loc = this._points[this.current_point];
            if (loc == null) {
               this.current_point = this._points.length - 1;
               loc = this._points[this.current_point];
            }

            if (!actor.isRunning()) {
               actor.setRunning();
            }

            actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(actor, loc, 0, 80, true));
         }
      }
   }

   private boolean aggroToController() {
      for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar(), 4000, 400)) {
         if (npc.getId() == 18775 && !npc.isDead()) {
            this.getActiveChar().setTarget(npc);
            this.setAttackTarget(npc);
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(100000));
            return true;
         }
      }

      return false;
   }

   private void aggroPlayers() {
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         boolean found = false;

         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null) {
               this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(1000));
               found = true;
            }
         }

         if (!found) {
            this.getActiveChar().getAI().setIntention(CtrlIntention.MOVING, new Location(-250403, 207273, -11952, 16384));
         }
      }
   }

   @Override
   public boolean checkAggression(Creature target) {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else {
         return this.getActiveChar().getReflectionId() == 0 && target != null && target.isPlayer() ? false : super.checkAggression(target);
      }
   }

   @Override
   protected void returnHome(boolean clearAggro, boolean teleport) {
   }

   @Override
   protected void teleportHome() {
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (this.getActiveChar().getReflectionId() != 0 || attacker == null || !attacker.isPlayer()) {
         super.onEvtAttacked(attacker, damage);
      }
   }
}
