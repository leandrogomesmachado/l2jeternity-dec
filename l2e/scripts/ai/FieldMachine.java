package l2e.scripts.ai;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class FieldMachine extends DefaultAI {
   private long _lastAction;

   public FieldMachine(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && attacker.getActingPlayer() != null) {
         if (System.currentTimeMillis() - this._lastAction > 15000L) {
            this._lastAction = System.currentTimeMillis();
            actor.broadcastPacket(
               new CreatureSay(
                  actor.getObjectId(), 0, actor.getName(), NpcStringId.THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE
               )
            );

            for(Npc npc : World.getInstance().getAroundNpc(actor, 1500, 200)) {
               if (npc.isMonster() && npc.getId() >= 22656 && npc.getId() <= 22659) {
                  npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(5000));
               }
            }
         }

         super.onEvtAttacked(attacker, damage);
      }
   }
}
