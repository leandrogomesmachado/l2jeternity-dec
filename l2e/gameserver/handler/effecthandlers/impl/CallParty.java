package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ConfirmDlg;

public class CallParty extends Effect {
   public CallParty(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CALLPC;
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public boolean onStart() {
      Party party = this.getEffector().getParty();
      if (party != null) {
         for(Player partyMember : party.getMembers()) {
            if (CallPc.checkSummonTargetStatus(partyMember, this.getEffector().getActingPlayer()) && this.getEffector() != partyMember) {
               if (partyMember.getBlockPartyRecall()) {
                  partyMember.addScript(new SummonRequestHolder(this.getEffector().getActingPlayer(), this.getSkill(), false));
                  ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
                  confirm.addCharName(this.getEffector());
                  confirm.addZoneName(this.getEffector().getX(), this.getEffector().getY(), this.getEffector().getZ());
                  confirm.addTime(30000);
                  confirm.addRequesterId(this.getEffector().getObjectId());
                  partyMember.sendPacket(confirm);
               } else {
                  partyMember.teleToLocation(this.getEffector().getX(), this.getEffector().getY(), this.getEffector().getZ(), true);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
