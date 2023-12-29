package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class BlockAction extends Effect {
   private final ArrayList<Integer> _blockedActions;

   public BlockAction(Env env, EffectTemplate template) {
      super(env, template);
      String[] rawActions = template.getParameters().getString("blockedActions").split(",");
      this._blockedActions = new ArrayList<>(rawActions.length);

      for(String act : rawActions) {
         int id = -1;

         try {
            id = Integer.parseInt(act);
            this._blockedActions.add(id);
         } catch (Exception var10) {
         }
      }
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         if (this._blockedActions.contains(-3)) {
            PunishmentManager.getInstance()
               .addPunishment(
                  this.getEffected().getActingPlayer(),
                  new PunishmentTemplate(
                     0,
                     String.valueOf(this.getEffected().getObjectId()),
                     PunishmentAffect.CHARACTER,
                     PunishmentType.PARTY_BAN,
                     0L,
                     "block action debuff",
                     "system",
                     true
                  ),
                  false
               );
         }

         if (this._blockedActions.contains(-5)) {
            PunishmentManager.getInstance()
               .addPunishment(
                  this.getEffected().getActingPlayer(),
                  new PunishmentTemplate(
                     0,
                     String.valueOf(this.getEffected().getObjectId()),
                     PunishmentAffect.CHARACTER,
                     PunishmentType.CHAT_BAN,
                     0L,
                     "block action debuff",
                     "system",
                     true
                  ),
                  false
               );
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this._blockedActions.contains(-3)) {
         PunishmentManager.getInstance()
            .stopPunishment(this.getEffected().getActingPlayer().getClient(), PunishmentType.PARTY_BAN, PunishmentAffect.CHARACTER);
      }

      if (this._blockedActions.contains(-5)) {
         PunishmentManager.getInstance().stopPunishment(this.getEffected().getActingPlayer().getClient(), PunishmentType.CHAT_BAN, PunishmentAffect.CHARACTER);
      }
   }

   @Override
   public boolean checkCondition(Object id) {
      return !this._blockedActions.contains(id);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.ACTION_BLOCK;
   }
}
