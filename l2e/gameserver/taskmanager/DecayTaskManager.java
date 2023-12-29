package l2e.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class DecayTaskManager implements Runnable {
   private final Map<Creature, Long> _decayTasks = new ConcurrentHashMap<>();

   protected DecayTaskManager() {
      ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 1000L);
   }

   @Override
   public final void run() {
      if (!this._decayTasks.isEmpty()) {
         long time = System.currentTimeMillis();

         for(Entry<Creature, Long> entry : this._decayTasks.entrySet()) {
            Creature creature = entry.getKey();
            if (time >= entry.getValue()) {
               creature.onDecay();
               this._decayTasks.remove(creature);
            }
         }
      }
   }

   public void add(Creature creature, long delay) {
      this._decayTasks.put(creature, System.currentTimeMillis() + delay * 1000L);
   }

   public void add(Creature creature) {
      long delay;
      if (creature.isRaid() && !creature.isRaidMinion()) {
         delay = (long)Config.RAID_BOSS_DECAY_TIME;
      } else {
         delay = (long)Config.NPC_DECAY_TIME;
      }

      if (creature instanceof Attackable && (((Attackable)creature).isSpoil() || ((Attackable)creature).isSeeded())) {
         delay += (long)Config.SPOILED_DECAY_TIME;
      }

      this._decayTasks.put(creature, System.currentTimeMillis() + delay * 1000L);
   }

   public void cancel(Creature creature) {
      this._decayTasks.remove(creature);
   }

   public long getRemainingTime(Creature creature) {
      Long time = this._decayTasks.get(creature);
      return time != null ? time - System.currentTimeMillis() : Long.MAX_VALUE;
   }

   public static DecayTaskManager getInstance() {
      return DecayTaskManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DecayTaskManager _instance = new DecayTaskManager();
   }
}
