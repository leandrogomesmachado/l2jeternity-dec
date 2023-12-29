package l2e.scripts.ai.dragonvalley;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.scripts.ai.AbstractNpcAI;

public class DragonGuards extends AbstractNpcAI {
   private static final int DRAGON_GUARD = 22852;
   private static final int DRAGON_MAGE = 22853;
   private static final int[] WALL_MONSTERS = new int[]{22852, 22853};

   public DragonGuards(String name, String descr) {
      super(name, descr);

      for(int mobId : WALL_MONSTERS) {
         this.addAggroRangeEnterId(new int[]{mobId});
         this.addAttackId(mobId);
      }
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !player.isDead()) {
         npc.setIsImmobilized(false);
         npc.setRunning();
         ((Attackable)npc).addDamageHate(player, 0, 999);
         ((Attackable)npc).getAI().setIntention(CtrlIntention.ATTACK, player);
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      if (npc instanceof MonsterInstance) {
         for(int mobId : WALL_MONSTERS) {
            if (mobId == npc.getId()) {
               MonsterInstance monster = (MonsterInstance)npc;
               monster.setIsImmobilized(false);
               monster.setRunning();
               if (!monster.getFaction().isNone()) {
                  int factionRange = (int)((double)monster.getFaction().getRange() + monster.getColRadius());

                  for(Npc obj : World.getInstance().getAroundNpc(monster)) {
                     if (obj != null && obj instanceof Attackable) {
                        Attackable called = (Attackable)obj;
                        if ((called.getFaction().isNone() || monster.isInFaction(called))
                           && monster.isInsideRadius(called, factionRange, true, false)
                           && called.hasAI()) {
                           called.setIsImmobilized(false);
                           called.addDamageHate(player, 0, 999);
                        }
                     }
                  }
               }
               break;
            }
         }
      }

      return super.onAttack(npc, player, damage, isSummon);
   }

   public static void main(String[] args) {
      new DragonGuards(DragonGuards.class.getSimpleName(), "ai");
   }
}
