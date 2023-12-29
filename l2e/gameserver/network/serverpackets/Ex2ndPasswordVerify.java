package l2e.gameserver.network.serverpackets;

public class Ex2ndPasswordVerify extends GameServerPacket {
   public static final int PASSWORD_OK = 0;
   public static final int PASSWORD_WRONG = 1;
   public static final int PASSWORD_BAN = 2;
   private final int _wrongTentatives;
   private final int _mode;

   public Ex2ndPasswordVerify(int mode, int wrongTentatives) {
      this._mode = mode;
      this._wrongTentatives = wrongTentatives;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._mode);
      this.writeD(this._wrongTentatives);
   }
}
