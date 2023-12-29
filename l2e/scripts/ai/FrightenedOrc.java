package l2e.scripts.ai;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class FrightenedOrc extends Fighter {
   protected ScheduledFuture<?> _rewardTask;
   protected ScheduledFuture<?> _despawnTask;
   private static final NpcStringId[] ATTACK_MSG = new NpcStringId[]{NpcStringId.I_DONT_WANT_TO_FIGHT, NpcStringId.IS_THIS_REALLY_NECESSARY};
   private static final NpcStringId[] REWARD_MSG = new NpcStringId[]{
      NpcStringId.TH_THANKS_I_COULD_HAVE_BECOME_GOOD_FRIENDS_WITH_YOU,
      NpcStringId.ILL_GIVE_YOU_10000000_ADENA_LIKE_I_PROMISED_I_MIGHT_BE_AN_ORC_WHO_KEEPS_MY_PROMISES
   };
   private static final NpcStringId[] REWARD_MSG1 = new NpcStringId[]{
      NpcStringId.TH_THANKS_I_COULD_HAVE_BECOME_GOOD_FRIENDS_WITH_YOU, NpcStringId.SORRY_BUT_THIS_IS_ALL_I_HAVE_GIVE_ME_A_BREAK
   };
   private static final NpcStringId[] REWARD_MSG2 = new NpcStringId[]{
      NpcStringId.THANKS_BUT_THAT_THING_ABOUT_10000000_ADENA_WAS_A_LIE_SEE_YA, NpcStringId.YOURE_PRETTY_DUMB_TO_BELIEVE_ME
   };

   public FrightenedOrc(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && Rnd.chance(10) && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor, 22, ATTACK_MSG[Rnd.get(2)]), 2000);
      } else if (actor.getCurrentHp() < actor.getMaxHp() * 0.2 && actor.isScriptValue(1)) {
         this._rewardTask = ThreadPoolManager.getInstance().schedule(new FrightenedOrc.checkReward(attacker, actor), 10000L);
         actor.broadcastPacket(new NpcSay(actor, 22, NpcStringId.WAIT_WAIT_STOP_SAVE_ME_AND_ILL_GIVE_YOU_10000000_ADENA), 2000);
         actor.setScriptValue(2);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (this._rewardTask != null) {
         this._rewardTask.cancel(false);
      }

      if (this._despawnTask != null) {
         this._despawnTask.cancel(false);
      }

      super.onEvtDead(killer);
   }

   protected class checkDespawn extends RunnableImpl {
      Attackable _npc;

      public checkDespawn(Attackable npc) {
         this._npc = npc;
      }

      @Override
      public void runImpl() {
         if (!this._npc.isDead()) {
            this._npc.setRunning();
            this._npc
               .getAI()
               .setIntention(
                  CtrlIntention.MOVING,
                  new Location(this._npc.getX() + Rnd.get(-800, 800), this._npc.getY() + Rnd.get(-800, 800), this._npc.getZ(), this._npc.getHeading())
               );
            this._npc.deleteMe();
         }
      }
   }

   protected class checkReward extends RunnableImpl {
      Creature _attacker;
      Attackable _npc;

      public checkReward(Creature attacker, Attackable npc) {
         this._attacker = attacker;
         this._npc = npc;
      }

      @Override
      public void runImpl() {
         if (!this._npc.isDead() && this._npc.isScriptValue(2)) {
            if (Rnd.get(100000) < 10) {
               this._npc.broadcastPacket(new NpcSay(this._npc, 22, FrightenedOrc.REWARD_MSG[Rnd.get(2)]), 2000);
               this._npc.setScriptValue(3);
               this._npc.doCast(new SkillHolder(6234, 1).getSkill());

               for(int i = 0; i < 10; ++i) {
                  this._npc.dropItem(this._attacker.getActingPlayer(), 57, 1000000L);
               }
            } else if (Rnd.get(100000) < 1000) {
               this._npc.broadcastPacket(new NpcSay(this._npc, 22, FrightenedOrc.REWARD_MSG1[Rnd.get(2)]), 2000);
               this._npc.setScriptValue(3);
               this._npc.doCast(new SkillHolder(6234, 1).getSkill());

               for(int i = 0; i < 10; ++i) {
                  this._npc.dropItem(this._attacker.getActingPlayer(), 57, 10000L);
               }
            } else {
               this._npc.broadcastPacket(new NpcSay(this._npc, 22, FrightenedOrc.REWARD_MSG2[Rnd.get(2)]), 2000);
            }

            FrightenedOrc.this._despawnTask = ThreadPoolManager.getInstance().schedule(FrightenedOrc.this.new checkDespawn(this._npc), 1000L);
         }
      }
   }
}
