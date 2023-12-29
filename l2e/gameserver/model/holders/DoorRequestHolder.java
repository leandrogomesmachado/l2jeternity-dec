package l2e.gameserver.model.holders;

import l2e.gameserver.model.actor.instance.DoorInstance;

public class DoorRequestHolder {
   private final DoorInstance _target;

   public DoorRequestHolder(DoorInstance door) {
      this._target = door;
   }

   public DoorInstance getDoor() {
      return this._target;
   }
}
