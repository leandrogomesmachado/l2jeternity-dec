package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class RequestBrLectureMark extends GameClientPacket {
   public static final int INITIAL_MARK = 1;
   public static final int EVANGELIST_MARK = 2;
   public static final int OFF_MARK = 3;
   private int _mark;

   @Override
   protected void readImpl() {
      this._mark = this.readC();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         switch(this._mark) {
            case 1:
            case 2:
            case 3:
               player.setLectureMark(this._mark);
               player.broadcastCharInfo();
         }
      }
   }
}
