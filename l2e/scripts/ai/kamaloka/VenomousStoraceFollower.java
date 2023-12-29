package l2e.scripts.ai.kamaloka;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class VenomousStoraceFollower extends Fighter {
   private long _skillTimer = 0L;
   private static final long _skillInterval = 20000L;

   public VenomousStoraceFollower(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._skillTimer == 0L) {
         this._skillTimer = System.currentTimeMillis();
      }

      if (this._skillTimer + 20000L < System.currentTimeMillis()) {
         Npc boss = null;

         for(Npc npc : World.getInstance().getAroundNpc(actor)) {
            if (npc.getId() == 18571) {
               boss = npc;
            }
         }

         if (boss != null) {
            if (boss.getCurrentHpPercents() < 70.0) {
               boss.setCurrentHp(boss.getCurrentHp() + boss.getMaxHp() * 0.2);
            } else {
               boss.setCurrentHp(boss.getMaxHp() - 10.0);
            }

            actor.broadcastPacket(
               new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.THERES_NOT_MUCH_I_CAN_DO_BUT_I_WILL_RISK_MY_LIFE_TO_HELP_YOU), 2000
            );
         }

         actor.doDie(null);
      }

      super.thinkAttack();
   }
}
