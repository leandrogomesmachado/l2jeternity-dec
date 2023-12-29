package l2e.scripts.ai.isle_of_prayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;

public class FafurionKindred extends Fighter {
   ScheduledFuture<?> _poisonTask;
   ScheduledFuture<?> _despawnTask;
   List<Npc> _spawns = new ArrayList<>();

   public FafurionKindred(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      this._spawns.clear();
      ThreadPoolManager.getInstance().schedule(new FafurionKindred.SpawnTask(22270), 500L);
      ThreadPoolManager.getInstance().schedule(new FafurionKindred.SpawnTask(22271), 500L);
      ThreadPoolManager.getInstance().schedule(new FafurionKindred.SpawnTask(22270), 500L);
      ThreadPoolManager.getInstance().schedule(new FafurionKindred.SpawnTask(22271), 500L);
      this._poisonTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FafurionKindred.PoisonTask(), 3000L, 3000L);
      this._despawnTask = ThreadPoolManager.getInstance().schedule(new FafurionKindred.DeSpawnTask(), 300000L);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this.cleanUp();
      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead() && skill != null) {
         if (skill.getId() == 2368) {
            actor.setCurrentHp(actor.getCurrentHp() + 3000.0);
         }

         actor.getAggroList().remove(caster);
      }
   }

   private void cleanUp() {
      if (this._poisonTask != null) {
         this._poisonTask.cancel(false);
         this._poisonTask = null;
      }

      if (this._despawnTask != null) {
         this._despawnTask.cancel(false);
         this._despawnTask = null;
      }

      for(Npc spawn : this._spawns) {
         if (spawn != null) {
            spawn.deleteMe();
         }
      }

      this._spawns.clear();
   }

   private void dropItem(Attackable actor, int id, int count) {
      ItemInstance item = ItemsParser.getInstance().createItem(id);
      item.setCount((long)count);
      item.dropMe(actor, Location.findPointToStay(actor, 100, true));
   }

   private class DeSpawnTask extends RunnableImpl {
      private DeSpawnTask() {
      }

      @Override
      public void runImpl() {
         Attackable actor = FafurionKindred.this.getActiveChar();
         FafurionKindred.this.dropItem(actor, 9691, Rnd.get(1, 2));
         if (Rnd.chance(36)) {
            FafurionKindred.this.dropItem(actor, 9700, Rnd.get(1, 3));
         }

         FafurionKindred.this.cleanUp();
         actor.deleteMe();
      }
   }

   private class PoisonTask extends RunnableImpl {
      private PoisonTask() {
      }

      @Override
      public void runImpl() {
         Attackable actor = FafurionKindred.this.getActiveChar();
         actor.reduceCurrentHp(500.0, actor, null);
      }
   }

   private class SpawnTask extends RunnableImpl {
      private final int _id;

      public SpawnTask(int id) {
         this._id = id;
      }

      @Override
      public void runImpl() {
         try {
            Attackable actor = FafurionKindred.this.getActiveChar();
            Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(this._id));
            sp.setLocation(Location.findPointToStay(actor, 100, 120, true));
            sp.setRespawnDelay(30, 40);
            Npc npc = sp.doSpawn(true);
            FafurionKindred.this._spawns.add(npc);
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }
}
