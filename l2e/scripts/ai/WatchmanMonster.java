package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class WatchmanMonster extends Fighter {
   private long _lastSearch = 0L;
   private boolean _isSearching = false;
   private Creature _attacker = null;

   public WatchmanMonster(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead()) {
         if (attacker != null && !actor.getFaction().isNone() && actor.getCurrentHpPercents() < 50.0 && this._lastSearch < System.currentTimeMillis() - 15000L
            )
          {
            this._lastSearch = System.currentTimeMillis();
            this._attacker = attacker;
            actor.broadcastPacket(new NpcSay(actor, 22, NpcStringId.getNpcStringId(Rnd.get(1000007, 1000027))), 2000);
            if (this.findHelp()) {
               return;
            }
         }

         super.onEvtAttacked(attacker, damage);
      }
   }

   private boolean findHelp() {
      this._isSearching = false;
      Attackable actor = this.getActiveChar();
      Creature attacker = this._attacker;
      if (!actor.isDead() && attacker != null) {
         for(Npc npc : World.getInstance().getAroundNpc(actor, 1000, 150)) {
            if (actor != null && !actor.isDead() && npc.isInFaction(actor) && !npc.isInCombat()) {
               this._isSearching = true;
               npc.setIsRunning(true);
               npc.getAI().setIntention(CtrlIntention.MOVING, actor.getLocation());
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this._lastSearch = 0L;
      this._attacker = null;
      this._isSearching = false;
      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtArrived() {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead()) {
         if (this._isSearching) {
            Creature attacker = this._attacker;
            if (attacker != null) {
               this.notifyFriends(attacker, 100);
            }

            this._isSearching = false;
            actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
         } else {
            super.onEvtArrived();
         }
      }
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
      if (!this._isSearching) {
         super.onEvtAggression(target, aggro);
      }
   }
}
