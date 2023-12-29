package l2e.gameserver.model;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.NpcWalkerTemplate;
import l2e.gameserver.model.quest.Quest;

public class WalkInfo {
   private final String _routeName;
   private ScheduledFuture<?> _walkCheckTask;
   private boolean _blocked = false;
   private boolean _suspended = false;
   private boolean _stoppedByAttack = false;
   private int _currentNode = 0;
   private boolean _forward = true;
   private long _lastActionTime;

   public WalkInfo(String routeName) {
      this._routeName = routeName;
   }

   public WalkRoute getRoute() {
      return WalkingManager.getInstance().getRoute(this._routeName);
   }

   public NpcWalkerTemplate getCurrentNode() {
      return this.getRoute().getNodeList().get(this._currentNode);
   }

   public void calculateNextNode(Npc npc) {
      if (this.getRoute().getRepeatType() == 3) {
         int newNode = this._currentNode;

         while(newNode == this._currentNode) {
            newNode = Rnd.get(this.getRoute().getNodesCount());
         }

         this._currentNode = newNode;
         npc.sendDebugMessage("Route: " + this.getRoute().getName() + ", next random node is " + this._currentNode);
      } else {
         if (this._forward) {
            ++this._currentNode;
         } else {
            --this._currentNode;
         }

         if (this._currentNode == this.getRoute().getNodesCount()) {
            if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ROUTE_FINISHED) != null) {
               for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ROUTE_FINISHED)) {
                  quest.notifyRouteFinished(npc);
               }
            }

            npc.sendDebugMessage("Route: " + this.getRoute().getName() + ", last node arrived");
            if (!this.getRoute().repeatWalk()) {
               WalkingManager.getInstance().cancelMoving(npc);
               return;
            }

            switch(this.getRoute().getRepeatType()) {
               case 0:
                  this._forward = false;
                  this._currentNode -= 2;
                  break;
               case 1:
                  this._currentNode = 0;
                  break;
               case 2:
                  npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), true);
                  this._currentNode = 0;
            }
         } else if (this._currentNode == -1) {
            this._currentNode = 1;
            this._forward = true;
         }
      }
   }

   public boolean isBlocked() {
      return this._blocked;
   }

   public void setBlocked(boolean val) {
      this._blocked = val;
   }

   public boolean isSuspended() {
      return this._suspended;
   }

   public void setSuspended(boolean val) {
      this._suspended = val;
   }

   public boolean isStoppedByAttack() {
      return this._stoppedByAttack;
   }

   public void setStoppedByAttack(boolean val) {
      this._stoppedByAttack = val;
   }

   public int getCurrentNodeId() {
      return this._currentNode;
   }

   public long getLastAction() {
      return this._lastActionTime;
   }

   public void setLastAction(long val) {
      this._lastActionTime = val;
   }

   public ScheduledFuture<?> getWalkCheckTask() {
      return this._walkCheckTask;
   }

   public void setWalkCheckTask(ScheduledFuture<?> val) {
      this._walkCheckTask = val;
   }
}
