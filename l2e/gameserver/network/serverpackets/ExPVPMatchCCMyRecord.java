package l2e.gameserver.network.serverpackets;

public class ExPVPMatchCCMyRecord extends GameServerPacket {
   private final int _kp;

   public ExPVPMatchCCMyRecord(int killPts) {
      this._kp = killPts;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._kp);
   }
}
