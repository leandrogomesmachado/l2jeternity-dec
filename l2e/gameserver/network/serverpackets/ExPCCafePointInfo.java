package l2e.gameserver.network.serverpackets;

public class ExPCCafePointInfo extends GameServerPacket {
   private final int _points;
   private final int _mAddPoint;
   private int _mPeriodType;
   private int _remainTime;
   private int _pointType = 0;

   public ExPCCafePointInfo() {
      this._points = 0;
      this._mAddPoint = 0;
      this._remainTime = 0;
      this._mPeriodType = 0;
      this._pointType = 0;
   }

   public ExPCCafePointInfo(int points, int modify_points, boolean mod, boolean _double, int hours_left) {
      this._points = points;
      this._mAddPoint = modify_points;
      this._remainTime = hours_left;
      if (mod && _double) {
         this._mPeriodType = 1;
         this._pointType = 0;
      } else if (mod) {
         this._mPeriodType = 1;
         this._pointType = 1;
      } else {
         this._mPeriodType = 2;
         this._pointType = 2;
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._points);
      this.writeD(this._mAddPoint);
      this.writeC(this._mPeriodType);
      this.writeD(this._remainTime);
      this.writeC(this._pointType);
   }
}
