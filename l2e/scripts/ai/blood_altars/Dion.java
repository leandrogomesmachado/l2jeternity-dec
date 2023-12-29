package l2e.scripts.ai.blood_altars;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.entity.BloodAltarsEngine;

public class Dion extends BloodAltarsEngine {
   private static ScheduledFuture<?> _changeStatusTask = null;
   private int _status = 0;
   private int _progress = 0;
   private final List<Integer> _bosses;

   public Dion(String name, String descr) {
      super(name, descr);
      this.restoreStatus(this.getName());
      this._bosses = this.getBossList(this.getName());

      for(int boss : this._bosses) {
         this.addAttackId(boss);
         this.addKillId(boss);
      }
   }

   @Override
   public boolean changeSpawnInterval(long time, int status, int progress) {
      if (_changeStatusTask != null) {
         _changeStatusTask.cancel(false);
         _changeStatusTask = null;
      }

      this._status = status;
      this._progress = progress;
      _changeStatusTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Dion.this.changeStatus(Dion.this.getName(), Dion.this.getChangeTime(), Dion.this.getStatus());
         }
      }, time);
      return true;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (this._bosses.contains(npc.getId()) && this.getStatus() != 2) {
         this._status = 2;
         this.updateStatus(this.getName(), this._status);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (this._bosses.contains(npc.getId())) {
         ++this._progress;
         this.updateProgress(this.getName(), this._progress);
         this.updateBossStatus(this.getName(), (RaidBossInstance)npc, 1);
         if (this.getProgress() >= this._bosses.size()) {
            this.changeStatus(this.getName(), this.getChangeTime(), this.getStatus());
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   @Override
   public int getStatus() {
      return this._status;
   }

   @Override
   public int getProgress() {
      return this._progress;
   }

   public static void main(String[] args) {
      new Dion(Dion.class.getSimpleName(), "ai");
   }
}
