package l2e.gameserver.model.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.communityhandlers.impl.CommunityBuffer;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.buffer.SingleBuff;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class BotFunctions {
   protected static final Logger _log = Logger.getLogger(BotFunctions.class.getName());
   protected static boolean ALLOW_AUTO_CB_TELEPORT_BY_ID;
   private static boolean ALLOW_AUTO_CB_TELEPORT_BY_COORDS;
   private static boolean ALLOW_AUTO_CB_TELEPORT_TO_RAID;
   private static boolean ALLOW_AUTO_GOTO_TELEPORT;
   private static boolean ALLOW_AUTO_VITALITY;
   private static boolean ALLOW_AUTO_BUFF_CB_SETS;
   private static long ADENA_MIN_LIMIT;

   protected BotFunctions() {
      this.load();
   }

   private final void load() {
      Properties var1 = new Properties();
      File var2 = new File("./config/mods/botFunctions.ini");

      try (FileInputStream var3 = new FileInputStream(var2)) {
         var1.load(var3);
      } catch (Exception var16) {
      }

      ALLOW_AUTO_CB_TELEPORT_BY_ID = Boolean.parseBoolean(var1.getProperty("AllowAutoCBTeleById", "False"));
      ALLOW_AUTO_CB_TELEPORT_BY_COORDS = Boolean.parseBoolean(var1.getProperty("AllowAutoCBTeleByCoords", "False"));
      ALLOW_AUTO_CB_TELEPORT_TO_RAID = Boolean.parseBoolean(var1.getProperty("AllowAutoCBTeleToRaid", "False"));
      ALLOW_AUTO_GOTO_TELEPORT = Boolean.parseBoolean(var1.getProperty("AllowAutoGotoTeleport", "False"));
      ALLOW_AUTO_VITALITY = Boolean.parseBoolean(var1.getProperty("AllowAutoVitality", "False"));
      ALLOW_AUTO_BUFF_CB_SETS = Boolean.parseBoolean(var1.getProperty("AllowAutoBuffSets", "False"));
      ADENA_MIN_LIMIT = Long.parseLong(var1.getProperty("AdenaMinLimit", "50000"));
   }

   public boolean isAutoBuffEnable(Player var1) {
      if (!ALLOW_AUTO_BUFF_CB_SETS) {
         return false;
      } else if (var1.getVarB("autoBuff@", false) && var1.getParty() != null) {
         if (!var1.getParty().isLeader(var1)) {
            var1.sendMessage("Only party leader can use this function!");
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public void getAutoBuffSet(Player var1) {
      if (this.isAutoBuffEnable(var1)) {
         boolean var2 = true;

         for(Player var4 : var1.getParty().getMembers()) {
            if (var4 != null && var4.getIPAddress().equalsIgnoreCase(var1.getIPAddress()) && !checkCondition(var4, true)) {
               var2 = false;
            }
         }

         if (!var2) {
            var1.sendMessage("Wrong conditions!!!");
         } else {
            for(Player var8 : var1.getParty().getMembers()) {
               if (var8 != null && var8.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                  int var5 = getPlayerGroupSet(var8);
                  if (var5 != -1) {
                     ArrayList var6 = CommunityBuffer.getInstance().getSetBuffs(var5);
                     if (var6 != null && !var6.isEmpty()) {
                        getBuffsToPlayer(var8, var6);
                     }
                  }
               }
            }
         }
      }
   }

   private static void getBuffsToPlayer(Player var0, ArrayList<SingleBuff> var1) {
      for(SingleBuff var3 : var1) {
         int var4 = var0.hasPremiumBonus() ? var3.getPremiumLevel() : var3.getLevel();
         Skill var5 = SkillsParser.getInstance().getInfo(var3.getSkillId(), var4);
         if (var5 != null) {
            int var6 = CommunityBuffer.getBuffTime(var0, var5.getId());
            if (var6 > 0 && var5.hasEffects()) {
               Env var7 = new Env();
               var7.setCharacter(var0);
               var7.setTarget(var0);
               var7.setSkill(var5);

               for(EffectTemplate var11 : var5.getEffectTemplates()) {
                  Effect var12 = var11.getEffect(var7);
                  if (var12 != null) {
                     var12.setAbnormalTime(var6 * 60);
                     var12.scheduleEffect(true);
                  }
               }
            } else {
               var5.getEffects(var0, var0, false);
            }
         }
      }
   }

   private static int getPlayerGroupSet(Player var0) {
      for(int var2 : CommunityBuffer.getInstance().getBuffClasses().keySet()) {
         List var3 = CommunityBuffer.getInstance().getBuffClasses().get(var2);
         if (var3 != null && !var3.isEmpty() && var3.contains(var0.getClassId().getId())) {
            return var2;
         }
      }

      return -1;
   }

   public void getAutoVitality(Player var1) {
      if (ALLOW_AUTO_VITALITY) {
         if (var1.getVarB("autoVitality@", false) && var1.getParty() != null) {
            if (!var1.getParty().isLeader(var1)) {
               var1.sendMessage("Only party leader can use this function!");
            } else {
               for(Player var3 : var1.getParty().getMembers()) {
                  if (var3 != null
                     && Util.checkIfInRange(1000, var1, var3, true)
                     && var3.getObjectId() != var1.getObjectId()
                     && checkCondition(var3, false)
                     && var3.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                     getVitalityToPlayer(var3);
                  }
               }
            }
         }
      }
   }

   private static void getVitalityToPlayer(Player var0) {
      Quest var1 = QuestManager.getInstance().getQuest("GiftOfVitality");
      if (var1 != null) {
         QuestState var2 = var0.getQuestState(var1.getName());
         if (var2 == null) {
            var2 = var1.newQuestState(var0);
         }

         long var3 = var2.get("reuse") != null ? Long.parseLong(var2.get("reuse")) : 0L;
         if (var3 > System.currentTimeMillis()) {
            long var5 = (var3 - System.currentTimeMillis()) / 1000L;
            int var7 = (int)(var5 / 3600L);
            int var8 = (int)(var5 % 3600L / 60L);
            SystemMessage var9 = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
            var9.addSkillName(23179);
            var9.addNumber(var7);
            var9.addNumber(var8);
            var0.sendPacket(var9);
         } else {
            var0.doCast(new SkillHolder(23179, 1).getSkill());
            var0.doCast(new SkillHolder(23180, 1).getSkill());
            var2.setState((byte)1);
            var2.set("reuse", String.valueOf(System.currentTimeMillis() + 18000000L));
         }
      }
   }

   public boolean isAutoTpToRaidEnable(Player var1) {
      if (!ALLOW_AUTO_CB_TELEPORT_TO_RAID) {
         return false;
      } else {
         return var1.getVarB("autoTeleport@", false) && var1.getParty() != null;
      }
   }

   public void getAutoTeleportToRaid(Player var1, Location var2, Location var3) {
      this.getAutoTeleportById(var1, var2, var3, 0);
   }

   public boolean isAutoTpGotoEnable(Player var1) {
      if (!ALLOW_AUTO_GOTO_TELEPORT) {
         return false;
      } else {
         return var1.getVarB("autoTeleport@", false) && var1.getParty() != null;
      }
   }

   public void getAutoGotoTeleport(Player var1, Location var2, Location var3) {
      this.getAutoTeleportById(var1, var2, var3, 1000);
   }

   public boolean isAutoTpByCoordsEnable(Player var1) {
      if (!ALLOW_AUTO_CB_TELEPORT_BY_COORDS) {
         return false;
      } else {
         return var1.getVarB("autoTeleport@", false) && var1.getParty() != null;
      }
   }

   public void getAutoTeleportByCoords(Player var1, Location var2, Location var3) {
      this.getAutoTeleportById(var1, var2, var3, 0);
   }

   public boolean isAutoTpByIdEnable(Player var1) {
      if (!ALLOW_AUTO_CB_TELEPORT_BY_ID) {
         return false;
      } else {
         return var1.getVarB("autoTeleport@", false) && var1.getParty() != null;
      }
   }

   public void getAutoTeleportById(Player var1, Location var2, Location var3, int var4) {
      if (!var1.getParty().isLeader(var1)) {
         var1.sendMessage("Only party leader can use this function!");
      } else {
         boolean var5 = true;

         for(Player var7 : var1.getParty().getMembers()) {
            if (var7 != null && var7.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
               if (var4 > 0 && !Util.checkIfInRange(var4, var2.getX(), var2.getY(), var2.getZ(), var7, true)) {
                  var5 = false;
               }

               if (!checkCondition(var7, false)) {
                  var5 = false;
               }
            }
         }

         if (!var5) {
            var1.sendMessage("Wrong conditions!!!");
         } else {
            for(Player var10 : var1.getParty().getMembers()) {
               if (var10 != null && var10.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                  Location var8 = Location.findAroundPosition(var3.getX(), var3.getY(), var3.getZ(), 40, 60, var1.getGeoIndex());
                  var10.teleToLocation(var8.getX(), var8.getY(), var8.getZ(), true);
               }
            }
         }
      }
   }

   public void getAutoTeleToTown(Player var1) {
      if (var1.getParty() != null) {
         if (!var1.getParty().isLeader(var1)) {
            var1.sendMessage("Only party leader can use this function!");
         } else {
            for(Player var3 : var1.getParty().getMembers()) {
               if (var3 != null && var3.getObjectId() != var1.getObjectId() && var3.isDead() && var3.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                  getTeleToTown(var3);
               }
            }
         }
      }
   }

   public void getAutoTransferAdena(Player var1) {
      if (var1.getParty() != null) {
         if (!var1.getParty().isLeader(var1)) {
            var1.sendMessage("Only party leader can use this function!");
         } else {
            for(Player var3 : var1.getParty().getMembers()) {
               if (var3 != null && var3.getObjectId() != var1.getObjectId() && var3.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                  getTransferAdena(var1, var3);
               }
            }
         }
      }
   }

   private static void getTransferAdena(Player var0, Player var1) {
      long var2 = var1.getAdena();
      if (var2 > ADENA_MIN_LIMIT) {
         long var4 = var1.getAdena() - ADENA_MIN_LIMIT;
         if (var4 > 0L) {
            var1.destroyItemByItemId("TransferAdena", 57, var4, var1, true);
            var0.getInventory().addItem("TransferAdena", 57, var4, var0, true);
         }
      }
   }

   private static void getTeleToTown(Player var0) {
      Location var1 = MapRegionManager.getInstance().getTeleToLocation(var0, TeleportWhereType.TOWN);
      if (var1 != null) {
         if (var0.getReflectionId() > 0) {
            var0.setReflectionId(0);
         }

         var0.setIsIn7sDungeon(false);
         var0.setIsPendingRevive(true);
         var0.teleToLocation(var1, true);
      }
   }

   public static boolean checkCondition(Player var0, boolean var1) {
      if (var0 == null) {
         return false;
      } else if (!var0.isInCombat()
         && !var0.isCombatFlagEquipped()
         && !var0.isBlocked()
         && !var0.isCursedWeaponEquipped()
         && !var0.isInDuel()
         && !var0.isFlying()
         && !var0.isJailed()
         && !var0.isInOlympiadMode()
         && !var0.inObserverMode()
         && !var0.isAlikeDead()
         && !var0.isInSiege()
         && !var0.isDead()) {
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(var0.getObjectId())) {
            return false;
         } else if (!var0.isInsideZone(ZoneId.PVP)) {
            return !var1 || !Config.ALLOW_COMMUNITY_PEACE_ZONE || var0.isInsideZone(ZoneId.PEACE);
         } else {
            if (var0.isInsideZone(ZoneId.FUN_PVP)) {
               FunPvpZone var2 = ZoneManager.getInstance().getZone(var0, FunPvpZone.class);
               if (var2 != null && (var1 && var2.canUseCbBuffs() || !var1 && var2.canUseCbTeleports())) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isAutoDropEnable(Player var1) {
      if (var1.getParty() == null) {
         return false;
      } else {
         Player var2 = var1.getParty().getLeader();
         return var2 != null
            && var2.getVarB("autoDrop@", false)
            && var2.getIPAddress().equalsIgnoreCase(var1.getIPAddress())
            && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, var1, var2, true);
      }
   }

   public boolean isAutoSpoilEnable(Player var1) {
      if (var1.getParty() == null) {
         return false;
      } else {
         Player var2 = var1.getParty().getLeader();
         return var2 != null
            && var2.getVarB("autoSpoil@", false)
            && var2.getIPAddress().equalsIgnoreCase(var1.getIPAddress())
            && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, var1, var2, true);
      }
   }

   public static final BotFunctions getInstance() {
      return BotFunctions.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final BotFunctions _instance = new BotFunctions();
   }
}
