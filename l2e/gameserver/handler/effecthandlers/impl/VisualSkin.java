package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class VisualSkin extends Effect {
   private final int _skinId;
   private final boolean _isWithEffect;

   public VisualSkin(Env env, EffectTemplate template) {
      super(env, template);
      this._skinId = template.getParameters().getInteger("skinId");
      this._isWithEffect = template.getParameters().getBool("withEffect");
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.VISUAL_SKIN;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer() && this._skinId > 0) {
         this.getEffected().getActingPlayer().setVar("visualBuff", this._skinId);
         if (this._isWithEffect) {
            this.getEffected().broadcastPacket(new MagicSkillUse(this.getEffected(), this.getEffected(), 22217, 1, 0, 0));
         }

         this.getEffected().getActingPlayer().broadcastUserInfo(true);
         this.getEffected().getActingPlayer().sendUserInfo(true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().getActingPlayer().setVar("visualBuff", 0);
         if (this._isWithEffect) {
            this.getEffected().broadcastPacket(new MagicSkillUse(this.getEffected(), this.getEffected(), 22217, 1, 0, 0));
         }

         this.getEffected().getActingPlayer().broadcastUserInfo(true);
         this.getEffected().getActingPlayer().sendUserInfo(true);
      }
   }
}
