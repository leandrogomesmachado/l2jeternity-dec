package l2e.gameserver.model.entity.events.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class MonsterAttackEvent extends AbstractFightEvent {
   private final int[] _monstersCount;
   private final int[] _wavesInterval;
   private final int[] _mobsId;
   private final List<Npc> _monsters = new CopyOnWriteArrayList<>();
   private int _waveCount = 0;
   private Npc _defender = null;
   private ScheduledFuture<?> _deathTask;
   private ScheduledFuture<?> _mobsTask;
   private final List<ScheduledFuture<?>> _activeTask = new ArrayList<>();

   public MonsterAttackEvent(MultiValueSet<String> set) {
      super(set);
      this._monstersCount = this.parseExcludedSkills(set.getString("mobsInWaveCount", ""));
      this._wavesInterval = this.parseExcludedSkills(set.getString("wavesInterval", ""));
      this._mobsId = this.parseExcludedSkills(set.getString("monstersId", ""));
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (victim.isMonster() && actor != null && actor.isPlayer()) {
         FightEventPlayer fActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (fActor != null) {
            fActor.increaseKills();
            this.updatePlayerScore(fActor);
         }

         actor.getActingPlayer().broadcastCharInfo();
      }

      super.onKilled(actor, victim);
   }

   @Override
   public void startRound() {
      super.startRound();
      this.spawnCommander();

      for(int i = 0; i < this._wavesInterval.length; ++i) {
         ScheduledFuture<?> task = ThreadPoolManager.getInstance()
            .schedule(new MonsterAttackEvent.WaveTask(this._monstersCount[i]), (long)(this._wavesInterval[i] * 1000));
         this._activeTask.add(task);
      }

      _log.info("MonsterAttackEvent: Loaded " + this._wavesInterval.length + " waves.");
   }

   @Override
   public void stopEvent() {
      for(ScheduledFuture<?> task : this._activeTask) {
         if (task != null) {
            task.cancel(false);
            Object var3 = null;
         }
      }

      if (this._defender != null) {
         super.stopEvent();
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.MATTACK_WIN", true);
      } else {
         ThreadPoolManager.getInstance().schedule(() -> {
            for(Player player : this.getAllFightingPlayers()) {
               this.leaveEvent(player, true);
               player.sendPacket(new ExShowScreenMessage("", 10, (byte)1, false));
            }
         }, 10000L);
         ThreadPoolManager.getInstance().schedule(() -> this.destroyMe(), (long)((15 + TIME_TELEPORT_BACK_TOWN) * 1000));
      }

      this.cleanUp();
   }

   private void cleanUp() {
      for(Npc npc : this._monsters) {
         if (npc != null) {
            npc.deleteMe();
         }
      }

      this._monsters.clear();
      if (this._deathTask != null) {
         this._deathTask.cancel(false);
         this._deathTask = null;
      }

      if (this._mobsTask != null) {
         this._mobsTask.cancel(false);
         this._mobsTask = null;
      }

      if (this._defender != null) {
         this._defender.deleteMe();
         this._defender = null;
      }
   }

   private void spawnCommander() {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.OVER && this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
         Location loc = Rnd.get((Location[])this.getMap().getDefLocations());

         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(53006);
            if (template != null) {
               Spawner spawn = new Spawner(template);
               spawn.setX(loc.getX());
               spawn.setY(loc.getY());
               spawn.setZ(loc.getZ());
               spawn.setAmount(1);
               spawn.setRespawnDelay(0);
               spawn.setReflectionId(this.getReflectionId());
               spawn.stopRespawn();
               SpawnParser.getInstance().addNewSpawn(spawn);
               spawn.init();
               this._defender = spawn.getLastSpawn();
               this._defender.setIsImmobilized(true);
               if (this._deathTask == null) {
                  this._deathTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MonsterAttackEvent.DeathTask(), 2000L, 2000L);
               }
            }
         } catch (Exception var4) {
         }
      }
   }

   @Override
   protected boolean inScreenShowBeScoreNotKills() {
      return false;
   }

   @Override
   public boolean isFriend(Creature c1, Creature c2) {
      return !c1.isMonster() && !c2.isMonster();
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO2", viewer.getLang());
         msg.add(fPlayer.getKills());
         return msg.toString();
      }
   }

   public void checkAlivePlayer() {
      boolean found = false;

      for(Player player : this.getAllFightingPlayers()) {
         if (player != null && !player.isDead()) {
            found = true;
            break;
         }
      }

      if (!found) {
         if (this._deathTask != null) {
            this._deathTask.cancel(false);
            this._deathTask = null;
         }

         if (this._mobsTask != null) {
            this._mobsTask.cancel(false);
            this._mobsTask = null;
         }

         this._defender.deleteMe();
         this._defender = null;
         this.stopEvent();
      }
   }

   private class DeathTask extends RunnableImpl {
      private DeathTask() {
      }

      @Override
      public void runImpl() throws Exception {
         if (MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.OVER
            && MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            if (MonsterAttackEvent.this._defender == null || MonsterAttackEvent.this._defender.isDead()) {
               MonsterAttackEvent.this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.FAIL_BACK", true);
               MonsterAttackEvent.this._defender = null;
               if (MonsterAttackEvent.this._deathTask != null) {
                  MonsterAttackEvent.this._deathTask.cancel(false);
                  MonsterAttackEvent.this._deathTask = null;
               }

               if (MonsterAttackEvent.this._mobsTask != null) {
                  MonsterAttackEvent.this._mobsTask.cancel(false);
                  MonsterAttackEvent.this._mobsTask = null;
               }

               MonsterAttackEvent.this.stopEvent();
            }
         }
      }
   }

   private class MonsterTask extends RunnableImpl {
      private MonsterTask() {
      }

      @Override
      public void runImpl() throws Exception {
         if (MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.OVER
            && MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            boolean found = false;

            for(Npc npc : MonsterAttackEvent.this._monsters) {
               if (npc != null && !npc.isDead()) {
                  found = true;
               }
            }

            if (!found) {
               MonsterAttackEvent.this.stopEvent();
            }
         }
      }
   }

   private class WaveTask extends RunnableImpl {
      private final int _mobAmount;

      private WaveTask(int mobAmount) {
         this._mobAmount = mobAmount;
      }

      @Override
      public void runImpl() throws Exception {
         if (MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.OVER
            && MonsterAttackEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            MonsterAttackEvent.this._waveCount++;
            MonsterAttackEvent.this.sendMessageToFighting(
               AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.WAVE_COME", true, String.valueOf(MonsterAttackEvent.this._waveCount)
            );
            Thread.sleep(5000L);

            for(int i = 0; i < this._mobAmount; ++i) {
               MonsterAttackEvent.this._monsters
                  .add(
                     MonsterAttackEvent.this.chooseLocAndSpawnNpc(
                        Rnd.get(MonsterAttackEvent.this._mobsId), MonsterAttackEvent.this.getMap().getKeyLocations(), 0, true
                     )
                  );
               Thread.sleep(500L);
            }

            if (MonsterAttackEvent.this._waveCount == MonsterAttackEvent.this._wavesInterval.length && MonsterAttackEvent.this._mobsTask == null) {
               MonsterAttackEvent.this._mobsTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(MonsterAttackEvent.this.new MonsterTask(), 2000L, 2000L);
            }
         }
      }
   }
}
