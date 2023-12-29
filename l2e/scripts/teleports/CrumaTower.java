package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class CrumaTower extends Quest {
   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      QuestState var4 = var2.getQuestState(this.getName());
      if (var4 == null) {
         return getNoQuestMsg(var2);
      } else {
         if (var2.getLevel() > Config.CRUMA_MAX_LEVEL) {
            var3 = "30483.htm";
         } else {
            if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
               for(Player var6 : var2.getParty().getMembers()) {
                  if (var6 != null
                     && var6.getObjectId() != var2.getObjectId()
                     && Util.checkIfInRange(1000, var2, var6, true)
                     && var6.getLevel() <= Config.CRUMA_MAX_LEVEL
                     && BotFunctions.checkCondition(var6, false)
                     && var6.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                     var6.teleToLocation(17724, 114004, -11672, true);
                  }
               }
            }

            var2.teleToLocation(17724, 114004, -11672, true);
         }

         return var3;
      }
   }

   public CrumaTower(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(30483);
      this.addTalkId(30483);
   }

   public static void main(String[] var0) {
      new CrumaTower(-1, CrumaTower.class.getSimpleName(), "teleports");
   }
}
