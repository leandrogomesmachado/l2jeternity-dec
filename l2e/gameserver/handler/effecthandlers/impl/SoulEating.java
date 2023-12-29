package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.events.listeners.IExperienceReceivedEventListener;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExSpawnEmitter;

public final class SoulEating extends Effect implements IExperienceReceivedEventListener {
   private final int _expNeeded;

   public SoulEating(Env env, EffectTemplate template) {
      super(env, template);
      this._expNeeded = template.getParameters().getInteger("expNeeded");
   }

   public SoulEating(Env env, Effect effect) {
      super(env, effect);
      this._expNeeded = effect.getEffectTemplate().getParameters().getInteger("expNeeded");
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onExperienceReceived(Playable playable, long exp) {
      Player player = this.getEffected().isPlayer() ? this.getEffected().getActingPlayer() : null;
      if (player != null && exp >= (long)this._expNeeded) {
         int maxSouls = (int)player.calcStat(Stats.MAX_SOULS, 0.0, null, null);
         if (player.getChargedSouls() >= maxSouls) {
            playable.sendPacket(SystemMessageId.SOUL_CANNOT_BE_ABSORBED_ANYMORE);
            return true;
         }

         player.increaseSouls(1);
         if (player.getTarget() != null && player.getTarget().isNpc()) {
            Npc npc = (Npc)playable.getTarget();
            player.broadcastPacket(new ExSpawnEmitter(player, npc), 500);
         }
      }

      return true;
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().getEvents().unregisterListener(this);
      }

      super.onExit();
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().getEvents().registerListener(this);
      }

      return super.onStart();
   }
}
