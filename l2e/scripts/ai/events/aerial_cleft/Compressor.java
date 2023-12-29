package l2e.scripts.ai.events.aerial_cleft;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.FlyMonsterInstance;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class Compressor extends Fighter {
   public Compressor(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      actor.setIsImmobilized(true);
      switch(actor.getId()) {
         case 22553:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_CENTRAL_STRONGHOLDS_COMPRESSOR_IS_WORKING, 2, 6000));
            break;
         case 22554:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IS_COMPRESSOR_IS_WORKING, 2, 6000));
            break;
         case 22555:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IIS_COMPRESSOR_IS_WORKING, 2, 6000));
            break;
         case 22556:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IIIS_COMPRESSOR_IS_WORKING, 2, 6000));
      }

      super.onEvtSpawn();
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0)) {
         for(Npc npc : World.getInstance().getAroundNpc(actor, 2000, 400)) {
            if (npc instanceof FlyMonsterInstance
               && (npc.getId() == 22557 || npc.getId() == 22558)
               && !npc.isAttackingNow()
               && !npc.isDead()
               && GeoEngine.canSeeTarget(actor, npc, true)) {
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(500));
            }
         }

         actor.setScriptValue(1);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      switch(actor.getId()) {
         case 22553:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_CENTRAL_STRONGHOLDS_COMPRESSOR_HAS_BEEN_DESTROYED, 2, 6000));
            break;
         case 22554:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IS_COMPRESSOR_HAS_BEEN_DESTROYED, 2, 6000));
            break;
         case 22555:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IIS_COMPRESSOR_HAS_BEEN_DESTROYED, 2, 6000));
            break;
         case 22556:
            actor.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONGHOLD_IIIS_COMPRESSOR_HAS_BEEN_DESTROYED, 2, 6000));
      }

      AerialCleftEvent.getInstance().checkNpcPoints(this.getActiveChar(), killer.getActingPlayer());
      super.onEvtDead(killer);
   }
}
