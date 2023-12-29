package l2e.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class MonsterInstance extends Attackable {
   protected boolean _enableMinions = true;
   private ScheduledFuture<?> _minionMaintainTask;
   private final MinionList _minionList;
   private boolean _canAgroWhileMoving = false;
   private boolean _isAutoAttackable = true;
   private boolean _isPassive = false;
   private boolean _isCanSupportMinion = true;
   protected int _aggroRangeOverride = 0;

   public MonsterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.MonsterInstance);
      this._minionList = new MinionList(this);
   }

   public int getKilledInterval(MinionInstance minion) {
      int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(minion.getId()) ? Config.MINIONS_RESPAWN_TIME.get(minion.getId()) * 1000 : -1;
      return respawnTime < 0 ? 0 : respawnTime;
   }

   public int getMinionUnspawnInterval() {
      return 5000;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return this._isAutoAttackable && !this.isEventMob() && attacker != null && !attacker.isMonster();
   }

   @Override
   public boolean isAggressive() {
      if (this._isPassive) {
         return false;
      } else {
         return this.getAggroRange() > 0 && !this.isEventMob();
      }
   }

   @Override
   public void onSpawn() {
      if (!this.isTeleporting()) {
         this.startMinionMaintainTask();
      }

      super.onSpawn();
   }

   public void enableMinions(boolean b) {
      this._enableMinions = b;
   }

   protected int getMaintenanceInterval() {
      return 1000;
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this._minionMaintainTask != null) {
         this._minionMaintainTask.cancel(false);
         this._minionMaintainTask = null;
      }

      super.onDeath(killer);
   }

   @Override
   public void deleteMe() {
      if (this._minionMaintainTask != null) {
         this._minionMaintainTask.cancel(false);
         this._minionMaintainTask = null;
      }

      if (this.getMinionList() != null && this.getMinionList().hasAliveMinions()) {
         for(MinionInstance m : this.getMinionList().getAliveMinions()) {
            if (m != null && !m.isInCombat()) {
               m.deleteMe();
            }
         }
      }

      super.deleteMe();
   }

   @Override
   public MinionList getMinionList() {
      return this._minionList;
   }

   @Override
   public boolean hasMinions() {
      return this.getMinionList().hasMinions();
   }

   @Override
   public boolean isMonster() {
      return true;
   }

   @Override
   public Npc getActingNpc() {
      return this;
   }

   public final boolean canAgroWhileMoving() {
      return this._canAgroWhileMoving;
   }

   public final void setCanAgroWhileMoving() {
      this._canAgroWhileMoving = true;
   }

   public void setClanOverride(String newClan) {
   }

   public void setIsAggresiveOverride(int aggroR) {
      this._aggroRangeOverride = aggroR;
   }

   @Override
   public void addDamageHate(Creature attacker, int damage, int aggro) {
      if (!this._isPassive) {
         super.addDamageHate(attacker, damage, aggro);
      }
   }

   public void setPassive(boolean state) {
      this._isPassive = state;
   }

   public boolean isPassive() {
      return this._isPassive;
   }

   @Override
   public void setAutoAttackable(boolean state) {
      this._isAutoAttackable = state;
   }

   @Override
   public boolean isWalker() {
      return this.getLeader() == null ? super.isWalker() : this.getLeader().isWalker();
   }

   @Override
   public boolean isRunner() {
      return this.getLeader() == null ? super.isRunner() : this.getLeader().isRunner();
   }

   @Override
   public boolean isEkimusFood() {
      return this.getLeader() == null ? super.isEkimusFood() : this.getLeader().isEkimusFood();
   }

   @Override
   public boolean isSpecialCamera() {
      return this.getLeader() == null ? super.isSpecialCamera() : this.getLeader().isSpecialCamera();
   }

   @Override
   public boolean giveRaidCurse() {
      return this.isRaidMinion() && this.getLeader() != null ? this.getLeader().giveRaidCurse() : super.giveRaidCurse();
   }

   @Override
   public Location getMinionPosition() {
      return Location.findPointToStay(this, 100, 150, false);
   }

   public void notifyMinionDied(MinionInstance minion) {
   }

   public void spawnMinion(MinionInstance minion) {
      minion.stopAllEffects();
      minion.setIsDead(false);
      minion.setDecayed(false);
      minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
      minion.setScriptValue(0);
      minion.setHeading(this.getHeading());
      minion.setReflectionId(this.getReflectionId());
      Location pos = this.getMinionPosition();
      if (pos != null) {
         minion.spawnMe(pos.getX(), pos.getY(), pos.getZ());
      }
   }

   public void startMinionMaintainTask() {
      if (this.getMinionList().hasMinions()) {
         if (this.getMinionList().isRandomMinons()) {
            this.getMinionList().deleteMinions();
         }

         if (this._minionMaintainTask != null) {
            this._minionMaintainTask.cancel(false);
            this._minionMaintainTask = null;
         }

         this._minionMaintainTask = ThreadPoolManager.getInstance()
            .schedule(new MonsterInstance.MinionMaintainTask(this.getMinionList().isRandomMinons()), 1000L);
      }
   }

   public boolean isCanSupportMinion() {
      return this._isCanSupportMinion;
   }

   public void isCanSupportMinion(boolean canSupportMinion) {
      this._isCanSupportMinion = canSupportMinion;
   }

   public class MinionMaintainTask extends RunnableImpl {
      private final boolean _isRandom;

      public MinionMaintainTask(boolean isRandom) {
         this._isRandom = isRandom;
      }

      @Override
      public void runImpl() {
         if (!MonsterInstance.this.isDead() && MonsterInstance.this._enableMinions) {
            if (this._isRandom) {
               MonsterInstance.this.getMinionList().spawnRndMinions();
            } else {
               MonsterInstance.this.getMinionList().spawnMinions();
            }
         }
      }
   }
}
