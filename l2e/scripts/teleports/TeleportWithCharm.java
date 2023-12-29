package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class TeleportWithCharm extends Quest {
   public TeleportWithCharm(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{30540, 30576});
      this.addTalkId(new int[]{30540, 30576});
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      QuestState var4 = var2.getQuestState(this.getName());
      if (var4 == null) {
         return null;
      } else {
         switch(var1.getId()) {
            case 30540:
               if (var4.hasQuestItems(1659)) {
                  if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                     for(Player var8 : var2.getParty().getMembers()) {
                        if (var8 != null
                           && var8.getObjectId() != var2.getObjectId()
                           && Util.checkIfInRange(1000, var2, var8, true)
                           && var8.getInventory().getItemByItemId(1659) != null
                           && BotFunctions.checkCondition(var8, false)
                           && var8.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                           takeItems(var8, 1659, 1L);
                           var8.teleToLocation(-80826, 149775, -3043, true);
                        }
                     }
                  }

                  var4.takeItems(1659, 1L);
                  var2.teleToLocation(-80826, 149775, -3043, true);
                  var4.exitQuest(true);
               } else {
                  var4.exitQuest(true);
                  var3 = "30540-01.htm";
               }
               break;
            case 30576:
               if (var4.hasQuestItems(1658)) {
                  if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                     for(Player var6 : var2.getParty().getMembers()) {
                        if (var6 != null
                           && var6.getObjectId() != var2.getObjectId()
                           && Util.checkIfInRange(1000, var2, var6, true)
                           && var6.getInventory().getItemByItemId(1658) != null
                           && BotFunctions.checkCondition(var6, false)
                           && var6.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                           takeItems(var6, 1658, 1L);
                           var6.teleToLocation(-80826, 149775, -3043, true);
                        }
                     }
                  }

                  var4.takeItems(1658, 1L);
                  var2.teleToLocation(-80826, 149775, -3043, true);
                  var4.exitQuest(true);
               } else {
                  var4.exitQuest(true);
                  var3 = "30576-01.htm";
               }
         }

         return var3;
      }
   }

   public static void main(String[] var0) {
      new TeleportWithCharm(-1, TeleportWithCharm.class.getSimpleName(), "teleports");
   }
}
