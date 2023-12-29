package l2e.gameserver.network.serverpackets;

public class PledgeReceiveUpdatePower extends GameServerPacket {
   private final int _privs;

   public PledgeReceiveUpdatePower(int privs) {
      this._privs = privs;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._privs);
   }
}
