package l2e.scripts.ai.freya;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.entity.Reflection;

public class JiniaGuild extends Fighter {
   private long _buffTimer = 0L;

   public JiniaGuild(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      Npc npc = this.getSelectTarget();
      if (npc != null) {
         this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(3000));
         this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(300));
      }

      return true;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && !attacker.isPlayable()) {
         if (!this.getActiveChar().isCastingNow() && this._buffTimer < System.currentTimeMillis() && this.defaultThinkBuff(100)) {
            this._buffTimer = System.currentTimeMillis() + 30000L;
         } else {
            super.onEvtAttacked(attacker, damage);
         }
      }
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target.isPlayable()) {
         return false;
      } else {
         Npc npc = this.getSelectTarget();
         if (npc != null) {
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(3000));
         }

         return super.checkAggression(target);
      }
   }

   private Npc getSelectTarget() {
      Reflection inst = ReflectionManager.getInstance().getReflection(this.getActiveChar().getReflectionId());
      if (inst != null) {
         for(Npc n : inst.getNpcs()) {
            if (n != null && (n.getId() == 29179 || n.getId() == 29180) && n.getReflectionId() == this.getActiveChar().getReflectionId()) {
               return n;
            }
         }
      }

      return null;
   }
}
