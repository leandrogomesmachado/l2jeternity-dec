package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestSaveBookMarkSlot extends GameClientPacket {
   private int icon;
   private String name;
   private String tag;

   @Override
   protected void readImpl() {
      this.name = this.readS();
      this.icon = this.readD();
      this.tag = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.teleportBookmarkAdd(activeChar.getX(), activeChar.getY(), activeChar.getZ(), this.icon, this.tag, this.name);
      }
   }
}
