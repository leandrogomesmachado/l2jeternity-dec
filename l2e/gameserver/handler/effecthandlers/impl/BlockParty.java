package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class BlockParty extends Effect {
   public BlockParty(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         PunishmentManager.getInstance()
            .addPunishment(
               this.getEffected().getActingPlayer(),
               new PunishmentTemplate(
                  0,
                  String.valueOf(this.getEffected().getObjectId()),
                  PunishmentAffect.CHARACTER,
                  PunishmentType.PARTY_BAN,
                  0L,
                  "Party banned by bot report",
                  "system",
                  true
               ),
               false
            );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      PunishmentManager.getInstance().stopPunishment(this.getEffected().getActingPlayer().getClient(), PunishmentType.PARTY_BAN, PunishmentAffect.CHARACTER);
   }
}
