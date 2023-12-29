package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.entity.underground_coliseum.UCArena;

public class ExPVPMatchUserDie extends GameServerPacket {
   private final int _blueKills;
   private final int _redKills;

   public ExPVPMatchUserDie(UCArena a) {
      this._redKills = a.getTeams()[0].getKillCount();
      this._blueKills = a.getTeams()[1].getKillCount();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._redKills);
      this.writeD(this._blueKills);
   }
}
