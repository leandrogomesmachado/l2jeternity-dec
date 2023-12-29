package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;

public class GemDragon extends Fighter {
   public GemDragon(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (actor.getMinionList() != null && actor.getMinionList().isWithMinions()) {
            actor.getMinionList().deleteMinions();
         }

         int chance = actor.getTemplate().getParameter("minionsSpawnChance", 0);
         if (((MonsterInstance)actor).isCanSupportMinion() && Rnd.chance(chance)) {
            int amount = actor.getTemplate().getParameter("minionsAmount", 0);
            actor.getMinionList().addMinion(new MinionData(new MinionTemplate(22830, amount)), true);
         }

         super.onEvtSpawn();
      }
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (actor.isInvul()) {
            actor.setIsInvul(false);
         }

         super.thinkAttack();
      }
   }

   @Override
   public void returnHome() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (!actor.isDead() && !actor.isInvul()) {
            actor.setIsInvul(true);
         }

         super.returnHome();
      }
   }
}
