package l2e.gameserver.network.serverpackets;

public class CharacterDeleteFail extends GameServerPacket {
   public static final int REASON_DELETION_FAILED = 1;
   public static final int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
   public static final int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
   private final int _error;

   public CharacterDeleteFail(int errorCode) {
      this._error = errorCode;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._error);
   }
}
