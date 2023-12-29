package l2e.gameserver.network.serverpackets;

public class ShowCalc extends GameServerPacket {
   private final int _calculatorId;

   public ShowCalc(int calculatorId) {
      this._calculatorId = calculatorId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._calculatorId);
   }
}
