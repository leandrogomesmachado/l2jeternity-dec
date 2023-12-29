package l2e.gameserver.network.serverpackets;

public class Ex2ndPasswordCheck extends GameServerPacket {
   public static final int PASSWORD_NEW = 0;
   public static final int PASSWORD_PROMPT = 1;
   public static final int PASSWORD_OK = 2;
   private final int _windowType;

   public Ex2ndPasswordCheck(int windowType) {
      this._windowType = windowType;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._windowType);
      this.writeD(0);
   }
}
