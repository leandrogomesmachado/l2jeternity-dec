package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;

public final class RequestPledgeMemberList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         if (clan != null) {
            PledgeShowMemberListAll pm = new PledgeShowMemberListAll(clan, activeChar);
            activeChar.sendPacket(pm);
         }
      }
   }
}
