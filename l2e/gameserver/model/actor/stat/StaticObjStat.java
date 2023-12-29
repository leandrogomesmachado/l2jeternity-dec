package l2e.gameserver.model.actor.stat;

import l2e.gameserver.model.actor.instance.StaticObjectInstance;

public class StaticObjStat extends CharStat {
   public StaticObjStat(StaticObjectInstance activeChar) {
      super(activeChar);
   }

   public StaticObjectInstance getActiveChar() {
      return (StaticObjectInstance)super.getActiveChar();
   }

   @Override
   public final byte getLevel() {
      return (byte)this.getActiveChar().getLevel();
   }
}
