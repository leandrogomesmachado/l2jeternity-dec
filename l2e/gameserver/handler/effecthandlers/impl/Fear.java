package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.PositionUtils;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.DefenderInstance;
import l2e.gameserver.model.actor.instance.FortCommanderInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Fear extends Effect {
   public Fear(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.FEAR.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.FEAR;
   }

   @Override
   public boolean onActionTime() {
      this.effectRunTask();
      return false;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffected().isAfraid()
         && !(this.getEffected() instanceof NpcInstance)
         && !(this.getEffected() instanceof DefenderInstance)
         && !(this.getEffected() instanceof FortCommanderInstance)
         && !(this.getEffected() instanceof SiegeFlagInstance)
         && !(this.getEffected() instanceof SiegeSummonInstance)) {
         this.getEffected().startFear();
         this.effectRunTask();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      this.getEffected().stopFear(false);
   }

   private void effectRunTask() {
      double angle = Math.toRadians(PositionUtils.calculateAngleFrom(this.getEffector(), this.getEffected()));
      int oldX = this.getEffected().getX();
      int oldY = this.getEffected().getY();
      int x = oldX + (int)(500.0 * Math.cos(angle));
      int y = oldY + (int)(500.0 * Math.sin(angle));
      if (!this.getEffected().isPet() && !this.getEffected().isRunning()) {
         this.getEffected().setRunning();
      }

      Location loc = Location.findPointToStay(new Location(x, y, this.getEffected().getZ()), 100, this.getEffected().getGeoIndex(), false);
      if (loc != null) {
         this.getEffected().getAI().setIntention(CtrlIntention.MOVING, loc);
      }
   }
}
