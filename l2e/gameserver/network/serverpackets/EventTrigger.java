package l2e.gameserver.network.serverpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.instance.DoorInstance;

public class EventTrigger extends GameServerPacket {
   private final int _emitterId;
   private final boolean _opened;
   private static final int[] REVERSE_DOORS = new int[]{16200023, 16200024, 16200025};

   public EventTrigger(DoorInstance door, boolean opened) {
      this._emitterId = door.getEmitter();
      if (Util.contains(REVERSE_DOORS, door.getDoorId())) {
         this._opened = !opened;
      } else {
         this._opened = opened;
      }
   }

   public EventTrigger(int id, boolean opened) {
      this._emitterId = id;
      this._opened = opened;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._emitterId);
      this.writeD(this._opened ? 0 : 1);
   }
}
