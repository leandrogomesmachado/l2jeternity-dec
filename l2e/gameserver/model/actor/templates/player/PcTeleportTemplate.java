package l2e.gameserver.model.actor.templates.player;

public class PcTeleportTemplate {
   private final int _id;
   private int _locX;
   private int _locY;
   private int _locZ;
   private final String _name;

   public PcTeleportTemplate(int id, String name, int locX, int locY, int locZ) {
      this._id = id;
      this._name = name;
      this._locX = locX;
      this._locY = locY;
      this._locZ = locZ;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public int getX() {
      return this._locX;
   }

   public int getY() {
      return this._locY;
   }

   public int getZ() {
      return this._locZ;
   }

   public void setX(int x) {
      this._locX = x;
   }

   public void setY(int y) {
      this._locY = y;
   }

   public void setZ(int z) {
      this._locZ = z;
   }
}
