package l2e.scripts.ai.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.scripts.ai.AbstractNpcAI;

public class HellboundCore extends AbstractNpcAI {
   private static final int NAIA = 18484;
   private static final int HELLBOUND_CORE = 32331;
   private static SkillHolder BEAM = new SkillHolder(5493, 1);

   private HellboundCore(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{32331});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("cast") && HellboundManager.getInstance().getLevel() <= 6) {
         for(Npc naia : World.getInstance().getAroundNpc(npc, 900, 200)) {
            if (naia.isMonster() && naia.getId() == 18484 && !naia.isDead()) {
               naia.setTarget(npc);
               naia.doSimultaneousCast(BEAM.getSkill());
            }
         }

         this.startQuestTimer("cast", 10000L, npc, null);
      }

      return null;
   }

   @Override
   public final String onSpawn(Npc npc) {
      this.startQuestTimer("cast", 10000L, npc, null);
      return super.onSpawn(npc);
   }

   public static void main(String[] args) {
      new HellboundCore(HellboundCore.class.getSimpleName(), "ai");
   }
}
