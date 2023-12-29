package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class GuardofDawn extends Fighter {
   public GuardofDawn(Attackable actor) {
      super(actor);
      ((MonsterInstance)actor).setCanAgroWhileMoving();
      ((MonsterInstance)actor).setSeeThroughSilentMove(true);
      ((MonsterInstance)actor).setCanReturnToSpawnPoint(false);
      actor.setIsInvul(true);
   }

   @Override
   protected void thinkAttack() {
   }

   @Override
   protected void onIntentionAttack(Creature target) {
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature attacker, int aggro) {
   }
}
