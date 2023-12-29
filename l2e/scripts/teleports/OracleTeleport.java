package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.network.SystemMessageId;

public class OracleTeleport extends Quest {
   private static final int[] TOWN_DAWN = new int[]{31078, 31079, 31080, 31081, 31083, 31084, 31082, 31692, 31694, 31997, 31168};
   private static final int[] TOWN_DUSK = new int[]{31085, 31086, 31087, 31088, 31090, 31091, 31089, 31693, 31695, 31998, 31169};
   private static final int[] TEMPLE_PRIEST = new int[]{31127, 31128, 31129, 31130, 31131, 31137, 31138, 31139, 31140, 31141};
   private static final int[] RIFT_POSTERS = new int[]{31488, 31489, 31490, 31491, 31492, 31493};
   private static final int[] TELEPORTERS = new int[]{
      31078,
      31079,
      31080,
      31081,
      31082,
      31083,
      31084,
      31692,
      31694,
      31997,
      31168,
      31085,
      31086,
      31087,
      31088,
      31089,
      31090,
      31091,
      31693,
      31695,
      31998,
      31169,
      31494,
      31495,
      31496,
      31497,
      31498,
      31499,
      31500,
      31501,
      31502,
      31503,
      31504,
      31505,
      31506,
      31507,
      31095,
      31096,
      31097,
      31098,
      31099,
      31100,
      31101,
      31102,
      31103,
      31104,
      31105,
      31106,
      31107,
      31108,
      31109,
      31110,
      31114,
      31115,
      31116,
      31117,
      31118,
      31119,
      31120,
      31121,
      31122,
      31123,
      31124,
      31125
   };
   private static final Location[] RETURN_LOCS = new Location[]{
      new Location(-80555, 150337, -3040),
      new Location(-13953, 121404, -2984),
      new Location(16354, 142820, -2696),
      new Location(83369, 149253, -3400),
      new Location(111386, 220858, -3544),
      new Location(83106, 53965, -1488),
      new Location(146983, 26595, -2200),
      new Location(148256, -55454, -2779),
      new Location(45664, -50318, -800),
      new Location(86795, -143078, -1341),
      new Location(115136, 74717, -2608),
      new Location(-82368, 151568, -3120),
      new Location(-14748, 123995, -3112),
      new Location(18482, 144576, -3056),
      new Location(81623, 148556, -3464),
      new Location(112486, 220123, -3592),
      new Location(82819, 54607, -1520),
      new Location(147570, 28877, -2264),
      new Location(149888, -56574, -2979),
      new Location(44528, -48370, -800),
      new Location(85129, -142103, -1542),
      new Location(116642, 77510, -2688),
      new Location(-41572, 209731, -5087),
      new Location(-52872, -250283, -7908),
      new Location(45256, 123906, -5411),
      new Location(46192, 170290, -4981),
      new Location(111273, 174015, -5437),
      new Location(-20604, -250789, -8165),
      new Location(-21726, 77385, -5171),
      new Location(140405, 79679, -5427),
      new Location(-52366, 79097, -4741),
      new Location(118311, 132797, -4829),
      new Location(172185, -17602, -4901),
      new Location(83000, 209213, -5439),
      new Location(-19500, 13508, -4901),
      new Location(12525, -248496, -9580),
      new Location(-41561, 209225, -5087),
      new Location(45242, 124466, -5413),
      new Location(110711, 174010, -5439),
      new Location(-22341, 77375, -5173),
      new Location(-52889, 79098, -4741),
      new Location(117760, 132794, -4831),
      new Location(171792, -17609, -4901),
      new Location(82564, 209207, -5439),
      new Location(-41565, 210048, -5085),
      new Location(45278, 123608, -5411),
      new Location(111510, 174013, -5437),
      new Location(-21489, 77372, -5171),
      new Location(-52016, 79103, -4739),
      new Location(118557, 132804, -4829),
      new Location(172570, -17605, -4899),
      new Location(83347, 209215, -5437),
      new Location(42495, 143944, -5381),
      new Location(45666, 170300, -4981),
      new Location(77138, 78389, -5125),
      new Location(139903, 79674, -5429),
      new Location(-20021, 13499, -4901),
      new Location(113418, 84535, -6541),
      new Location(-52940, -250272, -7907),
      new Location(46499, 170301, -4979),
      new Location(-20280, -250785, -8163),
      new Location(140673, 79680, -5437),
      new Location(-19182, 13503, -4899),
      new Location(12837, -248483, -9579)
   };

   public OracleTeleport(int var1, String var2, String var3) {
      super(var1, var2, var3);

      for(int var7 : RIFT_POSTERS) {
         this.addStartNpc(var7);
         this.addTalkId(var7);
      }

      for(int var20 : TELEPORTERS) {
         this.addStartNpc(var20);
         this.addTalkId(var20);
      }

      for(int var21 : TEMPLE_PRIEST) {
         this.addStartNpc(var21);
         this.addTalkId(var21);
      }

      for(int var22 : TOWN_DAWN) {
         this.addStartNpc(var22);
         this.addTalkId(var22);
      }

      for(int var23 : TOWN_DUSK) {
         this.addStartNpc(var23);
         this.addTalkId(var23);
      }
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      String var4 = "";
      QuestState var5 = var3.getQuestState(this.getName());
      int var6 = var2.getId();
      if (var1.equalsIgnoreCase("Return")) {
         if (Util.contains(TEMPLE_PRIEST, var6) && var5.getState() == 1) {
            Location var13 = RETURN_LOCS[var5.getInt("id")];
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var24 : var3.getParty().getMembers()) {
                  if (var24 != null
                     && var24.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var24, true)
                     && BotFunctions.checkCondition(var24, false)
                     && var24.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var24.teleToLocation(var13.getX(), var13.getY(), var13.getZ(), true);
                     QuestState var31 = var24.getQuestState(this.getName());
                     if (var31 != null) {
                        var31.exitQuest(true);
                     }

                     var24.setIsIn7sDungeon(false);
                  }
               }
            }

            var3.teleToLocation(var13.getX(), var13.getY(), var13.getZ(), true);
            var3.setIsIn7sDungeon(false);
            var5.exitQuest(true);
         } else if (Util.contains(RIFT_POSTERS, var6) && var5.getState() == 1) {
            Location var7 = RETURN_LOCS[var5.getInt("id")];
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var9 : var3.getParty().getMembers()) {
                  if (var9 != null
                     && var9.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var9, true)
                     && BotFunctions.checkCondition(var9, false)
                     && var9.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var9.teleToLocation(var7.getX(), var7.getY(), var7.getZ(), true);
                     QuestState var10 = var9.getQuestState(this.getName());
                     if (var10 != null) {
                        var10.exitQuest(true);
                     }
                  }
               }
            }

            var3.teleToLocation(var7.getX(), var7.getY(), var7.getZ(), true);
            var4 = "rift_back.htm";
            var5.exitQuest(true);
         }
      } else if (var1.equalsIgnoreCase("Festival")) {
         int var14 = var5.getInt("id");
         if (Util.contains(TOWN_DAWN, var14)) {
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var25 : var3.getParty().getMembers()) {
                  if (var25 != null
                     && var25.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var25, true)
                     && BotFunctions.checkCondition(var25, false)
                     && var25.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var25.teleToLocation(-80157, 111344, -4901, true);
                     var25.setIsIn7sDungeon(true);
                  }
               }
            }

            var3.teleToLocation(-80157, 111344, -4901, true);
            var3.setIsIn7sDungeon(true);
         } else if (Util.contains(TOWN_DUSK, var14)) {
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var26 : var3.getParty().getMembers()) {
                  if (var26 != null
                     && var26.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var26, true)
                     && BotFunctions.checkCondition(var26, false)
                     && var26.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var26.teleToLocation(-81261, 86531, -5157, true);
                     var26.setIsIn7sDungeon(true);
                  }
               }
            }

            var3.teleToLocation(-81261, 86531, -5157, true);
            var3.setIsIn7sDungeon(true);
         } else {
            var4 = "oracle1.htm";
         }
      } else if (var1.equalsIgnoreCase("Dimensional")) {
         var4 = "oracle.htm";
         if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
            for(Player var21 : var3.getParty().getMembers()) {
               if (var21 != null
                  && var21.getObjectId() != var3.getObjectId()
                  && Util.checkIfInRange(1000, var3, var21, true)
                  && BotFunctions.checkCondition(var21, false)
                  && var21.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                  var21.teleToLocation(-114755, -179466, -6752, true);
               }
            }
         }

         var3.teleToLocation(-114755, -179466, -6752, true);
      } else if (var1.equalsIgnoreCase("5.htm")) {
         int var16 = var5.getInt("id");
         if (var16 > -1) {
            var4 = "5a.htm";
         }

         int var22 = 0;

         for(int var12 : TELEPORTERS) {
            if (var12 == var6) {
               break;
            }

            ++var22;
         }

         if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
            for(Player var33 : var3.getParty().getMembers()) {
               if (var33 != null
                  && var33.getObjectId() != var3.getObjectId()
                  && Util.checkIfInRange(1000, var3, var33, true)
                  && BotFunctions.checkCondition(var33, false)
                  && var33.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                  QuestState var36 = var33.getQuestState(this.getName());
                  if (var36 == null) {
                     var36 = this.newQuestState(var33);
                  }

                  var36.setState((byte)1);
                  var36.set("id", Integer.toString(var22));
                  var33.teleToLocation(-114755, -179466, -6752, true);
               }
            }
         }

         var5.set("id", Integer.toString(var22));
         var5.setState((byte)1);
         var3.teleToLocation(-114755, -179466, -6752, true);
      } else if (var1.equalsIgnoreCase("6.htm")) {
         var4 = "6.htm";
         var5.exitQuest(true);
      } else if (var1.equalsIgnoreCase("zigurratDimensional")) {
         int var17 = var3.getLevel();
         if (var17 >= 20 && var17 < 30) {
            var5.takeItems(57, 2000L);
         } else if (var17 >= 30 && var17 < 40) {
            var5.takeItems(57, 4500L);
         } else if (var17 >= 40 && var17 < 50) {
            var5.takeItems(57, 8000L);
         } else if (var17 >= 50 && var17 < 60) {
            var5.takeItems(57, 12500L);
         } else if (var17 >= 60 && var17 < 70) {
            var5.takeItems(57, 18000L);
         } else if (var17 >= 70) {
            var5.takeItems(57, 24500L);
         }

         int var23 = 0;

         for(int var39 : TELEPORTERS) {
            if (var39 == var6) {
               break;
            }

            ++var23;
         }

         if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
            for(Player var35 : var3.getParty().getMembers()) {
               if (var35 != null
                  && var35.getObjectId() != var3.getObjectId()
                  && Util.checkIfInRange(1000, var3, var35, true)
                  && BotFunctions.checkCondition(var35, false)
                  && var35.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                  QuestState var38 = var35.getQuestState(this.getName());
                  if (var38 == null) {
                     var38 = this.newQuestState(var35);
                  }

                  var38.setState((byte)1);
                  var38.set("id", Integer.toString(var23));
                  var35.teleToLocation(-114755, -179466, -6752, true);
               }
            }
         }

         var5.set("id", Integer.toString(var23));
         var5.setState((byte)1);
         var5.playSound("ItemSound.quest_accept");
         var4 = "ziggurat_rift.htm";
         var3.teleToLocation(-114755, -179466, -6752, true);
      }

      return var4;
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      QuestState var4 = var2.getQuestState(this.getName());
      int var5 = var1.getId();
      if (Util.contains(TOWN_DAWN, var5)) {
         var4.setState((byte)1);
         int var6 = 0;

         for(int var10 : TELEPORTERS) {
            if (var10 == var5) {
               break;
            }

            ++var6;
         }

         if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
            for(Player var16 : var2.getParty().getMembers()) {
               if (var16 != null
                  && var16.getObjectId() != var2.getObjectId()
                  && Util.checkIfInRange(1000, var2, var16, true)
                  && BotFunctions.checkCondition(var16, false)
                  && var16.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                  QuestState var19 = var16.getQuestState(this.getName());
                  if (var19 == null) {
                     var19 = this.newQuestState(var16);
                  }

                  var19.setState((byte)1);
                  var19.set("id", Integer.toString(var6));
                  var16.teleToLocation(-80157, 111344, -4901, true);
                  var16.setIsIn7sDungeon(true);
               }
            }
         }

         var4.set("id", Integer.toString(var6));
         var4.playSound("ItemSound.quest_accept");
         var2.teleToLocation(-80157, 111344, -4901, true);
         var2.setIsIn7sDungeon(true);
      }

      if (Util.contains(TOWN_DUSK, var5)) {
         var4.setState((byte)1);
         int var11 = 0;

         for(int var22 : TELEPORTERS) {
            if (var22 == var5) {
               break;
            }

            ++var11;
         }

         if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
            for(Player var18 : var2.getParty().getMembers()) {
               if (var18 != null
                  && var18.getObjectId() != var2.getObjectId()
                  && Util.checkIfInRange(1000, var2, var18, true)
                  && BotFunctions.checkCondition(var18, false)
                  && var18.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                  QuestState var21 = var18.getQuestState(this.getName());
                  if (var21 == null) {
                     var21 = this.newQuestState(var18);
                  }

                  var21.setState((byte)1);
                  var21.set("id", Integer.toString(var11));
                  var18.teleToLocation(-81261, 86531, -5157, true);
                  var18.setIsIn7sDungeon(true);
               }
            }
         }

         var4.set("id", Integer.toString(var11));
         var4.playSound("ItemSound.quest_accept");
         var2.teleToLocation(-81261, 86531, -5157, true);
         var2.setIsIn7sDungeon(true);
      } else if (var5 >= 31494 && var5 <= 31507) {
         if (var2.getLevel() < 20) {
            var3 = "1.htm";
            var4.exitQuest(true);
         } else if (var2.getAllActiveQuests().length > 23) {
            var3 = "1a.htm";
            var4.exitQuest(true);
         } else if (!var4.hasQuestItems(7079)) {
            var3 = "3.htm";
         } else {
            var4.setState((byte)0);
            var3 = "4.htm";
         }
      } else if (var5 >= 31095 && var5 <= 31111 || var5 >= 31114 && var5 <= 31126) {
         int var12 = var2.getLevel();
         if (var12 < 20) {
            var3 = "ziggurat_lowlevel.htm";
            var4.exitQuest(true);
         } else if (var2.getAllActiveQuests().length > 40) {
            var2.sendPacket(SystemMessageId.TOO_MANY_QUESTS);
            var4.exitQuest(true);
         } else if (!var4.hasQuestItems(7079)) {
            var3 = "ziggurat_nofrag.htm";
            var4.exitQuest(true);
         } else if (var12 >= 20 && var12 < 30 && var4.getQuestItemsCount(57) < 2000L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else if (var12 >= 30 && var12 < 40 && var4.getQuestItemsCount(57) < 4500L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else if (var12 >= 40 && var12 < 50 && var4.getQuestItemsCount(57) < 8000L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else if (var12 >= 50 && var12 < 60 && var4.getQuestItemsCount(57) < 12500L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else if (var12 >= 60 && var12 < 70 && var4.getQuestItemsCount(57) < 18000L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else if (var12 >= 70 && var4.getQuestItemsCount(57) < 24500L) {
            var3 = "ziggurat_noadena.htm";
            var4.exitQuest(true);
         } else {
            var3 = "ziggurat.htm";
         }
      }

      return var3;
   }

   public static void main(String[] var0) {
      new OracleTeleport(-1, OracleTeleport.class.getSimpleName(), "teleports");
   }
}
