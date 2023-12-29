package l2e.gameserver.network.serverpackets;

public class ExVitalityPointInfo extends GameServerPacket {
   private final int _vitalityPoints;

   public ExVitalityPointInfo(int vitPoints) {
      this._vitalityPoints = vitPoints;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._vitalityPoints);
   }
}
