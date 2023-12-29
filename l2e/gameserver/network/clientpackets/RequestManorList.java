package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         List<String> manorsName = new ArrayList<>();
         manorsName.add("gludio");
         manorsName.add("dion");
         manorsName.add("giran");
         manorsName.add("oren");
         manorsName.add("aden");
         manorsName.add("innadril");
         manorsName.add("goddard");
         manorsName.add("rune");
         manorsName.add("schuttgart");
         ExSendManorList manorlist = new ExSendManorList(manorsName);
         player.sendPacket(manorlist);
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
