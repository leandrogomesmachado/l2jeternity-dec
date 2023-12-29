package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.serverpackets.QuestList;

public final class RequestQuestList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      this.sendPacket(new QuestList(this.getClient().getActiveChar()));
   }
}
