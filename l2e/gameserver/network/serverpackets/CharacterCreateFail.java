package l2e.gameserver.network.serverpackets;

public class CharacterCreateFail extends GameServerPacket {
   public static final int REASON_CREATION_FAILED = 0;
   public static final int REASON_TOO_MANY_CHARACTERS = 1;
   public static final int REASON_NAME_ALREADY_EXISTS = 2;
   public static final int REASON_16_ENG_CHARS = 3;
   public static final int REASON_INCORRECT_NAME = 4;
   public static final int REASON_CREATE_NOT_ALLOWED = 5;
   public static final int REASON_CHOOSE_ANOTHER_SVR = 6;
   private final int _error;

   public CharacterCreateFail(int errorCode) {
      this._error = errorCode;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._error);
   }
}
