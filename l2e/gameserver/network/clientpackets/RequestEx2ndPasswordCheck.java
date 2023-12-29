package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      if (Config.SECOND_AUTH_ENABLED && !this.getClient().getSecondaryAuth().isAuthed()) {
         this.getClient().getSecondaryAuth().openDialog();
      } else {
         this.sendPacket(new Ex2ndPasswordCheck(2));
      }
   }
}
