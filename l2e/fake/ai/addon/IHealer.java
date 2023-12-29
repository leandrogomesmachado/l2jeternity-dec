package l2e.fake.ai.addon;

import java.util.List;
import java.util.stream.Collectors;
import l2e.fake.FakePlayer;
import l2e.fake.ai.CombatAI;
import l2e.fake.model.HealingSpell;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public interface IHealer {
   default void tryTargetingLowestHpTargetInRadius(FakePlayer player, int radius) {
      if (player.getTarget() == null) {
         List<Creature> targets = World.getInstance()
            .getAroundPlayers(player, radius, 200)
            .stream()
            .filter(x -> x.isFakePlayer() && !x.isDead())
            .collect(Collectors.toList());
         if (!player.isDead()) {
            targets.add(player);
         }

         List<Creature> sortedTargets = targets.stream().sorted((x1, x2) -> Double.compare(x1.getCurrentHp(), x2.getCurrentHp())).collect(Collectors.toList());
         if (!sortedTargets.isEmpty()) {
            Creature target = sortedTargets.get(0);
            player.setTarget(target);
         }
      } else if (((Creature)player.getTarget()).isDead()) {
         player.setTarget(null);
      }
   }

   default void tryHealingTarget(FakePlayer player) {
      if (player.getTarget() != null && player.getTarget() instanceof Creature) {
         Creature target = (Creature)player.getTarget();
         if (player.getFakeAi() instanceof CombatAI) {
            HealingSpell skill = ((CombatAI)player.getFakeAi()).getRandomAvaiableHealingSpellForTarget();
            if (skill != null) {
               switch(skill.getCondition()) {
                  case LESSHPPERCENT:
                     double currentHpPercentage = (double)Math.round(100.0 / target.getMaxHp() * target.getCurrentHp());
                     if (currentHpPercentage <= (double)skill.getConditionValue()) {
                        player.getFakeAi().castSpell(player.getKnownSkill(skill.getSkillId()));
                     }
               }
            }
         }
      }
   }
}
