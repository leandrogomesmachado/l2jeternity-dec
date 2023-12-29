package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ClassMasterInstance;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends GameClientPacket {
   private String _bypass;

   @Override
   protected void readImpl() {
      this._bypass = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         ClassMasterInstance.onTutorialLink(player, this._bypass);
         QuestState qs = player.getQuestState("_255_Tutorial");
         if (qs != null) {
            qs.getQuest().notifyEvent(this._bypass, null, player);
         }

         if (AchievementManager.getInstance().isActive() && this._bypass.startsWith("_bbs_achievements")) {
            this._bypass = this._bypass.replaceAll("%", " ");
            if (this._bypass.length() < 5) {
               return;
            }

            AchievementManager.getInstance().onBypass(player, this._bypass, null);
         }
      }
   }
}
