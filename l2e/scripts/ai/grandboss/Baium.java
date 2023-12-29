package l2e.scripts.ai.grandboss;

import java.util.ArrayList;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.scripts.ai.AbstractNpcAI;

public class Baium extends AbstractNpcAI {
   private static final Location[] TELEPORT_OUT_LOC = new Location[]{
      new Location(108784, 16000, -4928), new Location(113824, 10448, -5164), new Location(115488, 22096, -5168)
   };
   private static final Location[] ARCHANGEL_LOC = new Location[]{
      new Location(115792, 16608, 10136, 0),
      new Location(115168, 17200, 10136, 0),
      new Location(115780, 15564, 10136, 13620),
      new Location(114880, 16236, 10136, 5400),
      new Location(114239, 17168, 10136, -1992)
   };
   private static final NoRestartZone _zone = ZoneManager.getInstance().getZoneById(70051, NoRestartZone.class);
   private GrandBossInstance _baium = null;
   private Npc _statue = null;
   private long _lastAttack = 0L;

   private Baium(String var1, String var2) {
      super(var1, var2);
      this.addTalkId(new int[]{31862, 31842, 29025});
      this.addStartNpc(new int[]{31862, 31842, 29025});
      this.addAttackId(new int[]{29020, 29021});
      this.addKillId(29020);
      this.addSpellFinishedId(new int[]{29020});
      StatsSet var5 = EpicBossManager.getInstance().getStatsSet(29020);
      double var6 = var5.getDouble("currentHP");
      double var8 = var5.getDouble("currentMP");
      int var10 = var5.getInteger("loc_x");
      int var11 = var5.getInteger("loc_y");
      int var12 = var5.getInteger("loc_z");
      int var13 = var5.getInteger("heading");
      long var14 = var5.getLong("respawnTime");
      switch(this.getStatus()) {
         case 1:
            this.setStatus(0);
         case 0:
            this._statue = addSpawn(29025, new Location(116033, 17447, 10107, -25348), false, 0L);
            break;
         case 2:
            this._baium = (GrandBossInstance)addSpawn(29020, var10, var11, var12, var13, false, 0L);
            this._baium.setCurrentHpMp(var6, var8);
            this._lastAttack = System.currentTimeMillis();
            this.addBoss(this._baium);

            for(Location var19 : ARCHANGEL_LOC) {
               addSpawn(29021, var19, false, 0L, true);
            }

            this.startQuestTimer("CHECK_ATTACK", 60000L, this._baium, null);
            break;
         case 3:
            long var3 = var14 - System.currentTimeMillis();
            if (var3 > 0L) {
               this.startQuestTimer("CLEAR_STATUS", var3, null, null);
            } else {
               this.notifyEvent("CLEAR_STATUS", null, null);
            }
      }
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      switch(var1) {
         case "teleportOut":
            Location var4 = TELEPORT_OUT_LOC[getRandom(TELEPORT_OUT_LOC.length)];
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var18 : var3.getParty().getMembers()) {
                  if (var18 != null
                     && var18.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var18, true)
                     && BotFunctions.checkCondition(var18, false)
                     && var18.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     var18.teleToLocation(var4.getX() + getRandom(100), var4.getY() + getRandom(100), var4.getZ(), true, true);
                  }
               }
            }

            var3.teleToLocation(var4.getX() + getRandom(100), var4.getY() + getRandom(100), var4.getZ(), true, true);
            break;
         case "wakeUp":
            if (this.getStatus() == 0) {
               var2.deleteMe();
               this._statue = null;
               this.setStatus(2);
               this._baium = (GrandBossInstance)addSpawn(29020, new Location(116033, 17447, 10107, -25348), false, 0L);
               this._baium.disableCoreAI(true);
               this.addBoss(this._baium);
               this._lastAttack = System.currentTimeMillis();
               this.startQuestTimer("WAKEUP_ACTION", 50L, this._baium, null);
               this.startQuestTimer("MANAGE_EARTHQUAKE", 2000L, this._baium, null);
               this.startQuestTimer("SOCIAL_ACTION", 10000L, this._baium, var3);
               this.startQuestTimer("CHECK_ATTACK", 60000L, this._baium, null);
            }
            break;
         case "WAKEUP_ACTION":
            if (var2 != null) {
               _zone.broadcastPacket(new SocialAction(this._baium.getObjectId(), 2));
            }
            break;
         case "MANAGE_EARTHQUAKE":
            if (var2 != null) {
               _zone.broadcastPacket(new EarthQuake(var2.getX(), var2.getY(), var2.getZ(), 40, 10));
               _zone.broadcastPacket(new PlaySound("BS02_A"));
            }
            break;
         case "SOCIAL_ACTION":
            if (var2 != null) {
               _zone.broadcastPacket(new SocialAction(var2.getObjectId(), 3));
               this.startQuestTimer("PLAYER_PORT", 6000L, var2, var3);
            }
            break;
         case "PLAYER_PORT":
            if (var2 != null) {
               if (var3 != null && var3.isInsideRadius(var2, 16000, true, false)) {
                  var3.teleToLocation(new Location(115910, 17337, 10105), false);
                  this.startQuestTimer("PLAYER_KILL", 3000L, var2, var3);
               } else {
                  Player var14 = this.getRandomPlayer(var2);
                  if (var14 != null) {
                     var14.teleToLocation(new Location(115910, 17337, 10105), false);
                     this.startQuestTimer("PLAYER_KILL", 3000L, var2, var14);
                  } else {
                     this.startQuestTimer("PLAYER_KILL", 3000L, var2, null);
                  }
               }
            }
            break;
         case "PLAYER_KILL":
            if (var3 != null && var3.isInsideRadius(var2, 16000, true, false)) {
               _zone.broadcastPacket(new SocialAction(var2.getObjectId(), 1));
               var2.broadcastSay(22, NpcStringId.HOW_DARE_YOU_WAKE_ME_NOW_YOU_SHALL_DIE, var3.getName());
               var2.setTarget(var3);
               var2.doCast(new SkillHolder(4136, 1).getSkill());
            }

            for(Player var17 : _zone.getPlayersInside()) {
               if (var17.isHero()) {
                  _zone.broadcastPacket(
                     new ExShowScreenMessage(
                        NpcStringId.NOT_EVEN_THE_GODS_THEMSELVES_COULD_TOUCH_ME_BUT_YOU_S1_YOU_DARE_CHALLENGE_ME_IGNORANT_MORTAL, 2, 4000, var17.getName()
                     )
                  );
                  break;
               }
            }

            this.startQuestTimer("SPAWN_ARCHANGEL", 8000L, var2, var3);
            break;
         case "SPAWN_ARCHANGEL":
            this._baium.disableCoreAI(false);

            for(Location var11 : ARCHANGEL_LOC) {
               addSpawn(29021, var11, false, 0L, true);
            }

            if (var3 != null && !var3.isDead()) {
               this.attackPlayer((Attackable)var2, var3);
            } else {
               Player var5 = this.getRandomPlayer(var2);
               if (var5 != null) {
                  this.attackPlayer((Attackable)var2, var5);
               }
            }
            break;
         case "CHECK_ATTACK":
            if (var2 != null && this._lastAttack + 1800000L < System.currentTimeMillis()) {
               this.notifyEvent("CLEAR_ZONE", null, null);
               if (this._statue != null) {
                  this._statue.deleteMe();
                  this._statue = null;
               }

               this._statue = addSpawn(29025, new Location(116033, 17447, 10107, -25348), false, 0L);
               this.setStatus(0);
            } else if (var2 != null) {
               if (this._lastAttack + 300000L < System.currentTimeMillis() && var2.getCurrentHp() < var2.getMaxHp() * 0.75) {
                  var2.setTarget(var2);
                  var2.doCast(new SkillHolder(4135, 1).getSkill());
               }

               this.startQuestTimer("CHECK_ATTACK", 60000L, var2, null);
            }
            break;
         case "CLEAR_STATUS":
            this.setStatus(0);
            if (this._statue != null) {
               this._statue.deleteMe();
               this._statue = null;
            }

            this._statue = addSpawn(29025, new Location(116033, 17447, 10107, -25348), false, 0L);
            break;
         case "CLEAR_ZONE":
            for(Creature var9 : _zone.getCharactersInside()) {
               if (var9 != null && var9.getReflectionId() == 0) {
                  if (var9.isNpc()) {
                     var9.deleteMe();
                  } else if (var9.isPlayer()) {
                     this.notifyEvent("teleportOut", null, (Player)var9);
                  }
               }
            }
      }

      return super.onAdvEvent(var1, var2, var3);
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      switch(var1.getId()) {
         case 31862:
            if (var2.isFlying()) {
               return "<html><body>" + ServerStorage.getInstance().getString(var2.getLang(), "Baium.PLAYER_FLY") + "</body></html>";
            } else if (this.getStatus() == 3) {
               return "<html><body>" + ServerStorage.getInstance().getString(var2.getLang(), "Baium.DEAD") + "</body></html>";
            } else if (this.getStatus() == 2) {
               return "<html><body>" + ServerStorage.getInstance().getString(var2.getLang(), "Baium.IN_FIGHT") + "</body></html>";
            } else if (!hasQuestItems(var2, 4295)) {
               return "<html><body>" + ServerStorage.getInstance().getString(var2.getLang(), "Baium.NOT_ITEM") + "</body></html>";
            } else {
               if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                  for(Player var5 : var2.getParty().getMembers()) {
                     if (var5 != null
                        && var5.getObjectId() != var2.getObjectId()
                        && Util.checkIfInRange(1000, var2, var5, true)
                        && var5.getInventory().getItemByItemId(4295) != null
                        && BotFunctions.checkCondition(var5, false)
                        && var5.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                        takeItems(var5, 4295, 1L);
                        var5.teleToLocation(114077, 15882, 10078, true);
                     }
                  }
               }

               takeItems(var2, 4295, 1L);
               var2.teleToLocation(new Location(114077, 15882, 10078), true);
            }
         default:
            return "";
      }
   }

   @Override
   public String onSpellFinished(Npc var1, Player var2, Skill var3) {
      if (!_zone.isCharacterInZone(var1) && this._baium != null) {
         this._baium.teleToLocation(new Location(116033, 17447, 10107, -25348), true);
      }

      return super.onSpellFinished(var1, var2, var3);
   }

   @Override
   public String onAttack(Npc var1, Player var2, int var3, boolean var4, Skill var5) {
      if (var2 != null && (var2.isPlayer() || var2.isSummon())) {
         this._lastAttack = System.currentTimeMillis();
         if (var1.getId() == 29020
            && var2.getMountType() == MountType.STRIDER
            && var2.getFirstEffect(new SkillHolder(4258, 1).getId()) == null
            && !var1.isSkillDisabled(new SkillHolder(4258, 1).getSkill())) {
            var1.setTarget(var2);
            var1.doCast(new SkillHolder(4258, 1).getSkill());
         }
      }

      return this.onAttack(var1, var2, var3, var4);
   }

   @Override
   public String onKill(Npc var1, Player var2, boolean var3) {
      if (_zone.isCharacterInZone(var2)) {
         addSpawn(31842, new Location(115017, 15549, 10090), false, 900000L);
         _zone.broadcastPacket(new PlaySound("BS01_D"));
         long var4 = EpicBossManager.getInstance().setRespawnTime(29020, Config.BAIUM_RESPAWN_PATTERN);
         this.startQuestTimer("CLEAR_STATUS", var4 - System.currentTimeMillis(), null, null);
         this.startQuestTimer("CLEAR_ZONE", 900000L, null, null);
         this.cancelQuestTimer("CHECK_ATTACK", var1, null);

         for(Npc var7 : World.getInstance().getAroundNpc(var1, 6000, 200)) {
            if (var7.getId() == 29021) {
               var7.deleteMe();
            }
         }
      }

      return super.onKill(var1, var2, var3);
   }

   private int getStatus() {
      return EpicBossManager.getInstance().getBossStatus(29020);
   }

   private void addBoss(GrandBossInstance var1) {
      EpicBossManager.getInstance().addBoss(var1);
   }

   private void setStatus(int var1) {
      EpicBossManager.getInstance().setBossStatus(29020, var1, true);
   }

   private Player getRandomPlayer(Npc var1) {
      ArrayList var2 = new ArrayList();

      for(Player var4 : World.getInstance().getAroundPlayers(var1, 6000, 200)) {
         if (var4 != null && !var4.isDead() && !var4.isInvisible() && GeoEngine.canSeeTarget(var1, var4, false)) {
            var2.add(var4);
         }
      }

      return var2.isEmpty() ? null : (Player)var2.get(getRandom(var2.size()));
   }

   @Override
   public boolean unload(boolean var1) {
      if (this._statue != null) {
         this._statue.deleteMe();
         this._statue = null;
      }

      this.notifyEvent("CLEAR_ZONE", null, null);
      return super.unload(var1);
   }

   public static void main(String[] var0) {
      new Baium(Baium.class.getSimpleName(), "ai");
   }
}
