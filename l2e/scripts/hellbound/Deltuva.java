package l2e.scripts.hellbound;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class Deltuva extends Quest {
   public Deltuva(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(32313);
      this.addTalkId(32313);
   }

   @Override
   public final String onAdvEvent(String var1, Npc var2, Player var3) {
      String var4 = null;
      if (var1.equalsIgnoreCase("teleport")) {
         QuestState var5 = var3.getQuestState("_132_MatrasCuriosity");
         if (var5 != null && var5.isCompleted()) {
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var7 : var3.getParty().getMembers()) {
                  if (var7 != null && var7.getObjectId() != var3.getObjectId() && Util.checkIfInRange(1000, var3, var7, true)) {
                     QuestState var8 = var7.getQuestState("_132_MatrasCuriosity");
                     if (var8 != null
                        && var8.isCompleted()
                        && BotFunctions.checkCondition(var7, false)
                        && var7.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                        var7.teleToLocation(17934, 283189, -9701, true);
                     }
                  }
               }
            }

            var3.teleToLocation(17934, 283189, -9701, true);
         } else {
            var4 = "32313-02.htm";
         }
      }

      return var4;
   }

   public static void main(String[] var0) {
      new Deltuva(-1, Deltuva.class.getSimpleName(), "hellbound");
   }
}
