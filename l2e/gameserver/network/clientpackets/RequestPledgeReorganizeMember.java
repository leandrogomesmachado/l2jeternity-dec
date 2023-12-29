package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public final class RequestPledgeReorganizeMember extends GameClientPacket {
   private int _isMemberSelected;
   private String _memberName;
   private int _newPledgeType;
   private String _selectedMember;

   @Override
   protected void readImpl() {
      this._isMemberSelected = this.readD();
      this._memberName = this.readS();
      this._newPledgeType = this.readD();
      this._selectedMember = this.readS();
   }

   @Override
   protected void runImpl() {
      if (this._isMemberSelected != 0) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            Clan clan = activeChar.getClan();
            if (clan != null) {
               if ((activeChar.getClanPrivileges() & 16) == 16) {
                  ClanMember member1 = clan.getClanMember(this._memberName);
                  if (member1 != null && member1.getObjectId() != clan.getLeaderId()) {
                     ClanMember member2 = clan.getClanMember(this._selectedMember);
                     if (member2 != null && member2.getObjectId() != clan.getLeaderId()) {
                        int oldPledgeType = member1.getPledgeType();
                        if (oldPledgeType != this._newPledgeType) {
                           member1.setPledgeType(this._newPledgeType);
                           member2.setPledgeType(oldPledgeType);
                           clan.broadcastClanStatus();
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
