package l2e.gameserver.network.communication;

public enum ServerType {
   NORMAL,
   RELAX,
   TEST,
   NO_LABEL,
   RESTRICTED,
   EVENT,
   FREE;

   private int _mask = 1 << this.ordinal();

   public int getMask() {
      return this._mask;
   }
}
