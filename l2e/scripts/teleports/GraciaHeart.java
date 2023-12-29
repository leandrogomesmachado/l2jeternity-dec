package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class GraciaHeart extends Quest {
   public GraciaHeart(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(36570);
      this.addTalkId(36570);
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      QuestState var4 = var2.getQuestState(this.getName());
      int var5 = var1.getId();
      if (var5 == 36570) {
         if (var2.getLevel() >= 75) {
            if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
               for(Player var7 : var2.getParty().getMembers()) {
                  if (var7 != null
                     && var7.getObjectId() != var2.getObjectId()
                     && Util.checkIfInRange(1000, var2, var7, true)
                     && var7.getLevel() >= 75
                     && BotFunctions.checkCondition(var7, false)
                     && var7.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                     var7.teleToLocation(-204288, 242026, 1744, true);
                  }
               }
            }

            var2.teleToLocation(-204288, 242026, 1744, true);
         } else {
            var3 = "36570-00.htm";
         }
      }

      var4.exitQuest(true);
      return var3;
   }

   public static void main(String[] var0) {
      new GraciaHeart(-1, GraciaHeart.class.getSimpleName(), "teleports");
   }
}
