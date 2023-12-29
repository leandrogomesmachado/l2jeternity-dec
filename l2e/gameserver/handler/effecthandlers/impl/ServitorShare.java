package l2e.gameserver.handler.effecthandlers.impl;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class ServitorShare extends Effect {
   private final Map<Stats, Double> stats = new HashMap<>(9);

   public ServitorShare(Env env, EffectTemplate template) {
      super(env, template);

      for(String key : template.getParameters().keySet()) {
         this.stats.put(Stats.valueOfXml(key), template.getParameters().getDouble(key, 1.0));
      }
   }

   @Override
   public boolean canBeStolen() {
      return false;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.SERVITOR_SHARE.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onStart() {
      super.onStart();
      this.getEffected().getActingPlayer().setServitorShare(this.stats);
      if (this.getEffected().getActingPlayer().getSummon() != null) {
         this.getEffected().getActingPlayer().getSummon().broadcastInfo();
         this.getEffected().getActingPlayer().getSummon().getStatus().startHpMpRegeneration();
      }

      return true;
   }

   @Override
   public void onExit() {
      this.getEffected().getActingPlayer().setServitorShare(null);
      if (this.getEffected().getSummon() != null) {
         if (this.getEffected().getSummon().getCurrentHp() > this.getEffected().getSummon().getMaxHp()) {
            this.getEffected().getSummon().setCurrentHp(this.getEffected().getSummon().getMaxHp());
         }

         if (this.getEffected().getSummon().getCurrentMp() > this.getEffected().getSummon().getMaxMp()) {
            this.getEffected().getSummon().setCurrentMp(this.getEffected().getSummon().getMaxMp());
         }

         this.getEffected().getSummon().broadcastInfo();
      }
   }
}
