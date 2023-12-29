package l2e.gameserver.network.serverpackets;

public class ExChangeClientEffectInfo extends GameServerPacket {
   public static final ExChangeClientEffectInfo STATIC_FREYA_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
   public static final ExChangeClientEffectInfo STATIC_FREYA_DESTROYED = new ExChangeClientEffectInfo(0, 0, 2);
   private final int _type;
   private final int _key;
   private final int _value;

   public ExChangeClientEffectInfo(int type, int key, int value) {
      this._type = type;
      this._key = key;
      this._value = value;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._key);
      this.writeD(this._value);
   }
}
