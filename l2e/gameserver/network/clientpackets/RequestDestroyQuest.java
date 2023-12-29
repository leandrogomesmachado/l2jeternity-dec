package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.QuestList;

public final class RequestDestroyQuest extends GameClientPacket {
   private int _questId;

   @Override
   protected void readImpl() {
      this._questId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Quest qe = QuestManager.getInstance().getQuest(this._questId);
         if (qe != null) {
            QuestState qs = activeChar.getQuestState(qe.getName());
            if (qs != null) {
               qs.exitQuest(true);
               activeChar.sendPacket(new QuestList(activeChar));
            } else if (Config.DEBUG) {
               _log.info("Player '" + activeChar.getName() + "' try to abort quest " + qe.getName() + " but he didn't have it started.");
            }
         } else if (Config.DEBUG) {
            _log.warning("Quest (id='" + this._questId + "') not found.");
         }
      }
   }
}
