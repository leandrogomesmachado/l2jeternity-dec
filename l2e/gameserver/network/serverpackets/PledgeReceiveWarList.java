package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;

public class PledgeReceiveWarList extends GameServerPacket {
   private final Clan _clan;
   private final int _tab;

   public PledgeReceiveWarList(Clan clan, int tab) {
      this._clan = clan;
      this._tab = tab;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._tab);
      this.writeD(0);
      this.writeD(this._tab == 0 ? this._clan.getWarList().size() : this._clan.getAttackerList().size());

      for(Integer i : this._tab == 0 ? this._clan.getWarList() : this._clan.getAttackerList()) {
         Clan clan = ClanHolder.getInstance().getClan(i);
         if (clan != null) {
            this.writeS(clan.getName());
            this.writeD(this._tab);
            this.writeD(this._tab);
         }
      }
   }
}
