package l2e.scripts.teleports;

import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.grandboss.Antharas;
import l2e.scripts.ai.grandboss.ValakasManager;

public class GrandBossTeleporters extends Quest {
   private Quest valakasAI() {
      return QuestManager.getInstance().getQuest(ValakasManager.class.getSimpleName());
   }

   private Quest antharasAI() {
      return QuestManager.getInstance().getQuest(Antharas.class.getSimpleName());
   }

   public GrandBossTeleporters(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{13001, 31859, 31384, 31385, 31540, 31686, 31687, 31759});
      this.addTalkId(new int[]{13001, 31859, 31384, 31385, 31540, 31686, 31687, 31759});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      String var4 = "";
      QuestState var5 = var3.getQuestState(this.getName());
      if (var1.equalsIgnoreCase("31540")) {
         if (var5.hasQuestItems(7267)) {
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var7 : var3.getParty().getMembers()) {
                  if (var7 != null
                     && var7.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var7, true)
                     && var7.getInventory().getItemByItemId(7267) != null
                     && BotFunctions.checkCondition(var7, false)
                     && var7.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var7.teleToLocation(183813, -115157, -3303, true);
                  }
               }
            }

            var3.teleToLocation(183813, -115157, -3303, true);
         } else {
            var4 = "31540-06.htm";
         }
      }

      return var4;
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var4 = "";
      QuestState var5 = var2.getQuestState(this.getName());
      if (var5 == null) {
         var5 = this.newQuestState(var2);
      }

      switch(var1.getId()) {
         case 13001:
            if (this.antharasAI() != null) {
               NoRestartZone var19 = ZoneManager.getInstance().getZoneById(70050, NoRestartZone.class);
               int var22 = EpicBossManager.getInstance().getBossStatus(29068);
               if (var22 == 2) {
                  var4 = "13001-02.htm";
               } else if (var22 == 3) {
                  var4 = "13001-01.htm";
               } else if (var2.isInParty()) {
                  Party var23 = var2.getParty();
                  boolean var25 = var23.isInCommandChannel();
                  List var10 = var25 ? var23.getCommandChannel().getMembers() : var23.getMembers();
                  boolean var11 = var25 ? var23.getCommandChannel().isLeader(var2) : var23.isLeader(var2);
                  if (!var11) {
                     var4 = "13001-05.htm";
                  } else if (!hasQuestItems(var2, 3865)) {
                     var4 = "13001-03.htm";
                  } else if (var19.getPlayersInside().size() + var10.size() > 200) {
                     var4 = "13001-04.htm";
                  } else {
                     boolean var12 = true;

                     for(Player var14 : var10) {
                        if (var14 != null) {
                           if (!hasQuestItems(var14, 3865)) {
                              SystemMessage var31 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                              var31.addPcName(var14);
                              var2.broadCast(var31);
                              var12 = false;
                              return null;
                           }

                           if (!var14.isInsideRadius(var2, 1000, true, true)) {
                              SystemMessage var15 = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
                              var15.addPcName(var14);
                              var2.broadCast(var15);
                              var12 = false;
                              return null;
                           }
                        }
                     }

                     if (var12) {
                        for(Player var30 : var10) {
                           if (var30.isInsideRadius(var1, 1000, true, false)) {
                              var30.teleToLocation(179700 + getRandom(700), 113800 + getRandom(2100), -7709, true);
                           }
                        }

                        if (var22 == 0) {
                           GrandBossInstance var29 = EpicBossManager.getInstance().getBoss(29068);
                           this.antharasAI().notifyEvent("waiting", var29, var2);
                        }
                     }
                  }
               } else if (!hasQuestItems(var2, 3865)) {
                  var4 = "13001-03.htm";
               } else if (var19.getPlayersInside().size() + 1 > 200) {
                  var4 = "13001-04.htm";
               } else {
                  var2.teleToLocation(179700 + getRandom(700), 113800 + getRandom(2100), -7709, true);
                  if (var22 == 0) {
                     GrandBossInstance var24 = EpicBossManager.getInstance().getBoss(29068);
                     this.antharasAI().notifyEvent("waiting", var24, var2);
                  }
               }
            }
            break;
         case 31384:
            DoorParser.getInstance().getDoor(24210004).openMe();
            break;
         case 31385:
            if (this.valakasAI() != null) {
               int var18 = EpicBossManager.getInstance().getBossStatus(29028);
               NoRestartZone var21 = ZoneManager.getInstance().getZoneById(70052, NoRestartZone.class);
               if (var18 == 0 || var18 == 1) {
                  if (var21.getPlayersInside().size() >= 200) {
                     var4 = "31385-03.htm";
                  } else if (hasQuestItems(var2, 7267)) {
                     if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                        for(Player var9 : var2.getParty().getMembers()) {
                           if (var9 != null
                              && var9.getObjectId() != var2.getObjectId()
                              && Util.checkIfInRange(1000, var2, var9, true)
                              && var9.getInventory().getItemByItemId(7267) != null
                              && BotFunctions.checkCondition(var9, false)
                              && var9.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                              var9.teleToLocation(204328 + getRandom(600), -111874 + getRandom(600), 70, true);
                           }
                        }
                     }

                     var2.teleToLocation(204328 + getRandom(600), -111874 + getRandom(600), 70, true);
                     if (var18 == 0) {
                        ValakasManager.setValakasSpawnTask();
                     }
                  } else {
                     var4 = "31385-04.htm";
                  }
               } else if (var18 == 2) {
                  var4 = "31385-02.htm";
               } else {
                  var4 = "31385-01.htm";
               }
            } else {
               var4 = "31385-01.htm";
            }
            break;
         case 31540:
            NoRestartZone var3 = ZoneManager.getInstance().getZoneById(70052, NoRestartZone.class);
            if (var3.getPlayersInside().size() < 50) {
               var4 = "31540-01.htm";
            } else if (var3.getPlayersInside().size() < 100) {
               var4 = "31540-02.htm";
            } else if (var3.getPlayersInside().size() < 150) {
               var4 = "31540-03.htm";
            } else if (var3.getPlayersInside().size() < 200) {
               var4 = "31540-04.htm";
            } else {
               var4 = "31540-05.htm";
            }
            break;
         case 31686:
            DoorParser.getInstance().getDoor(24210005).openMe();
            break;
         case 31687:
            DoorParser.getInstance().getDoor(24210006).openMe();
            break;
         case 31759:
            if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
               for(Player var20 : var2.getParty().getMembers()) {
                  if (var20 != null
                     && var20.getObjectId() != var2.getObjectId()
                     && Util.checkIfInRange(1000, var2, var20, true)
                     && BotFunctions.checkCondition(var20, false)
                     && var20.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                     var20.teleToLocation(150037 + getRandom(500), -57720 + getRandom(500), -2976, true);
                  }
               }
            }

            var2.teleToLocation(150037 + getRandom(500), -57720 + getRandom(500), -2976, true);
            break;
         case 31859:
            if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
               for(Player var7 : var2.getParty().getMembers()) {
                  if (var7 != null
                     && var7.getObjectId() != var2.getObjectId()
                     && Util.checkIfInRange(1000, var2, var7, true)
                     && BotFunctions.checkCondition(var7, false)
                     && var7.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                     var7.teleToLocation(79800 + getRandom(600), 151200 + getRandom(1100), -3534, true);
                  }
               }
            }

            var2.teleToLocation(79800 + getRandom(600), 151200 + getRandom(1100), -3534, true);
      }

      return var4;
   }

   public static void main(String[] var0) {
      new GrandBossTeleporters(-1, GrandBossTeleporters.class.getSimpleName(), "teleports");
   }
}
