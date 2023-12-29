package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;

public class PledgeShowInfoUpdate extends GameServerPacket {
   private final Clan _clan;

   public PledgeShowInfoUpdate(Clan clan) {
      this._clan = clan;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clan.getId());
      this.writeD(this._clan.getCrestId());
      this.writeD(this._clan.getLevel());
      this.writeD(this._clan.getCastleId());
      this.writeD(this._clan.getHideoutId());
      this.writeD(this._clan.getFortId());
      this.writeD(this._clan.getRank());
      this.writeD(this._clan.getReputationScore());
      this.writeD(0);
      this.writeD(0);
      this.writeD(this._clan.getAllyId());
      this.writeS(this._clan.getAllyName());
      this.writeD(this._clan.getAllyCrestId());
      this.writeD(this._clan.isAtWar() ? 1 : 0);
   }
}
