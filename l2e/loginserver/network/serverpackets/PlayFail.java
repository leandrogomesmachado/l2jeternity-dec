package l2e.loginserver.network.serverpackets;

public final class PlayFail extends LoginServerPacket {
   public static final int REASON_SYSTEM_ERROR = 1;
   public static final int REASON_ACCESS_FAILED_1 = 2;
   public static final int REASON_ACCOUNT_INFO_INCORRECT = 3;
   public static final int REASON_PASSWORD_INCORRECT_1 = 4;
   public static final int REASON_PASSWORD_INCORRECT_2 = 5;
   public static final int REASON_NO_REASON = 6;
   public static final int REASON_SYS_ERROR = 7;
   public static final int REASON_ACCESS_FAILED_2 = 8;
   public static final int REASON_HIGH_SERVER_TRAFFIC = 9;
   public static final int REASON_MIN_AGE = 10;
   private final int _reason;

   public PlayFail(int reason) {
      this._reason = reason;
   }

   @Override
   protected void writeImpl() {
      this.writeC(6);
      this.writeC(this._reason);
   }
}
