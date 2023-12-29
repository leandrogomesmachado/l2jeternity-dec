package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ClanGate extends Effect {
   public ClanGate(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CLAN_GATE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
      if (this.getEffected().isPlayer()) {
         Clan clan = this.getEffected().getActingPlayer().getClan();
         if (clan != null) {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL);
            clan.broadcastToOtherOnlineMembers(msg, this.getEffected().getActingPlayer());
         }
      }

      return true;
   }

   @Override
   public void onExit() {
      this.getEffected().stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
   }
}
