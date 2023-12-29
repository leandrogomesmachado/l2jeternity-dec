package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;

public class PledgeInfo extends GameServerPacket {
   private final Clan _clan;

   public PledgeInfo(Clan clan) {
      this._clan = clan;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clan.getId());
      this.writeS(this._clan.getName());
      this.writeS(this._clan.getAllyName());
   }
}
