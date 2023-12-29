package l2e.scripts.ai.fantasy_isle;

import l2e.gameserver.instancemanager.QuestManager;

public class StartMCShow implements Runnable {
   @Override
   public void run() {
      QuestManager.getInstance().getQuest("MC_Show").notifyEvent("Start", null, null);
   }
}
