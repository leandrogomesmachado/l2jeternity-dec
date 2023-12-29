package l2e.gameserver.handler.effecthandlers.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class BlockBuffSlot extends Effect {
   private final Set<String> _blockBuffSlots;

   public BlockBuffSlot(Env env, EffectTemplate template) {
      super(env, template);
      String blockBuffSlots = template.getParameters().getString("slot", null);
      if (blockBuffSlots != null && !blockBuffSlots.isEmpty()) {
         this._blockBuffSlots = new HashSet<>();

         for(String slot : blockBuffSlots.split(";")) {
            this._blockBuffSlots.add(slot);
         }
      } else {
         this._blockBuffSlots = Collections.emptySet();
      }
   }

   @Override
   public void onExit() {
      this.getEffected().getEffectList().removeBlockedBuffSlots(this._blockBuffSlots);
   }

   @Override
   public boolean onStart() {
      this.getEffected().getEffectList().addBlockedBuffSlots(this._blockBuffSlots);
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }
}
