package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;

public final class PledgeStatusChanged extends GameServerPacket {
   private final Clan _clan;

   public PledgeStatusChanged(Clan clan) {
      this._clan = clan;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clan.getLeaderId());
      this.writeD(this._clan.getId());
      this.writeD(this._clan.getCrestId());
      this.writeD(this._clan.getAllyId());
      this.writeD(this._clan.getAllyCrestId());
      this.writeD(0);
      this.writeD(0);
   }
}
