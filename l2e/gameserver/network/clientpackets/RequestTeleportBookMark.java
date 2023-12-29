package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestTeleportBookMark extends GameClientPacket {
   private int id;

   @Override
   protected void readImpl() {
      this.id = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.teleportBookmarkGo(this.id);
      }
   }
}
