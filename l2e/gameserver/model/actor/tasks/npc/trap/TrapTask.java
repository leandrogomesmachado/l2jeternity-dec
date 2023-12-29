package l2e.gameserver.model.actor.tasks.npc.trap;

import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.serverpackets.SocialAction;

public class TrapTask implements Runnable {
   private static final Logger _log = Logger.getLogger(TrapTask.class.getName());
   private static final int TICK = 500;
   private final TrapInstance _trap;

   public TrapTask(TrapInstance trap) {
      this._trap = trap;
   }

   @Override
   public void run() {
      try {
         if (!this._trap.isTriggered()) {
            if (this._trap.hasLifeTime()) {
               this._trap.setRemainingTime(this._trap.getRemainingTime() - 500);
               if (this._trap.getRemainingTime() < this._trap.getLifeTime() - 15000) {
                  this._trap.broadcastPacket(new SocialAction(this._trap.getObjectId(), 2));
               }

               if (this._trap.getRemainingTime() < 0) {
                  switch(this._trap.getSkill().getTargetType()) {
                     case AURA:
                     case FRONT_AURA:
                     case BEHIND_AURA:
                        this._trap.triggerTrap(this._trap);
                        break;
                     default:
                        this._trap.unSummon();
                  }

                  return;
               }
            }

            int range = this._trap.getSkill().getTargetType() == TargetType.ONE
               ? this._trap.getSkill().getCastRange() / 2
               : this._trap.getSkill().getAffectRange();

            for(Creature target : World.getInstance().getAroundCharacters(this._trap, range, 200)) {
               if (this._trap.checkTarget(target)) {
                  this._trap.triggerTrap(target);
                  return;
               }
            }

            ThreadPoolManager.getInstance().schedule(new TrapTask(this._trap), 500L);
         }
      } catch (Exception var4) {
         _log.severe(TrapInstance.class.getSimpleName() + ": " + var4.getMessage());
         this._trap.unSummon();
      }
   }
}
