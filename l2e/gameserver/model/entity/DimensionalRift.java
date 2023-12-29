package l2e.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.EarthQuake;

public class DimensionalRift {
   protected byte _type;
   protected Party _party;
   protected List<Byte> _completedRooms = new ArrayList<>();
   private static final long seconds_5 = 5000L;
   protected byte jumps_current = 0;
   private Timer teleporterTimer;
   private TimerTask teleporterTimerTask;
   private Timer spawnTimer;
   private TimerTask spawnTimerTask;
   private Future<?> earthQuakeTask;
   protected byte _choosenRoom = -1;
   private boolean _hasJumped = false;
   protected List<Player> deadPlayers = new CopyOnWriteArrayList<>();
   protected List<Player> revivedInWaitingRoom = new CopyOnWriteArrayList<>();
   private boolean isBossRoom = false;

   public DimensionalRift(Party party, byte type, byte room) {
      DimensionalRiftManager.getInstance().getRoom(type, room).setPartyInside(true);
      this._type = type;
      this._party = party;
      this._choosenRoom = room;
      int[] coords = this.getRoomCoord(room);
      party.setDimensionalRift(this);

      for(Player p : party.getMembers()) {
         Quest riftQuest = QuestManager.getInstance().getQuest(635);
         if (riftQuest != null) {
            QuestState qs = p.getQuestState(riftQuest.getName());
            if (qs == null) {
               qs = riftQuest.newQuestState(p);
            }

            if (!qs.isStarted()) {
               qs.startQuest();
            }
         }

         p.teleToLocation(coords[0], coords[1], coords[2], true);
      }

      this.createSpawnTimer(this._choosenRoom);
      this.createTeleporterTimer(true);
   }

   public byte getType() {
      return this._type;
   }

   public byte getCurrentRoom() {
      return this._choosenRoom;
   }

   protected void createTeleporterTimer(final boolean reasonTP) {
      if (this.teleporterTimerTask != null) {
         this.teleporterTimerTask.cancel();
         this.teleporterTimerTask = null;
      }

      if (this.teleporterTimer != null) {
         this.teleporterTimer.cancel();
         this.teleporterTimer = null;
      }

      if (this.earthQuakeTask != null) {
         this.earthQuakeTask.cancel(false);
         this.earthQuakeTask = null;
      }

      this.teleporterTimer = new Timer();
      this.teleporterTimerTask = new TimerTask() {
         @Override
         public void run() {
            if (DimensionalRift.this._choosenRoom > -1) {
               DimensionalRiftManager.getInstance().getRoom(DimensionalRift.this._type, DimensionalRift.this._choosenRoom).unspawn().setPartyInside(false);
            }

            if (reasonTP
               && DimensionalRift.this.jumps_current < DimensionalRift.this.getMaxJumps()
               && DimensionalRift.this._party.getMemberCount() > DimensionalRift.this.deadPlayers.size()) {
               ++DimensionalRift.this.jumps_current;
               DimensionalRift.this._completedRooms.add(DimensionalRift.this._choosenRoom);
               DimensionalRift.this._choosenRoom = -1;

               for(Player p : DimensionalRift.this._party.getMembers()) {
                  if (!DimensionalRift.this.revivedInWaitingRoom.contains(p)) {
                     DimensionalRift.this.teleportToNextRoom(p);
                  }
               }

               DimensionalRift.this.createTeleporterTimer(true);
               DimensionalRift.this.createSpawnTimer(DimensionalRift.this._choosenRoom);
            } else {
               for(Player p : DimensionalRift.this._party.getMembers()) {
                  if (!DimensionalRift.this.revivedInWaitingRoom.contains(p)) {
                     DimensionalRift.this.teleportToWaitingRoom(p);
                  }
               }

               DimensionalRift.this.killRift();
               this.cancel();
            }
         }
      };
      if (reasonTP) {
         long jumpTime = this.calcTimeToNextJump();
         this.teleporterTimer.schedule(this.teleporterTimerTask, jumpTime);
         this.earthQuakeTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               for(Player p : DimensionalRift.this._party.getMembers()) {
                  if (!DimensionalRift.this.revivedInWaitingRoom.contains(p)) {
                     p.sendPacket(new EarthQuake(p.getX(), p.getY(), p.getZ(), 65, 9));
                  }
               }
            }
         }, jumpTime - 7000L);
      } else {
         this.teleporterTimer.schedule(this.teleporterTimerTask, 5000L);
      }
   }

   public void createSpawnTimer(final byte room) {
      if (this.spawnTimerTask != null) {
         this.spawnTimerTask.cancel();
         this.spawnTimerTask = null;
      }

      if (this.spawnTimer != null) {
         this.spawnTimer.cancel();
         this.spawnTimer = null;
      }

      this.spawnTimer = new Timer();
      this.spawnTimerTask = new TimerTask() {
         @Override
         public void run() {
            DimensionalRiftManager.getInstance().getRoom(DimensionalRift.this._type, room).spawn();
         }
      };
      this.spawnTimer.schedule(this.spawnTimerTask, (long)Config.RIFT_SPAWN_DELAY);
   }

   public void partyMemberInvited() {
      this.createTeleporterTimer(false);
   }

   public void partyMemberExited(Player player) {
      if (this.deadPlayers.contains(player)) {
         this.deadPlayers.remove(player);
      }

      if (this.revivedInWaitingRoom.contains(player)) {
         this.revivedInWaitingRoom.remove(player);
      }

      if (this._party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || this._party.getMemberCount() == 1) {
         for(Player p : this._party.getMembers()) {
            this.teleportToWaitingRoom(p);
         }

         this.killRift();
      }
   }

   public void manualTeleport(Player player, Npc npc) {
      if (player.isInParty() && player.getParty().isInDimensionalRift()) {
         if (player.getObjectId() != player.getParty().getLeaderObjectId()) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
         } else if (this._hasJumped) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
         } else {
            this._hasJumped = true;
            DimensionalRiftManager.getInstance().getRoom(this._type, this._choosenRoom).unspawn().setPartyInside(false);
            this._completedRooms.add(this._choosenRoom);
            this._choosenRoom = -1;

            for(Player p : this._party.getMembers()) {
               this.teleportToNextRoom(p);
            }

            DimensionalRiftManager.getInstance().getRoom(this._type, this._choosenRoom).setPartyInside(true);
            this.createSpawnTimer(this._choosenRoom);
            this.createTeleporterTimer(true);
         }
      }
   }

   public void manualExitRift(Player player, Npc npc) {
      if (player.isInParty() && player.getParty().isInDimensionalRift()) {
         if (player.getObjectId() != player.getParty().getLeaderObjectId()) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
         } else {
            for(Player p : player.getParty().getMembers()) {
               this.teleportToWaitingRoom(p);
            }

            this.killRift();
         }
      }
   }

   protected void teleportToNextRoom(Player player) {
      if (this._choosenRoom == -1) {
         do {
            List<Byte> emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(this._type);
            emptyRooms.removeAll(this._completedRooms);
            if (emptyRooms.isEmpty()) {
               emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(this._type);
            }

            this._choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
         } while(DimensionalRiftManager.getInstance().getRoom(this._type, this._choosenRoom).isPartyInside());
      }

      DimensionalRiftManager.getInstance().getRoom(this._type, this._choosenRoom).setPartyInside(true);
      this.checkBossRoom(this._choosenRoom);
      int[] coords = this.getRoomCoord(this._choosenRoom);
      player.teleToLocation(coords[0], coords[1], coords[2], true);
   }

   protected void teleportToWaitingRoom(Player player) {
      DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
      Quest riftQuest = QuestManager.getInstance().getQuest(635);
      if (riftQuest != null) {
         QuestState qs = player.getQuestState(riftQuest.getName());
         if (qs != null && qs.isCond(1)) {
            qs.exitQuest(true, true);
         }
      }
   }

   public void killRift() {
      this._completedRooms = null;
      if (this._party != null) {
         this._party.setDimensionalRift(null);
      }

      this._party = null;
      this.revivedInWaitingRoom = null;
      this.deadPlayers = null;
      if (this.earthQuakeTask != null) {
         this.earthQuakeTask.cancel(false);
         this.earthQuakeTask = null;
      }

      DimensionalRiftManager.getInstance().getRoom(this._type, this._choosenRoom).unspawn().setPartyInside(false);
      DimensionalRiftManager.getInstance().killRift(this);
   }

   public Timer getTeleportTimer() {
      return this.teleporterTimer;
   }

   public TimerTask getTeleportTimerTask() {
      return this.teleporterTimerTask;
   }

   public Timer getSpawnTimer() {
      return this.spawnTimer;
   }

   public TimerTask getSpawnTimerTask() {
      return this.spawnTimerTask;
   }

   public void setTeleportTimer(Timer t) {
      this.teleporterTimer = t;
   }

   public void setTeleportTimerTask(TimerTask tt) {
      this.teleporterTimerTask = tt;
   }

   public void setSpawnTimer(Timer t) {
      this.spawnTimer = t;
   }

   public void setSpawnTimerTask(TimerTask st) {
      this.spawnTimerTask = st;
   }

   private long calcTimeToNextJump() {
      int time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
      return this.isBossRoom ? (long)((float)time * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY) : (long)time;
   }

   public void memberDead(Player player) {
      if (!this.deadPlayers.contains(player)) {
         this.deadPlayers.add(player);
      }
   }

   public void memberRessurected(Player player) {
      if (this.deadPlayers.contains(player)) {
         this.deadPlayers.remove(player);
      }
   }

   public void usedTeleport(Player player) {
      if (!this.revivedInWaitingRoom.contains(player)) {
         this.revivedInWaitingRoom.add(player);
      }

      if (!this.deadPlayers.contains(player)) {
         this.deadPlayers.add(player);
      }

      if (this._party.getMemberCount() - this.revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE) {
         for(Player p : this._party.getMembers()) {
            if (p != null && !this.revivedInWaitingRoom.contains(p)) {
               this.teleportToWaitingRoom(p);
            }
         }

         this.killRift();
      }
   }

   public List<Player> getDeadMemberList() {
      return this.deadPlayers;
   }

   public List<Player> getRevivedAtWaitingRoom() {
      return this.revivedInWaitingRoom;
   }

   public void checkBossRoom(byte room) {
      this.isBossRoom = DimensionalRiftManager.getInstance().getRoom(this._type, room).isBossRoom();
   }

   public int[] getRoomCoord(byte room) {
      return DimensionalRiftManager.getInstance().getRoom(this._type, room).getTeleportCoorinates();
   }

   public byte getMaxJumps() {
      return Config.RIFT_MAX_JUMPS <= 8 && Config.RIFT_MAX_JUMPS >= 1 ? (byte)Config.RIFT_MAX_JUMPS : 4;
   }
}
