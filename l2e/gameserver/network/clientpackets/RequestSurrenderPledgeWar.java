package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends GameClientPacket {
   private String _pledgeName;
   private Clan _clan;
   private Player _activeChar;

   @Override
   protected void readImpl() {
      this._pledgeName = this.readS();
   }

   @Override
   protected void runImpl() {
      this._activeChar = this.getClient().getActiveChar();
      if (this._activeChar != null) {
         this._clan = this._activeChar.getClan();
         if (this._clan != null) {
            Clan clan = ClanHolder.getInstance().getClanByName(this._pledgeName);
            if (clan == null) {
               this._activeChar.sendMessage("No such clan.");
               this._activeChar.sendActionFailed();
            } else {
               _log.info("RequestSurrenderPledgeWar by " + this.getClient().getActiveChar().getClan().getName() + " with " + this._pledgeName);
               if (!this._clan.isAtWarWith(clan.getId())) {
                  this._activeChar.sendMessage("You aren't at war with this clan.");
                  this._activeChar.sendActionFailed();
               } else {
                  SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
                  msg.addString(this._pledgeName);
                  this._activeChar.sendPacket(msg);
                  SystemMessage var3 = null;
                  this._activeChar.deathPenalty(null, false, false, false);
                  ClanHolder.getInstance().deleteclanswars(this._clan.getId(), clan.getId());
               }
            }
         }
      }
   }
}
