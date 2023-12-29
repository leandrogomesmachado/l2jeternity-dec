package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public final class RequestPledgeMemberInfo extends GameClientPacket {
   protected int _unk1;
   private String _player;

   @Override
   protected void readImpl() {
      this._unk1 = this.readD();
      this._player = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         if (clan != null) {
            ClanMember member = clan.getClanMember(this._player);
            if (member != null) {
               activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
            }
         }
      }
   }
}
