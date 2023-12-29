package l2e.gameserver.model.entity;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BlockInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.ExBlockUpSetList;
import l2e.gameserver.network.serverpackets.ExBlockUpSetState;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class BlockCheckerEngine {
   protected static final Logger _log = Logger.getLogger(BlockCheckerEngine.class.getName());
   protected ArenaParticipantsHolder _holder;
   protected Map<Player, Integer> _redTeamPoints = new ConcurrentHashMap<>();
   protected Map<Player, Integer> _blueTeamPoints = new ConcurrentHashMap<>();
   protected int _redPoints = 15;
   protected int _bluePoints = 15;
   protected int _arena = -1;
   protected List<Spawner> _spawns = new CopyOnWriteArrayList<>();
   protected boolean _isRedWinner;
   protected long _startedTime;
   protected static final int[][] _arenaCoordinates = new int[][]{
      {-58368, -62745, -57751, -62131, -58053, -62417},
      {-58350, -63853, -57756, -63266, -58053, -63551},
      {-57194, -63861, -56580, -63249, -56886, -63551},
      {-57200, -62727, -56584, -62115, -56850, -62391}
   };
   private static final int _zCoord = -2405;
   protected List<ItemInstance> _drops = new CopyOnWriteArrayList<>();
   private static final byte DEFAULT_ARENA = -1;
   protected boolean _isStarted = false;
   protected ScheduledFuture<?> _task;
   protected boolean _abnormalEnd = false;

   public BlockCheckerEngine(ArenaParticipantsHolder holder, int arena) {
      this._holder = holder;
      if (arena > -1 && arena < 4) {
         this._arena = arena;
      }

      for(Player player : holder.getRedPlayers()) {
         this._redTeamPoints.put(player, 0);
      }

      for(Player player : holder.getBluePlayers()) {
         this._blueTeamPoints.put(player, 0);
      }
   }

   public void updatePlayersOnStart(ArenaParticipantsHolder holder) {
      this._holder = holder;
   }

   public ArenaParticipantsHolder getHolder() {
      return this._holder;
   }

   public int getArena() {
      return this._arena;
   }

   public long getStarterTime() {
      return this._startedTime;
   }

   public int getRedPoints() {
      synchronized(this) {
         return this._redPoints;
      }
   }

   public int getBluePoints() {
      synchronized(this) {
         return this._bluePoints;
      }
   }

   public int getPlayerPoints(Player player, boolean isRed) {
      if (!this._redTeamPoints.containsKey(player) && !this._blueTeamPoints.containsKey(player)) {
         return 0;
      } else {
         return isRed ? this._redTeamPoints.get(player) : this._blueTeamPoints.get(player);
      }
   }

   public synchronized void increasePlayerPoints(Player player, int team) {
      if (player != null) {
         if (team == 0) {
            int points = this._redTeamPoints.get(player) + 1;
            this._redTeamPoints.put(player, points);
            ++this._redPoints;
            --this._bluePoints;
         } else {
            int points = this._blueTeamPoints.get(player) + 1;
            this._blueTeamPoints.put(player, points);
            ++this._bluePoints;
            --this._redPoints;
         }
      }
   }

   public void addNewDrop(ItemInstance item) {
      if (item != null) {
         this._drops.add(item);
      }
   }

   public boolean isStarted() {
      return this._isStarted;
   }

   protected void broadcastRelationChanged(Player plr) {
      for(Player p : this._holder.getAllPlayers()) {
         p.sendPacket(RelationChanged.update(plr, p, plr));
      }
   }

   public void endEventAbnormally() {
      try {
         synchronized(this) {
            this._isStarted = false;
            if (this._task != null) {
               this._task.cancel(true);
            }

            this._abnormalEnd = true;
            ThreadPoolManager.getInstance().execute(new BlockCheckerEngine.EndEvent());
            if (Config.DEBUG) {
               _log.config("Handys Block Checker Event at arena " + this._arena + " ended due lack of players!");
            }
         }
      } catch (Exception var4) {
         _log.log(Level.SEVERE, "Couldnt end Block Checker event at " + this._arena, (Throwable)var4);
      }
   }

   private class CarryingGirlUnspawn implements Runnable {
      private final Spawner _spawn;

      protected CarryingGirlUnspawn(Spawner spawn) {
         this._spawn = spawn;
      }

      @Override
      public void run() {
         if (this._spawn == null) {
            BlockCheckerEngine._log.warning("HBCE: Block Carrying Girl is null");
         } else {
            SpawnParser.getInstance().deleteSpawn(this._spawn);
            this._spawn.stopRespawn();
            this._spawn.getLastSpawn().deleteMe();
         }
      }
   }

   protected class EndEvent implements Runnable {
      private void clearMe() {
         HandysBlockCheckerManager.getInstance().clearPaticipantQueueByArenaId(BlockCheckerEngine.this._arena);
         BlockCheckerEngine.this._holder.clearPlayers();
         BlockCheckerEngine.this._blueTeamPoints.clear();
         BlockCheckerEngine.this._redTeamPoints.clear();
         HandysBlockCheckerManager.getInstance().setArenaFree(BlockCheckerEngine.this._arena);

         for(Spawner spawn : BlockCheckerEngine.this._spawns) {
            spawn.stopRespawn();
            spawn.getLastSpawn().deleteMe();
            SpawnParser.getInstance().deleteSpawn(spawn);
            Object var4 = null;
         }

         BlockCheckerEngine.this._spawns.clear();

         for(ItemInstance item : BlockCheckerEngine.this._drops) {
            if (item != null && item.isVisible() && item.getOwnerId() == 0) {
               item.decayMe();
            }
         }

         BlockCheckerEngine.this._drops.clear();
      }

      private void rewardPlayers() {
         if (BlockCheckerEngine.this._redPoints != BlockCheckerEngine.this._bluePoints) {
            BlockCheckerEngine.this._isRedWinner = BlockCheckerEngine.this._redPoints > BlockCheckerEngine.this._bluePoints;
            if (BlockCheckerEngine.this._isRedWinner) {
               this.rewardAsWinner(true);
               this.rewardAsLooser(false);
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
               msg.addString("Red Team");
               BlockCheckerEngine.this._holder.broadCastPacketToTeam(msg);
            } else if (BlockCheckerEngine.this._bluePoints > BlockCheckerEngine.this._redPoints) {
               this.rewardAsWinner(false);
               this.rewardAsLooser(true);
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
               msg.addString("Blue Team");
               BlockCheckerEngine.this._holder.broadCastPacketToTeam(msg);
            } else {
               this.rewardAsLooser(true);
               this.rewardAsLooser(false);
            }
         }
      }

      private void rewardAsWinner(boolean isRed) {
         Map<Player, Integer> tempPoints = isRed ? BlockCheckerEngine.this._redTeamPoints : BlockCheckerEngine.this._blueTeamPoints;

         for(Entry<Player, Integer> points : tempPoints.entrySet()) {
            if (points.getKey() != null) {
               if (points.getValue() >= 10) {
                  points.getKey().addItem("Block Checker", 13067, 2L, points.getKey(), true);
               } else {
                  tempPoints.remove(points.getKey());
               }
            }
         }

         int first = 0;
         int second = 0;
         Player winner1 = null;
         Player winner2 = null;

         for(Entry<Player, Integer> entry : tempPoints.entrySet()) {
            Player pc = entry.getKey();
            int pcPoints = entry.getValue();
            if (pcPoints > first) {
               second = first;
               winner2 = winner1;
               first = pcPoints;
               winner1 = pc;
            } else if (pcPoints > second) {
               second = pcPoints;
               winner2 = pc;
            }
         }

         if (winner1 != null) {
            winner1.addItem("Block Checker", 13067, 8L, winner1, true);
         }

         if (winner2 != null) {
            winner2.addItem("Block Checker", 13067, 5L, winner2, true);
         }
      }

      private void rewardAsLooser(boolean isRed) {
         Map<Player, Integer> tempPoints = isRed ? BlockCheckerEngine.this._redTeamPoints : BlockCheckerEngine.this._blueTeamPoints;

         for(Entry<Player, Integer> entry : tempPoints.entrySet()) {
            Player player = entry.getKey();
            if (player != null && entry.getValue() >= 10) {
               player.addItem("Block Checker", 13067, 2L, player, true);
            }
         }
      }

      private void setPlayersBack() {
         ExBlockUpSetState end = new ExBlockUpSetState(BlockCheckerEngine.this._isRedWinner);

         for(Player player : BlockCheckerEngine.this._holder.getAllPlayers()) {
            if (player != null) {
               player.stopAllEffects();
               player.setTeam(0);
               player.setBlockCheckerArena((byte)-1);
               PcInventory inv = player.getInventory();
               if (inv.getItemByItemId(13787) != null) {
                  long count = inv.getInventoryItemCount(13787, 0);
                  inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
               }

               if (inv.getItemByItemId(13788) != null) {
                  long count = inv.getInventoryItemCount(13788, 0);
                  inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
               }

               BlockCheckerEngine.this.broadcastRelationChanged(player);
               player.teleToLocation(-57478, -60367, -2370, true);
               player.sendPacket(end);
               player.broadcastUserInfo(true);
            }
         }
      }

      @Override
      public void run() {
         if (!BlockCheckerEngine.this._abnormalEnd) {
            this.rewardPlayers();
         }

         this.setPlayersBack();
         this.clearMe();
         BlockCheckerEngine.this._isStarted = false;
         BlockCheckerEngine.this._abnormalEnd = false;
      }
   }

   private class SpawnRound implements Runnable {
      int _numOfBoxes;
      int _round;

      SpawnRound(int numberOfBoxes, int round) {
         this._numOfBoxes = numberOfBoxes;
         this._round = round;
      }

      @Override
      public void run() {
         if (BlockCheckerEngine.this._isStarted) {
            switch(this._round) {
               case 1:
                  BlockCheckerEngine.this._task = ThreadPoolManager.getInstance().schedule(BlockCheckerEngine.this.new SpawnRound(20, 2), 60000L);
                  break;
               case 2:
                  BlockCheckerEngine.this._task = ThreadPoolManager.getInstance().schedule(BlockCheckerEngine.this.new SpawnRound(14, 3), 60000L);
                  break;
               case 3:
                  BlockCheckerEngine.this._task = ThreadPoolManager.getInstance().schedule(BlockCheckerEngine.this.new EndEvent(), 180000L);
            }

            byte random = 2;
            NpcTemplate template = NpcsParser.getInstance().getTemplate(18672);

            try {
               for(int i = 0; i < this._numOfBoxes; ++i) {
                  Spawner spawn = new Spawner(template);
                  spawn.setX(BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][4] + Rnd.get(-400, 400));
                  spawn.setY(BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][5] + Rnd.get(-400, 400));
                  spawn.setZ(-2405);
                  spawn.setAmount(1);
                  spawn.setHeading(1);
                  spawn.setRespawnDelay(1);
                  SpawnParser.getInstance().addNewSpawn(spawn);
                  spawn.init();
                  BlockInstance block = (BlockInstance)spawn.getLastSpawn();
                  if (random % 2 == 0) {
                     block.setRed(true);
                  } else {
                     block.setRed(false);
                  }

                  block.disableCoreAI(true);
                  BlockCheckerEngine.this._spawns.add(spawn);
                  ++random;
               }
            } catch (Exception var7) {
               BlockCheckerEngine._log.warning(this.getClass().getSimpleName() + ": " + var7.getMessage());
            }

            if (this._round == 1 || this._round == 2) {
               NpcTemplate girl = NpcsParser.getInstance().getTemplate(18676);

               try {
                  Spawner girlSpawn = new Spawner(girl);
                  girlSpawn.setX(BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][4] + Rnd.get(-400, 400));
                  girlSpawn.setY(BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][5] + Rnd.get(-400, 400));
                  girlSpawn.setZ(-2405);
                  girlSpawn.setAmount(1);
                  girlSpawn.setHeading(1);
                  girlSpawn.setRespawnDelay(1);
                  SpawnParser.getInstance().addNewSpawn(girlSpawn);
                  girlSpawn.init();
                  ThreadPoolManager.getInstance().schedule(BlockCheckerEngine.this.new CarryingGirlUnspawn(girlSpawn), 9000L);
               } catch (Exception var6) {
                  BlockCheckerEngine._log.warning("Couldnt Spawn Block Checker NPCs! Wrong instance type at npc table?");
                  BlockCheckerEngine._log.warning(this.getClass().getSimpleName() + ": " + var6.getMessage());
               }
            }

            BlockCheckerEngine.this._redPoints += this._numOfBoxes / 2;
            BlockCheckerEngine.this._bluePoints += this._numOfBoxes / 2;
            int timeLeft = (int)((BlockCheckerEngine.this.getStarterTime() - System.currentTimeMillis()) / 1000L);
            ExBlockUpSetState changePoints = new ExBlockUpSetState(timeLeft, BlockCheckerEngine.this.getBluePoints(), BlockCheckerEngine.this.getRedPoints());
            BlockCheckerEngine.this.getHolder().broadCastPacketToTeam(changePoints);
         }
      }
   }

   public class StartEvent implements Runnable {
      private final Skill _freeze = SkillsParser.getInstance().getInfo(6034, 1);
      private final Skill _transformationRed = SkillsParser.getInstance().getInfo(6035, 1);
      private final Skill _transformationBlue = SkillsParser.getInstance().getInfo(6036, 1);

      private void setUpPlayers() {
         HandysBlockCheckerManager.getInstance().setArenaBeingUsed(BlockCheckerEngine.this._arena);
         BlockCheckerEngine.this._redPoints = BlockCheckerEngine.this._spawns.size() / 2;
         BlockCheckerEngine.this._bluePoints = BlockCheckerEngine.this._spawns.size() / 2;
         ExBlockUpSetState initialPoints = new ExBlockUpSetState(300, BlockCheckerEngine.this._bluePoints, BlockCheckerEngine.this._redPoints);

         for(Player player : BlockCheckerEngine.this._holder.getAllPlayers()) {
            if (player != null) {
               boolean isRed = BlockCheckerEngine.this._holder.getRedPlayers().contains(player);
               ExBlockUpSetState clientSetUp = new ExBlockUpSetState(
                  300, BlockCheckerEngine.this._bluePoints, BlockCheckerEngine.this._redPoints, isRed, player, 0
               );
               player.sendPacket(clientSetUp);
               player.sendActionFailed();
               int tc = BlockCheckerEngine.this._holder.getPlayerTeam(player) * 2;
               int x = BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][tc];
               int y = BlockCheckerEngine._arenaCoordinates[BlockCheckerEngine.this._arena][tc + 1];
               player.teleToLocation(x, y, -2405, true);
               if (isRed) {
                  BlockCheckerEngine.this._redTeamPoints.put(player, 0);
                  player.setTeam(2);
               } else {
                  BlockCheckerEngine.this._blueTeamPoints.put(player, 0);
                  player.setTeam(1);
               }

               player.stopAllEffects();
               if (player.hasSummon()) {
                  player.getSummon().unSummon(player);
               }

               this._freeze.getEffects(player, player, false);
               if (BlockCheckerEngine.this._holder.getPlayerTeam(player) == 0) {
                  this._transformationRed.getEffects(player, player, false);
               } else {
                  this._transformationBlue.getEffects(player, player, false);
               }

               player.setBlockCheckerArena((byte)BlockCheckerEngine.this._arena);
               player.sendPacket(initialPoints);
               player.sendPacket(new ExBlockUpSetList(true));
               player.sendPacket(ExBasicActionList.STATIC_PACKET);
               BlockCheckerEngine.this.broadcastRelationChanged(player);
            }
         }
      }

      @Override
      public void run() {
         if (BlockCheckerEngine.this._arena == -1) {
            BlockCheckerEngine._log.severe("Couldnt set up the arena Id for the Block Checker event, cancelling event...");
         } else {
            BlockCheckerEngine.this._isStarted = true;
            ThreadPoolManager.getInstance().execute(BlockCheckerEngine.this.new SpawnRound(16, 1));
            this.setUpPlayers();
            BlockCheckerEngine.this._startedTime = System.currentTimeMillis() + 300000L;
         }
      }
   }
}
