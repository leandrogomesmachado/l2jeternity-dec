package l2e.gameserver.network.serverpackets;

public class TutorialEnableClientEvent extends GameServerPacket {
   private int _eventId = 0;

   public TutorialEnableClientEvent(int event) {
      this._eventId = event;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._eventId);
   }
}
