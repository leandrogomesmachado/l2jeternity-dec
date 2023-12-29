package l2e.gameserver.model.actor.stat;

import l2e.gameserver.model.actor.Summon;

public class SummonStat extends PlayableStat {
   public SummonStat(Summon activeChar) {
      super(activeChar);
   }

   public Summon getActiveChar() {
      return (Summon)super.getActiveChar();
   }
}
