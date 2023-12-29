package l2e.gameserver.network.serverpackets;

public class ExBrBuyProduct extends GameServerPacket {
   public static final int RESULT_OK = 1;
   public static final int RESULT_NOT_ENOUGH_POINTS = -1;
   public static final int RESULT_WRONG_PRODUCT = -2;
   public static final int RESULT_INVENTORY_FULL = -4;
   public static final int RESULT_SALE_PERIOD_ENDED = -7;
   public static final int RESULT_WRONG_USER_STATE = -9;
   public static final int RESULT_WRONG_PRODUCT_ITEM = -10;
   private final int _result;

   public ExBrBuyProduct(int result) {
      this._result = result;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._result);
   }
}
