package l2e.scripts.ai.gracia;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class DimensionDevice extends Fighter {
   private static final int[] _mobs = new int[]{22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596};
   private ScheduledFuture<?> _spawnTask = null;

   public DimensionDevice(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      if (this.getActiveChar().getReflectionId() == 0) {
         long time = this.calcRespawnTime();
         switch(SoDDefenceStage.getDefenceStage()) {
            case 0:
               if (this._spawnTask != null) {
                  this._spawnTask.cancel(false);
                  this._spawnTask = null;
               }

               this.getActiveChar().deleteMe();
               break;
            case 1:
            case 2:
            case 3:
            case 4:
               if (time > 0L) {
                  if (this._spawnTask != null) {
                     this._spawnTask.cancel(false);
                     this._spawnTask = null;
                  }

                  this._spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DimensionDevice.SpawnGuards(), (long)Rnd.get(1000, 5000), time);
               }
         }
      }

      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else {
         int stage = SoDDefenceStage.getDefenceStage();
         if (stage == 0) {
            if (this._spawnTask != null) {
               this._spawnTask.cancel(false);
               this._spawnTask = null;
            }

            this.getActiveChar().deleteMe();
            return true;
         } else {
            return true;
         }
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (this._spawnTask != null) {
         this._spawnTask.cancel(false);
         this._spawnTask = null;
      }

      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }

   private long calcRespawnTime() {
      switch(SoDDefenceStage.getDefenceStage()) {
         case 1:
            return (long)(Rnd.get(30, 60) * 1000);
         case 2:
            return (long)(Rnd.get(20, 30) * 1000);
         case 3:
            return (long)(Rnd.get(10, 20) * 1000);
         case 4:
            return (long)(Rnd.get(2, 5) * 1000);
         default:
            return -1L;
      }
   }

   private class SpawnGuards extends RunnableImpl {
      private SpawnGuards() {
      }

      @Override
      public void runImpl() {
         MonsterInstance npc = new MonsterInstance(
            IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(DimensionDevice._mobs[Rnd.get(DimensionDevice._mobs.length)])
         );
         if (npc != null) {
            Location loc = ((MonsterInstance)DimensionDevice.this.getActiveChar()).getMinionPosition();
            npc.setReflectionId(DimensionDevice.this.getActiveChar().getReflectionId());
            npc.setHeading(DimensionDevice.this.getActiveChar().getHeading());
            npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
            npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
         }
      }
   }
}
