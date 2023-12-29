package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.instance.DoorInstance;

public class DoorStatus extends CharStatus {
   public DoorStatus(DoorInstance activeChar) {
      super(activeChar);
   }

   public DoorInstance getActiveChar() {
      return (DoorInstance)super.getActiveChar();
   }
}
