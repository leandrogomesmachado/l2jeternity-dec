package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.RevengeManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ClassMasterInstance;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.quest.QuestState;

public class RequestTutorialQuestionMarkPressed extends GameClientPacket {
   private int _number = 0;

   @Override
   protected void readImpl() {
      this._number = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.isntAfk();
         if (player.isInFightEvent()) {
            FightEventManager.getInstance().sendEventPlayerMenu(player);
         } else if (this._number == 1002) {
            RevengeManager.getInstance().getRevengeList(player);
         } else {
            ClassMasterInstance.onTutorialQuestionMark(player, this._number);
            QuestState qs = player.getQuestState("_255_Tutorial");
            if (qs != null) {
               qs.getQuest().notifyEvent("QM" + this._number + "", null, player);
            }

            if (AchievementManager.getInstance().isActive() && this._number == player.getObjectId()) {
               AchievementManager.getInstance().onBypass(player, "_bbs_achievements", null);
            }
         }
      }
   }
}
