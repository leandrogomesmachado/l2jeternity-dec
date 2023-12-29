package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class NpcStatus extends CharStatus {
   public NpcStatus(Npc activeChar) {
      super(activeChar);
   }

   @Override
   public void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
      if (!this.getActiveChar().isDead()) {
         if (attacker != null) {
            Player attackerPlayer = attacker.getActingPlayer();
            if (attackerPlayer != null && attackerPlayer.isInDuel()) {
               attackerPlayer.setDuelState(4);
            }

            this.getActiveChar().addAttackerToAttackByList(attacker);
         }

         super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
      }
   }

   public Npc getActiveChar() {
      return (Npc)super.getActiveChar();
   }
}
