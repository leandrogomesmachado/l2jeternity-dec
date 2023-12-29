package l2e.gameserver.model.actor.templates;

public final class AirShipTeleportTemplate {
   private final int _location;
   private final int[] _fuel;
   private final VehicleTemplate[][] _routes;

   public AirShipTeleportTemplate(int loc, int[] f, VehicleTemplate[][] r) {
      this._location = loc;
      this._fuel = f;
      this._routes = r;
   }

   public int getLocation() {
      return this._location;
   }

   public int[] getFuel() {
      return this._fuel;
   }

   public VehicleTemplate[][] getRoute() {
      return this._routes;
   }
}
