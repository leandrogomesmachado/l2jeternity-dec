package l2e.scripts.ai;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;

public class SelfExplosiveKamikaze extends AbstractNpcAI {
   private static final Map<Integer, SkillHolder> MONSTERS = new HashMap<>();

   public SelfExplosiveKamikaze(String name, String descr) {
      super(name, descr);

      for(int npcId : MONSTERS.keySet()) {
         this.addAttackId(npcId);
         this.addSpellFinishedId(new int[]{npcId});
      }
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skil) {
      if (player != null
         && MONSTERS.containsKey(npc.getId())
         && !npc.isDead()
         && Util.checkIfInRange(MONSTERS.get(npc.getId()).getSkill().getAffectRange(), player, npc, true)) {
         npc.doCast(MONSTERS.get(npc.getId()).getSkill());
      }

      return super.onAttack(npc, player, damage, isSummon, skil);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (MONSTERS.containsKey(npc.getId()) && !npc.isDead() && (skill.getId() == 4614 || skill.getId() == 5376)) {
         npc.doDie(null);
      }

      return super.onSpellFinished(npc, player, skill);
   }

   public static void main(String[] args) {
      new SelfExplosiveKamikaze(SelfExplosiveKamikaze.class.getSimpleName(), "ai");
   }

   static {
      MONSTERS.put(18817, new SkillHolder(5376, 4));
      MONSTERS.put(18818, new SkillHolder(5376, 4));
      MONSTERS.put(18821, new SkillHolder(5376, 5));
      MONSTERS.put(21666, new SkillHolder(4614, 3));
      MONSTERS.put(21689, new SkillHolder(4614, 4));
      MONSTERS.put(21712, new SkillHolder(4614, 5));
      MONSTERS.put(21735, new SkillHolder(4614, 6));
      MONSTERS.put(21758, new SkillHolder(4614, 7));
      MONSTERS.put(21781, new SkillHolder(4614, 9));
   }
}
