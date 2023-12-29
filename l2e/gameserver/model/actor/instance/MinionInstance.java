package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class MinionInstance extends MonsterInstance {
   private MonsterInstance _master;

   public MinionInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   public void setLeader(MonsterInstance leader) {
      this._master = leader;
   }

   public MonsterInstance getLeader() {
      return this._master;
   }

   @Override
   protected void onDeath(Creature killer) {
      if (killer != null && killer.isPlayer()) {
         killer.getActingPlayer().getCounters().addAchivementInfo("minionKiller", this.getId(), -1L, false, false, false);
      }

      if (this.getLeader() != null) {
         this.getLeader().notifyMinionDied(this);
      }

      super.onDeath(killer);
   }

   @Override
   public Location getSpawnedLoc() {
      return this.getLeader() != null ? this.getLeader().getSpawnedLoc() : this.getLocation();
   }

   @Override
   public boolean isMinion() {
      return true;
   }
}
