package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.instance.StaticObjectInstance;

public class StaticObjStatus extends CharStatus {
   public StaticObjStatus(StaticObjectInstance activeChar) {
      super(activeChar);
   }

   public StaticObjectInstance getActiveChar() {
      return (StaticObjectInstance)super.getActiveChar();
   }
}
