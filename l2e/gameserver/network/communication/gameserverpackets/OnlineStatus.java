package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class OnlineStatus extends SendablePacket {
   private final boolean _online;

   public OnlineStatus(boolean online) {
      this._online = online;
   }

   @Override
   protected void writeImpl() {
      this.writeC(1);
      this.writeC(this._online ? 1 : 0);
   }
}
