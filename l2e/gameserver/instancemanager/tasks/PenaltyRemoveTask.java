package l2e.gameserver.instancemanager.tasks;

import l2e.gameserver.instancemanager.HandysBlockCheckerManager;

public final class PenaltyRemoveTask implements Runnable {
   private final int _objectId;

   public PenaltyRemoveTask(int id) {
      this._objectId = id;
   }

   @Override
   public void run() {
      HandysBlockCheckerManager.getInstance().removePenalty(this._objectId);
   }
}
