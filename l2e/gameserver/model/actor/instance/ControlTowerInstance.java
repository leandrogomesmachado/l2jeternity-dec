package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Tower;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;

public class ControlTowerInstance extends Tower {
   private List<Spawner> _guards;

   public ControlTowerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ControlTowerInstance);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.getCastle().getSiege().getIsInProgress()) {
         this.getCastle().getSiege().killedCT(this);
         if (this._guards != null && !this._guards.isEmpty()) {
            for(Spawner spawn : this._guards) {
               if (spawn != null) {
                  try {
                     spawn.stopRespawn();
                  } catch (Exception var5) {
                     _log.log(Level.WARNING, "Error at L2ControlTowerInstance", (Throwable)var5);
                  }
               }
            }

            this._guards.clear();
         }
      }

      super.onDeath(killer);
   }

   public void registerGuard(Spawner guard) {
      this.getGuards().add(guard);
   }

   private final List<Spawner> getGuards() {
      if (this._guards == null) {
         synchronized(this) {
            if (this._guards == null) {
               this._guards = new CopyOnWriteArrayList<>();
            }
         }
      }

      return this._guards;
   }
}
