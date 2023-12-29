package l2e.gameserver.handler.effecthandlers.impl;

import java.util.Calendar;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.player.CharacterVariable;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ResetReflectionEntry extends Effect {
   private final int _sharedReuseGroup;
   private final int _attempts;

   public ResetReflectionEntry(Env env, EffectTemplate template) {
      super(env, template);
      this._sharedReuseGroup = template.getParameters().getInteger("sharedReuseGroup");
      this._attempts = template.getParameters().getInteger("attempts");
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean isInstant() {
      return true;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         Player player = this.getEffected().getActingPlayer();

         for(int i : ReflectionParser.getInstance().getSharedReuseInstanceIdsByGroup(this._sharedReuseGroup)) {
            if (System.currentTimeMillis() < ReflectionManager.getInstance().getReflectionTime(player.getObjectId(), i)) {
               ReflectionManager.getInstance().deleteReflectionTime(player.getObjectId(), i);
            }
         }

         Calendar calendar = Calendar.getInstance();
         calendar.set(9, 0);
         calendar.set(10, 6);
         calendar.set(12, 30);
         calendar.set(13, 0);
         if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(5, 1);
         }

         String varName = "reflectionEntry_" + this._sharedReuseGroup;
         CharacterVariable var = player.getVariable(varName);
         if (var != null) {
            if (this._attempts > 1) {
               int attempts = Integer.parseInt(var.getValue());
               long time = var.getExpireTime();
               player.unsetVar(varName);
               if (attempts < this._attempts && !var.isExpired()) {
                  player.setVar(varName, String.valueOf(attempts + 1), time);
               } else {
                  player.setVar(varName, String.valueOf(1), calendar.getTimeInMillis());
               }
            } else {
               player.unsetVar(varName);
               player.setVar(varName, String.valueOf(1), calendar.getTimeInMillis());
            }
         } else {
            player.setVar(varName, String.valueOf(1), calendar.getTimeInMillis());
         }

         return true;
      } else {
         return false;
      }
   }
}
