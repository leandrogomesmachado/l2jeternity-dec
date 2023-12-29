package l2e.gameserver.network.serverpackets;

public class ExSetCompassZoneCode extends GameServerPacket {
   public static final int ALTEREDZONE = 8;
   public static final int SIEGEWARZONE1 = 10;
   public static final int SIEGEWARZONE2 = 11;
   public static final int PEACEZONE = 12;
   public static final int SEVENSIGNSZONE = 13;
   public static final int PVPZONE = 14;
   public static final int GENERALZONE = 15;
   private final int _zoneType;

   public ExSetCompassZoneCode(int val) {
      this._zoneType = val;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._zoneType);
   }
}
