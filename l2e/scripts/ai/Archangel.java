package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Archangel extends Fighter {
   private long _targetTask = 0L;

   public Archangel(Attackable actor) {
      super(actor);
      this.MAX_PURSUE_RANGE = 4000;
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      final Attackable npc = this.getActiveChar();
      if (npc != null) {
         this._targetTask = System.currentTimeMillis();
         super.onEvtSpawn();
         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               for(Creature target : World.getInstance().getAroundCharacters(npc, 3000, 200)) {
                  if (target != null && target.getId() == 29020 && !target.isDead()) {
                     npc.addDamageHate(target, 0, 100);
                  }
               }
            }
         }, 2000L);
      }
   }

   @Override
   protected void thinkAttack() {
      Attackable npc = this.getActiveChar();
      if (npc != null) {
         if (this._targetTask + 20000L < System.currentTimeMillis()) {
            List<Creature> alive = new ArrayList<>();

            for(Creature target : World.getInstance().getAroundCharacters(npc, 2000, 200)) {
               if (target != null && !target.isDead() && !target.isInvisible() && target.getId() != 29021 && (target.getId() != 29020 || Rnd.get(100) > 50)) {
                  alive.add(target);
               }
            }

            if (alive != null && !alive.isEmpty()) {
               Creature rndTarget = alive.get(Rnd.get(alive.size()));
               if (rndTarget != null && (rndTarget.getId() == 29020 || rndTarget.isPlayer())) {
                  Creature mostHate = npc.getMostHated();
                  if (mostHate != null) {
                     npc.addDamageHate(rndTarget, 0, npc.getHating(mostHate) + 500);
                  } else {
                     npc.addDamageHate(rndTarget, 0, 2000);
                  }

                  npc.setTarget(rndTarget);
                  this.setAttackTarget(rndTarget);
               }
            }

            this._targetTask = System.currentTimeMillis();
         }

         super.thinkAttack();
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable npc = this.getActiveChar();
      if (npc != null && !npc.isDead() && attacker != null && attacker.getId() == 29020) {
         npc.addDamageHate(attacker, 10, 1000);
         this.setIntention(CtrlIntention.ATTACK, attacker);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   public void returnHome() {
      Attackable actor = this.getActiveChar();
      Location sloc = actor.getSpawn().getLocation();
      actor.stopMove(null);
      actor.clearAggroList();
      this._attackTimeout = Integer.MAX_VALUE;
      this.setAttackTarget(null);
      this.changeIntention(CtrlIntention.ACTIVE, null, null);
      actor.broadcastPacket(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
      actor.teleToLocation(sloc.getX(), sloc.getY(), GeoEngine.getHeight(sloc, actor.getGeoIndex()), true);
   }
}
