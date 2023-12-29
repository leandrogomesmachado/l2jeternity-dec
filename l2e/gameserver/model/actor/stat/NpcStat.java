package l2e.gameserver.model.actor.stat;

import l2e.gameserver.model.actor.Npc;

public class NpcStat extends CharStat {
   public NpcStat(Npc activeChar) {
      super(activeChar);
   }

   @Override
   public byte getLevel() {
      return this.getActiveChar().getTemplate().getLevel();
   }

   public Npc getActiveChar() {
      return (Npc)super.getActiveChar();
   }
}
