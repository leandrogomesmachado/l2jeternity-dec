package l2e.scripts.ai.hellbound;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.scripts.ai.AbstractNpcAI;

public class Sandstorm extends AbstractNpcAI {
   public Sandstorm(String name, String descr) {
      super(name, descr);
      this.addAggroRangeEnterId(new int[]{32350});
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      npc.setTarget(player);
      npc.doCast(SkillsParser.getInstance().getInfo(5435, 1));
      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new Sandstorm(Sandstorm.class.getSimpleName(), "ai");
   }
}
