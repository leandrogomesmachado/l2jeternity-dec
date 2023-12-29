package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestSurrenderPersonally extends GameClientPacket {
   private String _pledgeName;

   @Override
   protected void readImpl() {
      this._pledgeName = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan playerClan = activeChar.getClan();
         if (playerClan != null) {
            Clan clan = ClanHolder.getInstance().getClanByName(this._pledgeName);
            if (clan != null) {
               if (playerClan.isAtWarWith(clan.getId()) && activeChar.getWantsPeace() != 1) {
                  activeChar.setWantsPeace(1);
                  activeChar.deathPenalty(null, false, false, false);
                  activeChar.sendPacket(
                     SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN).addString(this._pledgeName)
                  );
                  ClanHolder.getInstance().checkSurrender(playerClan, clan);
               } else {
                  activeChar.sendPacket(SystemMessageId.FAILED_TO_PERSONALLY_SURRENDER);
               }
            }
         }
      }
   }
}
