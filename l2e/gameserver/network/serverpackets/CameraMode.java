package l2e.gameserver.network.serverpackets;

public class CameraMode extends GameServerPacket {
   private final int _mode;
   public static final CameraMode FIRST_PERSON = new CameraMode(1);
   public static final CameraMode THIRD_PERSON = new CameraMode(0);

   public CameraMode(int mode) {
      this._mode = mode;
   }

   @Override
   public void writeImpl() {
      this.writeD(this._mode);
   }
}
