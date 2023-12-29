package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;

public class RequestTutorialClientEvent extends GameClientPacket {
   int eventId = 0;

   @Override
   protected void readImpl() {
      this.eventId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         QuestState qs = player.getQuestState("_255_Tutorial");
         if (qs != null) {
            qs.getQuest().notifyEvent("CE" + this.eventId + "", null, player);
         }
      }
   }
}
