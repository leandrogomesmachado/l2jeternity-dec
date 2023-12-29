package l2e.gameserver.network.serverpackets;

import l2e.gameserver.GameTimeController;

public class ClientSetTime extends GameServerPacket {
   public static final ClientSetTime STATIC_PACKET = new ClientSetTime();

   private ClientSetTime() {
   }

   @Override
   protected final void writeImpl() {
      this.writeD(GameTimeController.getInstance().getGameTime());
      this.writeD(6);
   }
}
