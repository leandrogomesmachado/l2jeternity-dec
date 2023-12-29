package l2e.gameserver.network.serverpackets;

public class ExDominionChannelSet extends GameServerPacket {
   public static final GameServerPacket ACTIVE = new ExDominionChannelSet(1);
   public static final GameServerPacket DEACTIVE = new ExDominionChannelSet(0);
   private final int _active;

   public ExDominionChannelSet(int active) {
      this._active = active;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._active);
   }
}
