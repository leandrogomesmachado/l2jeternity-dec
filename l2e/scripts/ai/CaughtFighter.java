package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class CaughtFighter extends Fighter {
   private final long TIME_TO_DIE = System.currentTimeMillis() + 60000L;
   private static final NpcStringId[] NPC_STRINGS_ON_SPAWN = new NpcStringId[]{
      NpcStringId.CROAK_CROAK_FOOD_LIKE_S1_IN_THIS_PLACE, NpcStringId.S1_HOW_LUCKY_I_AM, NpcStringId.PRAY_THAT_YOU_CAUGHT_A_WRONG_FISH_S1
   };
   private static final NpcStringId[] NPC_STRINGS_ON_KILL = new NpcStringId[]{
      NpcStringId.UGH_NO_CHANCE_HOW_COULD_THIS_ELDER_PASS_AWAY_LIKE_THIS, NpcStringId.CROAK_CROAK_A_FROG_IS_DYING, NpcStringId.A_FROG_TASTES_BAD_YUCK
   };
   private static final NpcStringId[] NPC_STRINGS_ON_ATTACK = new NpcStringId[]{
      NpcStringId.DO_YOU_KNOW_WHAT_A_FROG_TASTES_LIKE, NpcStringId.I_WILL_SHOW_YOU_THE_POWER_OF_A_FROG, NpcStringId.I_WILL_SWALLOW_AT_A_MOUTHFUL
   };

   public CaughtFighter(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      Attackable npc = this.getActiveChar();
      GameObject target = npc.getTarget();
      if (target != null && target.isPlayer() && Rnd.chance(75)) {
         Player player = target.getActingPlayer();
         NpcSay say = new NpcSay(npc, 22, NPC_STRINGS_ON_SPAWN[Rnd.get(NPC_STRINGS_ON_SPAWN.length)]);
         say.addStringParameter(player.getName());
         npc.broadcastPacket(say, 2000);
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable npc = this.getActiveChar();
      if (attacker != null && Rnd.chance(10)) {
         npc.broadcastPacket(new NpcSay(npc, 22, NPC_STRINGS_ON_ATTACK[Rnd.get(NPC_STRINGS_ON_ATTACK.length)]), 2000);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      if (Rnd.chance(75)) {
         actor.broadcastPacket(new NpcSay(actor, 22, NPC_STRINGS_ON_KILL[Rnd.get(NPC_STRINGS_ON_KILL.length)]), 2000);
      }

      super.onEvtDead(killer);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor != null && System.currentTimeMillis() >= this.TIME_TO_DIE) {
         actor.deleteMe();
         return false;
      } else {
         return super.thinkActive();
      }
   }
}
