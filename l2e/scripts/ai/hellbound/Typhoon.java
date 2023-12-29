package l2e.scripts.ai.hellbound;

import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.spawn.Spawner;
import l2e.scripts.ai.AbstractNpcAI;

public class Typhoon extends AbstractNpcAI {
   private static final int TYPHOON = 25539;
   private Npc _typhoon;
   private static SkillHolder STORM = new SkillHolder(5434, 1);

   private Typhoon(String name, String descr) {
      super(name, descr);
      this.addAggroRangeEnterId(new int[]{25539});
      this.addSpawnId(new int[]{25539});

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null && spawn.getId() == 25539) {
            this._typhoon = spawn.getLastSpawn();
            if (this._typhoon != null) {
               this.onSpawn(this._typhoon);
            }
         }
      }
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 25539 && !npc.isCastingNow() && !npc.isAttackingNow() && !player.isDead()) {
         npc.doSimultaneousCast(STORM.getSkill());
      }

      return null;
   }

   @Override
   public final String onSpawn(Npc npc) {
      ((Attackable)npc).setCanReturnToSpawnPoint(false);
      npc.setIsRunner(true);
      return super.onSpawn(npc);
   }

   public static void main(String[] args) {
      new Typhoon(Typhoon.class.getSimpleName(), "ai");
   }
}
