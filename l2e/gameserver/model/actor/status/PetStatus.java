package l2e.gameserver.model.actor.status;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PetStatus extends SummonStatus {
   private int _currentFed = 0;

   public PetStatus(PetInstance activeChar) {
      super(activeChar);
   }

   @Override
   public final void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   @Override
   public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
      if (!this.getActiveChar().isDead()) {
         super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
         if (attacker != null) {
            if (!isDOT && this.getActiveChar().getOwner() != null) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_C1);
               sm.addCharName(attacker);
               sm.addNumber((int)value);
               this.getActiveChar().sendPacket(sm);
            }

            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, Integer.valueOf((int)value));
         }
      }
   }

   public int getCurrentFed() {
      return this._currentFed;
   }

   public void setCurrentFed(int value) {
      this._currentFed = value;
   }

   public PetInstance getActiveChar() {
      return (PetInstance)super.getActiveChar();
   }
}
