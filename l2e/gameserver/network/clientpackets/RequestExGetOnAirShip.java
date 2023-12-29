package l2e.gameserver.network.clientpackets;

public class RequestExGetOnAirShip extends GameClientPacket {
   private int _x;
   private int _y;
   private int _z;
   private int _shipId;

   @Override
   protected void readImpl() {
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
      this._shipId = this.readD();
   }

   @Override
   protected void runImpl() {
      _log.info("[T1:ExGetOnAirShip] x: " + this._x);
      _log.info("[T1:ExGetOnAirShip] y: " + this._y);
      _log.info("[T1:ExGetOnAirShip] z: " + this._z);
      _log.info("[T1:ExGetOnAirShip] ship ID: " + this._shipId);
   }
}
