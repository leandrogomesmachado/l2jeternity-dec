package l2e.scripts.custom;

import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.scripts.ai.AbstractNpcAI;

public final class Sailren extends AbstractNpcAI {
   private final NoRestartZone zone = ZoneManager.getInstance().getZoneById(70049, NoRestartZone.class);
   private Sailren.Status _status = Sailren.Status.ALIVE;
   private int _killCount = 0;
   private long _lastAttack = 0L;
   private ScheduledFuture<?> _monsterSpawnTask = null;
   private ScheduledFuture<?> _timeOutTask = null;
   private ScheduledFuture<?> _lastActionTask = null;
   private ScheduledFuture<?> _unlockTask = null;

   private Sailren() {
      super(Sailren.class.getSimpleName(), "custom");
      this.addStartNpc(new int[]{32109, 32107});
      this.addTalkId(new int[]{32109, 32107});
      this.addFirstTalkId(32109);
      this.addKillId(new int[]{22218, 22199, 22217, 29065});
      this.addAttackId(new int[]{22218, 22199, 22217, 29065});
      long remain = ServerVariables.getLong("SailrenRespawn", 0L) - System.currentTimeMillis();
      if (remain > 0L) {
         this._status = Sailren.Status.DEAD;
         this._unlockTask = ThreadPoolManager.getInstance().schedule(new Sailren.UnlockSailren(), remain);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      if (npc == null) {
         return htmltext;
      } else {
         switch(event) {
            case "32109-01.htm":
               htmltext = "32109-01.htm";
               break;
            case "32109-01a.htm":
               htmltext = "32109-01a.htm";
               break;
            case "32109-02a.htm":
               htmltext = "32109-02a.htm";
               break;
            case "32109-03a.htm":
               htmltext = "32109-03a.htm";
               break;
            case "enter":
               if (!player.isInParty()) {
                  htmltext = "32109-01.htm";
               } else if (this._status == Sailren.Status.DEAD) {
                  htmltext = "32109-04.htm";
               } else if (this._status == Sailren.Status.IN_FIGHT) {
                  htmltext = "32109-05.htm";
               } else if (!player.getParty().isLeader(player)) {
                  htmltext = "32109-03.htm";
               } else if (!hasQuestItems(player, 8784)) {
                  htmltext = "32109-02.htm";
               } else {
                  takeItems(player, 1, 8784L);
                  this._status = Sailren.Status.IN_FIGHT;
                  this._lastAttack = System.currentTimeMillis();

                  for(Player member : player.getParty().getMembers()) {
                     if (member.isInsideRadius(npc, 1000, true, false)) {
                        member.teleToLocation(27549, -6638, -2008, true);
                     }
                  }

                  this.cleanUpStatus();
                  this._monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new Sailren.SpawnTask(1), 60000L);
                  this._timeOutTask = ThreadPoolManager.getInstance().schedule(new Sailren.TimeOut(), 3200000L);
                  this._lastActionTask = ThreadPoolManager.getInstance().schedule(new Sailren.LastAttack(), 120000L);
               }
               break;
            case "teleportOut":
               player.teleToLocation(TeleportWhereType.TOWN, true);
               break;
            case "SPAWN_SAILREN":
               GrandBossInstance sailren = (GrandBossInstance)addSpawn(29065, 27549, -6638, -2008, 0, false, 0L);
               Npc movieNpc = addSpawn(32110, sailren.getX(), sailren.getY(), sailren.getZ() + 30, 0, false, 26000L);
               sailren.setIsInvul(true);
               sailren.setIsImmobilized(true);
               this.zone.broadcastPacket(new SpecialCamera(movieNpc, 60, 110, 30, 4000, 1500, 20000, 0, 65, 1, 0, 0));
               this.startQuestTimer("ATTACK", 24600L, sailren, null);
               this.startQuestTimer("ANIMATION", 2000L, movieNpc, null);
               this.startQuestTimer("CAMERA_1", 4100L, movieNpc, null);
               break;
            case "ANIMATION":
               if (npc != null) {
                  npc.setTarget(npc);
                  npc.doCast(new SkillHolder(5090, 1).getSkill());
                  this.startQuestTimer("ANIMATION", 2000L, npc, null);
               }
               break;
            case "CAMERA_1":
               this.zone.broadcastPacket(new SpecialCamera(npc, 100, 180, 30, 3000, 1500, 20000, 0, 50, 1, 0, 0));
               this.startQuestTimer("CAMERA_2", 3000L, npc, null);
               break;
            case "CAMERA_2":
               this.zone.broadcastPacket(new SpecialCamera(npc, 150, 270, 25, 3000, 1500, 20000, 0, 30, 1, 0, 0));
               this.startQuestTimer("CAMERA_3", 3000L, npc, null);
               break;
            case "CAMERA_3":
               this.zone.broadcastPacket(new SpecialCamera(npc, 160, 360, 20, 3000, 1500, 20000, 10, 15, 1, 0, 0));
               this.startQuestTimer("CAMERA_4", 3000L, npc, null);
               break;
            case "CAMERA_4":
               this.zone.broadcastPacket(new SpecialCamera(npc, 160, 450, 10, 3000, 1500, 20000, 0, 10, 1, 0, 0));
               this.startQuestTimer("CAMERA_5", 3000L, npc, null);
               break;
            case "CAMERA_5":
               this.zone.broadcastPacket(new SpecialCamera(npc, 160, 560, 0, 3000, 1500, 20000, 0, 10, 1, 0, 0));
               this.startQuestTimer("CAMERA_6", 7000L, npc, null);
               break;
            case "CAMERA_6":
               this.zone.broadcastPacket(new SpecialCamera(npc, 70, 560, 0, 500, 1500, 7000, -15, 20, 1, 0, 0));
               break;
            case "ATTACK":
               npc.setIsInvul(false);
               npc.setIsImmobilized(false);
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (this.zone.isCharacterInZone(attacker)) {
         this._lastAttack = System.currentTimeMillis();
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (this.zone.isCharacterInZone(killer)) {
         switch(npc.getId()) {
            case 22199:
               Attackable trex = (Attackable)addSpawn(22217, 27313, -6766, -1975, 0, false, 0L);
               this.attackPlayer(trex, killer);
               break;
            case 22217:
               if (this._monsterSpawnTask != null) {
                  this._monsterSpawnTask.cancel(true);
                  this._monsterSpawnTask = null;
               }

               this._monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new Sailren.SpawnTask(2), 180000L);
               break;
            case 22218:
               ++this._killCount;
               if (this._killCount == 3) {
                  Attackable pterosaur = (Attackable)addSpawn(22199, 27313, -6766, -1975, 0, false, 0L);
                  this.attackPlayer(pterosaur, killer);
                  this._killCount = 0;
               }
               break;
            case 29065:
               this._status = Sailren.Status.DEAD;
               addSpawn(32107, 27644, -6638, -2008, 0, false, 300000L);
               long respawnTime = EpicBossManager.getInstance().setRespawnTime(29065, Config.SAILREN_RESPAWN_PATTERN);
               ServerVariables.set("SailrenRespawn", String.valueOf(respawnTime));
               this.cleanUpStatus();
               this._unlockTask = ThreadPoolManager.getInstance().schedule(new Sailren.UnlockSailren(), respawnTime);
               this._timeOutTask = ThreadPoolManager.getInstance().schedule(new Sailren.TimeOut(), 300000L);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      this.cleanUpStatus();
      if (this._status == Sailren.Status.IN_FIGHT) {
         this._log.info(this.getClass().getSimpleName() + ": Script is being unloaded while Sailren is active, clearing zone.");
         this._timeOutTask = ThreadPoolManager.getInstance().schedule(new Sailren.TimeOut(), 100L);
      }

      return super.unload(removeFromList);
   }

   private void cleanUpStatus() {
      this._killCount = 0;
      if (this._unlockTask != null) {
         this._unlockTask.cancel(true);
         this._unlockTask = null;
      }

      if (this._monsterSpawnTask != null) {
         this._monsterSpawnTask.cancel(true);
         this._monsterSpawnTask = null;
      }

      if (this._timeOutTask != null) {
         this._timeOutTask.cancel(true);
         this._timeOutTask = null;
      }

      if (this._lastActionTask != null) {
         this._lastActionTask.cancel(true);
         this._lastActionTask = null;
      }
   }

   public static void main(String[] args) {
      new Sailren();
   }

   private class LastAttack implements Runnable {
      private LastAttack() {
      }

      @Override
      public void run() {
         if (!Sailren.this.zone.getPlayersInside().isEmpty() && Sailren.this._lastAttack + 600000L < System.currentTimeMillis()) {
            if (Sailren.this._timeOutTask != null) {
               Sailren.this._timeOutTask.cancel(true);
               Sailren.this._timeOutTask = null;
            }

            Sailren.this._timeOutTask = ThreadPoolManager.getInstance().schedule(Sailren.this.new TimeOut(), 100L);
         } else {
            if (Sailren.this._lastActionTask != null) {
               Sailren.this._lastActionTask.cancel(true);
               Sailren.this._lastActionTask = null;
            }

            Sailren.this._lastActionTask = ThreadPoolManager.getInstance().schedule(Sailren.this.new LastAttack(), 120000L);
         }
      }
   }

   private class SpawnTask implements Runnable {
      private int _taskId = 0;

      public SpawnTask(int taskId) {
         this._taskId = taskId;
      }

      @Override
      public void run() {
         switch(this._taskId) {
            case 1:
               for(int i = 0; i < 3; ++i) {
                  Quest.addSpawn(22218, 27313 + Quest.getRandom(150), -6766 + Quest.getRandom(150), -1975, 0, false, 0L);
               }
               break;
            case 2:
               GrandBossInstance sailren = (GrandBossInstance)Quest.addSpawn(29065, 27549, -6638, -2008, 0, false, 0L);
               Npc movieNpc = Quest.addSpawn(32110, sailren.getX(), sailren.getY(), sailren.getZ() + 30, 0, false, 26000L);
               sailren.setIsInvul(true);
               sailren.setIsImmobilized(true);
               Sailren.this.zone.broadcastPacket(new SpecialCamera(movieNpc, 60, 110, 30, 4000, 1500, 20000, 0, 65, 1, 0, 0));
               Sailren.this.startQuestTimer("ATTACK", 24600L, sailren, null);
               Sailren.this.startQuestTimer("ANIMATION", 2000L, movieNpc, null);
               Sailren.this.startQuestTimer("CAMERA_1", 4100L, movieNpc, null);
         }
      }
   }

   private static enum Status {
      ALIVE,
      IN_FIGHT,
      DEAD;
   }

   private class TimeOut implements Runnable {
      private TimeOut() {
      }

      @Override
      public void run() {
         if (Sailren.this._status == Sailren.Status.IN_FIGHT) {
            Sailren.this._status = Sailren.Status.ALIVE;
         }

         for(Creature character : Sailren.this.zone.getCharactersInside()) {
            if (character != null) {
               if (character.isPlayer()) {
                  character.teleToLocation(TeleportWhereType.TOWN, true);
               } else if (character.isNpc()) {
                  character.deleteMe();
               }
            }
         }
      }
   }

   private class UnlockSailren implements Runnable {
      private UnlockSailren() {
      }

      @Override
      public void run() {
         Sailren.this._status = Sailren.Status.ALIVE;
      }
   }
}
