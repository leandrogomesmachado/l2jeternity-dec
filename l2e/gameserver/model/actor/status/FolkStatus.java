package l2e.gameserver.model.actor.status;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;

public class FolkStatus extends NpcStatus {
   public FolkStatus(Npc activeChar) {
      super(activeChar);
   }

   @Override
   public final void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
   }

   @Override
   public final void reduceMp(double value) {
      if (!Config.CH_BUFF_FREE || !(this.getActiveChar() instanceof ClanHallManagerInstance)) {
         super.reduceMp(value);
      }
   }

   public NpcInstance getActiveChar() {
      return (NpcInstance)super.getActiveChar();
   }
}
