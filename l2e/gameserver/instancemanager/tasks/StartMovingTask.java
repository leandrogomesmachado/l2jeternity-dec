package l2e.gameserver.instancemanager.tasks;

import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.actor.Npc;

public final class StartMovingTask implements Runnable {
   final Npc _npc;
   final String _routeName;

   public StartMovingTask(Npc npc, String routeName) {
      this._npc = npc;
      this._routeName = routeName;
   }

   @Override
   public void run() {
      if (this._npc != null) {
         WalkingManager.getInstance().startMoving(this._npc, this._routeName);
      }
   }
}
