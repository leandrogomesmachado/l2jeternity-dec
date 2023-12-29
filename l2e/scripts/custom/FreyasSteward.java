package l2e.scripts.custom;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.BotFunctions;
import l2e.scripts.ai.AbstractNpcAI;

public final class FreyasSteward extends AbstractNpcAI {
   private FreyasSteward() {
      super(FreyasSteward.class.getSimpleName(), "custom");
      this.addStartNpc(32029);
      this.addFirstTalkId(32029);
      this.addTalkId(32029);
   }

   @Override
   public String onFirstTalk(Npc var1, Player var2) {
      return "32029.htm";
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      if (var2.getLevel() < 82) {
         return "32029-1.htm";
      } else {
         if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
            for(Player var4 : var2.getParty().getMembers()) {
               if (var4 != null
                  && var4.getObjectId() != var2.getObjectId()
                  && Util.checkIfInRange(1000, var2, var4, true)
                  && var4.getLevel() >= 82
                  && BotFunctions.checkCondition(var4, false)
                  && var4.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                  var4.teleToLocation(103045, -124361, -2768, true);
               }
            }
         }

         var2.teleToLocation(103045, -124361, -2768, true);
         return null;
      }
   }

   public static void main(String[] var0) {
      new FreyasSteward();
   }
}
