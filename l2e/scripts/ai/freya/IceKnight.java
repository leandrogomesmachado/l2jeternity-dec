package l2e.scripts.ai.freya;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;

public class IceKnight extends Fighter {
   private boolean _iced;
   private ScheduledFuture<?> _task;

   public IceKnight(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      Attackable actor = this.getActiveChar();
      this._iced = true;
      actor.setDisplayEffect(1);
      actor.block();
      actor.setIsImmobilized(true);
      actor.disableCoreAI(true);
      this.aggroPlayers();
      this._task = ThreadPoolManager.getInstance().schedule(new IceKnight.ReleaseFromIce(), 6000L);
   }

   private void aggroPlayers() {
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null) {
               this.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(300));
            }
         }
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && this._iced) {
         this._iced = false;
         if (this._task != null) {
            this._task.cancel(false);
         }

         actor.disableCoreAI(false);
         actor.setIsImmobilized(false);
         actor.unblock();
         actor.setDisplayEffect(2);
      }

      super.onEvtAttacked(attacker, damage);
   }

   private class ReleaseFromIce extends RunnableImpl {
      private ReleaseFromIce() {
      }

      @Override
      public void runImpl() {
         if (IceKnight.this._iced) {
            IceKnight.this._iced = false;
            IceKnight.this.getActiveChar().disableCoreAI(false);
            IceKnight.this.getActiveChar().setIsImmobilized(false);
            IceKnight.this.getActiveChar().setDisplayEffect(2);
            IceKnight.this.getActiveChar().unblock();
            IceKnight.this.aggroPlayers();
         }
      }
   }
}
