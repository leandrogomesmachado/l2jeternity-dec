package l2e.gameserver.network.serverpackets;

public class ExNeedToChangeName extends GameServerPacket {
   private final int _type;
   private final int _subType;
   private final String _name;

   public ExNeedToChangeName(int type, int subType, String name) {
      this._type = type;
      this._subType = subType;
      this._name = name;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._subType);
      this.writeS(this._name);
   }
}
