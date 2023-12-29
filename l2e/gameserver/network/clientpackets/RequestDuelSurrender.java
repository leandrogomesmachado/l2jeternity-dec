package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.DuelManager;

public final class RequestDuelSurrender extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      DuelManager.getInstance().doSurrender(this.getClient().getActiveChar());
   }
}
