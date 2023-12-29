package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends GameClientPacket {
   private int _clanId;

   @Override
   protected void readImpl() {
      this._clanId = this.readD();
   }

   @Override
   protected void runImpl() {
      if (Config.DEBUG) {
         _log.log(Level.FINE, "Info for clan " + this._clanId + " requested");
      }

      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = ClanHolder.getInstance().getClan(this._clanId);
         if (clan == null) {
            if (Config.DEBUG) {
               _log.warning("Clan data for clanId " + this._clanId + " is missing for player " + activeChar.getName());
            }
         } else {
            PledgeInfo pc = new PledgeInfo(clan);
            activeChar.sendPacket(pc);
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
