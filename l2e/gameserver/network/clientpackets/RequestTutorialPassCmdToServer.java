package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.RevengeManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.quest.QuestState;

public class RequestTutorialPassCmdToServer extends GameClientPacket {
   private String _bypass = null;

   @Override
   protected void readImpl() {
      this._bypass = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.isntAfk();
         if (player.isInFightEvent()) {
            FightEventManager.getInstance().requestEventPlayerMenuBypass(player, this._bypass);
         } else if (player.isRevengeActive()) {
            RevengeManager.getInstance().requestPlayerMenuBypass(player, this._bypass);
         } else if (AchievementManager.getInstance().isActive() && this._bypass.startsWith("_bbs_achievements")) {
            String[] cm = this._bypass.split(" ");
            if (this._bypass.startsWith("_bbs_achievements_cat")) {
               int page = 0;
               if (cm.length < 1) {
                  page = 1;
               } else {
                  page = Integer.parseInt(cm[2]);
               }

               AchievementManager.getInstance().generatePage(player, Integer.parseInt(cm[1]), page);
            } else {
               AchievementManager.getInstance().onBypass(player, this._bypass, cm);
            }
         } else {
            QuestState qs = player.getQuestState("_255_Tutorial");
            if (qs != null) {
               qs.getQuest().notifyEvent(this._bypass, null, player);
            }
         }
      }
   }
}
