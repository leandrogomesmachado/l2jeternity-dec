package l2e.gameserver.network.serverpackets;

import l2e.gameserver.SevenSigns;

public class SSQInfo extends GameServerPacket {
   private int _state = 0;

   public SSQInfo() {
      int compWinner = SevenSigns.getInstance().getCabalHighestScore();
      if (SevenSigns.getInstance().isSealValidationPeriod()) {
         if (compWinner == 2) {
            this._state = 2;
         } else if (compWinner == 1) {
            this._state = 1;
         }
      }
   }

   public SSQInfo(int state) {
      this._state = state;
   }

   @Override
   protected final void writeImpl() {
      this.writeH(256 + this._state);
   }
}
