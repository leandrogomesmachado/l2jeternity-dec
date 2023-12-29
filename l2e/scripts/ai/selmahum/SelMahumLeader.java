package l2e.scripts.ai.selmahum;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class SelMahumLeader extends Fighter {
   private boolean _isBusy;
   private boolean _isImmobilized;
   private long _busyTimeout = 0L;
   private long _idleTimeout = 0L;
   private static final NpcStringId[] _message = new NpcStringId[]{NpcStringId.COME_AND_EAT, NpcStringId.LOOKS_DELICIOUS, NpcStringId.LETS_GO_EAT};
   private static int NPC_ID_FIRE = 18927;
   private static int NPC_ID_FIRE_FEED = 18933;

   public SelMahumLeader(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else {
         if (!this._isBusy) {
            if (System.currentTimeMillis() > this._idleTimeout) {
               for(Npc npc : World.getInstance().getAroundNpc(actor, (int)(600.0 + actor.getColRadius()), 400)) {
                  if (npc.getId() == NPC_ID_FIRE_FEED && GeoEngine.canSeeTarget(actor, npc, false)) {
                     this._isBusy = true;
                     actor.setRunning();
                     actor.setDisplayEffect(1);
                     this._busyTimeout = System.currentTimeMillis() + (long)(60 + Rnd.get(15)) * 1000L;
                     actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(npc, 50, 150, true));
                     if (Rnd.chance(40)) {
                        actor.broadcastPacket(new CreatureSay(actor.getObjectId(), 0, actor.getName(), _message[Rnd.get(2)]), 2000);
                     }
                  } else if (npc.getId() == NPC_ID_FIRE && npc.getDisplayEffect() == 1 && GeoEngine.canSeeTarget(actor, npc, false)) {
                     this._isBusy = true;
                     actor.setDisplayEffect(2);
                     this._busyTimeout = System.currentTimeMillis() + (long)(60 + Rnd.get(60)) * 1000L;
                     actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(npc, 50, 150, true));
                  }
               }
            }
         } else if (System.currentTimeMillis() > this._busyTimeout) {
            this.wakeUp();
            actor.setWalking();
            actor.getAI().setIntention(CtrlIntention.MOVING, actor.getSpawn().getLocation());
            return true;
         }

         return this._isImmobilized ? true : super.thinkActive();
      }
   }

   private void wakeUp() {
      Attackable actor = this.getActiveChar();
      if (this._isBusy) {
         this._isBusy = false;
         this._busyTimeout = 0L;
         this._idleTimeout = System.currentTimeMillis() + (long)Rnd.get(180, 300) * 1000L;
         if (this._isImmobilized) {
            this._isImmobilized = false;
            actor.setIsImmobilized(false);
            actor.setDisplayEffect(3);
            actor.setRHandId(0);
            actor.broadcastInfo();
         }
      }
   }

   @Override
   protected void onEvtArrived() {
      Attackable actor = this.getActiveChar();
      super.onEvtArrived();
      if (this._isBusy) {
         this._isImmobilized = true;
         actor.setIsImmobilized(true);
         actor.setRHandId(15280);
         actor.broadcastInfo();
      }
   }

   @Override
   protected void onIntentionActive() {
      this._idleTimeout = System.currentTimeMillis() + (long)Rnd.get(60, 300) * 1000L;
      super.onIntentionActive();
   }

   @Override
   protected void onIntentionAttack(Creature target) {
      this.wakeUp();
      super.onIntentionAttack(target);
   }
}
