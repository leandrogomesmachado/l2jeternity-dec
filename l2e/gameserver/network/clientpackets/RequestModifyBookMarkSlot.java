package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestModifyBookMarkSlot extends GameClientPacket {
   private int id;
   private int icon;
   private String name;
   private String tag;

   @Override
   protected void readImpl() {
      this.id = this.readD();
      this.name = this.readS();
      this.icon = this.readD();
      this.tag = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.teleportBookmarkModify(this.id, this.icon, this.tag, this.name);
      }
   }
}
