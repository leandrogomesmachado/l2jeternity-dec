package l2e.gameserver.model.actor.poly;

import l2e.gameserver.model.GameObject;

public class ObjectPoly {
   private final GameObject _activeObject;
   private int _polyId;
   private String _polyType;

   public ObjectPoly(GameObject activeObject) {
      this._activeObject = activeObject;
   }

   public void setPolyInfo(String polyType, String polyId) {
      this.setPolyId(Integer.parseInt(polyId));
      this.setPolyType(polyType);
   }

   public final GameObject getActiveObject() {
      return this._activeObject;
   }

   public final boolean isMorphed() {
      return this.getPolyType() != null;
   }

   public final int getPolyId() {
      return this._polyId;
   }

   public final void setPolyId(int value) {
      this._polyId = value;
   }

   public final String getPolyType() {
      return this._polyType;
   }

   public final void setPolyType(String value) {
      this._polyType = value;
   }
}
