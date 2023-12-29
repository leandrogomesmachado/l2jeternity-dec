package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.serverpackets.ExBrLoadEventTopRankers;

public class RequestExBrEventRankerList extends GameClientPacket {
   private int _eventId;
   private int _day;
   protected int _ranking;

   @Override
   protected void readImpl() {
      this._eventId = this.readD();
      this._day = this.readD();
      this._ranking = this.readD();
   }

   @Override
   protected void runImpl() {
      int count = 0;
      int bestScore = 0;
      int myScore = 0;
      this.getClient().sendPacket(new ExBrLoadEventTopRankers(this._eventId, this._day, 0, 0, 0));
   }
}
